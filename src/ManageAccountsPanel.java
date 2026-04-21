import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;

public class ManageAccountsPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable scoTable;
    private DefaultTableModel tableModel;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField employeeIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private final String currentUserEmail;

    public ManageAccountsPanel(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;

        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Manage Accounts");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(SCODashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        JPanel stacked = new JPanel();
        stacked.setOpaque(false);
        stacked.setLayout(new BoxLayout(stacked, BoxLayout.Y_AXIS));

        stacked.add(createCreateAccountPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createCurrentAccountsPanel());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(stacked, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(SCODashboard.ADMIN_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);

        loadAccounts();
    }

    private JPanel createCreateAccountPanel() {
        JPanel panel = createCardPanel("Create New SCO Account");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        firstNameField = new JTextField();
        lastNameField = new JTextField();
        emailField = new JTextField();
        employeeIdField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        formPanel.add(createLabeledField("First Name:", firstNameField));
        formPanel.add(createLabeledField("Last Name:", lastNameField));
        formPanel.add(createLabeledField("CSUSM Email:", emailField));
        formPanel.add(createLabeledField("Employee ID:", employeeIdField));
        formPanel.add(createLabeledField("Password:", passwordField));
        formPanel.add(createLabeledField("Confirm Password:", confirmPasswordField));

        JButton createButton = createActionButton("Create SCO Account");
        JButton clearButton = createSecondaryButton("Clear Form");

        createButton.addActionListener(e -> createScoAccount());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createButton);
        buttonPanel.add(clearButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        return panel;
    }

    private JPanel createCurrentAccountsPanel() {
        JPanel panel = createCardPanel("Current SCO Accounts");
        panel.setLayout(new BorderLayout());

        String[] columns = {"User ID", "Employee ID", "First Name", "Last Name", "Email", "Role", "Active"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scoTable = new JTable(tableModel);
        scoTable.setRowHeight(28);
        scoTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoTable.getTableHeader().setBackground(new Color(226, 235, 229));
        scoTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        scoTable.setSelectionBackground(new Color(214, 232, 220));
        scoTable.setGridColor(SCODashboard.BORDER);
        scoTable.setFillsViewportHeight(true);
        scoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(scoTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 220));

        JButton deleteButton = createDeleteButton("Delete Selected SCO");
        deleteButton.addActionListener(e -> deleteSelectedSco());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(deleteButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        return panel;
    }

    private void loadAccounts() {
        tableModel.setRowCount(0);

        String sql = """
                SELECT u.user_id,
                       u.first_name,
                       u.last_name,
                       u.email,
                       u.role,
                       u.is_active,
                       s.emp_id
                FROM user u
                JOIN sco s ON u.user_id = s.user_id
                WHERE u.role = ?
                ORDER BY u.last_name, u.first_name
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, UserRole.SCO.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SCO sco = new SCO(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            "",
                            rs.getBoolean("is_active"),
                            null,
                            rs.getInt("emp_id")
                    );

                    tableModel.addRow(new Object[]{
                            sco.getUserId(),
                            sco.getEmpId(),
                            sco.getFirstName(),
                            sco.getLastName(),
                            sco.getEmail(),
                            sco.getRole().name(),
                            sco.isActive() ? "Yes" : "No"
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load SCO accounts.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createScoAccount() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String employeeIdText = employeeIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || employeeIdText.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please complete all fields before creating an SCO account.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.endsWith("@csusm.edu")) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid CSUSM email address.",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!employeeIdText.matches("\\d+")) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID must be numeric.",
                    "Invalid Employee ID",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Password Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this,
                    "Password must be at least 6 characters.",
                    "Weak Password",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int employeeId = Integer.parseInt(employeeIdText);

        SCO newSco = new SCO(
                1,
                firstName,
                lastName,
                email,
                hashPassword(password),
                true,
                null,
                employeeId
        );

        String duplicateCheckSql = """
                SELECT 1
                FROM user u
                LEFT JOIN sco s ON u.user_id = s.user_id
                WHERE u.email = ? OR s.emp_id = ?
                LIMIT 1
                """;

        String insertUserSql = """
                INSERT INTO user (first_name, last_name, email, password_hash, role, is_active, last_login)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        String insertScoSql = """
                INSERT INTO sco (emp_id, user_id)
                VALUES (?, ?)
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement checkStmt = conn.prepareStatement(duplicateCheckSql)) {
                    checkStmt.setString(1, newSco.getEmail());
                    checkStmt.setInt(2, newSco.getEmpId());

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this,
                                    "An account with that email or employee ID already exists.",
                                    "Duplicate Account",
                                    JOptionPane.WARNING_MESSAGE);
                            conn.rollback();
                            return;
                        }
                    }
                }

                int userId;

                try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    userStmt.setString(1, newSco.getFirstName());
                    userStmt.setString(2, newSco.getLastName());
                    userStmt.setString(3, newSco.getEmail());
                    userStmt.setString(4, newSco.getPasswordHash());
                    userStmt.setString(5, newSco.getRole().name());
                    userStmt.setBoolean(6, newSco.isActive());
                    userStmt.setString(7, (String) null);
                    userStmt.executeUpdate();

                    try (ResultSet keys = userStmt.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new Exception("Failed to retrieve generated user ID.");
                        }
                        userId = keys.getInt(1);
                    }
                }

                try (PreparedStatement scoStmt = conn.prepareStatement(insertScoSql)) {
                    scoStmt.setInt(1, newSco.getEmpId());
                    scoStmt.setInt(2, userId);
                    scoStmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "SCO account created successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                clearForm();
                loadAccounts();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to create account.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedSco() {
        int selectedRow = scoTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an SCO account to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = tableModel.getValueAt(selectedRow, 4).toString();

        if (email.equalsIgnoreCase(currentUserEmail)) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String employeeId = tableModel.getValueAt(selectedRow, 1).toString();
        String firstName = tableModel.getValueAt(selectedRow, 2).toString();
        String lastName = tableModel.getValueAt(selectedRow, 3).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete SCO account for " + firstName + " " + lastName + " (ID: " + employeeId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String deleteScoSql = "DELETE FROM sco WHERE user_id = ?";
        String deleteUserSql = "DELETE FROM user WHERE user_id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement scoStmt = conn.prepareStatement(deleteScoSql)) {
                    scoStmt.setInt(1, userId);
                    scoStmt.executeUpdate();
                }

                try (PreparedStatement userStmt = conn.prepareStatement(deleteUserSql)) {
                    userStmt.setInt(1, userId);
                    userStmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "SCO account deleted successfully.",
                        "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);

                loadAccounts();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to delete account.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password.", e);
        }
    }

    private void clearForm() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailField.setText("");
        employeeIdField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
    }

    public void refresh() {
        loadAccounts();
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(SCODashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(SCODashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(SCODashboard.DARK_TEXT);

        panel.setLayout(new BorderLayout());
        panel.add(titleLabel, BorderLayout.NORTH);

        return panel;
    }

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(SCODashboard.ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(70, 110, 80), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(120, 130, 140));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(90, 100, 110), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createDeleteButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(178, 34, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 20, 20), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}