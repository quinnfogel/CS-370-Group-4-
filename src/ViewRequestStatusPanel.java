import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ViewRequestStatusPanel extends JPanel {

    private final Color STATUS_YELLOW = new Color(230, 180, 40);

    public ViewRequestStatusPanel() {
        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("View Request Status");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

        centerContent.add(createCertificationOverview());
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(createCoursesTablePanel());

        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createCertificationOverview() {
        JPanel overviewPanel = new JPanel(new BorderLayout());
        overviewPanel.setBackground(StudentDashboard.CARD_BG);
        overviewPanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        overviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 290));

        JLabel sectionTitle = new JLabel("Current Certification Overview");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 15, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        infoPanel.add(createInfoLabel("Request ID:"));
        infoPanel.add(createValueLabel("REQ-2025-001"));

        infoPanel.add(createInfoLabel("Term:"));
        infoPanel.add(createValueLabel("Fall 2025"));

        infoPanel.add(createInfoLabel("Total Classes:"));
        infoPanel.add(createValueLabel("4"));

        infoPanel.add(createInfoLabel("Total Units:"));
        infoPanel.add(createValueLabel("12"));

        infoPanel.add(createInfoLabel("Benefit Type:"));
        infoPanel.add(createValueLabel("CH33"));

        infoPanel.add(createInfoLabel("Status:"));
        infoPanel.add(createStatusPanel("In Review", STATUS_YELLOW));

        infoPanel.add(createInfoLabel("Estimated Allowance:"));
        infoPanel.add(createValueLabel("$3,200 / month"));

        infoPanel.add(createInfoLabel("Training Time:"));
        infoPanel.add(createValueLabel("Full-Time"));

        overviewPanel.add(sectionTitle, BorderLayout.NORTH);
        overviewPanel.add(infoPanel, BorderLayout.CENTER);

        return overviewPanel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StudentDashboard.CARD_BG);
        tablePanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));

        JLabel sectionTitle = new JLabel("Certified Courses");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        String[] columns = {"Prefix", "Course Number", "Class Number", "Units", "Weeks"};
        Object[][] data = {
                {"CSCI", "370", "12345", "3", "16"},
                {"CSCI", "341", "22346", "3", "16"},
                {"BUS", "301", "32347", "3", "16"},
                {"MIS", "302", "42348", "3", "16"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable coursesTable = new JTable(model);
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

        tablePanel.add(sectionTitle, BorderLayout.NORTH);
        tablePanel.add(content, BorderLayout.CENTER);

        return tablePanel;
    }

    private JPanel createStatusPanel(String statusText, Color circleColor) {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusPanel.setOpaque(false);

        JPanel statusCircle = new JPanel();
        statusCircle.setPreferredSize(new Dimension(14, 14));
        statusCircle.setBackground(circleColor);
        statusCircle.setBorder(new LineBorder(Color.DARK_GRAY, 1, true));

        JLabel statusLabel = new JLabel(statusText);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(StudentDashboard.DARK_TEXT);

        statusPanel.add(statusCircle);
        statusPanel.add(statusLabel);

        return statusPanel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        return label;
    }
}