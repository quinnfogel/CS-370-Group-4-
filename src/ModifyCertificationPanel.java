import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ModifyCertificationPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable coursesTable;
    private DefaultTableModel tableModel;

    private JTextField prefixField;
    private JTextField courseNumberField;
    private JTextField classNumberField;
    private JTextField unitsField;
    private JTextField lengthField;

    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;
    private JLabel statusValue;

    private JLabel requestIdValueLabel;
    private JLabel currentTermValueLabel;
    private JLabel benefitTypeValueLabel;

    private HomePagePanel homePagePanel;

    private int currentCertId = 0;
    private int currentStudentId = 0;

    public ModifyCertificationPanel(HomePagePanel homePagePanel) {
        this.homePagePanel = homePagePanel;

        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Modify Certification");
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

        upperSection.add(createCurrentCertificationPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createModifyCoursesPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createSummaryPanel());

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

    public void refreshData() {
        loadCurrentCertification();
    }

    private JPanel createCurrentCertificationPanel() {
        JPanel panel = createCardPanel("Current Certification Details");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValueLabel = createValueLabel("N/A");
        currentTermValueLabel = createValueLabel("N/A");
        benefitTypeValueLabel = createValueLabel("N/A");
        statusValue = createStatusLabel("N/A");

        infoPanel.add(createLabeledValue("Request ID:", requestIdValueLabel));
        infoPanel.add(createLabeledValue("Current Term:", currentTermValueLabel));
        infoPanel.add(createLabeledValue("Benefit Type:", benefitTypeValueLabel));
        infoPanel.add(createLabeledValue("Status:", statusValue));

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Currently Certified Courses");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Prefix", "Course Number", "Class Number", "Units", "Weeks"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(tableModel);
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

        panel.add(content, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createModifyCoursesPanel() {
        JPanel panel = createCardPanel("Modify Courses");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        prefixField = new JTextField();
        courseNumberField = new JTextField();
        classNumberField = new JTextField();
        unitsField = new JTextField();
        lengthField = new JTextField();

        formPanel.add(createLabeledField("Course Prefix:", prefixField));
        formPanel.add(createLabeledField("Course Number:", courseNumberField));
        formPanel.add(createLabeledField("Class Number:", classNumberField));
        formPanel.add(createLabeledField("Units:", unitsField));
        formPanel.add(createLabeledField("Course Length (Weeks):", lengthField));

        JButton addClassButton = createActionButton("Add Class");
        JButton dropClassButton = createActionButton("Drop Selected Class");

        addClassButton.addActionListener(e -> addClassToTable());
        dropClassButton.addActionListener(e -> dropSelectedClass());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addClassButton);
        buttonPanel.add(dropClassButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel("Updated Certification Summary");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        totalClassesValue = createValueLabel("0");
        totalUnitsValue = createValueLabel("0");
        trainingTimeValue = createValueLabel("N/A");
        allowanceValue = createValueLabel("$0 / month");

        infoPanel.add(createLabeledValue("Total Classes:", totalClassesValue));
        infoPanel.add(createLabeledValue("Total Units:", totalUnitsValue));
        infoPanel.add(createLabeledValue("Training Time:", trainingTimeValue));
        infoPanel.add(createLabeledValue("Estimated Allowance:", allowanceValue));

        JButton submitButton = createSubmitButton("Submit Modified Certification");
        JButton cancelButton = createCancelButton("Discard Unsaved Changes");

        submitButton.addActionListener(e -> submitModifiedCertification());
        cancelButton.addActionListener(e -> discardChanges());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
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

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
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

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(180, 120, 20));
        return label;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(StudentDashboard.TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(20, 110, 130), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSubmitButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(34, 139, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(20, 100, 20), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createCancelButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(178, 34, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 20, 20), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadCurrentCertification() {
        currentCertId = 0;
        currentStudentId = 0;

        requestIdValueLabel.setText("N/A");
        currentTermValueLabel.setText("N/A");
        benefitTypeValueLabel.setText("N/A");
        statusValue.setText("N/A");
        statusValue.setForeground(new Color(180, 120, 20));

        tableModel.setRowCount(0);

        int userId = Session.getUserId();
        if (userId == 0) {
            updateSummary();
            return;
        }

        String studentQuery = """
                SELECT student_id, benefit_type
                FROM student
                WHERE user_id = ?
                """;

        String requestQuery = """
                SELECT cert_id, academic_term_code, status
                FROM cert_request
                WHERE student_id = ?
                ORDER BY last_updated_date DESC, cert_id DESC
                LIMIT 1
                """;

        String coursesQuery = """
                SELECT course_prefix, course_number, section_number, units, course_length_weeks
                FROM course
                WHERE cert_id = ?
                ORDER BY course_prefix, course_number, section_number
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "No student record found for the current user.",
                                "No Student Record",
                                JOptionPane.WARNING_MESSAGE);
                        updateSummary();
                        return;
                    }

                    currentStudentId = rs.getInt("student_id");
                    benefitTypeValueLabel.setText(rs.getString("benefit_type"));
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(requestQuery)) {
                pstmt.setInt(1, currentStudentId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "No certification request was found to modify.",
                                "No Request Found",
                                JOptionPane.INFORMATION_MESSAGE);
                        updateSummary();
                        return;
                    }

                    currentCertId = rs.getInt("cert_id");
                    requestIdValueLabel.setText("REQ-" + currentCertId);
                    currentTermValueLabel.setText(formatAcademicTerm(rs.getInt("academic_term_code")));

                    String status = rs.getString("status");
                    statusValue.setText(status);
                    applyStatusColor(status);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(coursesQuery)) {
                pstmt.setInt(1, currentCertId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getString("course_prefix"),
                                String.valueOf(rs.getInt("course_number")),
                                rs.getString("section_number"),
                                stripTrailingZero(rs.getDouble("units")),
                                String.valueOf(rs.getInt("course_length_weeks"))
                        });
                    }
                }
            }

            updateSummary();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load certification request data.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            updateSummary();
        }
    }

    private void applyStatusColor(String status) {
        if (status == null) {
            statusValue.setForeground(new Color(180, 120, 20));
            return;
        }

        switch (status) {
            case "Submitted" -> statusValue.setForeground(new Color(204, 153, 0));
            case "In Review" -> statusValue.setForeground(new Color(40, 90, 180));
            case "Action Needed" -> statusValue.setForeground(new Color(178, 34, 34));
            case "Approved", "Certified" -> statusValue.setForeground(new Color(34, 139, 34));
            default -> statusValue.setForeground(new Color(180, 120, 20));
        }
    }

    private void addClassToTable() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String prefix = prefixField.getText().trim().toUpperCase();
        String courseNumber = courseNumberField.getText().trim();
        String classNumber = classNumberField.getText().trim();
        String units = unitsField.getText().trim();
        String length = lengthField.getText().trim();

        if (prefix.isEmpty() || courseNumber.isEmpty() || classNumber.isEmpty()
                || units.isEmpty() || length.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete all course fields before adding a class.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(courseNumber);
            Double.parseDouble(units);
            Integer.parseInt(length);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Course Number, Units, and Course Length must be numeric values.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (Double.parseDouble(units) < 0 || Integer.parseInt(length) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Units must be 0 or greater and weeks must be greater than 0.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isDuplicateCourse(prefix, courseNumber, classNumber)) {
            JOptionPane.showMessageDialog(this,
                    "That course/section already exists in this certification request.",
                    "Duplicate Course",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[]{prefix, courseNumber, classNumber, units, length});

        prefixField.setText("");
        courseNumberField.setText("");
        classNumberField.setText("");
        unitsField.setText("");
        lengthField.setText("");

        updateSummary();
    }

    private boolean isDuplicateCourse(String prefix, String courseNumber, String classNumber) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingPrefix = tableModel.getValueAt(i, 0).toString().trim();
            String existingCourseNumber = tableModel.getValueAt(i, 1).toString().trim();
            String existingClassNumber = tableModel.getValueAt(i, 2).toString().trim();

            if (existingPrefix.equalsIgnoreCase(prefix)
                    && existingCourseNumber.equals(courseNumber)
                    && existingClassNumber.equalsIgnoreCase(classNumber)) {
                return true;
            }
        }
        return false;
    }

    private void dropSelectedClass() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = coursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class to drop.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.removeRow(selectedRow);
        updateSummary();
    }

    private void updateSummary() {
        int rowCount = tableModel.getRowCount();
        double totalUnits = 0.0;

        for (int i = 0; i < rowCount; i++) {
            totalUnits += Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }

        totalClassesValue.setText(String.valueOf(rowCount));
        totalUnitsValue.setText(stripTrailingZero(totalUnits));
        trainingTimeValue.setText(getTrainingTime(totalUnits));
        allowanceValue.setText(getEstimatedAllowance(totalUnits));
    }

    private String getTrainingTime(double totalUnits) {
        if (totalUnits >= 12) return "Full-Time";
        if (totalUnits >= 9) return "3/4-Time";
        if (totalUnits >= 6) return "Half-Time";
        if (totalUnits > 0) return "Less Than Half-Time";
        return "N/A";
    }

    private String getUnitLoadCategory(double totalUnits) {
        if (totalUnits >= 12) return "FullTime";
        if (totalUnits >= 9) return "ThreeQuarterTime";
        if (totalUnits >= 6) return "HalfTime";
        return "LessThanHalfTime";
    }

    private String getEstimatedAllowance(double totalUnits) {
        double baseRate = loadBaseHousingRate();

        double multiplier;
        if (totalUnits >= 12) {
            multiplier = 1.00;
        } else if (totalUnits >= 9) {
            multiplier = 0.75;
        } else if (totalUnits >= 6) {
            multiplier = 0.50;
        } else if (totalUnits > 0) {
            multiplier = 0.25;
        } else {
            multiplier = 0.0;
        }

        double allowance = Math.round(baseRate * multiplier * 100.0) / 100.0;
        return "$" + String.format("%.2f", allowance) + " / month";
    }

    private double loadBaseHousingRate() {
        String query = """
                SELECT base_housing_rate
                FROM monthly_allowance_config
                WHERE config_id = 1
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("base_housing_rate");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0.0;
    }

    private void submitModifiedCertification() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "A certification request cannot be submitted with zero classes.",
                    "No Classes Remaining",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        double totalUnits = 0.0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalUnits += Double.parseDouble(tableModel.getValueAt(i, 3).toString());
        }

        String unitLoadCategory = getUnitLoadCategory(totalUnits);

        String deleteCoursesSql = "DELETE FROM course WHERE cert_id = ?";
        String insertCourseSql = """
                INSERT INTO course (
                    cert_id,
                    course_prefix,
                    course_number,
                    section_number,
                    units,
                    course_length_weeks
                ) VALUES (?, ?, ?, ?, ?, ?)
                """;

        String updateRequestSql = """
                UPDATE cert_request
                SET status = 'Submitted',
                    submission_date = CURRENT_TIMESTAMP,
                    last_updated_date = CURRENT_TIMESTAMP,
                    total_units = ?,
                    unit_load_category = ?,
                    is_draft = 0
                WHERE cert_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteCoursesSql)) {
                    deleteStmt.setInt(1, currentCertId);
                    deleteStmt.executeUpdate();
                }

                try (PreparedStatement insertStmt = conn.prepareStatement(insertCourseSql)) {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        String prefix = tableModel.getValueAt(i, 0).toString().trim().toUpperCase();
                        int courseNumber = Integer.parseInt(tableModel.getValueAt(i, 1).toString().trim());
                        String classNumber = tableModel.getValueAt(i, 2).toString().trim();
                        double units = Double.parseDouble(tableModel.getValueAt(i, 3).toString().trim());
                        int weeks = Integer.parseInt(tableModel.getValueAt(i, 4).toString().trim());

                        insertStmt.setInt(1, currentCertId);
                        insertStmt.setString(2, prefix);
                        insertStmt.setInt(3, courseNumber);
                        insertStmt.setString(4, classNumber);
                        insertStmt.setDouble(5, units);
                        insertStmt.setInt(6, weeks);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateRequestSql)) {
                    updateStmt.setDouble(1, totalUnits);
                    updateStmt.setString(2, unitLoadCategory);
                    updateStmt.setInt(3, currentCertId);
                    updateStmt.executeUpdate();
                }

                conn.commit();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            statusValue.setText("Submitted");
            applyStatusColor("Submitted");
            updateSummary();

            JOptionPane.showMessageDialog(this,
                    "Modified certification request submitted successfully.",
                    "Submission Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            if (homePagePanel != null) {
                homePagePanel.refreshSummary();
            }

            refreshData();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to save certification changes.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void discardChanges() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to reload.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Discard all unsaved changes and reload the saved certification request?",
                "Discard Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            refreshData();
            prefixField.setText("");
            courseNumberField.setText("");
            classNumberField.setText("");
            unitsField.setText("");
            lengthField.setText("");

            JOptionPane.showMessageDialog(this,
                    "Unsaved changes were discarded.",
                    "Changes Discarded",
                    JOptionPane.INFORMATION_MESSAGE);
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
            default -> code;
        };
    }

    private String stripTrailingZero(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}