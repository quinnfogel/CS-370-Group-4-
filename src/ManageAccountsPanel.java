import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ManageAccountsPanel extends JPanel {

    private JTable scoTable;
    private DefaultTableModel tableModel;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField employeeIdField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public ManageAccountsPanel() {
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

        String[] columns = {"Employee ID", "First Name", "Last Name", "Email", "Role"};
        Object[][] sampleData = {
                {"1001", "Jordan", "Hayes", "jhayes@csusm.edu", "SCO"},
                {"1002", "Monica", "Ramirez", "mramirez@csusm.edu", "SCO"},
                {"1003", "Anthony", "Brooks", "abrooks@csusm.edu", "SCO"}
        };

        tableModel = new DefaultTableModel(sampleData, columns) {
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

    private void createScoAccount() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String employeeId = employeeIdField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()
                || employeeId.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                    "Passwords do not match.",
                    "Password Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (isDuplicateEmployeeId(employeeId)) {
            JOptionPane.showMessageDialog(this,
                    "That Employee ID already exists.",
                    "Duplicate Employee ID",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.addRow(new Object[]{
                employeeId,
                firstName,
                lastName,
                email,
                "SCO"
        });

        JOptionPane.showMessageDialog(this,
                "SCO account created successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        clearForm();
    }

    private boolean isDuplicateEmployeeId(String employeeId) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingId = tableModel.getValueAt(i, 0).toString();
            if (existingId.equalsIgnoreCase(employeeId)) {
                return true;
            }
        }
        return false;
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

        String employeeId = tableModel.getValueAt(selectedRow, 0).toString();
        String firstName = tableModel.getValueAt(selectedRow, 1).toString();
        String lastName = tableModel.getValueAt(selectedRow, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete SCO account for " + firstName + " " + lastName + " (ID: " + employeeId + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            tableModel.removeRow(selectedRow);

            JOptionPane.showMessageDialog(this,
                    "SCO account deleted successfully.",
                    "Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
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
