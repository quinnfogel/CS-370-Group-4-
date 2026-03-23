import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

public class ViewRequestStatusPanel extends JPanel {
    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private final Color STATUS_YELLOW = new Color(230, 180, 40);
    private final Color STATUS_BLUE = new Color(70, 130, 180);
    private final Color STATUS_RED = new Color(220, 70, 70);
    private final Color STATUS_GREEN = new Color(70, 170, 90);
    private final Color STATUS_GRAY = new Color(140, 140, 140);

    private JLabel requestIdValue;
    private JLabel termValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel benefitTypeValue;
    private JPanel statusValuePanel;
    private JLabel estimatedAllowanceValue;
    private JLabel trainingTimeValue;

    private DefaultTableModel coursesTableModel;

    public ViewRequestStatusPanel() {
        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("View Request Status");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

        centerContent.add(createCertificationOverview());
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(createCoursesTablePanel());

        add(centerContent, BorderLayout.CENTER);

        loadRequestData();
    }

    public void refreshData() {
        loadRequestData();
    }

    private JPanel createCertificationOverview() {
        JPanel overviewPanel = new JPanel(new BorderLayout());
        overviewPanel.setBackground(StudentDashboard.CARD_BG);
        overviewPanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        overviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel sectionTitle = new JLabel("Current Certification Overview");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel infoPanel = new JPanel(new GridLayout(8, 2, 15, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValue = createValueLabel("—");
        termValue = createValueLabel("—");
        totalClassesValue = createValueLabel("0");
        totalUnitsValue = createValueLabel("0");
        benefitTypeValue = createValueLabel("N/A");
        statusValuePanel = createStatusPanel("Unknown", STATUS_GRAY);
        estimatedAllowanceValue = createValueLabel("$0.00 / month");
        trainingTimeValue = createValueLabel("—");

        infoPanel.add(createInfoLabel("Request ID:"));
        infoPanel.add(requestIdValue);

        infoPanel.add(createInfoLabel("Term:"));
        infoPanel.add(termValue);

        infoPanel.add(createInfoLabel("Total Classes:"));
        infoPanel.add(totalClassesValue);

        infoPanel.add(createInfoLabel("Total Units:"));
        infoPanel.add(totalUnitsValue);

        infoPanel.add(createInfoLabel("Benefit Type:"));
        infoPanel.add(benefitTypeValue);

        infoPanel.add(createInfoLabel("Status:"));
        infoPanel.add(statusValuePanel);

        infoPanel.add(createInfoLabel("Estimated Allowance:"));
        infoPanel.add(estimatedAllowanceValue);

        infoPanel.add(createInfoLabel("Training Time:"));
        infoPanel.add(trainingTimeValue);

        overviewPanel.add(sectionTitle, BorderLayout.NORTH);
        overviewPanel.add(infoPanel, BorderLayout.CENTER);

        return overviewPanel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StudentDashboard.CARD_BG);
        tablePanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        JLabel sectionTitle = new JLabel("Certified Courses");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        String[] columns = {"Prefix", "Course Number", "Class Number", "Units", "Weeks"};
        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable coursesTable = new JTable(coursesTableModel);
        coursesTable.setRowHeight(28);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        coursesTable.getTableHeader().setBackground(new Color(230, 236, 242));
        coursesTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(220, 240, 245));
        coursesTable.setGridColor(StudentDashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(800, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        tablePanel.add(sectionTitle, BorderLayout.NORTH);
        tablePanel.add(content, BorderLayout.CENTER);

        return tablePanel;
    }

    private void loadRequestData() {
        if (!Session.isLoggedIn()) {
            showNoDataState("No logged-in user.");
            return;
        }

        String sql = """
            SELECT
                cr.cert_id,
                cr.academic_term_code,
                cr.status,
                cr.total_units,
                cr.unit_load_category,
                s.benefit_type,
                mac.estimated_monthly_allowance
            FROM cert_request cr
            JOIN student s ON cr.student_id = s.student_id
            LEFT JOIN monthly_allowance_calculator mac ON cr.cert_id = mac.cert_id
            WHERE s.user_id = ?
            ORDER BY cr.last_updated_date DESC, cr.cert_id DESC
            LIMIT 1
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int certId = rs.getInt("cert_id");
                    int academicTermCode = rs.getInt("academic_term_code");
                    String status = rs.getString("status");
                    double totalUnits = rs.getDouble("total_units");
                    String unitLoadCategory = rs.getString("unit_load_category");
                    String benefitType = rs.getString("benefit_type");
                    double estimatedAllowance = rs.getDouble("estimated_monthly_allowance");

                    requestIdValue.setText("REQ-" + certId);
                    termValue.setText(formatAcademicTerm(academicTermCode));
                    totalUnitsValue.setText(formatNumber(totalUnits));
                    benefitTypeValue.setText(benefitType != null ? benefitType : "N/A");
                    estimatedAllowanceValue.setText(String.format("$%,.2f / month", estimatedAllowance));
                    trainingTimeValue.setText(formatTrainingTime(unitLoadCategory));

                    updateStatusPanel(status);
                    loadCoursesForRequest(conn, certId);
                } else {
                    showNoDataState("No certification request found.");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load request status.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
            showNoDataState("Unable to load data.");
        }
    }

    private void loadCoursesForRequest(Connection conn, int certId) throws SQLException {
        String sql = """
            SELECT
                course_prefix,
                course_number,
                section_number,
                units,
                course_length_weeks
            FROM course
            WHERE cert_id = ?
            ORDER BY course_prefix, course_number, section_number
            """;

        coursesTableModel.setRowCount(0);
        int courseCount = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coursesTableModel.addRow(new Object[]{
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("section_number"),
                            formatNumber(rs.getDouble("units")),
                            rs.getInt("course_length_weeks")
                    });
                    courseCount++;
                }
            }
        }

        totalClassesValue.setText(String.valueOf(courseCount));
    }

    private void showNoDataState(String message) {
        requestIdValue.setText("—");
        termValue.setText("—");
        totalClassesValue.setText("0");
        totalUnitsValue.setText("0");
        benefitTypeValue.setText("N/A");
        estimatedAllowanceValue.setText("$0.00 / month");
        trainingTimeValue.setText("—");
        updateStatusPanel("No Request");
        coursesTableModel.setRowCount(0);
        System.out.println(message);
    }

    private void updateStatusPanel(String statusText) {
        Color color = switch (statusText) {
            case "Submitted" -> STATUS_YELLOW;
            case "In Review" -> STATUS_BLUE;
            case "Action Needed" -> STATUS_RED;
            case "Approved", "Certified" -> STATUS_GREEN;
            default -> STATUS_GRAY;
        };

        Container parent = statusValuePanel.getParent();
        if (parent != null) {
            int index = -1;
            for (int i = 0; i < parent.getComponentCount(); i++) {
                if (parent.getComponent(i) == statusValuePanel) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                parent.remove(index);
                statusValuePanel = createStatusPanel(statusText, color);
                parent.add(statusValuePanel, index);
                parent.revalidate();
                parent.repaint();
            }
        } else {
            statusValuePanel = createStatusPanel(statusText, color);
        }
    }

    private String formatAcademicTerm(int academicTermCode) {
        String code = String.valueOf(academicTermCode);

        if (code.length() < 6) {
            return code;
        }

        String year = code.substring(0, 4);
        String termPart = code.substring(4);

        return switch (termPart) {
            case "01" -> "Spring " + year;
            case "05" -> "Summer " + year;
            case "08" -> "Fall " + year;
            default -> "Term " + code;
        };
    }

    private String formatTrainingTime(String unitLoadCategory) {
        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4 Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> unitLoadCategory != null ? unitLoadCategory : "—";
        };
    }

    private String formatNumber(double value) {
        if (value == (int) value) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

    private JPanel createStatusPanel(String statusText, Color circleColor) {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setOpaque(false);

        JPanel statusCircle = new JPanel();
        statusCircle.setPreferredSize(new Dimension(14, 14));
        statusCircle.setBackground(circleColor);
        statusCircle.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(StudentDashboard.DARK_TEXT);

        statusPanel.add(statusCircle);
        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        return label;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}