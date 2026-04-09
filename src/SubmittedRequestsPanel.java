import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubmittedRequestsPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable requestsTable;
    private JTable coursesTable;

    private DefaultTableModel requestsTableModel;
    private DefaultTableModel coursesTableModel;

    private JLabel requestIdValue;
    private JLabel studentNameValue;
    private JLabel termValue;
    private JLabel benefitTypeValue;
    private JLabel statusValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;

    private JComboBox<String> statusComboBox;
    private JTextArea noteArea;

    private final List<Integer> requestCertIds = new ArrayList<>();

    public SubmittedRequestsPanel() {
        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Submitted Requests");
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

        stacked.add(createRequestsTablePanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createSelectedRequestPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createCoursesPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createActionsPanel());

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

        loadSubmittedRequests();
    }

    private JPanel createRequestsTablePanel() {
        JPanel panel = createCardPanel("Submitted Certification Queue");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Date Submitted"};
        requestsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(requestsTableModel);
        requestsTable.setRowHeight(28);
        requestsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        requestsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        requestsTable.getTableHeader().setBackground(new Color(226, 235, 229));
        requestsTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        requestsTable.setSelectionBackground(new Color(214, 232, 220));
        requestsTable.setGridColor(SCODashboard.BORDER);
        requestsTable.setFillsViewportHeight(true);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        requestsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = requestsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        updateSelectedRequestDetails(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 200));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        return panel;
    }

    private JPanel createSelectedRequestPanel() {
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

    private JPanel createCoursesPanel() {
        JPanel panel = createCardPanel("Submitted Courses");
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
        coursesTable.getTableHeader().setBackground(new Color(226, 235, 229));
        coursesTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(214, 232, 220));
        coursesTable.setGridColor(SCODashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = createCardPanel("SCO Actions");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        statusComboBox = new JComboBox<>(new String[]{
                "In Review", "Approved", "Action Needed"
        });

        noteArea = new JTextArea(4, 30);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));

        formPanel.add(createLabeledField("Update Status:", statusComboBox));
        formPanel.add(createLabeledField("SCO Notes / Error Description:", noteScroll));

        JButton updateButton = createActionButton("Update Request");
        JButton sendErrorButton = createErrorButton("Send to Certification Errors");

        updateButton.addActionListener(e -> updateRequestStatus());
        sendErrorButton.addActionListener(e -> sendToErrors());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(updateButton);
        buttonPanel.add(sendErrorButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private void loadSubmittedRequests() {
        requestsTableModel.setRowCount(0);
        requestCertIds.clear();

        String sql = """
                SELECT
                    cr.cert_id,
                    u.first_name || ' ' || u.last_name AS student_name,
                    cr.academic_term_code,
                    s.benefit_type,
                    cr.status,
                    COALESCE(cr.submission_date, cr.last_updated_date) AS submitted_date
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                JOIN user u ON s.user_id = u.user_id
                WHERE cr.is_draft = 0
                  AND cr.status = 'Submitted'
                ORDER BY COALESCE(cr.submission_date, cr.last_updated_date) DESC, cr.cert_id DESC
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int certId = rs.getInt("cert_id");

                requestCertIds.add(certId);
                requestsTableModel.addRow(new Object[]{
                        formatRequestId(certId),
                        rs.getString("student_name"),
                        formatTerm(rs.getInt("academic_term_code")),
                        rs.getString("benefit_type"),
                        rs.getString("status"),
                        formatDateTime(rs.getString("submitted_date"))
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load submitted requests: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (requestsTableModel.getRowCount() > 0) {
            requestsTable.setRowSelectionInterval(0, 0);
            updateSelectedRequestDetails(0);
        } else {
            clearSelectedRequestDetails();
        }
    }

    private void updateSelectedRequestDetails(int row) {
        if (row < 0 || row >= requestCertIds.size()) {
            clearSelectedRequestDetails();
            return;
        }

        int certId = requestCertIds.get(row);

        String requestSql = """
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

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(requestSql)) {

            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String benefitType = rs.getString("benefit_type");
                    String unitLoad = rs.getString("unit_load_category");
                    double totalUnits = rs.getDouble("total_units");
                    int totalClasses = rs.getInt("total_classes");

                    requestIdValue.setText(formatRequestId(certId));
                    studentNameValue.setText(rs.getString("student_name"));
                    termValue.setText(formatTerm(rs.getInt("academic_term_code")));
                    benefitTypeValue.setText(benefitType);
                    statusValue.setText(rs.getString("status"));
                    totalClassesValue.setText(String.valueOf(totalClasses));
                    totalUnitsValue.setText(formatUnits(totalUnits));
                    trainingTimeValue.setText(formatTrainingTime(unitLoad));
                    allowanceValue.setText(calculateAllowanceText(conn, benefitType, unitLoad));

                    syncStatusComboBox(rs.getString("status"));
                    noteArea.setText(loadScoNote(conn, certId));

                    loadCourses(conn, certId);
                    return;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load request details: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        clearSelectedRequestDetails();
    }

    private void loadCourses(Connection conn, int certId) throws Exception {
        coursesTableModel.setRowCount(0);

        String courseSql = """
                SELECT course_prefix, course_number, COALESCE(crn, section_number) AS class_number,
                       units, course_length_weeks
                FROM course
                WHERE cert_id = ?
                ORDER BY course_prefix, course_number
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(courseSql)) {
            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    coursesTableModel.addRow(new Object[]{
                            rs.getString("course_prefix"),
                            String.valueOf(rs.getInt("course_number")),
                            rs.getString("class_number"),
                            formatUnits(rs.getDouble("units")),
                            String.valueOf(rs.getInt("course_length_weeks"))
                    });
                }
            }
        }
    }

    private void updateRequestStatus() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newStatus = statusComboBox.getSelectedItem().toString();
        String note = noteArea.getText().trim();
        int certId = requestCertIds.get(selectedRow);
        Integer empId = getCurrentScoEmpId();

        String sql = """
                UPDATE cert_request
                SET status = ?,
                    sco_note = ?,
                    reviewed_by_emp_id = ?,
                    last_updated_date = CURRENT_TIMESTAMP
                WHERE cert_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, note.isEmpty() ? null : note);

            if (empId == null) {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                pstmt.setInt(3, empId);
            }

            pstmt.setInt(4, certId);

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "Request status updated successfully.",
                        "Update Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                loadSubmittedRequests();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No request was updated.",
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to update request: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendToErrors() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int certId = requestCertIds.get(selectedRow);
        String note = noteArea.getText().trim();
        Integer empId = getCurrentScoEmpId();

        if (note.isEmpty()) {
            note = "Needs follow-up from SCO.";
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                String updateRequestSql = """
                        UPDATE cert_request
                        SET status = 'Action Needed',
                            sco_note = ?,
                            reviewed_by_emp_id = ?,
                            last_updated_date = CURRENT_TIMESTAMP
                        WHERE cert_id = ?
                        """;

                try (PreparedStatement pstmt = conn.prepareStatement(updateRequestSql)) {
                    pstmt.setString(1, note);

                    if (empId == null) {
                        pstmt.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        pstmt.setInt(2, empId);
                    }

                    pstmt.setInt(3, certId);
                    pstmt.executeUpdate();
                }

                String insertErrorSql = """
                        INSERT INTO cert_error (cert_id, error_message, error_type)
                        VALUES (?, ?, ?)
                        """;

                try (PreparedStatement pstmt = conn.prepareStatement(insertErrorSql)) {
                    pstmt.setInt(1, certId);
                    pstmt.setString(2, note);
                    pstmt.setString(3, "Missing Information");
                    pstmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Request marked as Action Needed and sent to Certification Errors.",
                        "Request Flagged",
                        JOptionPane.INFORMATION_MESSAGE);

                loadSubmittedRequests();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to send request to Certification Errors: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer getCurrentScoEmpId() {
        if (!Session.isLoggedIn()) {
            return null;
        }

        String sql = "SELECT emp_id FROM sco WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Session.getUserId());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("emp_id");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String loadScoNote(Connection conn, int certId) throws Exception {
        String sql = "SELECT sco_note FROM cert_request WHERE cert_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String note = rs.getString("sco_note");
                    return note == null ? "" : note;
                }
            }
        }

        return "";
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

    private void syncStatusComboBox(String currentStatus) {
        ComboBoxModel<String> model = statusComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equalsIgnoreCase(currentStatus)) {
                statusComboBox.setSelectedIndex(i);
                return;
            }
        }
        statusComboBox.setSelectedIndex(0);
    }

    private void clearSelectedRequestDetails() {
        requestIdValue.setText("");
        studentNameValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
        noteArea.setText("");
        coursesTableModel.setRowCount(0);
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

        switch (month) {
            case "01":
                return "Winter " + year;
            case "03":
                return "Spring " + year;
            case "05":
                return "Summer " + year;
            case "08":
                return "Fall " + year;
            default:
                return year + " Term " + month;
        }
    }

    private String formatTrainingTime(String unitLoadCategory) {
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

    private String formatDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.length() >= 10) {
            return value.substring(0, 10);
        }

        return value;
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

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        if (field instanceof JComboBox<?>) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        }

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(SCODashboard.DARK_TEXT);
        return label;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(SCODashboard.ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(63, 107, 74), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        return button;
    }

    private JButton createErrorButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(178, 34, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 20, 20), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        return button;
    }
}