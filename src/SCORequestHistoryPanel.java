import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SCORequestHistoryPanel extends JPanel {

    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JComboBox<String> semesterComboBox;

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

        updateHistoryTable("Spring 2026");
    }

    private JPanel createSemesterPanel() {
        JPanel panel = createCardPanel("Select Semester");
        panel.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        semesterComboBox = new JComboBox<>(new String[]{
                "Spring 2026",
                "Fall 2025",
                "Summer 2025"
        });
        semesterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        semesterComboBox.addActionListener(e -> {
            String selectedSemester = semesterComboBox.getSelectedItem().toString();
            updateHistoryTable(selectedSemester);
        });

        JLabel infoLabel = new JLabel("View successfully processed certification requests by semester.");
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

        String[] columns = {"Request ID", "Student Name", "Benefit Type", "Status", "Date Processed"};
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

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 250));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        return panel;
    }

    private void updateHistoryTable(String semester) {
        historyTableModel.setRowCount(0);

        if (semester.equals("Spring 2026")) {
            historyTableModel.addRow(new Object[]{"REQ-2026-010", "Nathan Green", "CH33", "Approved", "02/10/2026"});
            historyTableModel.addRow(new Object[]{"REQ-2026-011", "Jane Smith", "CH35", "Approved", "02/11/2026"});
            historyTableModel.addRow(new Object[]{"REQ-2026-012", "Chris Allen", "CH31", "Approved", "02/12/2026"});
        } else if (semester.equals("Fall 2025")) {
            historyTableModel.addRow(new Object[]{"REQ-2025-021", "Maria Lopez", "CH33D", "Approved", "09/15/2025"});
            historyTableModel.addRow(new Object[]{"REQ-2025-022", "Ava Brown", "CH33", "Approved", "09/16/2025"});
            historyTableModel.addRow(new Object[]{"REQ-2025-023", "John Davis", "CH31", "Approved", "09/18/2025"});
        } else {
            historyTableModel.addRow(new Object[]{"REQ-2025-031", "Emma White", "CH35", "Approved", "06/05/2025"});
            historyTableModel.addRow(new Object[]{"REQ-2025-032", "Liam Carter", "CH33", "Approved", "06/06/2025"});
        }
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
}