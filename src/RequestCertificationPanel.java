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
import java.sql.SQLException;

public class RequestCertificationPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private final HomePagePanel homePagePanel;

    private JTable coursesTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> termComboBox;
    private JComboBox<String> benefitTypeComboBox;

    private JTextField prefixField;
    private JTextField courseNumberField;
    private JTextField classNumberField;
    private JTextField unitsField;
    private JTextField lengthField;

    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;

    public RequestCertificationPanel(HomePagePanel homePagePanel) {
        this.homePagePanel = homePagePanel;

        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Request Certification");
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

        upperSection.add(createCertificationDetailsPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCourseEntryPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());
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
    }

    private JPanel createCertificationDetailsPanel() {
        JPanel detailsPanel = createCardPanel("Certification Details");
        detailsPanel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        termComboBox = new JComboBox<>(new String[]{
                "Fall 2025", "Spring 2026", "Summer 2026"
        });

        benefitTypeComboBox = new JComboBox<>(new String[]{
                "CH33", "CH33D", "CH31", "CH35"
        });

        formPanel.add(createLabeledField("Term:", termComboBox));
        formPanel.add(createLabeledField("Benefit Type:", benefitTypeComboBox));

        detailsPanel.add(formPanel, BorderLayout.CENTER);
        return detailsPanel;
    }

    private JPanel createCourseEntryPanel() {
        JPanel entryPanel = createCardPanel("Course Entry");
        entryPanel.setLayout(new BorderLayout());

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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);

        JButton addClassButton = createActionButton("Add Class");
        JButton removeClassButton = createActionButton("Remove Selected Class");

        addClassButton.addActionListener(e -> addClassToTable());
        removeClassButton.addActionListener(e -> removeSelectedClass());

        buttonPanel.add(addClassButton);
        buttonPanel.add(removeClassButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        entryPanel.add(wrapper, BorderLayout.CENTER);
        return entryPanel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel tablePanel = createCardPanel("Courses Added");
        tablePanel.setLayout(new BorderLayout());

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

        tablePanel.add(content, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = createCardPanel("Certification Summary");
        summaryPanel.setLayout(new BorderLayout());

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

        JButton submitButton = createSubmitButton("Submit Certification Request");
        submitButton.addActionListener(e -> submitCertificationRequest());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        summaryPanel.add(wrapper, BorderLayout.CENTER);
        return summaryPanel;
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
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 6));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLabeledValue(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 6));

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

    private void addClassToTable() {
        String prefix = prefixField.getText().trim();
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
            Integer.parseInt(units);
            Integer.parseInt(length);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Units and Course Length must be numeric values.",
                    "Invalid Input",
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

    private void removeSelectedClass() {
        int selectedRow = coursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class to remove.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.removeRow(selectedRow);
        updateSummary();
    }

    private void updateSummary() {
        int rowCount = tableModel.getRowCount();
        int totalUnits = 0;

        for (int i = 0; i < rowCount; i++) {
            totalUnits += Integer.parseInt(tableModel.getValueAt(i, 3).toString());
        }

        totalClassesValue.setText(String.valueOf(rowCount));
        totalUnitsValue.setText(String.valueOf(totalUnits));
        trainingTimeValue.setText(getTrainingTime(totalUnits));
        allowanceValue.setText(getEstimatedAllowance(totalUnits));
    }

    private String getTrainingTime(int totalUnits) {
        if (totalUnits >= 12) return "Full-Time";
        if (totalUnits >= 9) return "3/4-Time";
        if (totalUnits >= 6) return "Half-Time";
        if (totalUnits > 0) return "Less Than Half-Time";
        return "N/A";
    }

    private String getUnitLoadCategory(int totalUnits) {
        if (totalUnits >= 12) return "FullTime";
        if (totalUnits >= 9) return "ThreeQuarterTime";
        if (totalUnits >= 6) return "HalfTime";
        if (totalUnits > 0) return "LessThanHalfTime";
        return "LessThanHalfTime";
    }

    private String getEstimatedAllowance(int totalUnits) {
        if (totalUnits >= 12) return "$3,200 / month";
        if (totalUnits >= 9) return "$2,400 / month";
        if (totalUnits >= 6) return "$1,600 / month";
        if (totalUnits > 0) return "$800 / month";
        return "$0 / month";
    }

    private int getAcademicTermCode(String selectedTerm) {
        return switch (selectedTerm) {
            case "Fall 2025" -> 202508;
            case "Spring 2026" -> 202601;
            case "Summer 2026" -> 202605;
            default -> 202508;
        };
    }

    private int getStudentId(Connection conn, int userId) throws SQLException {
        String sql = "SELECT student_id FROM student WHERE user_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("student_id");
                }
            }
        }

        throw new SQLException("Student record not found for user.");
    }

    private void submitCertificationRequest() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one class before submitting your certification request.",
                    "No Classes Added",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "No active session found.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int totalUnits = Integer.parseInt(totalUnitsValue.getText());
        String selectedBenefitType = benefitTypeComboBox.getSelectedItem().toString();
        String selectedTerm = termComboBox.getSelectedItem().toString();
        int academicTermCode = getAcademicTermCode(selectedTerm);
        String unitLoadCategory = getUnitLoadCategory(totalUnits);

        String updateStudentSql = """
            UPDATE student
            SET benefit_type = ?
            WHERE user_id = ?
            """;

        String insertRequestSql = """
            INSERT INTO cert_request (
                student_id,
                academic_term_code,
                status,
                submission_date,
                last_updated_date,
                total_units,
                unit_load_category,
                is_draft
            ) VALUES (?, ?, 'Submitted', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, 0)
            """;

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

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            int studentId = getStudentId(conn, Session.getUserId());

            try (PreparedStatement updateStudentPs = conn.prepareStatement(updateStudentSql);
                 PreparedStatement insertRequestPs = conn.prepareStatement(insertRequestSql, PreparedStatement.RETURN_GENERATED_KEYS);
                 PreparedStatement insertCoursePs = conn.prepareStatement(insertCourseSql)) {

                // Update student benefit type
                updateStudentPs.setString(1, selectedBenefitType);
                updateStudentPs.setInt(2, Session.getUserId());
                updateStudentPs.executeUpdate();

                // Insert cert request
                insertRequestPs.setInt(1, studentId);
                insertRequestPs.setInt(2, academicTermCode);
                insertRequestPs.setDouble(3, totalUnits);
                insertRequestPs.setString(4, unitLoadCategory);
                insertRequestPs.executeUpdate();

                // Get generated cert_id
                int certId;
                try (ResultSet rs = insertRequestPs.getGeneratedKeys()) {
                    if (rs.next()) {
                        certId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to retrieve generated cert_id.");
                    }
                }

                // Insert courses
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String prefix = tableModel.getValueAt(i, 0).toString();
                    int courseNumber = Integer.parseInt(tableModel.getValueAt(i, 1).toString());
                    String sectionNumber = tableModel.getValueAt(i, 2).toString();
                    double units = Double.parseDouble(tableModel.getValueAt(i, 3).toString());
                    int weeks = Integer.parseInt(tableModel.getValueAt(i, 4).toString());

                    insertCoursePs.setInt(1, certId);
                    insertCoursePs.setString(2, prefix);
                    insertCoursePs.setInt(3, courseNumber);
                    insertCoursePs.setString(4, sectionNumber);
                    insertCoursePs.setDouble(5, units);
                    insertCoursePs.setInt(6, weeks);
                    insertCoursePs.executeUpdate();
                }

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }

            JOptionPane.showMessageDialog(this,
                    "Certification request submitted successfully.",
                    "Submission Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            if (homePagePanel != null) {
                homePagePanel.refreshSummary();
            }

            clearForm();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to submit certification request.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        prefixField.setText("");
        courseNumberField.setText("");
        classNumberField.setText("");
        unitsField.setText("");
        lengthField.setText("");

        tableModel.setRowCount(0);
        totalClassesValue.setText("0");
        totalUnitsValue.setText("0");
        trainingTimeValue.setText("N/A");
        allowanceValue.setText("$0 / month");
    }
}