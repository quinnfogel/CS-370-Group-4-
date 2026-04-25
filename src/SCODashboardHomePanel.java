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

        int submittedCount = getCountByStatus(RequestStatus.SUBMITTED);
        int certifiedCount = getCountByStatus(RequestStatus.CERTIFIED);
        int actionNeededCount = getCountByStatus(RequestStatus.ACTION_NEEDED);

        cardsPanel.add(createSummaryCard("Submitted Requests", String.valueOf(submittedCount)));
        cardsPanel.add(createSummaryCard("Certified", String.valueOf(certifiedCount)));
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
        content.add(createBodyLabel("1. Open Certification Queue to review newly submitted certifications."));
        content.add(createBodyLabel("2. Check enrolled classes, total units, and benefit type for the current term."));
        content.add(createBodyLabel("3. Update the request status to Submitted, Certified, or Action Needed."));
        content.add(createBodyLabel("4. If an issue is found, send the request to Certification Errors for follow-up."));
        content.add(createBodyLabel("5. Use Request History to search for and review certifications already in the system."));
        content.add(Box.createRigidArea(new Dimension(0, 12)));

        content.add(createSectionLabel("Status Guidance:"));
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(createBodyLabel("• Submitted: Request has been received and is ready for SCO review."));
        content.add(createBodyLabel("• Certified: Request has been verified and approved."));
        content.add(createBodyLabel("• Action Needed: Student must update or correct the certification request."));
        content.add(createBodyLabel("• Cancelled: Student cancelled the certification request or it was closed."));

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

        addRateRow(model, BenefitType.CH33, "Uses MonthlyAllowanceCalculator");
        addRateRow(model, BenefitType.CH33D, "Uses MonthlyAllowanceCalculator");
        addRateRow(model, BenefitType.CH31, "Uses MonthlyAllowanceCalculator");
        addRateRow(model, BenefitType.CH35, "Uses MonthlyAllowanceCalculator");

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

    private void addRateRow(DefaultTableModel model, BenefitType benefitType, String note) {
        double fullTime = MonthlyAllowanceCalculator.calculateMonthlyAllowance(benefitType, "FullTime");
        double threeQuarter = MonthlyAllowanceCalculator.calculateMonthlyAllowance(benefitType, "ThreeQuarterTime");
        double halfTime = MonthlyAllowanceCalculator.calculateMonthlyAllowance(benefitType, "HalfTime");

        model.addRow(new Object[]{
                benefitType.getDisplayName(),
                formatMoney(fullTime),
                formatMoney(threeQuarter),
                formatMoney(halfTime),
                note
        });
    }

    private JPanel createNotesPanel() {
        JPanel panel = createCardPanel("Important Notes");

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        int submittedCount = getCountByStatus(RequestStatus.SUBMITTED);
        int unresolvedErrors = getUnresolvedErrorCount();
        int certifiedCount = getCountByStatus(RequestStatus.CERTIFIED);

        content.add(createBodyLabel("• Submitted requests currently in the system: " + submittedCount));
        content.add(createBodyLabel("• Unresolved certification errors: " + unresolvedErrors));
        content.add(createBodyLabel("• Certified requests currently in the system: " + certifiedCount));
        content.add(createBodyLabel("• CH31 students may also require Purchase Order tracking and bookstore verification."));
        content.add(createBodyLabel("• Certification Errors should be used for requests that require student correction or follow-up."));
        content.add(createBodyLabel("• Request History should be used when searching for a specific student or certification record."));

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

    private int getCountByStatus(RequestStatus status) {
        if (status == null) {
            return 0;
        }

        String sql = """
                SELECT COUNT(*)
                FROM cert_request
                WHERE is_draft = 0
                    AND (
                        UPPER(REPLACE(status, ' ', '_')) = ?
                        OR UPPER(REPLACE(status, ' ', '_')) IN (?, ?, ?, ?)
                        )
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String normalized = status.name(); // SUBMITTED, ACTION_NEEDED, etc.

            pstmt.setString(1, normalized);

            switch (status) {
                case SUBMITTED -> {
                    pstmt.setString(2, "PENDING");
                    pstmt.setString(3, "IN_REVIEW");
                    pstmt.setString(4, "APPROVED");
                    pstmt.setString(5, "DRAFT");
                }
                case ACTION_NEEDED -> {
                    pstmt.setString(2, "ERROR");
                    pstmt.setString(3, "ERROR_FOUND");
                    pstmt.setString(4, "ACTION_NEEDED");
                    pstmt.setString(5, "ACTIONNEEDED");
                }
                case CERTIFIED -> {
                    pstmt.setString(2, "CERTIFIED");
                    pstmt.setString(3, "CERTIFIED_SUBMITTED");
                    pstmt.setString(4, "APPROVED");
                    pstmt.setString(5, "DONE");
                }
                case CANCELLED -> {
                    pstmt.setString(2, "CANCELLED");
                    pstmt.setString(3, "CANCELED");
                    pstmt.setString(4, "VOID");
                    pstmt.setString(5, "DELETED");
                }
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    private int getUnresolvedErrorCount() {
        String sql = "SELECT COUNT(*) FROM cert_error WHERE is_resolved = 0";

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

    private String formatLegacyStatus(RequestStatus status) {
        return switch (status) {
            case SUBMITTED -> "Submitted";
            case ACTION_NEEDED -> "Action Needed";
            case CERTIFIED -> "Certified";
            case CANCELLED -> "Cancelled";
            default -> "N/A";
        };
    }

    private String formatMoney(double amount) {
        return String.format("$%,.2f", amount);
    }
}