import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViewRequestStatusPanel extends JPanel {
    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private final Color STATUS_YELLOW = new Color(230, 180, 40);
    private final Color STATUS_BLUE = new Color(70, 130, 180);
    private final Color STATUS_RED = new Color(220, 70, 70);
    private final Color STATUS_GREEN = new Color(70, 170, 90);
    private final Color STATUS_GRAY = new Color(140, 140, 140);
    private final Color STATUS_PURPLE = new Color(128, 0, 128);

    private JLabel requestIdValue;
    private JLabel termValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel benefitTypeValue;
    private JLabel statusValueLabel;
    private JLabel estimatedAllowanceValue;
    private JLabel trainingTimeValue;
    private JTextArea scoMessageArea;

    private DefaultTableModel coursesTableModel;

    private DefaultListModel<RequestListItem> requestListModel;
    private JList<RequestListItem> requestList;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private int currentStudentId = 0;

    private static final String LIST_CARD = "LIST";
    private static final String DETAIL_CARD = "DETAIL";

    public ViewRequestStatusPanel() {
        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("View Request Status");
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
        cardPanel.add(createDetailScreen(), DETAIL_CARD);

        add(cardPanel, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        loadRequestList();
        showListScreen();
    }

    private void showListScreen() {
        cardLayout.show(cardPanel, LIST_CARD);
    }

    private void showDetailScreen() {
        cardLayout.show(cardPanel, DETAIL_CARD);
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

        JLabel helperLabel = new JLabel("Select a certification request, then click View Status.");
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

                if (value instanceof RequestListItem item) {
                    label.setText("<html><b>" + item.requestLabel + "</b><br/>"
                            + item.termLabel + " - " + item.status + "</html>");
                    label.setBorder(new EmptyBorder(10, 12, 10, 12));
                }

                return label;
            }
        });

        requestList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openSelectedRequest();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(requestList);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(1000, 330));

        JButton viewButton = createActionButton("View Status");
        viewButton.setPreferredSize(new Dimension(140, 42));
        viewButton.addActionListener(e -> openSelectedRequest());

        JPanel contentPanel = new JPanel(new BorderLayout(0, 16));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 0, 0, 0));
        contentPanel.add(helperLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(12, 0, 0, 0));
        buttonPanel.add(viewButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        wrapper.add(panel, BorderLayout.NORTH);

        return wrapper;
    }

    private JComponent createDetailScreen() {
        JPanel centerContent = new JPanel();
        centerContent.setOpaque(false);
        centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));

        JButton backButton = createNeutralButton("Back to Request List");
        backButton.addActionListener(e -> showListScreen());

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(backButton);
        topBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel overviewPanel = createCertificationOverview();
        JPanel messagePanel = createScoMessagePanel();
        JPanel coursesPanel = createCoursesTablePanel();

        overviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        messagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        coursesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerContent.add(topBar);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(overviewPanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(messagePanel);
        centerContent.add(Box.createRigidArea(new Dimension(0, 20)));
        centerContent.add(coursesPanel);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(centerContent, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return scrollPane;
    }

    private JPanel createCertificationOverview() {
        JPanel overviewPanel = new JPanel(new BorderLayout());
        overviewPanel.setBackground(StudentDashboard.CARD_BG);
        overviewPanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        overviewPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JLabel sectionTitle = new JLabel("Certification Request Overview");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel infoPanel = new JPanel(new GridLayout(8, 2, 15, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValue = createValueLabel("—");
        termValue = createValueLabel("—");
        totalClassesValue = createValueLabel("0");
        totalUnitsValue = createValueLabel("0");
        benefitTypeValue = createValueLabel("N/A");
        statusValueLabel = createStatusLabel("Unknown", STATUS_GRAY);
        estimatedAllowanceValue = createValueLabel("$0.00 / month");
        trainingTimeValue = createValueLabel("—");

        infoPanel.add(createInfoLabel("Request ID:"));
        infoPanel.add(requestIdValue);
        infoPanel.add(createInfoLabel("Term:"));
        infoPanel.add(termValue);
        infoPanel.add(createInfoLabel("Total Classes:"));
        infoPanel.add(totalClassesValue);
        infoPanel.add(createInfoLabel("Total Units:"));
        infoPanel.add(totalUnitsValue);
        infoPanel.add(createInfoLabel("Benefit Type:"));
        infoPanel.add(benefitTypeValue);
        infoPanel.add(createInfoLabel("Status:"));
        infoPanel.add(statusValueLabel);
        infoPanel.add(createInfoLabel("Estimated Allowance:"));
        infoPanel.add(estimatedAllowanceValue);
        infoPanel.add(createInfoLabel("Training Time:"));
        infoPanel.add(trainingTimeValue);

        overviewPanel.add(sectionTitle, BorderLayout.NORTH);
        overviewPanel.add(infoPanel, BorderLayout.CENTER);
        return overviewPanel;
    }

    private JPanel createScoMessagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(StudentDashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JLabel title = new JLabel("SCO Error / Action Needed Message");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(StudentDashboard.DARK_TEXT);

        scoMessageArea = new JTextArea(5, 40);
        scoMessageArea.setEditable(false);
        scoMessageArea.setLineWrap(true);
        scoMessageArea.setWrapStyleWord(true);
        scoMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoMessageArea.setForeground(new Color(140, 25, 25));
        scoMessageArea.setBackground(new Color(255, 244, 244));
        scoMessageArea.setText("No current SCO error message.");

        JScrollPane messageScroll = new JScrollPane(scoMessageArea);
        messageScroll.setBorder(new LineBorder(new Color(225, 170, 170), 1, true));
        messageScroll.setPreferredSize(new Dimension(900, 100));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(messageScroll, BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(StudentDashboard.CARD_BG);
        tablePanel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JLabel sectionTitle = new JLabel("Certified Courses");
        sectionTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sectionTitle.setForeground(StudentDashboard.DARK_TEXT);

        String[] columns = {
                "Section Number",
                "Course Prefix",
                "Course Number",
                "Title / Course Name",
                "CRN",
                "Units",
                "Course Length (Weeks)"
        };

        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable coursesTable = new JTable(coursesTableModel);
        coursesTable.setRowHeight(28);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        coursesTable.getTableHeader().setBackground(new Color(230, 236, 242));
        coursesTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(220, 240, 245));
        coursesTable.setGridColor(StudentDashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);

        // Match ModifyCertificationPanel formatting
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

        tablePanel.add(sectionTitle, BorderLayout.NORTH);
        tablePanel.add(content, BorderLayout.CENTER);
        return tablePanel;
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

    private void openSelectedRequest() {
        RequestListItem selected = requestList.getSelectedValue();

        if (selected == null) {
            return;
        }

        loadRequestById(selected.certId);
        showDetailScreen();
    }

    private void loadRequestList() {
        resetDisplayedRequest();
        requestListModel.clear();
        currentStudentId = 0;

        int userId = Session.getUserId();
        if (userId == 0) {
            return;
        }

        String studentQuery = """
                SELECT student_id
                FROM student
                WHERE user_id = ?
                """;

        String requestQuery = """
                SELECT cert_id,
                       academic_term_code,
                       status,
                       last_updated_date
                FROM cert_request
                WHERE student_id = ?
                ORDER BY last_updated_date DESC, cert_id DESC
                """;

        try (Connection conn = getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(studentQuery)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this,
                                "No student record found for the current user.",
                                "No Student Record",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    currentStudentId = rs.getInt("student_id");
                }
            }

            List<RequestListItem> items = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(requestQuery)) {
                ps.setInt(1, currentStudentId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int certId = rs.getInt("cert_id");
                        int termCode = rs.getInt("academic_term_code");
                        String status = rs.getString("status");

                        items.add(new RequestListItem(
                                certId,
                                "REQ-" + certId,
                                formatAcademicTerm(termCode),
                                status != null ? status : "Unknown"
                        ));
                    }
                }
            }

            if (items.isEmpty()) {
                return;
            }

            for (RequestListItem item : items) {
                requestListModel.addElement(item);
            }

            requestList.clearSelection();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load certification request list.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRequestById(int certId) {
        if (currentStudentId == 0 || certId == 0) {
            showNoDataState("No certification request found.");
            return;
        }

        String sql = """
                SELECT
                    cr.cert_id,
                    cr.academic_term_code,
                    cr.status,
                    cr.total_units,
                    cr.unit_load_category,
                    s.benefit_type,
                    mac.estimated_monthly_allowance,
                    COALESCE(cr.sco_note, '') AS sco_note,
                    COALESCE(cr.cancel_requested, 0) AS cancel_requested
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                LEFT JOIN monthly_allowance_calculator mac ON cr.cert_id = mac.cert_id
                WHERE cr.cert_id = ?
                  AND cr.student_id = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, certId);
            ps.setInt(2, currentStudentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int academicTermCode = rs.getInt("academic_term_code");
                    String status = rs.getString("status");
                    double totalUnits = rs.getDouble("total_units");
                    String unitLoadCategory = rs.getString("unit_load_category");
                    String benefitType = rs.getString("benefit_type");
                    double estimatedAllowance = rs.getDouble("estimated_monthly_allowance");
                    String scoNote = rs.getString("sco_note");
                    boolean cancelRequested = rs.getInt("cancel_requested") == 1;

                    requestIdValue.setText("REQ-" + certId);
                    termValue.setText(formatAcademicTerm(academicTermCode));
                    totalUnitsValue.setText(formatNumber(totalUnits));
                    benefitTypeValue.setText(benefitType != null ? benefitType : "N/A");
                    estimatedAllowanceValue.setText(String.format("$%,.2f / month", estimatedAllowance));
                    trainingTimeValue.setText(formatTrainingTime(unitLoadCategory));

                    updateStatusPanel(status != null ? status : "Unknown");
                    updateScoMessage(status, scoNote, cancelRequested);
                    loadCoursesForRequest(conn, certId);
                } else {
                    showNoDataState("No certification request found.");
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load request status.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            showNoDataState("Unable to load data.");
        }
    }

    private void updateScoMessage(String status, String scoNote, boolean cancelRequested) {
        if ("Cancellation Pending".equals(status) || cancelRequested) {
            scoMessageArea.setText("Cancellation request submitted. Waiting for SCO approval.");
            return;
        }

        if (scoNote != null && !scoNote.isBlank()) {
            scoMessageArea.setText(scoNote);
            return;
        }

        if ("Action Needed".equals(status) || "Error".equals(status)) {
            scoMessageArea.setText("Your certification needs correction. Please review the request and make the required changes.");
            return;
        }

        scoMessageArea.setText("No current SCO error message.");
    }

    private void loadCoursesForRequest(Connection conn, int certId) throws SQLException {
        String sql = """
                SELECT
                    section_number,
                    course_prefix,
                    course_number,
                    title,
                    crn,
                    units,
                    course_length_weeks
                FROM course
                WHERE cert_id = ?
                ORDER BY course_prefix, course_number, section_number
                """;

        coursesTableModel.setRowCount(0);
        int courseCount = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    coursesTableModel.addRow(new Object[]{
                            rs.getString("section_number"),
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("title"),
                            rs.getString("crn"),
                            formatNumber(rs.getDouble("units")),
                            rs.getInt("course_length_weeks")
                    });
                    courseCount++;
                }
            }
        }

        totalClassesValue.setText(String.valueOf(courseCount));
    }

    private void resetDisplayedRequest() {
        requestIdValue.setText("—");
        termValue.setText("—");
        totalClassesValue.setText("0");
        totalUnitsValue.setText("0");
        benefitTypeValue.setText("N/A");
        estimatedAllowanceValue.setText("$0.00 / month");
        trainingTimeValue.setText("—");
        scoMessageArea.setText("No current SCO error message.");
        updateStatusPanel("No Request");
        coursesTableModel.setRowCount(0);
    }

    private void showNoDataState(String message) {
        resetDisplayedRequest();
        System.out.println(message);
    }

    private void updateStatusPanel(String statusText) {
        Color color = switch (statusText) {
            case "Submitted" -> STATUS_YELLOW;
            case "In Review" -> STATUS_BLUE;
            case "Action Needed", "Error" -> STATUS_RED;
            case "Approved", "Certified" -> STATUS_GREEN;
            case "Cancellation Pending" -> STATUS_PURPLE;
            case "Cancelled" -> STATUS_GRAY;
            default -> STATUS_GRAY;
        };

        statusValueLabel.setText(statusText);
        statusValueLabel.setBackground(color);
        statusValueLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusValueLabel.setBorder(new CompoundBorder(
                new LineBorder(color.darker(), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        Dimension size = statusValueLabel.getPreferredSize();
        statusValueLabel.setPreferredSize(new Dimension(Math.max(size.width, 90), Math.max(size.height, 26)));
    }

    private JLabel createStatusLabel(String text, Color bgColor) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(bgColor);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBorder(new CompoundBorder(
                new LineBorder(bgColor.darker(), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        Dimension size = label.getPreferredSize();
        label.setPreferredSize(new Dimension(Math.max(size.width, 90), Math.max(size.height, 26)));
        return label;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(StudentDashboard.DARK_TEXT);
        return label;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
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

    private String formatTrainingTime(String unitLoadCategory) {
        if (unitLoadCategory == null) {
            return "—";
        }

        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4-Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> unitLoadCategory;
        };
    }

    private String formatNumber(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private static class RequestListItem {
        private final int certId;
        private final String requestLabel;
        private final String termLabel;
        private final String status;

        private RequestListItem(int certId, String requestLabel, String termLabel, String status) {
            this.certId = certId;
            this.requestLabel = requestLabel;
            this.termLabel = termLabel;
            this.status = status;
        }

        @Override
        public String toString() {
            return requestLabel + " - " + termLabel + " - " + status;
        }
    }
}