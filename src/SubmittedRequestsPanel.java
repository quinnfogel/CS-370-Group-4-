import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SubmittedRequestsPanel extends JPanel {

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

    private final Object[][] submittedData = {
            {"REQ-2026-001", "Nathan Green", "Spring 2026", "CH33", "Submitted", "01/12/2026"},
            {"REQ-2026-002", "Jane Smith", "Spring 2026", "CH35", "Submitted", "01/13/2026"},
            {"REQ-2026-003", "John Davis", "Spring 2026", "CH31", "Submitted", "01/14/2026"},
            {"REQ-2026-004", "Maria Lopez", "Spring 2026", "CH33D", "Submitted", "01/14/2026"}
    };

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

        if (requestsTable.getRowCount() > 0) {
            requestsTable.setRowSelectionInterval(0, 0);
            updateSelectedRequestDetails(0);
        }
    }

    private JPanel createRequestsTablePanel() {
        JPanel panel = createCardPanel("Submitted Certification Queue");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Date Submitted"};
        requestsTableModel = new DefaultTableModel(submittedData, columns) {
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

    private void updateSelectedRequestDetails(int row) {
        String requestId = requestsTableModel.getValueAt(row, 0).toString();
        String studentName = requestsTableModel.getValueAt(row, 1).toString();
        String term = requestsTableModel.getValueAt(row, 2).toString();
        String benefitType = requestsTableModel.getValueAt(row, 3).toString();
        String status = requestsTableModel.getValueAt(row, 4).toString();

        requestIdValue.setText(requestId);
        studentNameValue.setText(studentName);
        termValue.setText(term);
        benefitTypeValue.setText(benefitType);
        statusValue.setText(status);

        coursesTableModel.setRowCount(0);

        if (requestId.equals("REQ-2026-001")) {
            totalClassesValue.setText("4");
            totalUnitsValue.setText("12");
            trainingTimeValue.setText("Full-Time");
            allowanceValue.setText("$3,200 / month");

            coursesTableModel.addRow(new Object[]{"CSCI", "370", "12345", "3", "16"});
            coursesTableModel.addRow(new Object[]{"CSCI", "341", "22346", "3", "16"});
            coursesTableModel.addRow(new Object[]{"BUS", "301", "32347", "3", "16"});
            coursesTableModel.addRow(new Object[]{"MIS", "302", "42348", "3", "16"});
        } else if (requestId.equals("REQ-2026-002")) {
            totalClassesValue.setText("3");
            totalUnitsValue.setText("9");
            trainingTimeValue.setText("3/4-Time");
            allowanceValue.setText("$2,400 / month");

            coursesTableModel.addRow(new Object[]{"PSYC", "100", "11221", "3", "16"});
            coursesTableModel.addRow(new Object[]{"ENGL", "202", "11222", "3", "16"});
            coursesTableModel.addRow(new Object[]{"SOC", "201", "11223", "3", "16"});
        } else if (requestId.equals("REQ-2026-003")) {
            totalClassesValue.setText("2");
            totalUnitsValue.setText("6");
            trainingTimeValue.setText("Half-Time");
            allowanceValue.setText("Varies");

            coursesTableModel.addRow(new Object[]{"BUS", "201", "77881", "3", "16"});
            coursesTableModel.addRow(new Object[]{"MIS", "301", "77882", "3", "16"});
        } else {
            totalClassesValue.setText("4");
            totalUnitsValue.setText("12");
            trainingTimeValue.setText("Full-Time");
            allowanceValue.setText("$3,200 / month");

            coursesTableModel.addRow(new Object[]{"MATH", "160", "99111", "3", "16"});
            coursesTableModel.addRow(new Object[]{"CSCI", "210", "99112", "3", "16"});
            coursesTableModel.addRow(new Object[]{"HIST", "101", "99113", "3", "16"});
            coursesTableModel.addRow(new Object[]{"COMM", "103", "99114", "3", "16"});
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
        requestsTableModel.setValueAt(newStatus, selectedRow, 4);
        statusValue.setText(newStatus);

        JOptionPane.showMessageDialog(this,
                "Request status updated successfully.",
                "Update Complete",
                JOptionPane.INFORMATION_MESSAGE);
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

        requestsTableModel.setValueAt("Action Needed", selectedRow, 4);
        statusValue.setText("Action Needed");

        JOptionPane.showMessageDialog(this,
                "Request marked as Action Needed and sent to Certification Errors.",
                "Request Flagged",
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