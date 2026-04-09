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
    private final Color STATUS_PURPLE = new Color(128, 0, 128);

    private JLabel requestIdValue;
    private JLabel termValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel benefitTypeValue;
    private JPanel statusValuePanel;
    private JLabel estimatedAllowanceValue;
    private JLabel trainingTimeValue;

    private JTextArea scoMessageArea;

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

        JPanel overviewPanel = createCertificationOverview();
        JPanel messagePanel = createScoMessagePanel();
        JPanel coursesPanel = createCoursesTablePanel();

        overviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        coursesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerContent.add(overviewPanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(messagePanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(coursesPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(centerContent, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

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

    private JPanel createScoMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StudentDashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JLabel title = new JLabel("SCO Error / Action Needed Message");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(StudentDashboard.DARK_TEXT);

        scoMessageArea = new JTextArea(5, 40);
        scoMessageArea.setEditable(false);
        scoMessageArea.setLineWrap(true);
        scoMessageArea.setWrapStyleWord(true);
        scoMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoMessageArea.setForeground(new Color(140, 25, 25));
        scoMessageArea.setBackground(new Color(255, 244, 244));
        scoMessageArea.setText("No current SCO error message.");

        JScrollPane messageScroll = new JScrollPane(scoMessageArea);
        messageScroll.setBorder(new LineBorder(new Color(225, 170, 170), 1, true));
        messageScroll.setPreferredSize(new Dimension(900, 100));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(messageScroll, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StudentDashboard.CARD_BG);
        tablePanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JLabel sectionTitle = new JLabel("Certified Courses");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        String[] columns = {
                "Section Number",
                "Course Prefix",
                "Course Number",
                "Title / Course Name",
                "CRN",
                "Units",
                "Course Length (Weeks)"
        };

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
        coursesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(240);
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        coursesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        coursesTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(980, 200));

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
                mac.estimated_monthly_allowance,
                COALESCE(cr.sco_note, '') AS sco_note,
                COALESCE(cr.cancel_requested, 0) AS cancel_requested
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
                    String scoNote = rs.getString("sco_note");
                    boolean cancelRequested = rs.getInt("cancel_requested") == 1;

                    requestIdValue.setText("REQ-" + certId);
                    termValue.setText(formatAcademicTerm(academicTermCode));
                    totalUnitsValue.setText(formatNumber(totalUnits));
                    benefitTypeValue.setText(benefitType != null ? benefitType : "N/A");
                    estimatedAllowanceValue.setText(String.format("$%,.2f / month", estimatedAllowance));
                    trainingTimeValue.setText(formatTrainingTime(unitLoadCategory));

                    updateStatusPanel(status);
                    updateScoMessage(status, scoNote, cancelRequested);
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

    private void updateScoMessage(String status, String scoNote, boolean cancelRequested) {
        if ("Cancellation Pending".equals(status) || cancelRequested) {
            scoMessageArea.setText("Cancellation request submitted. Waiting for SCO approval.");
            return;
        }

        if (scoNote != null && !scoNote.isBlank()) {
            scoMessageArea.setText(scoNote);
            return;
        }

        if ("Action Needed".equals(status)) {
            scoMessageArea.setText("Your certification needs correction. Please review the request and make the required changes.");
            return;
        }

        scoMessageArea.setText("No current SCO error message.");
    }

    private void loadCoursesForRequest(Connection conn, int certId) throws SQLException {
        String sql = """
            SELECT
                section_number,
                course_prefix,
                course_number,
                title,
                crn,
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
                            rs.getString("section_number"),
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("title"),
                            rs.getString("crn"),
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
        scoMessageArea.setText("No current SCO error message.");
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
            case "Cancellation Pending" -> STATUS_PURPLE;
            case "Cancelled" -> STATUS_GRAY;
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

    private JPanel createStatusPanel(String text, Color bgColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(true);
        panel.setBackground(bgColor);
        panel.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(Color.WHITE);

        panel.add(label);
        return panel;
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
            default -> code;
        };
    }

    private String formatTrainingTime(String unitLoadCategory) {
        if (unitLoadCategory == null) {
            return "—";
        }

        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4-Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> unitLoadCategory;
        };
    }

    private String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}