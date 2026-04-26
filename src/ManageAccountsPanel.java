import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class ManageAccountsPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";
    private static final String DEFAULT_BENEFIT_TYPE = "CH33";

    private JTable scoTable;
    private JTable studentTable;
    private DefaultTableModel scoTableModel;
    private DefaultTableModel studentTableModel;

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

        stacked.add(createAccountButtonPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createScoAccountsPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createStudentAccountsPanel());

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

    private JPanel createAccountButtonPanel() {
        JPanel panel = createCardPanel("Create Account");
        panel.setLayout(new BorderLayout());

        JLabel description = new JLabel("Choose whether to create a Student account or an SCO account.");
        description.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        description.setForeground(Color.GRAY);
        description.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton createButton = createActionButton("Create an Account");
        createButton.addActionListener(e -> showCreateAccountChoiceDialog());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(createButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(description, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        return panel;
    }

    private JPanel createScoAccountsPanel() {
        JPanel panel = createCardPanel("SCO Accounts");
        panel.setLayout(new BorderLayout());

        JLabel tableLabel = new JLabel("SCO");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableLabel.setForeground(SCODashboard.DARK_TEXT);
        tableLabel.setBorder(new EmptyBorder(18, 0, 8, 0));

        String[] columns = {"User ID", "Employee ID", "First Name", "Last Name", "Email", "Role"};

        scoTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scoTable = createStyledTable(scoTableModel);

        JScrollPane scrollPane = new JScrollPane(scoTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 220));

        JButton editButton = createActionButton("Edit Selected SCO");
        editButton.addActionListener(e -> editSelectedSco());

        JButton deleteButton = createDeleteButton("Delete Selected SCO");
        deleteButton.addActionListener(e -> deleteSelectedSco());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(tableLabel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 0, 0, 0));
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        return panel;
    }

    private JPanel createStudentAccountsPanel() {
        JPanel panel = createCardPanel("Student Accounts");
        panel.setLayout(new BorderLayout());

        JLabel tableLabel = new JLabel("Students");
        tableLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableLabel.setForeground(SCODashboard.DARK_TEXT);
        tableLabel.setBorder(new EmptyBorder(18, 0, 8, 0));

        String[] columns = {"User ID", "Student ID", "First Name", "Last Name", "Email", "Role", "Benefit Type"};

        studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = createStyledTable(studentTableModel);

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 220));

        JButton editButton = createActionButton("Edit Selected Student");
        editButton.addActionListener(e -> editSelectedStudent());

        JButton deleteButton = createDeleteButton("Delete Selected Student");
        deleteButton.addActionListener(e -> deleteSelectedStudent());

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(tableLabel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(8, 0, 0, 0));
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        return panel;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(226, 235, 229));
        table.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        table.setSelectionBackground(new Color(214, 232, 220));
        table.setGridColor(SCODashboard.BORDER);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    private void loadAccounts() {
        loadScoAccounts();
        loadStudentAccounts();
    }

    private void loadScoAccounts() {
        scoTableModel.setRowCount(0);

        String sql = """
                SELECT u.user_id,
                       u.first_name,
                       u.last_name,
                       u.email,
                       u.role,
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
                    scoTableModel.addRow(new Object[]{
                            rs.getInt("user_id"),
                            rs.getInt("emp_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("role")
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

    private void loadStudentAccounts() {
        studentTableModel.setRowCount(0);

        String sql = """
                SELECT u.user_id,
                       s.student_id,
                       u.first_name,
                       u.last_name,
                       u.email,
                       u.role,
                       s.benefit_type
                FROM user u
                JOIN student s ON u.user_id = s.user_id
                WHERE u.role = ?
                ORDER BY u.last_name, u.first_name
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "Student");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    studentTableModel.addRow(new Object[]{
                            rs.getInt("user_id"),
                            rs.getInt("student_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("role"),
                            rs.getString("benefit_type")
                    });
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load student accounts.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showCreateAccountChoiceDialog() {
        Object[] options = {"Student", "SCO", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Which type of account would you like to create?",
                "Create an Account",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            showCreateStudentDialog();
        } else if (choice == 1) {
            showCreateScoDialog();
        }
    }

    private void showCreateScoDialog() {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField employeeIdField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("CSUSM Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Employee ID:"));
        formPanel.add(employeeIdField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Confirm Password:"));
        formPanel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Create SCO Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            createScoAccount(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    employeeIdField.getText().trim(),
                    new String(passwordField.getPassword()),
                    new String(confirmPasswordField.getPassword())
            );
        }
    }

    private void showCreateStudentDialog() {
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Re-Enter Password:"));
        formPanel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Create Student Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            createStudentAccount(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    new String(passwordField.getPassword()).trim(),
                    new String(confirmPasswordField.getPassword()).trim()
            );
        }
    }

    private void createScoAccount(String firstName,
                                  String lastName,
                                  String email,
                                  String employeeIdText,
                                  String password,
                                  String confirmPassword) {
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
                    userStmt.setString(7, null);
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

    private void createStudentAccount(String firstName,
                                      String lastName,
                                      String email,
                                      String password,
                                      String confirmPassword) {
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
                FROM user
                WHERE email = ?
                """;

        String insertUserSql = """
                INSERT INTO user (first_name, last_name, email, password_hash, role, is_active, last_login)
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

                int userId = insertStudentUser(conn, insertUserSql, firstName, lastName, email, passwordHash);
                insertStudent(conn, insertStudentSql, userId, DEFAULT_BENEFIT_TYPE);

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Student account created successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

                loadAccounts();

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

    private void editSelectedSco() {
        int selectedRow = scoTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an SCO account to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) scoTableModel.getValueAt(selectedRow, 0);
        int currentEmpId = (int) scoTableModel.getValueAt(selectedRow, 1);
        String currentFirstName = scoTableModel.getValueAt(selectedRow, 2).toString();
        String currentLastName = scoTableModel.getValueAt(selectedRow, 3).toString();
        String currentEmail = scoTableModel.getValueAt(selectedRow, 4).toString();

        JTextField firstNameField = new JTextField(currentFirstName);
        JTextField lastNameField = new JTextField(currentLastName);
        JTextField emailField = new JTextField(currentEmail);
        JTextField employeeIdField = new JTextField(String.valueOf(currentEmpId));
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("CSUSM Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Employee ID:"));
        formPanel.add(employeeIdField);
        formPanel.add(new JLabel("New Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Confirm New Password:"));
        formPanel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Edit SCO Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String employeeIdText = employeeIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || employeeIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name, last name, email, and employee ID are required.",
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

        boolean updatePassword = !password.isEmpty() || !confirmPassword.isEmpty();

        if (updatePassword) {
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
        }

        int empId = Integer.parseInt(employeeIdText);

        String checkSql = """
                SELECT 1
                FROM user u
                LEFT JOIN sco s ON u.user_id = s.user_id
                WHERE (u.email = ? OR s.emp_id = ?)
                  AND u.user_id <> ?
                LIMIT 1
                """;

        String updateUserWithPasswordSql = """
                UPDATE user
                SET first_name = ?, last_name = ?, email = ?, password_hash = ?
                WHERE user_id = ?
                """;

        String updateUserWithoutPasswordSql = """
                UPDATE user
                SET first_name = ?, last_name = ?, email = ?
                WHERE user_id = ?
                """;

        String updateScoSql = """
                UPDATE sco
                SET emp_id = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setString(1, email);
                    pstmt.setInt(2, empId);
                    pstmt.setInt(3, userId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this,
                                    "Another account already uses that email or employee ID.",
                                    "Duplicate Data",
                                    JOptionPane.WARNING_MESSAGE);
                            conn.rollback();
                            return;
                        }
                    }
                }

                if (updatePassword) {
                    try (PreparedStatement pstmt = conn.prepareStatement(updateUserWithPasswordSql)) {
                        pstmt.setString(1, firstName);
                        pstmt.setString(2, lastName);
                        pstmt.setString(3, email);
                        pstmt.setString(4, hashPassword(password));
                        pstmt.setInt(5, userId);
                        pstmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement pstmt = conn.prepareStatement(updateUserWithoutPasswordSql)) {
                        pstmt.setString(1, firstName);
                        pstmt.setString(2, lastName);
                        pstmt.setString(3, email);
                        pstmt.setInt(4, userId);
                        pstmt.executeUpdate();
                    }
                }

                try (PreparedStatement pstmt = conn.prepareStatement(updateScoSql)) {
                    pstmt.setInt(1, empId);
                    pstmt.setInt(2, userId);
                    pstmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "SCO account updated successfully.",
                        "Success",
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
                    "Failed to update SCO account.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student account to edit.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) studentTableModel.getValueAt(selectedRow, 0);
        String currentFirstName = studentTableModel.getValueAt(selectedRow, 2).toString();
        String currentLastName = studentTableModel.getValueAt(selectedRow, 3).toString();
        String currentEmail = studentTableModel.getValueAt(selectedRow, 4).toString();

        JTextField firstNameField = new JTextField(currentFirstName);
        JTextField lastNameField = new JTextField(currentLastName);
        JTextField emailField = new JTextField(currentEmail);
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();

        JPanel formPanel = new JPanel(new GridLayout(0, 2, 12, 12));
        formPanel.add(new JLabel("First Name:"));
        formPanel.add(firstNameField);
        formPanel.add(new JLabel("Last Name:"));
        formPanel.add(lastNameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("New Password:"));
        formPanel.add(passwordField);
        formPanel.add(new JLabel("Confirm New Password:"));
        formPanel.add(confirmPasswordField);

        int result = JOptionPane.showConfirmDialog(
                this,
                formPanel,
                "Edit Student Account",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "First name, last name, and email are required.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid email address.",
                    "Invalid Email",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean updatePassword = !password.isEmpty() || !confirmPassword.isEmpty();

        if (updatePassword) {
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match.",
                        "Password Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this,
                        "Password must be at least 8 characters long.",
                        "Weak Password",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String checkSql = """
                SELECT 1
                FROM user
                WHERE email = ?
                  AND user_id <> ?
                LIMIT 1
                """;

        String updateUserWithPasswordSql = """
                UPDATE user
                SET first_name = ?, last_name = ?, email = ?, password_hash = ?
                WHERE user_id = ?
                """;

        String updateUserWithoutPasswordSql = """
                UPDATE user
                SET first_name = ?, last_name = ?, email = ?
                WHERE user_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                    pstmt.setString(1, email);
                    pstmt.setInt(2, userId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(this,
                                    "Another account already uses that email.",
                                    "Duplicate Email",
                                    JOptionPane.WARNING_MESSAGE);
                            conn.rollback();
                            return;
                        }
                    }
                }

                if (updatePassword) {
                    try (PreparedStatement pstmt = conn.prepareStatement(updateUserWithPasswordSql)) {
                        pstmt.setString(1, firstName);
                        pstmt.setString(2, lastName);
                        pstmt.setString(3, email);
                        pstmt.setString(4, hashPassword(password));
                        pstmt.setInt(5, userId);
                        pstmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement pstmt = conn.prepareStatement(updateUserWithoutPasswordSql)) {
                        pstmt.setString(1, firstName);
                        pstmt.setString(2, lastName);
                        pstmt.setString(3, email);
                        pstmt.setInt(4, userId);
                        pstmt.executeUpdate();
                    }
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Student account updated successfully.",
                        "Success",
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
                    "Failed to update student account.",
                    "Database Error",
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

    private int insertStudentUser(Connection conn,
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

    private void deleteSelectedSco() {
        int selectedRow = scoTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an SCO account to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = scoTableModel.getValueAt(selectedRow, 4).toString();

        if (email.equalsIgnoreCase(currentUserEmail)) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) scoTableModel.getValueAt(selectedRow, 0);
        String employeeId = scoTableModel.getValueAt(selectedRow, 1).toString();
        String firstName = scoTableModel.getValueAt(selectedRow, 2).toString();
        String lastName = scoTableModel.getValueAt(selectedRow, 3).toString();

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
                    "Failed to delete SCO account.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedStudent() {
        int selectedRow = studentTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a student account to delete.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String email = studentTableModel.getValueAt(selectedRow, 4).toString();

        if (email.equalsIgnoreCase(currentUserEmail)) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.",
                    "Action Not Allowed",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) studentTableModel.getValueAt(selectedRow, 0);
        String firstName = studentTableModel.getValueAt(selectedRow, 2).toString();
        String lastName = studentTableModel.getValueAt(selectedRow, 3).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete student account for " + firstName + " " + lastName + " and all related records?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String getStudentIdSql = """
                SELECT student_id
                FROM student
                WHERE user_id = ?
                """;

        String deleteCertErrorsSql = """
                DELETE FROM cert_error
                WHERE cert_id IN (
                    SELECT cert_id
                    FROM cert_request
                    WHERE student_id = ?
                )
                """;

        String deleteCoursesSql = """
                DELETE FROM course
                WHERE cert_id IN (
                    SELECT cert_id
                    FROM cert_request
                    WHERE student_id = ?
                )
                """;

        String deleteCertRequestsSql = """
                DELETE FROM cert_request
                WHERE student_id = ?
                """;

        String deleteStudentSql = """
                DELETE FROM student
                WHERE student_id = ?
                """;

        String deleteUserSql = """
                DELETE FROM user
                WHERE user_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                Integer studentId = null;

                try (PreparedStatement pstmt = conn.prepareStatement(getStudentIdSql)) {
                    pstmt.setInt(1, userId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            studentId = rs.getInt("student_id");
                        }
                    }
                }

                if (studentId == null) {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this,
                            "Could not find the student record for this user.",
                            "Delete Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deleteCertErrorsSql)) {
                    pstmt.setInt(1, studentId);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deleteCoursesSql)) {
                    pstmt.setInt(1, studentId);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deleteCertRequestsSql)) {
                    pstmt.setInt(1, studentId);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deleteStudentSql)) {
                    pstmt.setInt(1, studentId);
                    pstmt.executeUpdate();
                }

                try (PreparedStatement pstmt = conn.prepareStatement(deleteUserSql)) {
                    pstmt.setInt(1, userId);
                    pstmt.executeUpdate();
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Student account and related records deleted successfully.",
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
                    "Failed to delete student account.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
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