import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SCORequestHistoryPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable historyTable;
    private JTable coursesTable;

    private DefaultTableModel historyTableModel;
    private DefaultTableModel coursesTableModel;

    private JComboBox<String> semesterComboBox;

    private JLabel requestIdValue;
    private JLabel studentNameValue;
    private JLabel termValue;
    private JLabel benefitTypeValue;
    private JLabel statusValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;

    private JTextArea scoMessageArea;

    private final List<Integer> historyCertIds = new ArrayList<>();

    public SCORequestHistoryPanel() {
        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Request History");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(SCODashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        JPanel stacked = new JPanel();
        stacked.setOpaque(false);
        stacked.setLayout(new BoxLayout(stacked, BoxLayout.Y_AXIS));

        stacked.add(createSemesterPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createHistoryTablePanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createSummaryPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createScoMessagePanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createCoursesTablePanel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(stacked, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(SCODashboard.ADMIN_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        loadSemesterOptions();
        loadRequestHistory();
    }

    private JPanel createSemesterPanel() {
        JPanel panel = createCardPanel("Select Semester");
        panel.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        semesterComboBox = new JComboBox<>(new String[]{"All Semesters"});
        semesterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        semesterComboBox.addActionListener(e -> loadRequestHistory());

        JLabel infoLabel = new JLabel("View approved or certified requests by semester.");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        infoLabel.setForeground(SCODashboard.DARK_TEXT);

        content.add(infoLabel, BorderLayout.NORTH);
        content.add(semesterComboBox, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        return panel;
    }

    private JPanel createHistoryTablePanel() {
        JPanel panel = createCardPanel("Successfully Processed Requests");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Date Processed"};
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
        historyTable.getTableHeader().setBackground(new Color(226, 235, 229));
        historyTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        historyTable.setSelectionBackground(new Color(214, 232, 220));
        historyTable.setGridColor(SCODashboard.BORDER);
        historyTable.setFillsViewportHeight(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        historyTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = historyTable.getSelectedRow();
                if (selectedRow != -1 && selectedRow < historyCertIds.size()) {
                    loadSelectedRequestDetails(historyCertIds.get(selectedRow));
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(980, 220));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel("Selected Request Summary");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValue = createValueLabel("");
        studentNameValue = createValueLabel("");
        termValue = createValueLabel("");
        benefitTypeValue = createValueLabel("");
        statusValue = createValueLabel("");
        totalClassesValue = createValueLabel("");
        totalUnitsValue = createValueLabel("");
        trainingTimeValue = createValueLabel("");
        allowanceValue = createValueLabel("");

        infoPanel.add(createLabeledValue("Request ID:", requestIdValue));
        infoPanel.add(createLabeledValue("Student Name:", studentNameValue));
        infoPanel.add(createLabeledValue("Term:", termValue));
        infoPanel.add(createLabeledValue("Benefit Type:", benefitTypeValue));
        infoPanel.add(createLabeledValue("Status:", statusValue));
        infoPanel.add(createLabeledValue("Total Classes:", totalClassesValue));
        infoPanel.add(createLabeledValue("Total Units:", totalUnitsValue));
        infoPanel.add(createLabeledValue("Training Time:", trainingTimeValue));

        JPanel lowerInfo = new JPanel(new BorderLayout());
        lowerInfo.setOpaque(false);
        lowerInfo.setBorder(new EmptyBorder(15, 0, 0, 0));
        lowerInfo.add(createLabeledValue("Estimated Allowance:", allowanceValue), BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.add(lowerInfo, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        return panel;
    }

    private JPanel createScoMessagePanel() {
        JPanel panel = createCardPanel("SCO Notes");
        panel.setLayout(new BorderLayout());

        scoMessageArea = new JTextArea(5, 40);
        scoMessageArea.setEditable(false);
        scoMessageArea.setLineWrap(true);
        scoMessageArea.setWrapStyleWord(true);
        scoMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoMessageArea.setForeground(SCODashboard.DARK_TEXT);
        scoMessageArea.setBackground(new Color(245, 249, 246));
        scoMessageArea.setText("No SCO note for this request.");

        JScrollPane messageScroll = new JScrollPane(scoMessageArea);
        messageScroll.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        messageScroll.setPreferredSize(new Dimension(980, 105));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(messageScroll, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Processed Courses");
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
        coursesTable.getTableHeader().setBackground(new Color(226, 235, 229));
        coursesTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(214, 232, 220));
        coursesTable.setGridColor(SCODashboard.BORDER);
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
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(980, 190));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        return panel;
    }

    private void loadSemesterOptions() {
        String previousSelection = semesterComboBox.getSelectedItem() != null
                ? semesterComboBox.getSelectedItem().toString()
                : "All Semesters";

        semesterComboBox.removeAllItems();
        semesterComboBox.addItem("All Semesters");

        String sql = """
                SELECT DISTINCT academic_term_code
                FROM cert_request
                WHERE is_draft = 0
                  AND status IN ('Approved', 'Certified')
                ORDER BY academic_term_code DESC
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                semesterComboBox.addItem(formatTerm(rs.getInt("academic_term_code")));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load semesters.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        ComboBoxModel<String> model = semesterComboBox.getModel();
        boolean found = false;
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equals(previousSelection)) {
                semesterComboBox.setSelectedIndex(i);
                found = true;
                break;
            }
        }
        if (!found) {
            semesterComboBox.setSelectedIndex(0);
        }
    }

    private void loadRequestHistory() {
        if (historyTableModel == null || semesterComboBox == null) {
            return;
        }

        historyTableModel.setRowCount(0);
        historyCertIds.clear();
        clearSelectedRequestDetails();

        StringBuilder sql = new StringBuilder("""
                SELECT
                    cr.cert_id,
                    u.first_name || ' ' || u.last_name AS student_name,
                    cr.academic_term_code,
                    s.benefit_type,
                    cr.status,
                    COALESCE(cr.last_updated_date, cr.submission_date) AS processed_date
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                JOIN user u ON s.user_id = u.user_id
                WHERE cr.is_draft = 0
                  AND cr.status IN ('Approved', 'Certified')
                """);

        String selectedSemester = semesterComboBox.getSelectedItem() != null
                ? semesterComboBox.getSelectedItem().toString()
                : "All Semesters";

        boolean filterBySemester = !"All Semesters".equals(selectedSemester);
        if (filterBySemester) {
            sql.append(" AND cr.academic_term_code = ?");
        }

        sql.append(" ORDER BY datetime(COALESCE(cr.last_updated_date, cr.submission_date)) DESC, cr.cert_id DESC");

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            if (filterBySemester) {
                pstmt.setInt(1, parseTermCode(selectedSemester));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int certId = rs.getInt("cert_id");
                    historyCertIds.add(certId);
                    historyTableModel.addRow(new Object[]{
                            formatRequestId(certId),
                            rs.getString("student_name"),
                            formatTerm(rs.getInt("academic_term_code")),
                            rs.getString("benefit_type"),
                            rs.getString("status"),
                            formatDateTime(rs.getString("processed_date"))
                    });
                }
            }

            if (historyTableModel.getRowCount() > 0) {
                historyTable.setRowSelectionInterval(0, 0);
                loadSelectedRequestDetails(historyCertIds.get(0));
            } else {
                scoMessageArea.setText("No processed requests found for the selected semester.");
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load request history.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedRequestDetails(int certId) {
        loadRequestSummary(certId);
        loadCourses(certId);
        loadScoMessage(certId);
    }

    private void loadRequestSummary(int certId) {
        String sql = """
                SELECT
                    cr.cert_id,
                    u.first_name || ' ' || u.last_name AS student_name,
                    cr.academic_term_code,
                    s.benefit_type,
                    cr.status,
                    cr.total_units,
                    cr.unit_load_category,
                    COUNT(c.course_id) AS total_classes
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                JOIN user u ON s.user_id = u.user_id
                LEFT JOIN course c ON cr.cert_id = c.cert_id
                WHERE cr.cert_id = ?
                GROUP BY cr.cert_id, student_name, cr.academic_term_code, s.benefit_type,
                         cr.status, cr.total_units, cr.unit_load_category
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String benefitType = rs.getString("benefit_type");
                    String unitLoad = rs.getString("unit_load_category");

                    requestIdValue.setText(formatRequestId(certId));
                    studentNameValue.setText(rs.getString("student_name"));
                    termValue.setText(formatTerm(rs.getInt("academic_term_code")));
                    benefitTypeValue.setText(benefitType);
                    statusValue.setText(rs.getString("status"));
                    totalClassesValue.setText(String.valueOf(rs.getInt("total_classes")));
                    totalUnitsValue.setText(formatUnits(rs.getDouble("total_units")));
                    trainingTimeValue.setText(formatTrainingTime(unitLoad));
                    allowanceValue.setText(calculateAllowanceText(conn, benefitType, unitLoad));
                    applyStatusColor(statusValue.getText());
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load request summary.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadScoMessage(int certId) {
        String sql = "SELECT sco_note FROM cert_request WHERE cert_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String note = rs.getString("sco_note");
                    scoMessageArea.setText(note == null || note.isBlank()
                            ? "No SCO note for this request."
                            : note);
                } else {
                    scoMessageArea.setText("No SCO note for this request.");
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load SCO note.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCourses(int certId) {
        coursesTableModel.setRowCount(0);

        String sql = """
                SELECT section_number, course_prefix, course_number, title, crn, units, course_length_weeks
                FROM course
                WHERE cert_id = ?
                ORDER BY course_prefix, course_number, section_number
                """;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    coursesTableModel.addRow(new Object[]{
                            rs.getString("section_number"),
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("title"),
                            rs.getString("crn"),
                            formatUnits(rs.getDouble("units")),
                            rs.getInt("course_length_weeks")
                    });
                }
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to load processed courses.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearSelectedRequestDetails() {
        requestIdValue.setText("");
        studentNameValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        statusValue.setForeground(SCODashboard.DARK_TEXT);
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
        scoMessageArea.setText("No SCO note for this request.");
        coursesTableModel.setRowCount(0);
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private String calculateAllowanceText(Connection conn, String benefitType, String unitLoadCategory) throws Exception {
        if (!"CH33".equalsIgnoreCase(benefitType) && !"CH33D".equalsIgnoreCase(benefitType)) {
            return "Varies";
        }

        Double baseRate = getBaseHousingRate(conn);
        if (baseRate == null) {
            return "Not Configured";
        }

        double amount;
        switch (unitLoadCategory) {
            case "FullTime":
                amount = baseRate;
                break;
            case "ThreeQuarterTime":
                amount = baseRate * 0.75;
                break;
            case "HalfTime":
                amount = baseRate * 0.50;
                break;
            default:
                amount = 0.0;
                break;
        }

        return formatMoney(amount) + " / month";
    }

    private Double getBaseHousingRate(Connection conn) throws Exception {
        String sql = "SELECT base_housing_rate FROM monthly_allowance_config WHERE config_id = 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble("base_housing_rate");
            }
        }

        return null;
    }

    private void applyStatusColor(String status) {
        if (status == null) {
            statusValue.setForeground(SCODashboard.DARK_TEXT);
            return;
        }

        switch (status) {
            case "Approved", "Certified" -> statusValue.setForeground(new Color(34, 139, 34));
            case "Action Needed" -> statusValue.setForeground(new Color(178, 34, 34));
            case "In Review" -> statusValue.setForeground(new Color(40, 90, 180));
            default -> statusValue.setForeground(SCODashboard.DARK_TEXT);
        }
    }

    private String formatRequestId(int certId) {
        return String.format("REQ-%06d", certId);
    }

    private String formatTerm(int academicTermCode) {
        String value = String.valueOf(academicTermCode);
        if (value.length() != 6) {
            return value;
        }

        String year = value.substring(0, 4);
        String month = value.substring(4);

        return switch (month) {
            case "01" -> "Spring " + year;
            case "05" -> "Summer " + year;
            case "08" -> "Fall " + year;
            default -> year + " Term " + month;
        };
    }

    private int parseTermCode(String formattedTerm) {
        String[] parts = formattedTerm.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Unrecognized term format: " + formattedTerm);
        }

        String season = parts[0];
        String year = parts[1];

        return switch (season) {
            case "Spring" -> Integer.parseInt(year + "01");
            case "Summer" -> Integer.parseInt(year + "05");
            case "Fall" -> Integer.parseInt(year + "08");
            default -> throw new IllegalArgumentException("Unrecognized term format: " + formattedTerm);
        };
    }

    private String formatTrainingTime(String unitLoadCategory) {
        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4-Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> unitLoadCategory;
        };
    }

    private String formatDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.length() >= 10 ? value.substring(0, 10) : value;
    }

    private String formatUnits(double units) {
        if (units == Math.floor(units)) {
            return String.valueOf((int) units);
        }
        return String.format("%.1f", units);
    }

    private String formatMoney(double amount) {
        return String.format("$%,.2f", amount);
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(SCODashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(SCODashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(SCODashboard.DARK_TEXT);

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

    private JLabel createValueLabel(String value) {
        JLabel label = new JLabel(value);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(SCODashboard.DARK_TEXT);
        return label;
    }
}
