import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ModifyCertificationPanel extends JPanel {

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

    private boolean certificationCancelled = false;

    public ModifyCertificationPanel() {
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
        updateSummary();

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

    private JPanel createCurrentCertificationPanel() {
        JPanel panel = createCardPanel("Current Certification Details");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        statusValue = createStatusLabel("In Review");

        infoPanel.add(createLabeledValue("Request ID:", createValueLabel("REQ-2025-001")));
        infoPanel.add(createLabeledValue("Current Term:", createValueLabel("Spring 2026")));
        infoPanel.add(createLabeledValue("Benefit Type:", createValueLabel("CH33")));
        infoPanel.add(createLabeledValue("Status:", statusValue));

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Currently Certified Courses");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Prefix", "Course Number", "Class Number", "Units", "Weeks"};
        Object[][] data = {
                {"CSCI", "370", "12345", "3", "16"},
                {"MIS", "302", "22346", "3", "16"},
                {"BUS", "301", "32347", "3", "16"},
                {"CSCI", "341", "42348", "3", "16"}
        };

        tableModel = new DefaultTableModel(data, columns) {
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
        JButton cancelButton = createCancelButton("Cancel Certification");

        submitButton.addActionListener(e -> submitModifiedCertification());
        cancelButton.addActionListener(e -> cancelCertification());

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

    private void addClassToTable() {
        if (certificationCancelled) {
            JOptionPane.showMessageDialog(this,
                    "This certification request has been cancelled and can no longer be modified.",
                    "Modification Disabled",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

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

    private void dropSelectedClass() {
        if (certificationCancelled) {
            JOptionPane.showMessageDialog(this,
                    "This certification request has been cancelled and can no longer be modified.",
                    "Modification Disabled",
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

    private String getEstimatedAllowance(int totalUnits) {
        if (totalUnits >= 12) return "$3,200 / month";
        if (totalUnits >= 9) return "$2,400 / month";
        if (totalUnits >= 6) return "$1,600 / month";
        if (totalUnits > 0) return "$800 / month";
        return "$0 / month";
    }

    private void submitModifiedCertification() {
        if (certificationCancelled) {
            JOptionPane.showMessageDialog(this,
                    "This certification request has already been cancelled.",
                    "Already Cancelled",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "A certification request cannot be submitted with zero classes. Use Cancel Certification if you want to cancel the request.",
                    "No Classes Remaining",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Modified certification request submitted successfully.",
                "Submission Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void cancelCertification() {
        if (certificationCancelled) {
            JOptionPane.showMessageDialog(this,
                    "This certification request has already been cancelled.",
                    "Already Cancelled",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this certification request?\nThis action cannot be undone.",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            certificationCancelled = true;
            statusValue.setText("Cancelled");
            statusValue.setForeground(new Color(178, 34, 34));

            JOptionPane.showMessageDialog(this,
                    "Your certification request has been successfully cancelled.",
                    "Certification Cancelled",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}