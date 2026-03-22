import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class RequestHistoryPanel extends JPanel {

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

    private final Object[][] historyData = {
            {"REQ-2025-001", "Fall 2025", "CH33", "Approved", "08/15/2025"},
            {"REQ-2025-002", "Spring 2026", "CH33", "Approved", "01/12/2026"},
            {"REQ-2024-003", "Summer 2025", "CH33", "Approved", "05/20/2025"}
    };

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
        upperSection.add(createCoursesTablePanel());

        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.add(upperSection, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(upperWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);

        // Select first row by default so page doesn't open empty
        if (historyTable.getRowCount() > 0) {
            historyTable.setRowSelectionInterval(0, 0);
            updateSelectedRequestDetails(0);
        }
    }

    private JPanel createHistoryTablePanel() {
        JPanel panel = createCardPanel("Submitted Certification Requests");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Term", "Benefit Type", "Status", "Date Submitted"};
        historyTableModel = new DefaultTableModel(historyData, columns) {
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

        historyTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = historyTable.getSelectedRow();
                    if (selectedRow != -1) {
                        updateSelectedRequestDetails(selectedRow);
                    }
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

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Certified Courses");
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
        coursesTable.getTableHeader().setBackground(new Color(230, 236, 242));
        coursesTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(220, 240, 245));
        coursesTable.setGridColor(StudentDashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void updateSelectedRequestDetails(int row) {
        String requestId = historyTableModel.getValueAt(row, 0).toString();
        String term = historyTableModel.getValueAt(row, 1).toString();
        String benefitType = historyTableModel.getValueAt(row, 2).toString();
        String status = historyTableModel.getValueAt(row, 3).toString();

        requestIdValue.setText(requestId);
        termValue.setText(term);
        benefitTypeValue.setText(benefitType);
        statusValue.setText(status);

        // Dummy data for summary + courses based on selected request
        coursesTableModel.setRowCount(0);

        if (requestId.equals("REQ-2025-001")) {
            totalClassesValue.setText("4");
            totalUnitsValue.setText("12");
            trainingTimeValue.setText("Full-Time");
            allowanceValue.setText("$3,200 / month");

            coursesTableModel.addRow(new Object[]{"CSCI", "370", "12345", "3", "16"});
            coursesTableModel.addRow(new Object[]{"CSCI", "341", "22346", "3", "16"});
            coursesTableModel.addRow(new Object[]{"BUS", "301", "32347", "3", "16"});
            coursesTableModel.addRow(new Object[]{"MIS", "302", "42348", "3", "16"});
        } else if (requestId.equals("REQ-2025-002")) {
            totalClassesValue.setText("3");
            totalUnitsValue.setText("9");
            trainingTimeValue.setText("3/4-Time");
            allowanceValue.setText("$2,400 / month");

            coursesTableModel.addRow(new Object[]{"MIS", "201", "11223", "3", "16"});
            coursesTableModel.addRow(new Object[]{"BUS", "302", "21224", "3", "16"});
            coursesTableModel.addRow(new Object[]{"CSCI", "312", "31225", "3", "16"});
        } else {
            totalClassesValue.setText("2");
            totalUnitsValue.setText("6");
            trainingTimeValue.setText("Half-Time");
            allowanceValue.setText("$1,600 / month");

            coursesTableModel.addRow(new Object[]{"MATH", "160", "55111", "3", "8"});
            coursesTableModel.addRow(new Object[]{"ENGL", "202", "55112", "3", "8"});
        }
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
}