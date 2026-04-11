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

public class CertificationErrorsPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable errorsTable;
    private JTable coursesTable;

    private DefaultTableModel errorsTableModel;
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
    private JTextArea errorNoteArea;

    private final List<Integer> requestCertIds = new ArrayList<>();
    private final List<Integer> errorIds = new ArrayList<>();

    public CertificationErrorsPanel() {
        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Certification Errors");
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

        stacked.add(createErrorsTablePanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createSelectedRequestPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createCoursesPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createResolutionPanel());

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

        loadErrorRequests();
    }

    public void refreshData() {
        loadErrorRequests();
    }

    private JPanel createErrorsTablePanel() {
        JPanel panel = createCardPanel("Error Queue");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Issue"};
        errorsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        errorsTable = new JTable(errorsTableModel);
        errorsTable.setRowHeight(28);
        errorsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        errorsTable.getTableHeader().setBackground(new Color(226, 235, 229));
        errorsTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        errorsTable.setSelectionBackground(new Color(214, 232, 220));
        errorsTable.setGridColor(SCODashboard.BORDER);
        errorsTable.setFillsViewportHeight(true);
        errorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        errorsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = errorsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        updateSelectedErrorDetails(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(errorsTable);
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

    private JPanel createResolutionPanel() {
        JPanel panel = createCardPanel("Error Resolution");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        statusComboBox = new JComboBox<>(new String[]{
                "In Review", "Approved", "Action Needed"
        });

        errorNoteArea = new JTextArea(4, 30);
        errorNoteArea.setLineWrap(true);
        errorNoteArea.setWrapStyleWord(true);
        errorNoteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane noteScroll = new JScrollPane(errorNoteArea);
        noteScroll.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));

        formPanel.add(createLabeledField("Update Status:", statusComboBox));
        formPanel.add(createLabeledField("Resolution / Error Notes:", noteScroll));

        JButton updateButton = createActionButton("Update Request");
        JButton clearErrorButton = createActionButton("Resolve Error");

        updateButton.addActionListener(e -> updateErrorRequest());
        clearErrorButton.addActionListener(e -> resolveError());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearErrorButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private void loadErrorRequests() {
        errorsTableModel.setRowCount(0);
        coursesTableModel.setRowCount(0);
        requestCertIds.clear();
        errorIds.clear();

        String sql = """
            SELECT
                ce.error_id,
                cr.cert_id,
                u.first_name || ' ' || u.last_name AS student_name,
                cr.academic_term_code,
                s.benefit_type,
                cr.status,
                ce.error_message
            FROM cert_error ce
            JOIN cert_request cr ON ce.cert_id = cr.cert_id
            JOIN student s ON cr.student_id = s.student_id
            JOIN user u ON s.user_id = u.user_id
            WHERE ce.is_resolved = 0
            ORDER BY ce.error_id DESC
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int errorId = rs.getInt("error_id");
                int certId = rs.getInt("cert_id");

                errorIds.add(errorId);
                requestCertIds.add(certId);

                errorsTableModel.addRow(new Object[]{
                        formatRequestId(certId),
                        rs.getString("student_name"),
                        formatTerm(rs.getInt("academic_term_code")),
                        rs.getString("benefit_type"),
                        rs.getString("status"),
                        rs.getString("error_message")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load certification errors: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (errorsTableModel.getRowCount() > 0) {
            errorsTable.setRowSelectionInterval(0, 0);
            updateSelectedErrorDetails(0);
        } else {
            clearSelectedErrorDetails();
        }
    }

    private void updateSelectedErrorDetails(int row) {
        if (row < 0 || row >= requestCertIds.size()) {
            clearSelectedErrorDetails();
            return;
        }

        int certId = requestCertIds.get(row);
        int errorId = errorIds.get(row);

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
                    errorNoteArea.setText(loadErrorNote(conn, errorId));

                    loadCourses(conn, certId);
                    return;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load error request details: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        clearSelectedErrorDetails();
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

    private void updateErrorRequest() {
        int selectedRow = errorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int certId = requestCertIds.get(selectedRow);
        String newStatus = statusComboBox.getSelectedItem().toString();
        String note = errorNoteArea.getText().trim();
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
                errorsTableModel.setValueAt(newStatus, selectedRow, 4);
                errorsTableModel.setValueAt(note, selectedRow, 5);
                statusValue.setText(newStatus);

                JOptionPane.showMessageDialog(this,
                        "Certification error request updated successfully.",
                        "Update Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                loadErrorRequests();
            } else {
                JOptionPane.showMessageDialog(this,
                        "No request was updated.",
                        "Update Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to update certification error request: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resolveError() {
        int selectedRow = errorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int certId = requestCertIds.get(selectedRow);
        int errorId = errorIds.get(selectedRow);
        String note = errorNoteArea.getText().trim();
        Integer empId = getCurrentScoEmpId();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                String updateRequestSql = """
                        UPDATE cert_request
                        SET status = 'In Review',
                            sco_note = ?,
                            reviewed_by_emp_id = ?,
                            last_updated_date = CURRENT_TIMESTAMP
                        WHERE cert_id = ?
                        """;

                try (PreparedStatement pstmt = conn.prepareStatement(updateRequestSql)) {
                    pstmt.setString(1, note.isEmpty() ? null : note);

                    if (empId == null) {
                        pstmt.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        pstmt.setInt(2, empId);
                    }

                    pstmt.setInt(3, certId);
                    pstmt.executeUpdate();
                }

                String resolveErrorSql = """
                        UPDATE cert_error
                        SET is_resolved = 1
                        WHERE error_id = ?
                        """;

                try (PreparedStatement pstmt = conn.prepareStatement(resolveErrorSql)) {
                    pstmt.setInt(1, errorId);
                    pstmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Request moved out of error state and returned to In Review.",
                        "Error Resolved",
                        JOptionPane.INFORMATION_MESSAGE);

                loadErrorRequests();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to resolve certification error: " + ex.getMessage(),
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

    private String loadErrorNote(Connection conn, int errorId) throws Exception {
        String sql = "SELECT error_message FROM cert_error WHERE error_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, errorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String note = rs.getString("error_message");
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

    private void clearSelectedErrorDetails() {
        requestIdValue.setText("");
        studentNameValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
        errorNoteArea.setText("");
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
}