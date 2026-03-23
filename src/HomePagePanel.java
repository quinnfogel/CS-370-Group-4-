import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class HomePagePanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

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

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        centerContent.add(createSummaryCards(), BorderLayout.NORTH);

        JPanel lowerSection = new JPanel();
        lowerSection.setOpaque(false);
        lowerSection.setLayout(new BoxLayout(lowerSection, BoxLayout.Y_AXIS));

        JPanel contactPanel = createContactPanel();
        JPanel infoPanel = createInformationPanel();

        contactPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        lowerSection.add(contactPanel);
        lowerSection.add(Box.createRigidArea(new Dimension(0, 20)));
        lowerSection.add(infoPanel);

        JPanel lowerWrapper = new JPanel(new BorderLayout());
        lowerWrapper.setOpaque(false);
        lowerWrapper.add(lowerSection, BorderLayout.NORTH);

        centerContent.add(lowerWrapper, BorderLayout.CENTER);

        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        DashboardSummary summary = loadDashboardSummary();

        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);
        cardsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        cardsPanel.add(createSummaryCard("Current Term", summary.currentTerm));
        cardsPanel.add(createSummaryCard("Benefit Type", summary.benefitType));
        cardsPanel.add(createSummaryCard("Latest Status", summary.latestStatus));

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

                        String benefitType = rs.getString("benefit_type");
                        if (benefitType != null && !benefitType.isBlank()) {
                            summary.benefitType = benefitType;
                        }
                    } else {
                        return summary;
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(latestRequestQuery)) {
                pstmt.setInt(1, studentId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        summary.currentTerm = String.valueOf(rs.getInt("academic_term_code"));

                        String latestStatus = rs.getString("status");
                        if (latestStatus != null && !latestStatus.isBlank()) {
                            summary.latestStatus = latestStatus;
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return summary;
    }

    private JPanel createSummaryCard(String title, String value) {
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

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(StudentDashboard.DARK_TEXT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);

        return card;
    }

    private JPanel createContactPanel() {
        JPanel contactPanel = new JPanel();
        contactPanel.setLayout(new BoxLayout(contactPanel, BoxLayout.Y_AXIS));
        contactPanel.setBackground(StudentDashboard.CARD_BG);
        contactPanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        contactPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JLabel title = new JLabel("Contact Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(StudentDashboard.DARK_TEXT);

        JLabel line1 = createBodyLabel("CSUSM Veterans Services");
        JLabel line2 = createBodyLabel("333 S. Twin Oaks Valley Rd");
        JLabel line3 = createBodyLabel("San Marcos, CA 92096");
        JLabel line4 = createBodyLabel("Phone: (760) 750-4827");
        JLabel line5 = createBodyLabel("Email: veterans@csusm.edu");
        JLabel line6 = createBodyLabel("Hours: Monday–Thursday 8am–5pm");
        JLabel line7 = createBodyLabel("Friday: 8am–4pm");

        contactPanel.add(title);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contactPanel.add(line1);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        contactPanel.add(line2);
        contactPanel.add(line3);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contactPanel.add(line4);
        contactPanel.add(line5);
        contactPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contactPanel.add(line6);
        contactPanel.add(line7);

        return contactPanel;
    }

    private JPanel createInformationPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(StudentDashboard.CARD_BG);
        infoPanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        infoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JLabel title = new JLabel("VetConnect Information");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(StudentDashboard.DARK_TEXT);

        JLabel p1 = createBodyLabel("VetConnect allows students using VA education benefits");
        JLabel p2 = createBodyLabel("to request and manage certification of their courses.");

        JLabel supportedTitle = new JLabel("Through this system you can:");
        supportedTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        supportedTitle.setForeground(StudentDashboard.DARK_TEXT);

        JLabel b1 = createBodyLabel("• Submit a certification request");
        JLabel b2 = createBodyLabel("• Modify certification after adding or dropping classes");
        JLabel b3 = createBodyLabel("• Track certification processing status");
        JLabel b4 = createBodyLabel("• View previously submitted requests");

        JLabel footer = createBodyLabel("If you have questions regarding VA education benefits,");
        JLabel footer2 = createBodyLabel("please contact the Veterans Center using the information above.");

        infoPanel.add(title);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        infoPanel.add(p1);
        infoPanel.add(p2);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        infoPanel.add(supportedTitle);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(b1);
        infoPanel.add(b2);
        infoPanel.add(b3);
        infoPanel.add(b4);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        infoPanel.add(footer);
        infoPanel.add(footer2);

        return infoPanel;
    }

    private JLabel createBodyLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        return label;
    }

    private static class DashboardSummary {
        String currentTerm;
        String benefitType;
        String latestStatus;
    }
}