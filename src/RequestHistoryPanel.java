import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


public class RequestHistoryPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable historyTable;
    private JTable coursesTable;

    private DefaultTableModel historyTableModel;
    private DefaultTableModel coursesTableModel;

    private JLabel requestIdValue;
    private JLabel termValue;
    private JLabel benefitTypeValue;
    private JLabel statusValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;

    private JTextArea scoMessageArea;

    private final List<Integer> historyCertIds = new ArrayList<>();

    public RequestHistoryPanel() {
        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Request History");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        pageTitle.setForeground(StudentDashboard.DARK_TEXT);

        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(pageTitle, BorderLayout.WEST);

        add(topWrapper, BorderLayout.NORTH);

        JPanel centerContent = new JPanel(new BorderLayout(0, 20));
        centerContent.setOpaque(false);

        JPanel upperSection = new JPanel();
        upperSection.setOpaque(false);
        upperSection.setLayout(new BoxLayout(upperSection, BoxLayout.Y_AXIS));

        upperSection.add(createHistoryTablePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createSummaryPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createScoMessagePanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());

        JPanel upperWrapper = new JPanel(new BorderLayout());
        upperWrapper.setOpaque(false);
        upperWrapper.add(upperSection, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(upperWrapper);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(StudentDashboard.LIGHT_BG);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHistoryTablePanel() {
        JPanel panel = createCardPanel("Submitted Certification Requests");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Term", "Benefit Type", "Status", "Date Submitted"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setRowHeight(28);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        historyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        historyTable.getTableHeader().setBackground(new Color(230, 236, 242));
        historyTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        historyTable.setSelectionBackground(new Color(220, 240, 245));
        historyTable.setGridColor(StudentDashboard.BORDER);
        historyTable.setFillsViewportHeight(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        historyTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = historyTable.getSelectedRow();
                if (selectedRow != -1 && selectedRow < historyCertIds.size()) {
                    int certId = historyCertIds.get(selectedRow);
                    loadSelectedRequestDetails(certId);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = createCardPanel("Selected Request Summary");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValue = createValueLabel("");
        termValue = createValueLabel("");
        benefitTypeValue = createValueLabel("");
        statusValue = createValueLabel("");
        totalClassesValue = createValueLabel("");
        totalUnitsValue = createValueLabel("");
        trainingTimeValue = createValueLabel("");
        allowanceValue = createValueLabel("");

        infoPanel.add(createLabeledValue("Request ID:", requestIdValue));
        infoPanel.add(createLabeledValue("Term:", termValue));
        infoPanel.add(createLabeledValue("Benefit Type:", benefitTypeValue));
        infoPanel.add(createLabeledValue("Status:", statusValue));
        infoPanel.add(createLabeledValue("Total Classes:", totalClassesValue));
        infoPanel.add(createLabeledValue("Total Units:", totalUnitsValue));
        infoPanel.add(createLabeledValue("Training Time:", trainingTimeValue));
        infoPanel.add(createLabeledValue("Estimated Allowance:", allowanceValue));

        panel.add(infoPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createScoMessagePanel() {
        JPanel panel = createCardPanel("SCO Error / Action Needed Message");
        panel.setLayout(new BorderLayout());

        scoMessageArea = new JTextArea(5, 40);
        scoMessageArea.setEditable(false);
        scoMessageArea.setLineWrap(true);
        scoMessageArea.setWrapStyleWord(true);
        scoMessageArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoMessageArea.setForeground(new Color(140, 25, 25));
        scoMessageArea.setBackground(new Color(255, 244, 244));
        scoMessageArea.setText("No SCO note or error message for this request.");

        JScrollPane scrollPane = new JScrollPane(scoMessageArea);
        scrollPane.setBorder(new LineBorder(new Color(225, 170, 170), 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 100));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel panel = createCardPanel("Certified Courses");
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

        coursesTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTable = new JTable(coursesTableModel);
        coursesTable.setRowHeight(28);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        coursesTable.getTableHeader().setBackground(new Color(230, 236, 242));
        coursesTable.getTableHeader().setForeground(StudentDashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(220, 240, 245));
        coursesTable.setGridColor(StudentDashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);
        coursesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(420);
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        coursesTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        coursesTable.getColumnModel().getColumn(6).setPreferredWidth(180);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(StudentDashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(1100, 180));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    public void refreshData() {
        loadRequestHistory();
    }

    private void loadRequestHistory() {
        historyTableModel.setRowCount(0);
        historyCertIds.clear();
        clearSummary();
        coursesTableModel.setRowCount(0);
        scoMessageArea.setText("No SCO note or error message for this request.");

        String sql =
                "SELECT cr.cert_id, cr.academic_term_code, s.benefit_type, cr.status, " +
                        "       COALESCE(cr.submission_date, cr.last_updated_date) AS submitted_on " +
                        "FROM cert_request cr " +
                        "JOIN student s ON cr.student_id = s.student_id " +
                        "WHERE s.user_id = ? " +
                        "ORDER BY datetime(COALESCE(cr.submission_date, cr.last_updated_date)) DESC, cr.cert_id DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Session.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int certId = rs.getInt("cert_id");
                    int termCode = rs.getInt("academic_term_code");
                    BenefitType benefitType = parseBenefitType(rs.getString("benefit_type"));
                    System.out.println("DB STATUS VALUE: [" + rs.getString("status") + "]");
                    RequestStatus status = parseRequestStatus(rs.getString("status"));
                    String submittedOn = rs.getString("submitted_on");

                    historyCertIds.add(certId);
                    historyTableModel.addRow(new Object[]{
                            formatRequestId(certId),
                            formatAcademicTerm(termCode),
                            formatBenefitType(benefitType),
                            formatStatus(status),
                            formatDateTime(submittedOn)
                    });
                }
            }

            if (historyTableModel.getRowCount() > 0) {
                historyTable.setRowSelectionInterval(0, 0);
                loadSelectedRequestDetails(historyCertIds.get(0));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load request history.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadSelectedRequestDetails(int certId) {
        try (Connection conn = getConnection()) {
            CertRequest certRequest = loadCertRequest(conn, certId);

            if (certRequest == null) {
                clearSummary();
                coursesTableModel.setRowCount(0);
                scoMessageArea.setText("No SCO note or error message for this request.");
                return;
            }

            displayRequestSummary(certRequest);
            displayCourses(certRequest.getCourses());
            displayScoMessage(certRequest);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load request details.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private CertRequest loadCertRequest(Connection conn, int certId) throws SQLException {
        String sql =
                "SELECT cr.cert_id, cr.academic_term_code, cr.status, cr.submission_date, cr.last_updated_date, " +
                        "       cr.total_units, cr.unit_load_category, " +
                        "       COALESCE(cr.sco_note, '') AS sco_note, " +
                        "       COALESCE(cr.cancel_requested, 0) AS cancel_requested, " +
                        "       s.benefit_type " +
                        "FROM cert_request cr " +
                        "JOIN student s ON cr.student_id = s.student_id " +
                        "WHERE cr.cert_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);

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

                List<Course> courses = loadCoursesForRequest(conn, certId);
                for (Course course : courses) {
                    certRequest.addCourse(course);
                }

                RequestStatus status = parseRequestStatus(rs.getString("status"));
                if (status == RequestStatus.CANCELLED) {
                    String approvedCancelMessage = "SCO has approved this certification cancellation.";
                    certRequest.cancel();
                    certRequest.setScoNote(approvedCancelMessage);
                    return certRequest;
                }
                String scoNote = rs.getString("sco_note");
                boolean cancelRequested = rs.getInt("cancel_requested") == 1;

                if (status == RequestStatus.SUBMITTED) {
                    certRequest.submit();
                } else if (status == RequestStatus.ACTION_NEEDED) {
                    certRequest.submit();
                    certRequest.markActionNeeded(scoNote);
                } else if (status == RequestStatus.CERTIFIED) {
                    certRequest.submit();
                    certRequest.resolveAllErrors();
                    certRequest.markCertified();
                } else if (status == RequestStatus.CANCELLED) {
                    certRequest.cancel();
                }

                if (scoNote != null && !scoNote.isBlank() && status != RequestStatus.ACTION_NEEDED) {
                    certRequest.setScoNote(scoNote);
                }

                if (cancelRequested && certRequest.getStatus() != RequestStatus.CANCELLED) {
                    certRequest.setScoNote("Student requested cancellation. Waiting for SCO approval.");
                }

                return certRequest;
            }
        }
    }

    private List<Course> loadCoursesForRequest(Connection conn, int certId) throws SQLException {
        String sql =
                "SELECT section_number, course_prefix, course_number, title, crn, units, course_length_weeks " +
                        "FROM course " +
                        "WHERE cert_id = ? " +
                        "ORDER BY course_prefix, course_number, section_number";

        List<Course> courses = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String crnValue = rs.getString("crn");

                    Course course = new Course(
                            rs.getString("section_number"),
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("title"),
                            crnValue,
                            rs.getDouble("units"),
                            rs.getInt("course_length_weeks")
                    );

                    courses.add(course);
                }
            }
        }

        return courses;
    }

    private void displayRequestSummary(CertRequest certRequest) {
        requestIdValue.setText(formatRequestId(certRequest.getCertId()));
        termValue.setText(formatAcademicTerm(certRequest.getAcademicTermCode()));
        benefitTypeValue.setText(formatBenefitType(certRequest.getBenefitType()));
        statusValue.setText(formatStatus(certRequest.getStatus()));
        totalClassesValue.setText(String.valueOf(certRequest.getCourses().size()));
        totalUnitsValue.setText(formatNumber(certRequest.getTotalUnits()));
        trainingTimeValue.setText(formatTrainingTime(certRequest.getUnitLoadCategory()));
        allowanceValue.setText(certRequest.getFormattedEstimatedMonthlyAllowance());

        applyStatusColor(certRequest.getStatus());
    }

    private void displayCourses(List<Course> courses) {
        coursesTableModel.setRowCount(0);

        for (Course course : courses) {
            coursesTableModel.addRow(new Object[]{
                    course.getSectionNumber(),
                    course.getCoursePrefix(),
                    course.getCourseNumber(),
                    course.getTitle(),
                    String.valueOf(course.getCrn()),
                    formatNumber(course.getUnits()),
                    course.getCourseLengthWeeks()
            });
        }
    }

    private void displayScoMessage(CertRequest certRequest) {
        String note = certRequest.getScoNote();
        RequestStatus status = certRequest.getStatus();

        if (status == RequestStatus.CANCELLED && (note == null || note.isBlank())) {
            scoMessageArea.setText("This certification was cancelled and approved by the SCO.");
            return;
        }

        if (note != null && !note.isBlank()) {
            scoMessageArea.setText(note);
            return;
        }

        if (status == RequestStatus.ACTION_NEEDED) {
            scoMessageArea.setText("This request was marked Action Needed, but no detailed note was saved.");
        } else {
            scoMessageArea.setText("No SCO note or error message for this request.");
        }
    }

    private void clearSummary() {
        requestIdValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        statusValue.setForeground(StudentDashboard.DARK_TEXT);
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
    }

    private void applyStatusColor(RequestStatus status) {
        if (status == null) {
            statusValue.setForeground(StudentDashboard.DARK_TEXT);
            return;
        }

        switch (status) {
            case SUBMITTED -> statusValue.setForeground(new Color(204, 153, 0));
            case ACTION_NEEDED -> statusValue.setForeground(new Color(178, 34, 34));
            case CERTIFIED -> statusValue.setForeground(new Color(34, 139, 34));
            case CANCELLED -> statusValue.setForeground(new Color(178, 34, 34));
            default -> statusValue.setForeground(StudentDashboard.DARK_TEXT);
        }
    }

    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(StudentDashboard.CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(StudentDashboard.BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(StudentDashboard.DARK_TEXT);

        panel.setLayout(new BorderLayout());
        panel.add(titleLabel, BorderLayout.NORTH);

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

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private String formatRequestId(int certId) {
        return "REQ-" + certId;
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

    private RequestStatus parseRequestStatus(String dbValue) {
        if (dbValue == null || dbValue.isBlank()) {
            return null;
        }

        String normalized = dbValue.trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        return switch (normalized) {
            case "SUBMITTED", "PENDING", "IN_REVIEW", "APPROVED", "DRAFT" -> RequestStatus.SUBMITTED;
            case "ACTION_NEEDED", "ERROR", "ERROR_FOUND" -> RequestStatus.ACTION_NEEDED;
            case "CERTIFIED" -> RequestStatus.CERTIFIED;
            case "CANCELLED", "CANCELED" -> RequestStatus.CANCELLED;
            default -> null;
        };
    }

    private String formatBenefitType(BenefitType benefitType) {
        if (benefitType == null) {
            return "";
        }
        return benefitType.getDisplayName();
    }

    private String formatStatus(RequestStatus status) {
        if (status == null) {
            return "Unknown Status";
        }

        return switch (status) {
            case SUBMITTED -> "Submitted";
            case ACTION_NEEDED -> "Action Needed";
            case CERTIFIED -> "Certified";
            case CANCELLED -> "Cancelled";
            default -> "";
        };
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
            return "";
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
    private String formatDateTime(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value
                .replace("T", " ")
                .replaceAll("\\.\\d+$", "");
    }
}