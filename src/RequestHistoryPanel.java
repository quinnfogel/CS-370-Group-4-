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

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

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

    private JTextArea scoMessageArea;

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
        upperSection.add(createScoMessagePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());

        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.add(upperSection, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(upperWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

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

    private JPanel createScoMessagePanel() {
        JPanel panel = createCardPanel("SCO Error / Action Needed Message");
        panel.setLayout(new BorderLayout());

        scoMessageArea = new JTextArea(5, 40);
        scoMessageArea.setEditable(false);
        scoMessageArea.setLineWrap(true);
        scoMessageArea.setWrapStyleWord(true);
        scoMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoMessageArea.setForeground(new Color(140, 25, 25));
        scoMessageArea.setBackground(new Color(255, 244, 244));
        scoMessageArea.setText("No SCO note or error message for this request.");

        JScrollPane scrollPane = new JScrollPane(scoMessageArea);
        scrollPane.setBorder(new LineBorder(new Color(225, 170, 170), 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 100));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Certified Courses");
        panel.setLayout(new BorderLayout());

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

        coursesTable = new JTable(coursesTableModel);
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
        scrollPane.setPreferredSize(new Dimension(980, 180));

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
        scoMessageArea.setText("No SCO note or error message for this request.");

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
                            formatAcademicTerm(termCode),
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
        loadScoMessage(certId);
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
                    termValue.setText(formatAcademicTerm(rs.getInt("academic_term_code")));
                    benefitTypeValue.setText(rs.getString("benefit_type"));
                    statusValue.setText(rs.getString("status"));
                    totalClassesValue.setText(String.valueOf(rs.getInt("total_classes")));
                    totalUnitsValue.setText(formatNumber(rs.getDouble("total_units")));
                    trainingTimeValue.setText(formatTrainingTime(rs.getString("unit_load_category")));
                    allowanceValue.setText("$" + String.format("%.2f", rs.getDouble("estimated_monthly_allowance")) + " / month");
                    applyStatusColor(statusValue.getText());
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

    private void loadScoMessage(int certId) {
        String sql =
                "SELECT COALESCE(status, '') AS status, " +
                        "       COALESCE(sco_note, '') AS sco_note, " +
                        "       COALESCE(cancel_requested, 0) AS cancel_requested " +
                        "FROM cert_request " +
                        "WHERE cert_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    String scoNote = rs.getString("sco_note");
                    boolean cancelRequested = rs.getInt("cancel_requested") == 1;

                    if ("Cancellation Pending".equals(status) || cancelRequested) {
                        scoMessageArea.setText("Student requested cancellation. Waiting for SCO approval.");
                    } else if (scoNote != null && !scoNote.isBlank()) {
                        scoMessageArea.setText(scoNote);
                    } else if ("Action Needed".equals(status)) {
                        scoMessageArea.setText("This request was marked Action Needed, but no detailed note was saved.");
                    } else if ("Cancelled".equals(status)) {
                        scoMessageArea.setText("This certification was cancelled and approved by the SCO.");
                    } else {
                        scoMessageArea.setText("No SCO note or error message for this request.");
                    }
                } else {
                    scoMessageArea.setText("No SCO note or error message for this request.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load SCO message.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadCoursesForRequest(int certId) {
        String sql =
                "SELECT section_number, course_prefix, course_number, title, crn, units, course_length_weeks " +
                        "FROM course " +
                        "WHERE cert_id = ? " +
                        "ORDER BY course_prefix, course_number, section_number";

        coursesTableModel.setRowCount(0);

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

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
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load course history.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearSummary() {
        requestIdValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        statusValue.setForeground(StudentDashboard.DARK_TEXT);
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
    }

    private void applyStatusColor(String status) {
        if (status == null) {
            statusValue.setForeground(StudentDashboard.DARK_TEXT);
            return;
        }

        switch (status) {
            case "Submitted" -> statusValue.setForeground(new Color(204, 153, 0));
            case "In Review" -> statusValue.setForeground(new Color(40, 90, 180));
            case "Action Needed" -> statusValue.setForeground(new Color(178, 34, 34));
            case "Approved", "Certified" -> statusValue.setForeground(new Color(34, 139, 34));
            case "Cancellation Pending" -> statusValue.setForeground(new Color(128, 0, 128));
            case "Cancelled" -> statusValue.setForeground(new Color(120, 120, 120));
            default -> statusValue.setForeground(StudentDashboard.DARK_TEXT);
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(StudentDashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

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

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private String formatRequestId(int certId) {
        return "REQ-" + certId;
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
            return "";
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