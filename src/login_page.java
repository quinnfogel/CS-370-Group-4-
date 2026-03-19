import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;

public class login_page extends JFrame {
    private JButton CreateACCButton;
    private JLabel TitleLabel;
    private JPanel MainPanel;
    private JLabel SubTitle;
    private JLabel UsernameLabel;
    private JLabel PasswordLabel;
    private JLabel CreateACCLabel;
    private JLabel LogoLabel;
    private JPanel LoginPanel;
    private JPasswordField PasswordField;
    private JTextField UsernameForm;
    private JButton forgotPasswordButton;
    private JButton LoginButton;
    private JPanel LoginButtonWrap;

    // Path to SQLite database
    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    public login_page() {
        setContentPane(MainPanel);
        setTitle("VetConnect Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);

        CreateACCButton.addActionListener(e -> {
            new signup_page();
            setVisible(false);
        });

        LoginButton.addActionListener(e -> loginUser());

        forgotPasswordButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Forgot password feature is not implemented yet.")
        );

        setVisible(true);
    }

    private void loginUser() {
        String username = UsernameForm.getText().trim();
        String password = new String(PasswordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "SELECT * FROM Accounts WHERE Username = ? AND Password = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String firstName = rs.getString("FirstName");

                    JOptionPane.showMessageDialog(this,
                            "Login successful. Welcome, " + firstName + "!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Open next page here if needed
                    // new dashboard_page();
                    // dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid username or password.",
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new login_page();
    }

    private void createUIComponents() {
        TitleLabel = new JLabel("Welcome to VetConnect");
        Font T = TitleLabel.getFont();
        TitleLabel.setFont(T.deriveFont(45f));
        TitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        SubTitle = new JLabel("Please enter your login information below");
        Font S = SubTitle.getFont();
        SubTitle.setFont(S.deriveFont(25f));
        SubTitle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        UsernameLabel = new JLabel("Username:");
        Font U = UsernameLabel.getFont();
        UsernameLabel.setFont(U.deriveFont(25f));
        UsernameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PasswordLabel = new JLabel("Password:");
        Font P = PasswordLabel.getFont();
        PasswordLabel.setFont(P.deriveFont(25f));
        PasswordLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        CreateACCLabel = new JLabel("Don't have an account, create one here");
        Font C = CreateACCLabel.getFont();
        CreateACCLabel.setFont(C.deriveFont(15f));
        CreateACCLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        LoginButtonWrap = new JPanel();
        LoginButtonWrap.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        LogoLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/temp_logo.jpg"));
        Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        LogoLabel.setIcon(new ImageIcon(scaled));
    }
}