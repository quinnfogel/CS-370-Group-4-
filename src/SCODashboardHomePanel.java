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

public class SCODashboardHomePanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

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

        int pendingCount = getCount(
                "SELECT COUNT(*) FROM cert_request WHERE status = 'Submitted' AND is_draft = 0"
        );
        int inReviewCount = getCount(
                "SELECT COUNT(*) FROM cert_request WHERE status = 'In Review' AND is_draft = 0"
        );
        int actionNeededCount = getCount(
                "SELECT COUNT(*) FROM cert_request WHERE status = 'Action Needed' AND is_draft = 0"
        );

        cardsPanel.add(createSummaryCard("Pending Requests", String.valueOf(pendingCount)));
        cardsPanel.add(createSummaryCard("In Review", String.valueOf(inReviewCount)));
        cardsPanel.add(createSummaryCard("Action Needed", String.valueOf(actionNeededCount)));

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
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Double baseHousingRate = getBaseHousingRate();

        if (baseHousingRate != null) {
            model.addRow(new Object[]{
                    "CH33",
                    formatMoney(baseHousingRate),
                    formatMoney(baseHousingRate * 0.75),
                    formatMoney(baseHousingRate * 0.50),
                    "Calculated from monthly_allowance_config"
            });

            model.addRow(new Object[]{
                    "CH33D",
                    formatMoney(baseHousingRate),
                    formatMoney(baseHousingRate * 0.75),
                    formatMoney(baseHousingRate * 0.50),
                    "Calculated from monthly_allowance_config"
            });
        } else {
            model.addRow(new Object[]{"CH33", "Not Configured", "Not Configured", "Not Configured", "No base_housing_rate found"});
            model.addRow(new Object[]{"CH33D", "Not Configured", "Not Configured", "Not Configured", "No base_housing_rate found"});
        }

        model.addRow(new Object[]{"CH31", "Varies", "Varies", "Varies", "VR&E rates may differ"});
        model.addRow(new Object[]{"CH35", "Varies", "Varies", "Varies", "Rate not stored in current database"});

        JTable table = new JTable(model);
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

        int submittedCount = getCount(
                "SELECT COUNT(*) FROM cert_request WHERE status = 'Submitted' AND is_draft = 0"
        );
        int unresolvedErrors = getCount(
                "SELECT COUNT(*) FROM cert_error WHERE is_resolved = 0"
        );
        int approvedCount = getCount(
                "SELECT COUNT(*) FROM cert_request WHERE status = 'Approved' AND is_draft = 0"
        );

        content.add(createBodyLabel("• Submitted requests currently in the system: " + submittedCount));
        content.add(createBodyLabel("• Unresolved certification errors: " + unresolvedErrors));
        content.add(createBodyLabel("• Approved requests currently in the system: " + approvedCount));
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

    private int getCount(String sql) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private Double getBaseHousingRate() {
        String sql = "SELECT base_housing_rate FROM monthly_allowance_config WHERE config_id = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("base_housing_rate");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private String formatMoney(double amount) {
        return String.format("$%,.2f", amount);
    }
}