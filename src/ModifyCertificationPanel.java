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

    private JTextField sectionNumberField;
    private JTextField prefixField;
    private JTextField courseNumberField;
    private JTextField titleField;
    private JTextField crnField;
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

    private JTextArea scoErrorMessageArea;

    private final HomePagePanel homePagePanel;

    private int currentCertId = 0;
    private int currentStudentId = 0;
    private boolean cancelRequested = false;

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
        upperSection.add(createErrorMessagePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createSummaryPanel());

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

        String[] columns = {
                "Section Number",
                "Course Prefix",
                "Course Number",
                "Title / Course Name",
                "CRN",
                "Units",
                "Course Length (Weeks)"
        };

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

    private JPanel createModifyCoursesPanel() {
        JPanel panel = createCardPanel("Modify Courses");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        sectionNumberField = new JTextField();
        prefixField = new JTextField();
        courseNumberField = new JTextField();
        titleField = new JTextField();
        crnField = new JTextField();
        unitsField = new JTextField();
        lengthField = new JTextField();

        formPanel.add(createLabeledField("Section Number:", sectionNumberField));
        formPanel.add(createLabeledField("Course Prefix:", prefixField));
        formPanel.add(createLabeledField("Course Number:", courseNumberField));
        formPanel.add(createLabeledField("Title / Course Name:", titleField));
        formPanel.add(createLabeledField("CRN (5 digits):", crnField));
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

    private JPanel createErrorMessagePanel() {
        JPanel panel = createCardPanel("SCO Error / Action Needed Message");
        panel.setLayout(new BorderLayout());

        scoErrorMessageArea = new JTextArea(4, 40);
        scoErrorMessageArea.setEditable(false);
        scoErrorMessageArea.setLineWrap(true);
        scoErrorMessageArea.setWrapStyleWord(true);
        scoErrorMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoErrorMessageArea.setForeground(new Color(140, 25, 25));
        scoErrorMessageArea.setBackground(new Color(255, 244, 244));
        scoErrorMessageArea.setText("No current SCO error message.");

        JScrollPane scrollPane = new JScrollPane(scoErrorMessageArea);
        scrollPane.setBorder(new LineBorder(new Color(225, 170, 170), 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 110));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
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
        JButton discardButton = createNeutralButton("Discard Unsaved Changes");
        JButton cancelCertificationButton = createDeleteButton("Cancel Certification");

        submitButton.addActionListener(e -> submitModifiedCertification());
        discardButton.addActionListener(e -> discardChanges());
        cancelCertificationButton.addActionListener(e -> cancelCertification());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        buttonPanel.add(discardButton);
        buttonPanel.add(cancelCertificationButton);

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

    private JButton createNeutralButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(108, 117, 125));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(80, 85, 90), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createDeleteButton(String text) {
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
        cancelRequested = false;

        requestIdValueLabel.setText("N/A");
        currentTermValueLabel.setText("N/A");
        benefitTypeValueLabel.setText("N/A");
        statusValue.setText("N/A");
        statusValue.setForeground(new Color(180, 120, 20));
        scoErrorMessageArea.setText("No current SCO error message.");
        tableModel.setRowCount(0);

        clearEntryFields();

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
                SELECT cert_id, academic_term_code, status, COALESCE(sco_note, '') AS sco_note,
                       COALESCE(cancel_requested, 0) AS cancel_requested
                FROM cert_request
                WHERE student_id = ?
                ORDER BY last_updated_date DESC, cert_id DESC
                LIMIT 1
                """;

        String coursesQuery = """
                SELECT section_number, course_prefix, course_number, title, crn, units, course_length_weeks
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

                    String scoNote = rs.getString("sco_note");
                    cancelRequested = rs.getInt("cancel_requested") == 1;

                    if (cancelRequested) {
                        scoErrorMessageArea.setText("Cancellation request submitted. Waiting for SCO approval.");
                    } else if (scoNote != null && !scoNote.isBlank()) {
                        scoErrorMessageArea.setText(scoNote);
                    } else {
                        scoErrorMessageArea.setText("No current SCO error message.");
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(coursesQuery)) {
                pstmt.setInt(1, currentCertId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getString("section_number"),
                                rs.getString("course_prefix"),
                                String.valueOf(rs.getInt("course_number")),
                                rs.getString("title"),
                                rs.getString("crn"),
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
            case "Cancellation Pending" -> statusValue.setForeground(new Color(128, 0, 128));
            case "Cancelled" -> statusValue.setForeground(new Color(120, 120, 120));
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

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "This certification is pending cancellation and cannot be modified.",
                    "Cancellation Pending",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sectionNumber = sectionNumberField.getText().trim().toUpperCase();
        String prefix = prefixField.getText().trim().toUpperCase();
        String courseNumber = courseNumberField.getText().trim();
        String title = titleField.getText().trim();
        String crn = crnField.getText().trim();
        String units = unitsField.getText().trim();
        String length = lengthField.getText().trim();

        if (sectionNumber.isEmpty() || prefix.isEmpty() || courseNumber.isEmpty()
                || title.isEmpty() || crn.isEmpty() || units.isEmpty() || length.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete all course fields before adding a class.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!crn.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this,
                    "CRN must be exactly 5 digits.",
                    "Invalid CRN",
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

        double parsedUnits = Double.parseDouble(units);
        int parsedWeeks = Integer.parseInt(length);

        if (parsedUnits <= 0 || parsedWeeks <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Units and weeks must be greater than 0.",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isDuplicateCourse(sectionNumber, prefix, courseNumber, crn)) {
            JOptionPane.showMessageDialog(this,
                    "That course/section already exists in this certification request.",
                    "Duplicate Course",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[]{
                sectionNumber,
                prefix,
                courseNumber,
                title,
                crn,
                stripTrailingZero(parsedUnits),
                String.valueOf(parsedWeeks)
        });

        clearEntryFields();
        updateSummary();
    }

    private boolean isDuplicateCourse(String sectionNumber, String prefix, String courseNumber, String crn) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingSection = tableModel.getValueAt(i, 0).toString().trim();
            String existingPrefix = tableModel.getValueAt(i, 1).toString().trim();
            String existingCourseNumber = tableModel.getValueAt(i, 2).toString().trim();
            String existingCrn = tableModel.getValueAt(i, 4).toString().trim();

            if (existingSection.equalsIgnoreCase(sectionNumber)
                    && existingPrefix.equalsIgnoreCase(prefix)
                    && existingCourseNumber.equals(courseNumber)
                    && existingCrn.equals(crn)) {
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

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "This certification is pending cancellation and cannot be modified.",
                    "Cancellation Pending",
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
            totalUnits += Double.parseDouble(tableModel.getValueAt(i, 5).toString());
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
        if (totalUnits > 0) return "LessThanHalfTime";
        return "LessThanHalfTime";
    }

    private String getEstimatedAllowance(double totalUnits) {
        if (totalUnits >= 12) return "$3,200 / month";
        if (totalUnits >= 9) return "$2,400 / month";
        if (totalUnits >= 6) return "$1,600 / month";
        if (totalUnits > 0) return "$800 / month";
        return "$0 / month";
    }

    private void submitModifiedCertification() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "This certification is pending cancellation and cannot be modified.",
                    "Cancellation Pending",
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
            totalUnits += Double.parseDouble(tableModel.getValueAt(i, 5).toString());
        }

        String unitLoadCategory = getUnitLoadCategory(totalUnits);

        String deleteCoursesSql = "DELETE FROM course WHERE cert_id = ?";
        String insertCourseSql = """
                INSERT INTO course (
                    cert_id,
                    section_number,
                    course_prefix,
                    course_number,
                    title,
                    crn,
                    units,
                    course_length_weeks
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String updateRequestSql = """
                UPDATE cert_request
                SET status = 'Submitted',
                    submission_date = CURRENT_TIMESTAMP,
                    last_updated_date = CURRENT_TIMESTAMP,
                    total_units = ?,
                    unit_load_category = ?,
                    is_draft = 0,
                    cancel_requested = 0
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
                        String sectionNumber = tableModel.getValueAt(i, 0).toString().trim().toUpperCase();
                        String prefix = tableModel.getValueAt(i, 1).toString().trim().toUpperCase();
                        int courseNumber = Integer.parseInt(tableModel.getValueAt(i, 2).toString().trim());
                        String title = tableModel.getValueAt(i, 3).toString().trim();
                        String crn = tableModel.getValueAt(i, 4).toString().trim();
                        double units = Double.parseDouble(tableModel.getValueAt(i, 5).toString().trim());
                        int weeks = Integer.parseInt(tableModel.getValueAt(i, 6).toString().trim());

                        insertStmt.setInt(1, currentCertId);
                        insertStmt.setString(2, sectionNumber);
                        insertStmt.setString(3, prefix);
                        insertStmt.setInt(4, courseNumber);
                        insertStmt.setString(5, title);
                        insertStmt.setString(6, crn);
                        insertStmt.setDouble(7, units);
                        insertStmt.setInt(8, weeks);
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

    private void cancelCertification() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to cancel.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "A cancellation request has already been submitted for this certification.",
                    "Already Pending",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Cancel this entire certification request?\n\nThis will send the request to the SCO for cancellation approval.",
                "Cancel Certification",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
                UPDATE cert_request
                SET status = 'Cancellation Pending',
                    cancel_requested = 1,
                    sco_note = 'Student requested cancellation. Awaiting SCO approval.',
                    last_updated_date = CURRENT_TIMESTAMP
                WHERE cert_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentCertId);
            ps.executeUpdate();

            cancelRequested = true;
            statusValue.setText("Cancellation Pending");
            applyStatusColor("Cancellation Pending");
            scoErrorMessageArea.setText("Cancellation request submitted. Waiting for SCO approval.");

            JOptionPane.showMessageDialog(this,
                    "Certification cancellation request submitted to the SCO.",
                    "Cancellation Requested",
                    JOptionPane.INFORMATION_MESSAGE);

            if (homePagePanel != null) {
                homePagePanel.refreshSummary();
            }

            refreshData();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to submit cancellation request.",
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
            JOptionPane.showMessageDialog(this,
                    "Unsaved changes were discarded.",
                    "Changes Discarded",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearEntryFields() {
        sectionNumberField.setText("");
        prefixField.setText("");
        courseNumberField.setText("");
        titleField.setText("");
        crnField.setText("");
        unitsField.setText("");
        lengthField.setText("");
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