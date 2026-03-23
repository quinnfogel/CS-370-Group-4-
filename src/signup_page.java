import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class signup_page extends JFrame {
    private JPanel MainCPanel;
    private JPanel CreationPanel;
    private JLabel TitleCLabel;
    private JLabel PasswordCLabel;
    private JLabel LoginPageLabel;
    private JButton LoginPageButton;
    private JLabel LogoLabel;
    private JTextField EmailForm;
    private JLabel EmailLabel;
    private JPasswordField PasswordField;
    private JPanel CRAButtonWrap;
    private JButton LoginButton;
    private JPasswordField passwordField1;
    private JLabel RePassLabel;
    private JTextField LastNameForm;
    private JLabel FirstNameLabel;
    private JLabel LastNameLabel;
    private JTextField FirstNameForm;

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    public signup_page() {
        setContentPane(MainCPanel);
        setTitle("VetConnect Account Creation");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1920, 1080);
        setLocationRelativeTo(null);

        LoginPageButton.addActionListener(e -> {
            new login_page();
            dispose();
        });

        LoginButton.addActionListener(e -> createAccount());

        setVisible(true);
    }

    private void createAccount() {
        String firstName = FirstNameForm.getText().trim();
        String lastName = LastNameForm.getText().trim();
        String email = EmailForm.getText().trim();
        String password = new String(PasswordField.getPassword()).trim();
        String confirmPassword = new String(passwordField1.getPassword()).trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Password Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 8 characters long.",
                    "Weak Password",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passwordHash = hashPassword(password);

        String insertUserSql = """
                INSERT INTO "user" (first_name, last_name, email, password_hash, role)
                VALUES (?, ?, ?, ?, 'Student')
                """;

        String insertStudentSql = """
                INSERT INTO student (user_id, benefit_type)
                VALUES (?, 'N/A')
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, firstName);
                userStmt.setString(2, lastName);
                userStmt.setString(3, email);
                userStmt.setString(4, passwordHash);

                int rowsAffected = userStmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new Exception("Failed to create user account.");
                }

                int userId;
                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getInt(1);
                    } else {
                        throw new Exception("Failed to retrieve generated user ID.");
                    }
                }

                try (PreparedStatement studentStmt = conn.prepareStatement(insertStudentSql)) {
                    studentStmt.setInt(1, userId);
                    studentStmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Account created successfully. You can now log in.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                new login_page();
                dispose();

            } catch (Exception ex) {
                conn.rollback();

                String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
                if (message.contains("unique") || message.contains("email")) {
                    JOptionPane.showMessageDialog(this,
                            "That email is already registered.",
                            "Duplicate Email",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Error creating account: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database connection error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && !email.contains(" ");
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
        SwingUtilities.invokeLater(signup_page::new);
    }

    private void createUIComponents() {
        TitleCLabel = new JLabel("Welcome to VetConnect");
        Font t = TitleCLabel.getFont();
        TitleCLabel.setFont(t.deriveFont(30f));
        TitleCLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        FirstNameLabel = new JLabel("First Name:");
        Font fn = FirstNameLabel.getFont();
        FirstNameLabel.setFont(fn.deriveFont(12f));

        LastNameLabel = new JLabel("Last Name:");
        Font ln = LastNameLabel.getFont();
        LastNameLabel.setFont(ln.deriveFont(12f));

        EmailLabel = new JLabel("Email:");
        Font u = EmailLabel.getFont();
        EmailLabel.setFont(u.deriveFont(12f));
        EmailLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PasswordCLabel = new JLabel("Password:");
        Font p = PasswordCLabel.getFont();
        PasswordCLabel.setFont(p.deriveFont(12f));
        PasswordCLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        RePassLabel = new JLabel("Re-Enter Password:");
        Font rp = RePassLabel.getFont();
        RePassLabel.setFont(rp.deriveFont(12f));
        RePassLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        LoginPageLabel = new JLabel("Already have an account?");
        Font c = LoginPageLabel.getFont();
        LoginPageLabel.setFont(c.deriveFont(10f));
        LoginPageLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        CRAButtonWrap = new JPanel();
        CRAButtonWrap.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        LogoLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/temp_logo.jpg"));
        Image scaled = icon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH);
        LogoLabel.setIcon(new ImageIcon(scaled));
    }
}