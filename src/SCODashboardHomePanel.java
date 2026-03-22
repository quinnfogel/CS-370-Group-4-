import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class SCODashboardHomePanel extends JPanel {

    public SCODashboardHomePanel() {
        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("SCO Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(SCODashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        JPanel upperSection = new JPanel(new BorderLayout(0, 20));
        upperSection.setOpaque(false);

        JPanel stackedPanels = new JPanel();
        stackedPanels.setOpaque(false);
        stackedPanels.setLayout(new BoxLayout(stackedPanels, BoxLayout.Y_AXIS));

        JPanel workflowPanel = createWorkflowPanel();
        JPanel ratesPanel = createRatesPanel();
        JPanel notesPanel = createNotesPanel();

        workflowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ratesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        stackedPanels.add(workflowPanel);
        stackedPanels.add(Box.createRigidArea(new Dimension(0, 20)));
        stackedPanels.add(ratesPanel);
        stackedPanels.add(Box.createRigidArea(new Dimension(0, 20)));
        stackedPanels.add(notesPanel);

        JPanel lowerWrapper = new JPanel(new BorderLayout());
        lowerWrapper.setOpaque(false);
        lowerWrapper.add(stackedPanels, BorderLayout.NORTH);

        upperSection.add(createSummaryCards(), BorderLayout.NORTH);
        upperSection.add(lowerWrapper, BorderLayout.CENTER);

        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.add(upperSection, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(upperWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(SCODashboard.ADMIN_BG);

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        cardsPanel.add(createSummaryCard("Pending Requests", "12"));
        cardsPanel.add(createSummaryCard("In Review", "7"));
        cardsPanel.add(createSummaryCard("Action Needed", "3"));

        return cardsPanel;
    }

    private JPanel createSummaryCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SCODashboard.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(SCODashboard.BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.setPreferredSize(new Dimension(220, 110));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(SCODashboard.DARK_TEXT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);

        return card;
    }

    private JPanel createWorkflowPanel() {
        JPanel panel = createCardPanel("How to Use VetConnect");

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        content.add(createBodyLabel("Use the navigation menu on the left to review, manage, and resolve student certification requests."));
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        content.add(createSectionLabel("Recommended SCO Workflow:"));
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(createBodyLabel("1. Open Submitted Requests to review newly submitted certifications."));
        content.add(createBodyLabel("2. Check enrolled classes, total units, and benefit type for the current term."));
        content.add(createBodyLabel("3. Update the request status to In Review, Approved, or Action Needed."));
        content.add(createBodyLabel("4. If an issue is found, send the request to Certification Errors for follow-up."));
        content.add(createBodyLabel("5. Use Manage Requests to search for and update any certification already in the system."));
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        content.add(createSectionLabel("Status Guidance:"));
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(createBodyLabel("• In Review: Request has been received and is being reviewed by the SCO."));
        content.add(createBodyLabel("• Approved: Request has been verified and is ready for final processing."));
        content.add(createBodyLabel("• Action Needed: Student must update or correct the certification request."));
        content.add(createBodyLabel("• Cancelled: Student cancelled the certification request."));

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JPanel createRatesPanel() {
        JPanel panel = createCardPanel("Monthly Benefit Rate Reference");

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        String[] columns = {"Benefit Type", "Full-Time", "3/4-Time", "Half-Time", "Notes"};
        Object[][] data = {
                {"CH33", "$3,200", "$2,400", "$1,600", "Post-9/11 GI Bill"},
                {"CH33D", "$3,200", "$2,400", "$1,600", "Transferred Benefits"},
                {"CH31", "Varies", "Varies", "Varies", "VR&E rates may differ"},
                {"CH35", "$1,488", "$1,176", "$862", "DEA monthly student payment"}
        };

        JTable table = new JTable(data, columns);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(226, 235, 229));
        table.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        table.setSelectionBackground(new Color(214, 232, 220));
        table.setGridColor(SCODashboard.BORDER);
        table.setFillsViewportHeight(true);
        table.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 150));

        content.add(scrollPane, BorderLayout.CENTER);
        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

        return panel;
    }

    private JPanel createNotesPanel() {
        JPanel panel = createCardPanel("Important Notes");

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        content.add(createBodyLabel("• Monthly rate values shown on this page are for dashboard reference only."));
        content.add(createBodyLabel("• Final payment amounts may depend on training time, course length, and VA rules."));
        content.add(createBodyLabel("• CH31 students may also require Purchase Order tracking and bookstore verification."));
        content.add(createBodyLabel("• Certification Errors should be used for requests that require student correction or follow-up."));
        content.add(createBodyLabel("• Manage Requests should be used when searching for a specific student or certification record."));

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(SCODashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(SCODashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(SCODashboard.DARK_TEXT);

        panel.setLayout(new BorderLayout());
        panel.add(titleLabel, BorderLayout.NORTH);

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(SCODashboard.DARK_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 17));
        label.setForeground(SCODashboard.DARK_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}