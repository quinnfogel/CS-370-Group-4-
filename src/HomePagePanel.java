import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HomePagePanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JLabel currentTermValueLabel;
    private JLabel benefitTypeValueLabel;
    private JLabel latestStatusValueLabel;

    public HomePagePanel() {
        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Student Dashboard");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

        JPanel summaryCards = createSummaryCards();
        JPanel contactPanel = createContactPanel();
        JPanel infoPanel = createInformationPanel();
        JPanel benefitsPanel = createBenefitsSection();
        JPanel unitTablePanel = createFullTimeUnitsPanel();

        summaryCards.setAlignmentX(Component.LEFT_ALIGNMENT);
        contactPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        benefitsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitTablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerContent.add(summaryCards);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(contactPanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(infoPanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(benefitsPanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(unitTablePanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(centerContent, BorderLayout.NORTH);

        add(wrapper, BorderLayout.CENTER);

        refreshSummary();
    }

    public void refreshSummary() {
        DashboardSummary summary = loadDashboardSummary();
        currentTermValueLabel.setText(summary.currentTerm);
        benefitTypeValueLabel.setText(summary.benefitType);
        latestStatusValueLabel.setText(summary.latestStatus);
    }

    private JPanel createSummaryCards() {
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        currentTermValueLabel = createSummaryValueLabel("N/A");
        benefitTypeValueLabel = createSummaryValueLabel("N/A");
        latestStatusValueLabel = createSummaryValueLabel("N/A");

        cardsPanel.add(createSummaryCard("Current Term", currentTermValueLabel));
        cardsPanel.add(createSummaryCard("Benefit Type", benefitTypeValueLabel));
        cardsPanel.add(createSummaryCard("Latest Status", latestStatusValueLabel));

        return cardsPanel;
    }

    private DashboardSummary loadDashboardSummary() {
        DashboardSummary summary = new DashboardSummary();
        summary.currentTerm = "N/A";
        summary.benefitType = "N/A";
        summary.latestStatus = "N/A";

        int userId = Session.getUserId();
        if (userId == 0) {
            return summary;
        }

        String studentQuery = """
                SELECT student_id, benefit_type
                FROM student
                WHERE user_id = ?
                """;

        String latestRequestQuery = """
                SELECT academic_term_code, status
                FROM cert_request
                WHERE student_id = ?
                ORDER BY last_updated_date DESC, cert_id DESC
                LIMIT 1
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int studentId = 0;

            try (PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        studentId = rs.getInt("student_id");
                        summary.benefitType = formatBenefitType(rs.getString("benefit_type"));
                    } else {
                        return summary;
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(latestRequestQuery)) {
                pstmt.setInt(1, studentId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        summary.currentTerm = formatAcademicTerm(rs.getInt("academic_term_code"));
                        summary.latestStatus = formatRequestStatus(rs.getString("status"));
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return summary;
    }

    private String formatBenefitType(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return "N/A";
        }

        try {
            return BenefitType.valueOf(dbValue.trim().toUpperCase()).getDisplayName();
        } catch (IllegalArgumentException ex) {
            return dbValue;
        }
    }

    private String formatRequestStatus(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return "N/A";
        }

        try {
            RequestStatus status = RequestStatus.valueOf(dbValue.trim().toUpperCase());

            return switch (status) {
                case SUBMITTED -> "Submitted";
                case ACTION_NEEDED -> "Action Needed";
                case CERTIFIED -> "Certified";
                case CANCELLED -> "Cancelled";
                default -> "N/A";
            };
        } catch (IllegalArgumentException ex) {
            return dbValue;
        }
    }

    private String formatAcademicTerm(int academicTermCode) {
        String code = String.valueOf(academicTermCode);

        if (code.length() == 4) {
            String yearPrefix = code.substring(0, 2);
            String termPart = code.substring(2);

            String fullYear = "20" + yearPrefix;

            return switch (termPart) {
                case "2" -> "Spring " + fullYear;
                case "3" -> "Summer " + fullYear;
                case "4" -> "Fall " + fullYear;
                default -> code;
            };
        }

        if (code.length() >= 6) {
            String year = code.substring(0, 4);
            String termPart = code.substring(4);

            return switch (termPart) {
                case "01" -> "Spring " + year;
                case "05" -> "Summer " + year;
                case "08" -> "Fall " + year;
                default -> code;
            };
        }

        return code;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(StudentDashboard.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.setPreferredSize(new Dimension(250, 110));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        titleLabel.setForeground(Color.GRAY);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);

        return card;
    }

    private JLabel createSummaryValueLabel(String text) {
        JLabel valueLabel = new JLabel(text);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(StudentDashboard.DARK_TEXT);
        return valueLabel;
    }

    private JPanel createContactPanel() {
        JPanel contactPanel = createCardPanel("Contact Information");
        contactPanel.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        content.add(createBodyLabel("CSUSM Veterans Services"));
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(createBodyLabel("The Epstein Family Veterans Center"));
        content.add(Box.createRigidArea(new Dimension(0, 6)));
        content.add(createBodyLabel("333 S. Twin Oaks Valley Rd"));
        content.add(createBodyLabel("San Marcos, CA 92096"));
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(createBodyLabel("Phone: (760) 750-4827"));
        content.add(createBodyLabel("Email: veterans@csusm.edu"));
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(createBodyLabel("Hours: Monday–Thursday 8am–5pm"));
        content.add(createBodyLabel("Friday: 8am–4pm"));

        contactPanel.add(content, BorderLayout.CENTER);
        return contactPanel;
    }

    private JPanel createInformationPanel() {
        JPanel infoPanel = createCardPanel("VetConnect Information");
        infoPanel.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        JLabel p1 = createWrappedBodyLabel(
                "VetConnect allows students using VA education benefits to request and manage certification of their courses."
        );

        JLabel supportedTitle = new JLabel("Through this system you can:");
        supportedTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        supportedTitle.setForeground(StudentDashboard.DARK_TEXT);

        JLabel b1 = createBodyLabel("• Submit a certification request");
        JLabel b2 = createBodyLabel("• Modify certification after adding or dropping classes");
        JLabel b3 = createBodyLabel("• Track certification processing status");
        JLabel b4 = createBodyLabel("• View previously submitted requests");

        JLabel footer = createWrappedBodyLabel(
                "If you have questions regarding VA education benefits, please contact the Veterans Center using the information above."
        );

        content.add(p1);
        content.add(Box.createRigidArea(new Dimension(0, 18)));
        content.add(supportedTitle);
        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(b1);
        content.add(b2);
        content.add(b3);
        content.add(b4);
        content.add(Box.createRigidArea(new Dimension(0, 18)));
        content.add(footer);

        infoPanel.add(content, BorderLayout.CENTER);
        return infoPanel;
    }

    private JPanel createBenefitsSection() {
        JPanel sectionPanel = createCardPanel("VA Benefit Information");
        sectionPanel.setLayout(new BorderLayout());

        JPanel stacked = new JPanel();
        stacked.setOpaque(false);
        stacked.setLayout(new BoxLayout(stacked, BoxLayout.Y_AXIS));
        stacked.setBorder(new EmptyBorder(20, 0, 0, 0));

        stacked.add(createBenefitCard(
                "CH33 – Post-9/11 GI Bill",
                new String[]{
                        "Covers tuition and fees paid directly to the school",
                        "Monthly housing allowance based on location",
                        "Annual book stipend (up to $1,000 per year)"
                },
                "https://www.va.gov/education/about-gi-bill-benefits/post-9-11/"
        ));
        stacked.add(Box.createRigidArea(new Dimension(0, 18)));

        stacked.add(createBenefitCard(
                "CH33D – Transferred Benefits",
                new String[]{
                        "Benefits transferred from a service member to a dependent",
                        "Requires VA approval and time-in-service requirements",
                        "Same benefits as standard Post-9/11 GI Bill"
                },
                "https://www.va.gov/education/transfer-post-9-11-gi-bill-benefits/"
        ));
        stacked.add(Box.createRigidArea(new Dimension(0, 18)));

        stacked.add(createBenefitCard(
                "CH31 – Veteran Readiness & Employment (VR&E)",
                new String[]{
                        "Supports veterans with service-connected disabilities",
                        "Provides tuition, supplies, and career counseling",
                        "Purchase Orders issued by VR&E counselors"
                },
                "https://www.va.gov/careers-employment/vocational-rehabilitation/"
        ));
        stacked.add(Box.createRigidArea(new Dimension(0, 18)));

        stacked.add(createBenefitCard(
                "CH35 – Dependents' Educational Assistance (DEA)",
                new String[]{
                        "Education benefits for dependents of disabled or fallen veterans",
                        "Up to 36 months of support for schooling or training",
                        "Monthly payment directly to the student",
                        "Tuition not covered, paired with CALVET"
                },
                "https://www.va.gov/education/survivor-dependent-benefits/dependents-education-assistance/"
        ));

        sectionPanel.add(stacked, BorderLayout.CENTER);
        return sectionPanel;
    }

    private JPanel createBenefitCard(String title, String[] bullets, String url) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);
        card.setBackground(StudentDashboard.CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.NAVY, 2, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(StudentDashboard.NAVY);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(12, 0, 0, 0));

        for (String bullet : bullets) {
            body.add(createBodyLabel("• " + bullet));
            body.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        JButton linkButton = new JButton("Click here for more information →");
        linkButton.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        linkButton.setForeground(Color.BLUE);
        linkButton.setBackground(StudentDashboard.CARD_BG);
        linkButton.setBorderPainted(false);
        linkButton.setFocusPainted(false);
        linkButton.setContentAreaFilled(false);
        linkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        linkButton.addActionListener(e -> openLink(url));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        bottom.setOpaque(false);
        bottom.add(linkButton);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createFullTimeUnitsPanel() {
        JPanel panel = createCardPanel("HOW MANY UNITS ARE CONSIDERED FULL TIME?");
        panel.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        String[] columns = {"", "16 weeks", "8 weeks", "6 weeks", "4 weeks", "Pay Status"};
        Object[][] data = {
                {"Units", "12", "6", "4", "3", "Full Time"},
                {"", "9", "4", "3", "2", "3/4 Time"},
                {"", "7", "3", "2.5", "1.5", ">1/2 Time"},
                {"No BAH", "6", "2", "1", "1", "<= 1/2 Time"}
        };

        JTable table = new JTable(new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });

        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
        table.getTableHeader().setBackground(new Color(230, 236, 242));
        table.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        table.setGridColor(StudentDashboard.BORDER);
        table.setSelectionBackground(new Color(220, 240, 245));
        table.setEnabled(false);
        table.setFillsViewportHeight(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(90);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 170));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JLabel note = createWrappedBodyLabel(
                "Note: Post 9/11 Education Benefit® pays to the nearest 10% of the student status. For example, if you are considered 3/4 time (75%), the VA will pay 80% of the full-time BAH rate."
        );
        note.setBorder(new EmptyBorder(12, 0, 0, 0));

        content.add(scrollPane, BorderLayout.CENTER);
        content.add(note, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
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

    private JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createWrappedBodyLabel(String text) {
        JLabel label = new JLabel("<html><div style='width:900px;'>" + text + "</div></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void openLink(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                JOptionPane.showMessageDialog(this,
                        "Desktop browsing is not supported on this system.",
                        "Unable to Open Link",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Unable to open link:\n" + url,
                    "Link Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class DashboardSummary {
        String currentTerm;
        String benefitType;
        String latestStatus;
    }
}