import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CertificationErrorsPanel extends JPanel {

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

    private final Object[][] errorData = {
            {"REQ-2026-004", "Maria Lopez", "Spring 2026", "CH33D", "Action Needed", "Missing updated class information"},
            {"REQ-2026-007", "Chris Allen", "Spring 2026", "CH31", "Action Needed", "Units mismatch"},
            {"REQ-2026-009", "Ava Brown", "Spring 2026", "CH33", "Flagged", "Student resubmitted request"}
    };

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

        if (errorsTable.getRowCount() > 0) {
            errorsTable.setRowSelectionInterval(0, 0);
            updateSelectedErrorDetails(0);
        }
    }

    private JPanel createErrorsTablePanel() {
        JPanel panel = createCardPanel("Error Queue");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Issue"};
        errorsTableModel = new DefaultTableModel(errorData, columns) {
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

    private void updateSelectedErrorDetails(int row) {
        String requestId = errorsTableModel.getValueAt(row, 0).toString();
        String studentName = errorsTableModel.getValueAt(row, 1).toString();
        String term = errorsTableModel.getValueAt(row, 2).toString();
        String benefitType = errorsTableModel.getValueAt(row, 3).toString();
        String status = errorsTableModel.getValueAt(row, 4).toString();
        String issue = errorsTableModel.getValueAt(row, 5).toString();

        requestIdValue.setText(requestId);
        studentNameValue.setText(studentName);
        termValue.setText(term);
        benefitTypeValue.setText(benefitType);
        statusValue.setText(status);
        errorNoteArea.setText(issue);

        coursesTableModel.setRowCount(0);

        if (requestId.equals("REQ-2026-004")) {
            totalClassesValue.setText("4");
            totalUnitsValue.setText("12");
            trainingTimeValue.setText("Full-Time");
            allowanceValue.setText("$3,200 / month");

            coursesTableModel.addRow(new Object[]{"MATH", "160", "99111", "3", "16"});
            coursesTableModel.addRow(new Object[]{"CSCI", "210", "99112", "3", "16"});
            coursesTableModel.addRow(new Object[]{"HIST", "101", "99113", "3", "16"});
            coursesTableModel.addRow(new Object[]{"COMM", "103", "99114", "3", "16"});
        } else if (requestId.equals("REQ-2026-007")) {
            totalClassesValue.setText("2");
            totalUnitsValue.setText("6");
            trainingTimeValue.setText("Half-Time");
            allowanceValue.setText("Varies");

            coursesTableModel.addRow(new Object[]{"BUS", "201", "77881", "3", "16"});
            coursesTableModel.addRow(new Object[]{"MIS", "301", "77882", "3", "16"});
        } else {
            totalClassesValue.setText("3");
            totalUnitsValue.setText("9");
            trainingTimeValue.setText("3/4-Time");
            allowanceValue.setText("$2,400 / month");

            coursesTableModel.addRow(new Object[]{"PSYC", "100", "11221", "3", "16"});
            coursesTableModel.addRow(new Object[]{"ENGL", "202", "11222", "3", "16"});
            coursesTableModel.addRow(new Object[]{"SOC", "201", "11223", "3", "16"});
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

        String newStatus = statusComboBox.getSelectedItem().toString();
        errorsTableModel.setValueAt(newStatus, selectedRow, 4);
        statusValue.setText(newStatus);
        errorsTableModel.setValueAt(errorNoteArea.getText().trim(), selectedRow, 5);

        JOptionPane.showMessageDialog(this,
                "Certification error request updated successfully.",
                "Update Complete",
                JOptionPane.INFORMATION_MESSAGE);
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

        errorsTableModel.setValueAt("In Review", selectedRow, 4);
        statusValue.setText("In Review");

        JOptionPane.showMessageDialog(this,
                "Request moved out of error state and returned to In Review.",
                "Error Resolved",
                JOptionPane.INFORMATION_MESSAGE);
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