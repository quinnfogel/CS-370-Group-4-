import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class StudentDashboard extends JFrame {

    public static final Color NAVY = new Color(15, 44, 68);
    public static final Color TEAL = new Color(45, 166, 196);
    public static final Color LIGHT_BG = new Color(245, 247, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color DARK_TEXT = new Color(33, 37, 41);
    public static final Color BORDER = new Color(220, 224, 230);
    public static final Color LOGOUT_RED = new Color(178, 34, 34);

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private HomePagePanel homePagePanel;
    private RequestCertificationPanel requestCertificationPanel;
    private ViewRequestStatusPanel viewRequestStatusPanel;

    public StudentDashboard() {
        setTitle("CSUSM VetConnect");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);
        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setPreferredSize(new Dimension(1280, 75));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel titleLabel = new JLabel("CSUSM VetConnect");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        rightPanel.setOpaque(false);

        String firstName = Session.getFirstName() != null ? Session.getFirstName() : "";
        String lastName = Session.getLastName() != null ? Session.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        if (fullName.isEmpty()) {
            fullName = "User";
        }

        String role = Session.getRole() != null ? Session.getRole() : "Student";

        JLabel welcomeLabel = new JLabel("Welcome, " + fullName);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JLabel roleLabel = new JLabel("Role: " + role);
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JButton logoutButton = createLogoutButton("Logout");
        logoutButton.addActionListener(e -> {
            Session.clearSession();
            new login_page().setVisible(true);
            dispose();
        });

        rightPanel.add(welcomeLabel);
        rightPanel.add(roleLabel);
        rightPanel.add(logoutButton);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(NAVY);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 25, 15));

        JLabel navTitle = new JLabel("Navigation");
        navTitle.setForeground(Color.WHITE);
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        navTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebar.add(navTitle);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton homeButton = createSidebarButton("Dashboard Home");
        homeButton.addActionListener(e -> {
            homePagePanel.refreshSummary();
            cardLayout.show(contentPanel, "HOME");
        });

        JButton requestButton = createSidebarButton("Request Certification");
        requestButton.addActionListener(e -> cardLayout.show(contentPanel, "REQUEST"));

        JButton modifyButton = createSidebarButton("Modify Certification");

        JButton statusButton = createSidebarButton("View Request Status");
        statusButton.addActionListener(e -> {
            viewRequestStatusPanel.refreshData();
            cardLayout.show(contentPanel, "STATUS");
        });

        JButton historyButton = createSidebarButton("Request History");

        sidebar.add(homeButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(requestButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(modifyButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(statusButton);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        sidebar.add(historyButton);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private JPanel createContentPanel() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        homePagePanel = new HomePagePanel();
        viewRequestStatusPanel = new ViewRequestStatusPanel();
        requestCertificationPanel = new RequestCertificationPanel(homePagePanel);

        contentPanel.add(new JScrollPane(homePagePanel), "HOME");
        contentPanel.add(new JScrollPane(requestCertificationPanel), "REQUEST");
        contentPanel.add(new JScrollPane(viewRequestStatusPanel), "STATUS");

        cardLayout.show(contentPanel, "HOME");

        return contentPanel;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setFocusPainted(false);
        button.setBackground(NAVY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(TEAL, 1, true),
                new EmptyBorder(10, 15, 10, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createLogoutButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(LOGOUT_RED);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(new CompoundBorder(
                new LineBorder(Color.WHITE, 2, true),
                new EmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }
}