import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ModifyCertificationPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable coursesTable;
    private DefaultTableModel tableModel;

    private JTextField sectionNumberField;
    private JTextField prefixField;
    private JTextField courseNumberField;
    private JTextField titleField;
    private JTextField crnField;
    private JTextField unitsField;
    private JTextField lengthField;

    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;
    private JLabel statusValue;

    private JLabel requestIdValueLabel;
    private JLabel currentTermValueLabel;
    private JLabel benefitTypeValueLabel;

    private JTextArea scoErrorMessageArea;

    private final HomePagePanel homePagePanel;

    private int currentCertId = 0;
    private int currentStudentId = 0;
    private boolean cancelRequested = false;

    private boolean hasUnsavedChanges = false;
    private boolean loadingRequest = false;

    private DefaultListModel<CertificationListItem> requestListModel;
    private JList<CertificationListItem> requestList;
    private boolean suppressRequestSelectionEvent = false;
    private int lastSelectedRequestIndex = -1;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private static final String LIST_CARD = "LIST";
    private static final String EDITOR_CARD = "EDITOR";

    public ModifyCertificationPanel(HomePagePanel homePagePanel) {
        this.homePagePanel = homePagePanel;

        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Modify Certification");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);
        add(topWrapper, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(createRequestListScreen(), LIST_CARD);
        cardPanel.add(createEditorScreen(), EDITOR_CARD);

        add(cardPanel, BorderLayout.CENTER);

        installUnsavedChangeTracking();
        refreshData();
    }

    public void refreshData() {
        loadCertificationRequests();
        showListScreen();
    }

    public boolean confirmNavigateAway() {
        if (!hasUnsavedChanges) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes.\n\nIf you leave this page, your changes will be discarded.\n\nDo you want to leave?",
                "Unsaved Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            hasUnsavedChanges = false;
            return true;
        }

        return false;
    }

    private void showListScreen() {
        cardLayout.show(cardPanel, LIST_CARD);
    }

    private void showEditorScreen() {
        cardLayout.show(cardPanel, EDITOR_CARD);
    }

    private JPanel createRequestListScreen() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel panel = createCardPanel("Your Certification Requests");
        panel.setLayout(new BorderLayout());
        panel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));

        JLabel helperLabel = new JLabel("Select a certification request, then click Modify.");
        helperLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        helperLabel.setForeground(Color.GRAY);

        requestListModel = new DefaultListModel<>();
        requestList = new JList<>(requestListModel);
        requestList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        requestList.setFixedCellHeight(58);
        requestList.setBackground(Color.WHITE);
        requestList.setBorder(new EmptyBorder(6, 6, 6, 6));

        requestList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof CertificationListItem item) {
                    label.setText("<html><b>" + item.requestLabel + "</b><br/>"
                            + item.termLabel + " - " + item.statusLabel + "</html>");
                    label.setBorder(new EmptyBorder(10, 12, 10, 12));
                }

                return label;
            }
        });

        requestList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() || suppressRequestSelectionEvent) {
                return;
            }
            lastSelectedRequestIndex = requestList.getSelectedIndex();
        });

        JScrollPane scrollPane = new JScrollPane(requestList);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(1000, 330));

        JButton modifyButton = createActionButton("Modify");
        modifyButton.setPreferredSize(new Dimension(130, 42));
        modifyButton.addActionListener(e -> openSelectedRequestForModification());

        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        contentPanel.add(helperLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttonPanel.add(modifyButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private void openSelectedRequestForModification() {
        CertificationListItem selected = requestList.getSelectedValue();

        if (selected == null) {
            return;
        }

        lastSelectedRequestIndex = requestList.getSelectedIndex();
        loadCertificationById(selected.certId);
        showEditorScreen();
    }

    private JPanel createEditorScreen() {
        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        JButton backButton = createNeutralButton("Back to Request List");
        backButton.addActionListener(e -> goBackToRequestList());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(backButton);

        JPanel upperSection = new JPanel();
        upperSection.setOpaque(false);
        upperSection.setLayout(new BoxLayout(upperSection, BoxLayout.Y_AXIS));

        upperSection.add(createCurrentCertificationPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createModifyCoursesPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createErrorMessagePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createSummaryPanel());

        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.add(upperSection, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(upperWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        centerContent.add(topBar, BorderLayout.NORTH);
        centerContent.add(scrollPane, BorderLayout.CENTER);

        return centerContent;
    }

    private void goBackToRequestList() {
        if (hasUnsavedChanges) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "You have unsaved changes.\n\nGoing back will discard those changes.\n\nDo you want to continue?",
                    "Unsaved Changes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        hasUnsavedChanges = false;
        clearEntryFields();
        loadCertificationRequests();
        showListScreen();
    }

    private JPanel createCurrentCertificationPanel() {
        JPanel panel = createCardPanel("Selected Certification Details");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValueLabel = createValueLabel("N/A");
        currentTermValueLabel = createValueLabel("N/A");
        benefitTypeValueLabel = createValueLabel("N/A");
        statusValue = createStatusLabel("N/A");

        infoPanel.add(createLabeledValue("Request ID:", requestIdValueLabel));
        infoPanel.add(createLabeledValue("Current Term:", currentTermValueLabel));
        infoPanel.add(createLabeledValue("Benefit Type:", benefitTypeValueLabel));
        infoPanel.add(createLabeledValue("Status:", statusValue));

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Currently Certified Courses");
        panel.setLayout(new BorderLayout());

        String[] columns = {
                "Section Number",
                "Course Prefix",
                "Course Number",
                "Title / Course Name",
                "CRN",
                "Units",
                "Course Length (Weeks)"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(tableModel);
        coursesTable.setRowHeight(28);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        coursesTable.getTableHeader().setBackground(new Color(230, 236, 242));
        coursesTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(220, 240, 245));
        coursesTable.setGridColor(StudentDashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);
        coursesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(320);
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        coursesTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        coursesTable.getColumnModel().getColumn(6).setPreferredWidth(170);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(980, 180));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createModifyCoursesPanel() {
        JPanel panel = createCardPanel("Modify Courses");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        sectionNumberField = new JTextField();
        prefixField = new JTextField();
        courseNumberField = new JTextField();
        titleField = new JTextField();
        crnField = new JTextField();
        unitsField = new JTextField();
        lengthField = new JTextField();

        formPanel.add(createLabeledField("Section Number:", sectionNumberField));
        formPanel.add(createLabeledField("Course Prefix:", prefixField));
        formPanel.add(createLabeledField("Course Number:", courseNumberField));
        formPanel.add(createLabeledField("Title / Course Name:", titleField));
        formPanel.add(createLabeledField("CRN (5 digits):", crnField));
        formPanel.add(createLabeledField("Units:", unitsField));
        formPanel.add(createLabeledField("Course Length (Weeks):", lengthField));

        JButton addClassButton = createActionButton("Add Class");
        JButton dropClassButton = createActionButton("Drop Selected Class");

        addClassButton.addActionListener(e -> addClassToTable());
        dropClassButton.addActionListener(e -> dropSelectedClass());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addClassButton);
        buttonPanel.add(dropClassButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createErrorMessagePanel() {
        JPanel panel = createCardPanel("SCO Error / Action Needed Message");
        panel.setLayout(new BorderLayout());

        scoErrorMessageArea = new JTextArea(4, 40);
        scoErrorMessageArea.setEditable(false);
        scoErrorMessageArea.setLineWrap(true);
        scoErrorMessageArea.setWrapStyleWord(true);
        scoErrorMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoErrorMessageArea.setForeground(new Color(140, 25, 25));
        scoErrorMessageArea.setBackground(new Color(255, 244, 244));
        scoErrorMessageArea.setText("No current SCO error message.");

        JScrollPane scrollPane = new JScrollPane(scoErrorMessageArea);
        scrollPane.setBorder(new LineBorder(new Color(225, 170, 170), 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 110));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel("Updated Certification Summary");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        totalClassesValue = createValueLabel("0");
        totalUnitsValue = createValueLabel("0");
        trainingTimeValue = createValueLabel("N/A");
        allowanceValue = createValueLabel("$0.00 / month");

        infoPanel.add(createLabeledValue("Total Classes:", totalClassesValue));
        infoPanel.add(createLabeledValue("Total Units:", totalUnitsValue));
        infoPanel.add(createLabeledValue("Training Time:", trainingTimeValue));
        infoPanel.add(createLabeledValue("Estimated Allowance:", allowanceValue));

        JButton submitButton = createSubmitButton("Submit Modified Certification");
        JButton discardButton = createNeutralButton("Discard Unsaved Changes");
        JButton cancelCertificationButton = createDeleteButton("Cancel Certification");

        submitButton.addActionListener(e -> submitModifiedCertification());
        discardButton.addActionListener(e -> discardChanges());
        cancelCertificationButton.addActionListener(e -> cancelCertification());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);
        buttonPanel.add(discardButton);
        buttonPanel.add(cancelCertificationButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
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

    private JPanel createLabeledValue(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        panel.add(label, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        return label;
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(new Color(180, 120, 20));
        return label;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(StudentDashboard.TEAL);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(20, 110, 130), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSubmitButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(34, 139, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(20, 100, 20), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createNeutralButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(108, 117, 125));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(80, 85, 90), 1, true),
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

    private void installUnsavedChangeTracking() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                markUnsavedIfUserEditing();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                markUnsavedIfUserEditing();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                markUnsavedIfUserEditing();
            }
        };

        sectionNumberField.getDocument().addDocumentListener(listener);
        prefixField.getDocument().addDocumentListener(listener);
        courseNumberField.getDocument().addDocumentListener(listener);
        titleField.getDocument().addDocumentListener(listener);
        crnField.getDocument().addDocumentListener(listener);
        unitsField.getDocument().addDocumentListener(listener);
        lengthField.getDocument().addDocumentListener(listener);
    }

    private void markUnsavedIfUserEditing() {
        if (!loadingRequest) {
            hasUnsavedChanges = true;
        }
    }

    private void loadCertificationRequests() {
        resetDisplayedRequest();

        int userId = Session.getUserId();
        if (userId == 0) {
            return;
        }

        String studentQuery = """
                SELECT student_id, benefit_type
                FROM student
                WHERE user_id = ?
                """;

        String requestQuery = """
                SELECT cert_id,
                       academic_term_code,
                       status
                FROM cert_request
                WHERE student_id = ?
                  AND status IN ('SUBMITTED', 'ACTION_NEEDED', 'Submitted', 'Action Needed')
                ORDER BY last_updated_date DESC, cert_id DESC
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement pstmt = conn.prepareStatement(studentQuery)) {
                pstmt.setInt(1, userId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "No student record found for the current user.",
                                "No Student Record",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    currentStudentId = rs.getInt("student_id");

                    BenefitType benefitType = parseBenefitType(rs.getString("benefit_type"));
                    if (benefitTypeValueLabel != null) {
                        benefitTypeValueLabel.setText(benefitType != null ? benefitType.getDisplayName() : "N/A");
                    }
                }
            }

            requestListModel.clear();
            List<CertificationListItem> items = new ArrayList<>();

            try (PreparedStatement pstmt = conn.prepareStatement(requestQuery)) {
                pstmt.setInt(1, currentStudentId);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int certId = rs.getInt("cert_id");
                        int termCode = rs.getInt("academic_term_code");
                        RequestStatus status = parseRequestStatus(rs.getString("status"));

                        items.add(new CertificationListItem(
                                certId,
                                "REQ-" + certId,
                                formatAcademicTerm(termCode),
                                formatStatus(status)
                        ));
                    }
                }
            }

            for (CertificationListItem item : items) {
                requestListModel.addElement(item);
            }

            suppressRequestSelectionEvent = true;
            requestList.clearSelection();
            suppressRequestSelectionEvent = false;
            lastSelectedRequestIndex = -1;

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load certification request list.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCertificationById(int certId) {
        currentCertId = 0;
        cancelRequested = false;

        loadingRequest = true;
        try {
            requestIdValueLabel.setText("N/A");
            currentTermValueLabel.setText("N/A");
            statusValue.setText("N/A");
            statusValue.setForeground(new Color(180, 120, 20));
            scoErrorMessageArea.setText("No current SCO error message.");
            tableModel.setRowCount(0);
            clearEntryFields();

            if (currentStudentId == 0 || certId == 0) {
                updateSummary();
                hasUnsavedChanges = false;
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                CertRequest certRequest = loadCertRequest(conn, certId, currentStudentId);

                if (certRequest == null) {
                    JOptionPane.showMessageDialog(this,
                            "The selected certification request could not be loaded.",
                            "Request Not Found",
                            JOptionPane.WARNING_MESSAGE);
                    updateSummary();
                    return;
                }

                currentCertId = certRequest.getCertId();
                requestIdValueLabel.setText("REQ-" + certRequest.getCertId());
                currentTermValueLabel.setText(formatAcademicTerm(certRequest.getAcademicTermCode()));
                benefitTypeValueLabel.setText(certRequest.getBenefitType().getDisplayName());
                statusValue.setText(formatStatus(certRequest.getStatus()));
                applyStatusColor(certRequest.getStatus());

                String note = certRequest.getScoNote();
                if (certRequest.isCancelRequested()) {
                    cancelRequested = true;
                    scoErrorMessageArea.setText("Cancellation request submitted. Waiting for SCO approval.");
                } else if (note != null && !note.isBlank()) {
                    scoErrorMessageArea.setText(note);
                } else {
                    scoErrorMessageArea.setText("No current SCO error message.");
                }

                for (Course course : certRequest.getCourses()) {
                    tableModel.addRow(new Object[]{
                            course.getSectionNumber(),
                            course.getCoursePrefix(),
                            String.valueOf(course.getCourseNumber()),
                            course.getTitle(),
                            course.getCrn(),
                            stripTrailingZero(course.getUnits()),
                            String.valueOf(course.getCourseLengthWeeks())
                    });
                }
            }

            updateSummary();
            hasUnsavedChanges = false;

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load certification request data.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            updateSummary();
        } finally {
            loadingRequest = false;
        }
    }

    private CertRequest loadCertRequest(Connection conn, int certId, int studentId) throws Exception {
        String sql = """
                SELECT
                    cr.cert_id,
                    cr.academic_term_code,
                    cr.status,
                    COALESCE(cr.sco_note, '') AS sco_note,
                    COALESCE(cr.cancel_requested, 0) AS cancel_requested,
                    s.benefit_type
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                WHERE cr.cert_id = ?
                  AND cr.student_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);
            ps.setInt(2, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                BenefitType benefitType = parseBenefitType(rs.getString("benefit_type"));
                if (benefitType == null) {
                    benefitType = BenefitType.CH33;
                }

                CertRequest certRequest = new CertRequest(
                        rs.getInt("cert_id"),
                        rs.getInt("academic_term_code"),
                        benefitType
                );

                List<Course> courses = loadCourseObjects(conn, certId);
                for (Course course : courses) {
                    certRequest.addCourse(course);
                }

                RequestStatus status = parseRequestStatus(rs.getString("status"));
                String scoNote = rs.getString("sco_note");
                boolean cancelRequestedFlag = rs.getInt("cancel_requested") == 1;

                if (status == RequestStatus.SUBMITTED) {
                    certRequest.submit();
                } else if (status == RequestStatus.ACTION_NEEDED) {
                    certRequest.submit();
                    certRequest.markActionNeeded(scoNote);
                } else if (status == RequestStatus.CERTIFIED) {
                    certRequest.submit();
                    certRequest.markCertified();
                } else if (status == RequestStatus.CANCELLED) {
                    certRequest.cancel();
                }

                if (scoNote != null && !scoNote.isBlank() && status != RequestStatus.ACTION_NEEDED) {
                    certRequest.setScoNote(scoNote);
                }

                if (cancelRequestedFlag && certRequest.getStatus() != RequestStatus.CANCELLED) {
                    certRequest.setScoNote("Student requested cancellation. Awaiting SCO approval.");
                }

                return certRequest;
            }
        }
    }

    private List<Course> loadCourseObjects(Connection conn, int certId) throws Exception {
        String sql = """
                SELECT section_number, course_prefix, course_number, title, crn, units, course_length_weeks
                FROM course
                WHERE cert_id = ?
                ORDER BY course_prefix, course_number, section_number
                """;

        List<Course> courses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(new Course(
                            rs.getString("section_number"),
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("title"),
                            rs.getString("crn"),
                            rs.getDouble("units"),
                            rs.getInt("course_length_weeks")
                    ));
                }
            }
        }

        return courses;
    }

    private void resetDisplayedRequest() {
        currentCertId = 0;
        cancelRequested = false;
        hasUnsavedChanges = false;
        lastSelectedRequestIndex = -1;

        if (requestIdValueLabel != null) requestIdValueLabel.setText("N/A");
        if (currentTermValueLabel != null) currentTermValueLabel.setText("N/A");
        if (benefitTypeValueLabel != null) benefitTypeValueLabel.setText("N/A");
        if (statusValue != null) {
            statusValue.setText("N/A");
            statusValue.setForeground(new Color(180, 120, 20));
        }
        if (scoErrorMessageArea != null) {
            scoErrorMessageArea.setText("No current SCO error message.");
        }
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }

        clearEntryFields();
        updateSummary();
    }

    private void applyStatusColor(RequestStatus status) {
        if (status == null) {
            statusValue.setForeground(new Color(180, 120, 20));
            return;
        }

        switch (status) {
            case SUBMITTED -> statusValue.setForeground(new Color(204, 153, 0));
            case ACTION_NEEDED -> statusValue.setForeground(new Color(178, 34, 34));
            case CERTIFIED -> statusValue.setForeground(new Color(34, 139, 34));
            case CANCELLED -> statusValue.setForeground(new Color(120, 120, 120));
            default -> statusValue.setForeground(new Color(180, 120, 20));
        }
    }

    private void addClassToTable() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "This certification is pending cancellation and cannot be modified.",
                    "Cancellation Pending",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Course newCourse = buildCourseFromFields();

            if (isDuplicateCourse(newCourse)) {
                JOptionPane.showMessageDialog(this,
                        "That course/section already exists in this certification request.",
                        "Duplicate Course",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            tableModel.addRow(new Object[]{
                    newCourse.getSectionNumber(),
                    newCourse.getCoursePrefix(),
                    String.valueOf(newCourse.getCourseNumber()),
                    newCourse.getTitle(),
                    newCourse.getCrn(),
                    stripTrailingZero(newCourse.getUnits()),
                    String.valueOf(newCourse.getCourseLengthWeeks())
            });

            clearEntryFields();
            hasUnsavedChanges = true;
            updateSummary();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private Course buildCourseFromFields() {
        String sectionNumber = sectionNumberField.getText().trim().toUpperCase();
        String prefix = prefixField.getText().trim().toUpperCase();
        String courseNumberText = courseNumberField.getText().trim();
        String title = titleField.getText().trim();
        String crnText = crnField.getText().trim();
        String unitsText = unitsField.getText().trim();
        String lengthText = lengthField.getText().trim();

        if (sectionNumber.isEmpty() || prefix.isEmpty() || courseNumberText.isEmpty()
                || title.isEmpty() || crnText.isEmpty() || unitsText.isEmpty() || lengthText.isEmpty()) {
            throw new IllegalArgumentException("Please complete all course fields before adding a class.");
        }

        if (!crnText.matches("\\d{5}")) {
            throw new IllegalArgumentException("CRN must be exactly 5 digits.");
        }

        int courseNumber;
        double units;
        int courseLengthWeeks;

        try {
            courseNumber = Integer.parseInt(courseNumberText);
            units = Double.parseDouble(unitsText);
            courseLengthWeeks = Integer.parseInt(lengthText);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Course Number, Units, and Course Length must be numeric values.");
        }

        if (courseNumber <= 0 || units <= 0 || courseLengthWeeks <= 0) {
            throw new IllegalArgumentException("Course Number, Units, and Course Length must be greater than 0.");
        }

        return new Course(
                sectionNumber,
                prefix,
                courseNumber,
                title,
                crnText,
                units,
                courseLengthWeeks
        );
    }

    private boolean isDuplicateCourse(Course newCourse) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String existingSection = tableModel.getValueAt(i, 0).toString().trim();
            String existingPrefix = tableModel.getValueAt(i, 1).toString().trim();
            String existingCourseNumber = tableModel.getValueAt(i, 2).toString().trim();
            String existingCrn = tableModel.getValueAt(i, 4).toString().trim();

            if (existingSection.equalsIgnoreCase(newCourse.getSectionNumber())
                    && existingPrefix.equalsIgnoreCase(newCourse.getCoursePrefix())
                    && existingCourseNumber.equals(String.valueOf(newCourse.getCourseNumber()))
                    && existingCrn.equals(newCourse.getCrn())) {
                return true;
            }
        }
        return false;
    }

    private void dropSelectedClass() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "This certification is pending cancellation and cannot be modified.",
                    "Cancellation Pending",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = coursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class to drop.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.removeRow(selectedRow);
        hasUnsavedChanges = true;
        updateSummary();
    }

    private void updateSummary() {
        CertRequest previewRequest = buildPreviewRequest();

        if (totalClassesValue != null) totalClassesValue.setText(String.valueOf(previewRequest.getCourses().size()));
        if (totalUnitsValue != null) totalUnitsValue.setText(stripTrailingZero(previewRequest.getTotalUnits()));
        if (trainingTimeValue != null) trainingTimeValue.setText(formatTrainingTime(previewRequest.getUnitLoadCategory()));
        if (allowanceValue != null) allowanceValue.setText(previewRequest.getFormattedEstimatedMonthlyAllowance());
    }

    private CertRequest buildPreviewRequest() {
        BenefitType benefitType = parseDisplayedBenefitType(benefitTypeValueLabel != null ? benefitTypeValueLabel.getText() : null);
        if (benefitType == null) {
            benefitType = BenefitType.CH33;
        }

        int academicTermCode = parseDisplayedAcademicTerm(currentTermValueLabel != null ? currentTermValueLabel.getText() : null);
        if (academicTermCode <= 0) {
            academicTermCode = 202601;
        }

        CertRequest previewRequest = new CertRequest(
                currentCertId > 0 ? currentCertId : 1,
                academicTermCode,
                benefitType
        );

        for (Course course : getCoursesFromTable()) {
            previewRequest.addCourse(course);
        }

        return previewRequest;
    }

    private List<Course> getCoursesFromTable() {
        List<Course> courses = new ArrayList<>();

        if (tableModel == null) {
            return courses;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            courses.add(new Course(
                    tableModel.getValueAt(i, 0).toString(),
                    tableModel.getValueAt(i, 1).toString(),
                    Integer.parseInt(tableModel.getValueAt(i, 2).toString()),
                    tableModel.getValueAt(i, 3).toString(),
                    tableModel.getValueAt(i, 4).toString(),
                    Double.parseDouble(tableModel.getValueAt(i, 5).toString()),
                    Integer.parseInt(tableModel.getValueAt(i, 6).toString())
            ));
        }

        return courses;
    }

    private String formatTrainingTime(String unitLoadCategory) {
        if (unitLoadCategory == null) {
            return "N/A";
        }

        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4-Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> "N/A";
        };
    }

    private void submitModifiedCertification() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to modify.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "This certification is pending cancellation and cannot be modified.",
                    "Cancellation Pending",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Course> updatedCourses = getCoursesFromTable();

        if (updatedCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "A certification request cannot be submitted with zero classes.",
                    "No Classes Remaining",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                BenefitType benefitType = parseDisplayedBenefitType(benefitTypeValueLabel.getText());
                if (benefitType == null) {
                    benefitType = BenefitType.CH33;
                }

                int termCode = parseDisplayedAcademicTerm(currentTermValueLabel.getText());
                CertRequest certRequest = new CertRequest(currentCertId, termCode, benefitType);

                for (Course course : updatedCourses) {
                    certRequest.addCourse(course);
                }

                certRequest.submit();

                deleteCoursesForRequest(conn, currentCertId);
                saveCourses(conn, currentCertId, updatedCourses);
                updateCertRequest(conn, certRequest, currentCertId);
                resolveErrorsForRequest(conn, currentCertId);
                //saveMonthlyAllowance(conn, currentCertId, certRequest);

                conn.commit();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            cancelRequested = false;
            statusValue.setText("Submitted");
            applyStatusColor(RequestStatus.SUBMITTED);
            scoErrorMessageArea.setText("No current SCO error message.");
            hasUnsavedChanges = false;
            updateSummary();

            JOptionPane.showMessageDialog(this,
                    "Modified certification request submitted successfully.",
                    "Submission Complete",
                    JOptionPane.INFORMATION_MESSAGE);

            if (homePagePanel != null) {
                homePagePanel.refreshSummary();
            }

            refreshData();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to save certification changes.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCoursesForRequest(Connection conn, int certId) throws Exception {
        String sql = "DELETE FROM course WHERE cert_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, certId);
            pstmt.executeUpdate();
        }
    }

    private void saveCourses(Connection conn, int certId, List<Course> courses) throws Exception {
        String sql = """
                INSERT INTO course (
                    cert_id,
                    section_number,
                    course_prefix,
                    course_number,
                    title,
                    crn,
                    units,
                    course_length_weeks
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Course course : courses) {
                pstmt.setInt(1, certId);
                pstmt.setString(2, course.getSectionNumber());
                pstmt.setString(3, course.getCoursePrefix());
                pstmt.setInt(4, course.getCourseNumber());
                pstmt.setString(5, course.getTitle());
                pstmt.setString(6, course.getCrn());
                pstmt.setDouble(7, course.getUnits());
                pstmt.setInt(8, course.getCourseLengthWeeks());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    private void updateCertRequest(Connection conn, CertRequest certRequest, int certId) throws Exception {
        String sql = """
                UPDATE cert_request
                SET status = ?,
                    submission_date = ?,
                    last_updated_date = ?,
                    total_units = ?,
                    unit_load_category = ?,
                    is_draft = 0,
                    cancel_requested = 0,
                    sco_note = ''
                WHERE cert_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String now = LocalDateTime.now().format(formatter);
            pstmt.setString(1, toDatabaseStatus(certRequest.getStatus()));
            pstmt.setString(2, now);
            pstmt.setString(3, now);
            pstmt.setDouble(4, certRequest.getTotalUnits());
            pstmt.setString(5, certRequest.getUnitLoadCategory());
            pstmt.setInt(6, certId);
            pstmt.executeUpdate();
        }
    }

    private void resolveErrorsForRequest(Connection conn, int certId) throws Exception {
        String sql = """
                UPDATE cert_error
                SET is_resolved = 1
                WHERE cert_id = ?
                  AND is_resolved = 0
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, certId);
            pstmt.executeUpdate();
        }
    }

    private void cancelCertification() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to cancel.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cancelRequested) {
            JOptionPane.showMessageDialog(this,
                    "A cancellation request has already been submitted for this certification.",
                    "Already Pending",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Cancel this entire certification request?\n\nThis will send the request to the SCO for cancellation approval.",
                "Cancel Certification",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
                UPDATE cert_request
                SET cancel_requested = 1,
                    sco_note = 'Student requested cancellation. Awaiting SCO approval.',
                    last_updated_date = CURRENT_TIMESTAMP
                WHERE cert_id = ?
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentCertId);
            ps.executeUpdate();

            cancelRequested = true;
            hasUnsavedChanges = false;
            scoErrorMessageArea.setText("Cancellation request submitted. Waiting for SCO approval.");

            JOptionPane.showMessageDialog(this,
                    "Certification cancellation request submitted to the SCO.",
                    "Cancellation Requested",
                    JOptionPane.INFORMATION_MESSAGE);

            if (homePagePanel != null) {
                homePagePanel.refreshSummary();
            }

            refreshData();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to submit cancellation request.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void discardChanges() {
        if (currentCertId == 0) {
            JOptionPane.showMessageDialog(this,
                    "There is no certification request available to reload.",
                    "No Request Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!hasUnsavedChanges) {
            JOptionPane.showMessageDialog(this,
                    "There are no unsaved changes to discard.",
                    "No Changes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Discard all unsaved changes and reload the saved certification request?",
                "Discard Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            loadCertificationById(currentCertId);
            JOptionPane.showMessageDialog(this,
                    "Unsaved changes were discarded.",
                    "Changes Discarded",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void clearEntryFields() {
        if (sectionNumberField != null) sectionNumberField.setText("");
        if (prefixField != null) prefixField.setText("");
        if (courseNumberField != null) courseNumberField.setText("");
        if (titleField != null) titleField.setText("");
        if (crnField != null) crnField.setText("");
        if (unitsField != null) unitsField.setText("");
        if (lengthField != null) lengthField.setText("");
    }

    private String formatAcademicTerm(int academicTermCode) {
        String code = String.valueOf(academicTermCode);

        if (code.length() < 6) {
            return code;
        }

        String year = code.substring(0, 4);
        String termPart = code.substring(4);

        return switch (termPart) {
            case "01" -> "Spring " + year;
            case "05" -> "Summer " + year;
            case "08" -> "Fall " + year;
            default -> code;
        };
    }

    private int parseDisplayedAcademicTerm(String termText) {
        if (termText == null || termText.isBlank() || "N/A".equalsIgnoreCase(termText)) {
            return 0;
        }

        return switch (termText.trim()) {
            case "Spring 2026" -> 202601;
            case "Summer 2026" -> 202605;
            case "Fall 2025" -> 202508;
            default -> 0;
        };
    }

    private String stripTrailingZero(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void saveMonthlyAllowance(Connection conn, int certId, CertRequest certRequest) throws Exception {
        String sql = """
                INSERT INTO monthly_allowance_calculator (
                    cert_id,
                    estimated_monthly_allowance
                ) VALUES (?, ?)
                ON CONFLICT(cert_id) DO UPDATE SET
                    estimated_monthly_allowance = excluded.estimated_monthly_allowance
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, certId);
            pstmt.setDouble(2, certRequest.getEstimatedMonthlyAllowance());
            pstmt.executeUpdate();
        }
    }

    private BenefitType parseBenefitType(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }

        try {
            return BenefitType.valueOf(dbValue.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private BenefitType parseDisplayedBenefitType(String displayValue) {
        if (displayValue == null || displayValue.isBlank() || "N/A".equalsIgnoreCase(displayValue)) {
            return null;
        }

        for (BenefitType type : BenefitType.values()) {
            if (type.name().equalsIgnoreCase(displayValue.trim())
                    || type.getDisplayName().equalsIgnoreCase(displayValue.trim())) {
                return type;
            }
        }

        return null;
    }

    private RequestStatus parseRequestStatus(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }

        String normalized = dbValue.trim().toUpperCase().replace(" ", "_");

        try {
            return RequestStatus.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String formatStatus(RequestStatus status) {
        if (status == null) {
            return "N/A";
        }

        return switch (status) {
            case SUBMITTED -> "Submitted";
            case ACTION_NEEDED -> "Action Needed";
            case CERTIFIED -> "Certified";
            case CANCELLED -> "Cancelled";
            default -> "N/A";
        };
    }

    private static class CertificationListItem {
        private final int certId;
        private final String requestLabel;
        private final String termLabel;
        private final String statusLabel;

        public CertificationListItem(int certId, String requestLabel, String termLabel, String statusLabel) {
            this.certId = certId;
            this.requestLabel = requestLabel;
            this.termLabel = termLabel;
            this.statusLabel = statusLabel;
        }

        @Override
        public String toString() {
            return requestLabel + " - " + termLabel + " - " + statusLabel;
        }
    }
    private String toDatabaseStatus(RequestStatus status) {
        if (status == null) {
            return "Draft";
        }

        return switch (status) {
            case SUBMITTED -> "Submitted";
            case ACTION_NEEDED -> "Action Needed";
            case CERTIFIED -> "Certified";
            case CANCELLED -> "Cancelled";
            default -> "N/A";
        };
    }
}