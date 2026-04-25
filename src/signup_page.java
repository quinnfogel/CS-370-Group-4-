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
    private static final String DEFAULT_BENEFIT_TYPE = "CH33";

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

        String checkEmailSql = """
                SELECT 1
                FROM "user"
                WHERE email = ?
                """;

        String insertUserSql = """
                INSERT INTO "user" (first_name, last_name, email, password_hash, role, is_active, last_login)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String insertStudentSql = """
                INSERT INTO student (user_id, benefit_type)
                VALUES (?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                if (emailAlreadyExists(conn, checkEmailSql, email)) {
                    JOptionPane.showMessageDialog(this,
                            "That email is already registered.",
                            "Duplicate Email",
                            JOptionPane.ERROR_MESSAGE);
                    conn.rollback();
                    return;
                }

                int userId = insertUser(conn, insertUserSql, firstName, lastName, email, passwordHash);
                insertStudent(conn, insertStudentSql, userId, DEFAULT_BENEFIT_TYPE);

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Account created successfully. You can now log in.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                new login_page();
                dispose();

            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();

                JOptionPane.showMessageDialog(this,
                        "Error creating account: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database connection error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean emailAlreadyExists(Connection conn, String sql, String email) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private int insertUser(Connection conn,
                           String sql,
                           String firstName,
                           String lastName,
                           String email,
                           String passwordHash) throws Exception {
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.setString(4, passwordHash);
            pstmt.setString(5, "Student");
            pstmt.setInt(6, 1);
            pstmt.setNull(7, Types.TIMESTAMP);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new Exception("Failed to create user account.");
            }

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new Exception("Failed to retrieve generated user ID.");
    }

    private void insertStudent(Connection conn, String sql, int userId, String benefitType) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, benefitType);
            pstmt.executeUpdate();
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

    private void createUIComponents() {
        TitleCLabel = new JLabel("Welcome to VetConnect");
        Font t = TitleCLabel.getFont();
        TitleCLabel.setFont(t.deriveFont(30f));
        TitleCLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        FirstNameLabel = new JLabel("First Name:");
        Font fn = FirstNameLabel.getFont();
        FirstNameLabel.setFont(fn.deriveFont(25f));

        LastNameLabel = new JLabel("Last Name:");
        Font ln = LastNameLabel.getFont();
        LastNameLabel.setFont(ln.deriveFont(25f));

        EmailLabel = new JLabel("Email:");
        Font u = EmailLabel.getFont();
        EmailLabel.setFont(u.deriveFont(25f));
        EmailLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        PasswordCLabel = new JLabel("Password:");
        Font p = PasswordCLabel.getFont();
        PasswordCLabel.setFont(p.deriveFont(25f));
        PasswordCLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        RePassLabel = new JLabel("Re-Enter Password:");
        Font rp = RePassLabel.getFont();
        RePassLabel.setFont(rp.deriveFont(25f));
        RePassLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        LoginPageLabel = new JLabel("Already have an account?");
        Font c = LoginPageLabel.getFont();
        LoginPageLabel.setFont(c.deriveFont(15f));
        LoginPageLabel.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));

        CRAButtonWrap = new JPanel();
        CRAButtonWrap.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        LogoLabel = new JLabel();
        ImageIcon icon = new ImageIcon(getClass().getResource("/images/logo.jpg"));
        Image scaled = icon.getImage().getScaledInstance(220, 220, Image.SCALE_SMOOTH);
        LogoLabel.setIcon(new ImageIcon(scaled));
    }
}