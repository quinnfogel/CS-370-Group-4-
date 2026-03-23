import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.BorderFactory;

public class login_page extends JFrame {
    private JButton CreateACCButton;
    private JLabel TitleLabel;
    private JPanel MainPanel;
    private JLabel SubTitle;
    private JLabel EmailLabel;
    private JLabel PasswordLabel;
    private JLabel CreateACCLabel;
    private JLabel LogoLabel;
    private JPanel LoginPanel;
    private JPasswordField PasswordField;
    private JTextField EmailForm;
    private JButton forgotPasswordButton;
    private JButton LoginButton;
    private JPanel LoginButtonWrap;

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    public login_page() {
        setContentPane(MainPanel);
        setTitle("VetConnect Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);

        CreateACCButton.addActionListener(e -> {
            new signup_page();
            dispose();
        });

        LoginButton.addActionListener(e -> loginUser());

        forgotPasswordButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Forgot password feature is not implemented yet.")
        );

        setVisible(true);
    }

    private void loginUser() {
        String email = EmailForm.getText().trim();
        String password = new String(PasswordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both email and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hashedPassword = hashPassword(password);

        String query = """
                SELECT user_id, first_name, last_name, email, role
                FROM "user"
                WHERE email = ? AND password_hash = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String firstName = rs.getString("first_name");
                    String lastName = rs.getString("last_name");
                    String userEmail = rs.getString("email");
                    String role = rs.getString("role");

                    Session.startSession(userId, userEmail, firstName, lastName, role);
                    updateLastLogin(conn, userId);

                    JOptionPane.showMessageDialog(this,
                            "Login successful. Welcome, " + firstName + "!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    if ("Student".equalsIgnoreCase(role)) {
                        new StudentDashboard().setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Admin page WIP",
                                "Work In Progress",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid email or password.",
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

    private void updateLastLogin(Connection conn, int userId) {
        String updateQuery = """
                UPDATE "user"
                SET last_login = CURRENT_TIMESTAMP
                WHERE user_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password.", e);
        }
    }

    public static void main(String[] args) {
        new login_page();
    }

    private void createUIComponents() {
        TitleLabel = new JLabel("Welcome to VetConnect");
        Font t = TitleLabel.getFont();
        TitleLabel.setFont(t.deriveFont(45f));
        TitleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        SubTitle = new JLabel("Please enter your login information below");
        Font s = SubTitle.getFont();
        SubTitle.setFont(s.deriveFont(25f));
        SubTitle.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        EmailLabel = new JLabel("Email:");
        Font u = EmailLabel.getFont();
        EmailLabel.setFont(u.deriveFont(25f));
        EmailLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PasswordLabel = new JLabel("Password:");
        Font p = PasswordLabel.getFont();
        PasswordLabel.setFont(p.deriveFont(25f));
        PasswordLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        CreateACCLabel = new JLabel("Don't have an account, create one here");
        Font c = CreateACCLabel.getFont();
        CreateACCLabel.setFont(c.deriveFont(15f));
        CreateACCLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        LoginButtonWrap = new JPanel();
        LoginButtonWrap.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        LogoLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/temp_logo.jpg"));
        Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        LogoLabel.setIcon(new ImageIcon(scaled));
    }
}