import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestHistoryPanel extends JPanel {

    private JTable historyTable;
    private JTable coursesTable;

    private DefaultTableModel historyTableModel;
    private DefaultTableModel coursesTableModel;

    private JLabel requestIdValue;
    private JLabel termValue;
    private JLabel benefitTypeValue;
    private JLabel statusValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;

    // Keeps real cert_id values aligned with visible table rows
    private final List<Integer> historyCertIds = new ArrayList<>();

    public RequestHistoryPanel() {
        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Request History");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        JPanel upperSection = new JPanel();
        upperSection.setOpaque(false);
        upperSection.setLayout(new BoxLayout(upperSection, BoxLayout.Y_AXIS));

        upperSection.add(createHistoryTablePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createSummaryPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());

        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.add(upperSection, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(upperWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHistoryTablePanel() {
        JPanel panel = createCardPanel("Submitted Certification Requests");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Term", "Benefit Type", "Status", "Date Submitted"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTable.getTableHeader().setBackground(new Color(230, 236, 242));
        historyTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        historyTable.setSelectionBackground(new Color(220, 240, 245));
        historyTable.setGridColor(StudentDashboard.BORDER);
        historyTable.setFillsViewportHeight(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        historyTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = historyTable.getSelectedRow();
                if (selectedRow != -1 && selectedRow < historyCertIds.size()) {
                    int certId = historyCertIds.get(selectedRow);
                    loadSelectedRequestDetails(certId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel("Selected Request Summary");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValue = createValueLabel("");
        termValue = createValueLabel("");
        benefitTypeValue = createValueLabel("");
        statusValue = createValueLabel("");
        totalClassesValue = createValueLabel("");
        totalUnitsValue = createValueLabel("");
        trainingTimeValue = createValueLabel("");
        allowanceValue = createValueLabel("");

        infoPanel.add(createLabeledValue("Request ID:", requestIdValue));
        infoPanel.add(createLabeledValue("Term:", termValue));
        infoPanel.add(createLabeledValue("Benefit Type:", benefitTypeValue));
        infoPanel.add(createLabeledValue("Status:", statusValue));
        infoPanel.add(createLabeledValue("Total Classes:", totalClassesValue));
        infoPanel.add(createLabeledValue("Total Units:", totalUnitsValue));
        infoPanel.add(createLabeledValue("Training Time:", trainingTimeValue));
        infoPanel.add(createLabeledValue("Estimated Allowance:", allowanceValue));

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Certified Courses");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Prefix", "Course Number", "Class Number", "Units", "Weeks"};
        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(coursesTableModel);
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
        scrollPane.setPreferredSize(new Dimension(900, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        loadRequestHistory();
    }

    private void loadRequestHistory() {
        historyTableModel.setRowCount(0);
        historyCertIds.clear();
        clearSummary();
        coursesTableModel.setRowCount(0);

        String sql =
                "SELECT cr.cert_id, cr.academic_term_code, s.benefit_type, cr.status, " +
                        "       COALESCE(cr.submission_date, cr.last_updated_date) AS submitted_on " +
                        "FROM cert_request cr " +
                        "JOIN student s ON cr.student_id = s.student_id " +
                        "WHERE s.user_id = ? " +
                        "ORDER BY datetime(COALESCE(cr.submission_date, cr.last_updated_date)) DESC, cr.cert_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int certId = rs.getInt("cert_id");
                    int termCode = rs.getInt("academic_term_code");
                    String benefitType = rs.getString("benefit_type");
                    String status = rs.getString("status");
                    String submittedOn = rs.getString("submitted_on");

                    historyCertIds.add(certId);
                    historyTableModel.addRow(new Object[]{
                            formatRequestId(certId),
                            String.valueOf(termCode),
                            benefitType,
                            status,
                            submittedOn != null ? submittedOn : "-"
                    });
                }
            }

            if (historyTableModel.getRowCount() > 0) {
                historyTable.setRowSelectionInterval(0, 0);
                loadSelectedRequestDetails(historyCertIds.get(0));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load request history.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadSelectedRequestDetails(int certId) {
        loadRequestSummary(certId);
        loadCoursesForRequest(certId);
    }

    private void loadRequestSummary(int certId) {
        String sql =
                "SELECT cr.cert_id, cr.academic_term_code, s.benefit_type, cr.status, " +
                        "       cr.total_units, cr.unit_load_category, " +
                        "       COALESCE(mac.estimated_monthly_allowance, 0) AS estimated_monthly_allowance, " +
                        "       (SELECT COUNT(*) FROM course c WHERE c.cert_id = cr.cert_id) AS total_classes " +
                        "FROM cert_request cr " +
                        "JOIN student s ON cr.student_id = s.student_id " +
                        "LEFT JOIN monthly_allowance_calculator mac ON cr.cert_id = mac.cert_id " +
                        "WHERE cr.cert_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    requestIdValue.setText(formatRequestId(rs.getInt("cert_id")));
                    termValue.setText(String.valueOf(rs.getInt("academic_term_code")));
                    benefitTypeValue.setText(rs.getString("benefit_type"));
                    statusValue.setText(rs.getString("status"));
                    totalClassesValue.setText(String.valueOf(rs.getInt("total_classes")));
                    totalUnitsValue.setText(String.valueOf(rs.getDouble("total_units")));
                    trainingTimeValue.setText(formatTrainingTime(rs.getString("unit_load_category")));
                    allowanceValue.setText("$" + String.format("%.2f", rs.getDouble("estimated_monthly_allowance")) + " / month");
                } else {
                    clearSummary();
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load request summary.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadCoursesForRequest(int certId) {
        coursesTableModel.setRowCount(0);

        String sql =
                "SELECT course_prefix, course_number, section_number, units, course_length_weeks " +
                        "FROM course " +
                        "WHERE cert_id = ? " +
                        "ORDER BY course_prefix, course_number, section_number";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coursesTableModel.addRow(new Object[]{
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("section_number"),
                            rs.getDouble("units"),
                            rs.getInt("course_length_weeks")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load course details.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private String formatRequestId(int certId) {
        return String.format("REQ-%06d", certId);
    }

    private String formatTrainingTime(String unitLoadCategory) {
        if (unitLoadCategory == null) return "-";

        switch (unitLoadCategory) {
            case "FullTime":
                return "Full-Time";
            case "ThreeQuarterTime":
                return "3/4-Time";
            case "HalfTime":
                return "Half-Time";
            case "LessThanHalfTime":
                return "Less Than Half-Time";
            default:
                return unitLoadCategory;
        }
    }

    private void clearSummary() {
        requestIdValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(StudentDashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(StudentDashboard.DARK_TEXT);

        panel.setLayout(new BorderLayout());
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createLabeledValue(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        panel.add(label, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        return label;
    }
}