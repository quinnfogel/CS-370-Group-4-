import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SCOCertificationQueue extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable requestsTable;
    private JTable coursesTable;

    private DefaultTableModel requestsTableModel;
    private DefaultTableModel coursesTableModel;

    private JLabel requestIdValue;
    private JLabel studentNameValue;
    private JLabel termValue;
    private JLabel benefitTypeValue;
    private JLabel statusValue;
    private JLabel totalClassesValue;
    private JLabel totalUnitsValue;
    private JLabel trainingTimeValue;
    private JLabel allowanceValue;

    private JComboBox<String> statusComboBox;
    private JTextArea noteArea;

    private final List<Integer> requestCertIds = new ArrayList<>();

    public SCOCertificationQueue() {
        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Certification Requests Queue");
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

        stacked.add(createRequestsTablePanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createSelectedRequestPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createCoursesPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createActionsPanel());

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

        loadSubmittedRequests();
    }

    public void refreshData() {
        loadSubmittedRequests();
    }

    private JPanel createRequestsTablePanel() {
        JPanel panel = createCardPanel("Submitted Requests");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Date Submitted"};
        requestsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(requestsTableModel);
        requestsTable.setRowHeight(28);
        requestsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        requestsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        requestsTable.getTableHeader().setBackground(new Color(226, 235, 229));
        requestsTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        requestsTable.setSelectionBackground(new Color(214, 232, 220));
        requestsTable.setGridColor(SCODashboard.BORDER);
        requestsTable.setFillsViewportHeight(true);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        requestsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = requestsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        updateSelectedRequestDetails(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 200));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        return panel;
    }

    private JPanel createSelectedRequestPanel() {
        JPanel panel = createCardPanel("Selected Request Summary");
        panel.setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 20, 12));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        requestIdValue = createValueLabel("");
        studentNameValue = createValueLabel("");
        termValue = createValueLabel("");
        benefitTypeValue = createValueLabel("");
        statusValue = createValueLabel("");
        totalClassesValue = createValueLabel("");
        totalUnitsValue = createValueLabel("");
        trainingTimeValue = createValueLabel("");
        allowanceValue = createValueLabel("");

        infoPanel.add(createLabeledValue("Request ID:", requestIdValue));
        infoPanel.add(createLabeledValue("Student Name:", studentNameValue));
        infoPanel.add(createLabeledValue("Term:", termValue));
        infoPanel.add(createLabeledValue("Benefit Type:", benefitTypeValue));
        infoPanel.add(createLabeledValue("Status:", statusValue));
        infoPanel.add(createLabeledValue("Total Classes:", totalClassesValue));
        infoPanel.add(createLabeledValue("Total Units:", totalUnitsValue));
        infoPanel.add(createLabeledValue("Training Time:", trainingTimeValue));

        JPanel lowerInfo = new JPanel(new BorderLayout());
        lowerInfo.setOpaque(false);
        lowerInfo.setBorder(new EmptyBorder(15, 0, 0, 0));
        lowerInfo.add(createLabeledValue("Estimated Allowance:", allowanceValue), BorderLayout.WEST);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.add(lowerInfo, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        return panel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = createCardPanel("Submitted Courses");
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
        coursesTable.getTableHeader().setBackground(new Color(226, 235, 229));
        coursesTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        coursesTable.setSelectionBackground(new Color(214, 232, 220));
        coursesTable.setGridColor(SCODashboard.BORDER);
        coursesTable.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(900, 180));

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = createCardPanel("SCO Actions");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        statusComboBox = new JComboBox<>(new String[]{
                "Submitted", "Action Needed", "Certified", "Cancelled"
        });

        noteArea = new JTextArea(4, 30);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));

        formPanel.add(createLabeledField("Update Status:", statusComboBox));
        formPanel.add(createLabeledField("SCO Notes / Error Description:", noteScroll));

        JButton updateButton = createActionButton("Update Request");
        JButton sendErrorButton = createErrorButton("Send to Certification Errors");

        updateButton.addActionListener(e -> updateRequestStatus());
        sendErrorButton.addActionListener(e -> sendToErrors());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(updateButton);
        buttonPanel.add(sendErrorButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private void loadSubmittedRequests() {
        requestsTableModel.setRowCount(0);
        requestCertIds.clear();

        String sql = """
                SELECT
                    cr.cert_id,
                    u.first_name || ' ' || u.last_name AS student_name,
                    cr.academic_term_code,
                    s.benefit_type,
                    cr.status,
                    COALESCE(cr.submission_date, cr.last_updated_date) AS submitted_date
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                JOIN user u ON s.user_id = u.user_id
                WHERE cr.is_draft = 0
                AND (
                    UPPER(REPLACE(cr.status, ' ', '_')) IN (
                            'SUBMITTED',
                            'PENDING',
                            'IN_REVIEW',
                            'APPROVED',
                            'DRAFT'
                            )
                            OR COALESCE(cr.cancel_requested, 0) = 1
                            )
                ORDER BY COALESCE(cr.submission_date, cr.last_updated_date) DESC, cr.cert_id DESC
                """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int certId = rs.getInt("cert_id");
                BenefitType benefitType = parseBenefitType(rs.getString("benefit_type"));
                RequestStatus status = parseRequestStatus(rs.getString("status"));

                requestCertIds.add(certId);
                requestsTableModel.addRow(new Object[]{
                        formatRequestId(certId),
                        rs.getString("student_name"),
                        formatTerm(rs.getInt("academic_term_code")),
                        benefitType != null ? benefitType.getDisplayName() : "N/A",
                        formatStatus(status),
                        formatDateTime(rs.getString("submitted_date"))
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load submitted requests: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (requestsTableModel.getRowCount() > 0) {
            requestsTable.setRowSelectionInterval(0, 0);
            updateSelectedRequestDetails(0);
        } else {
            clearSelectedRequestDetails();
        }
    }

    private void updateSelectedRequestDetails(int row) {
        if (row < 0 || row >= requestCertIds.size()) {
            clearSelectedRequestDetails();
            return;
        }

        int certId = requestCertIds.get(row);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            LoadedRequest loaded = loadRequestObject(conn, certId);

            if (loaded == null) {
                clearSelectedRequestDetails();
                return;
            }

            CertRequest certRequest = loaded.certRequest;

            requestIdValue.setText(formatRequestId(certRequest.getCertId()));
            studentNameValue.setText(loaded.studentName);
            termValue.setText(formatTerm(certRequest.getAcademicTermCode()));
            benefitTypeValue.setText(certRequest.getBenefitType().getDisplayName());
            statusValue.setText(formatStatus(certRequest.getStatus()));
            totalClassesValue.setText(String.valueOf(certRequest.getCourses().size()));
            totalUnitsValue.setText(formatUnits(certRequest.getTotalUnits()));
            trainingTimeValue.setText(formatTrainingTime(certRequest.getUnitLoadCategory()));
            allowanceValue.setText(certRequest.getFormattedEstimatedMonthlyAllowance());

            syncStatusComboBox(certRequest.getStatus());
            noteArea.setText(certRequest.getScoNote() != null ? certRequest.getScoNote() : "");

            loadCourses(certRequest.getCourses());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load request details: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            clearSelectedRequestDetails();
        }
    }

    private LoadedRequest loadRequestObject(Connection conn, int certId) throws Exception {
        String requestSql = """
                SELECT
                    cr.cert_id,
                    cr.academic_term_code,
                    cr.status,
                    cr.sco_note,
                    COALESCE(cr.cancel_requested, 0) AS cancel_requested,
                    s.benefit_type,
                    u.first_name || ' ' || u.last_name AS student_name
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                JOIN user u ON s.user_id = u.user_id
                WHERE cr.cert_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(requestSql)) {
            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
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
                boolean cancelRequested = rs.getInt("cancel_requested") == 1;

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

                if (cancelRequested && certRequest.getStatus() != RequestStatus.CANCELLED) {
                    certRequest.setScoNote("Student requested cancellation. Awaiting SCO approval.");
                }

                return new LoadedRequest(certRequest, rs.getString("student_name"));
            }
        }
    }

    private List<Course> loadCourseObjects(Connection conn, int certId) throws Exception {
        String courseSql = """
                SELECT section_number, course_prefix, course_number, title, crn, units, course_length_weeks
                FROM course
                WHERE cert_id = ?
                ORDER BY course_prefix, course_number, section_number
                """;

        List<Course> courses = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(courseSql)) {
            pstmt.setInt(1, certId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String crnValue = rs.getString("crn");

                    courses.add(new Course(
                            rs.getString("section_number"),
                            rs.getString("course_prefix"),
                            rs.getInt("course_number"),
                            rs.getString("title"),
                            crnValue,
                            rs.getDouble("units"),
                            rs.getInt("course_length_weeks")
                    ));
                }
            }
        }

        return courses;
    }

    private void loadCourses(List<Course> courses) {
        coursesTableModel.setRowCount(0);

        for (Course course : courses) {
            coursesTableModel.addRow(new Object[]{
                    course.getSectionNumber(),
                    course.getCoursePrefix(),
                    String.valueOf(course.getCourseNumber()),
                    course.getTitle(),
                    String.valueOf(course.getCrn()),
                    formatUnits(course.getUnits()),
                    String.valueOf(course.getCourseLengthWeeks())
            });
        }
    }

    private void updateRequestStatus() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Session.isLoggedIn() || Session.getRole() != UserRole.SCO) {
            JOptionPane.showMessageDialog(this,
                    "No active SCO session found.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int empId = Session.getUserId();

        int certId = requestCertIds.get(selectedRow);
        String note = noteArea.getText().trim();
        String selectedStatusText = statusComboBox.getSelectedItem().toString();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                LoadedRequest loaded = loadRequestObject(conn, certId);
                if (loaded == null) {
                    throw new IllegalStateException("Selected request could not be loaded.");
                }

                CertRequest certRequest = loaded.certRequest;

                RequestStatus selectedStatus = parseRequestStatus(selectedStatusText);
                if (selectedStatus == null) {
                    throw new IllegalArgumentException("Invalid status selected.");
                }

                if (selectedStatus == RequestStatus.SUBMITTED) {
                    certRequest.submit();
                    certRequest.setScoNote(note);
                } else if (selectedStatus == RequestStatus.ACTION_NEEDED) {
                    certRequest.markActionNeeded(note.isBlank() ? "Needs follow-up from SCO." : note);                } else if (selectedStatus == RequestStatus.CERTIFIED) {
                    certRequest.resolveAllErrors();
                    certRequest.markCertified();
                } else if (selectedStatus == RequestStatus.CANCELLED) {
                    certRequest.cancel();
                    certRequest.setScoNote(note.isBlank() ? "Certification cancelled by SCO." : note);
                }

                updateCertRequestRecord(conn, certRequest, empId);

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Request status updated successfully.",
                        "Update Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                loadSubmittedRequests();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to update request: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendToErrors() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Session.isLoggedIn() || Session.getRole() != UserRole.SCO) {
            JOptionPane.showMessageDialog(this,
                    "No active SCO session found.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int empId = Session.getUserId();

        int certId = requestCertIds.get(selectedRow);
        String note = noteArea.getText().trim();

        if (note.isEmpty()) {
            note = "Needs follow-up from SCO.";
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                LoadedRequest loaded = loadRequestObject(conn, certId);
                if (loaded == null) {
                    throw new IllegalStateException("Selected request could not be loaded.");
                }

                CertRequest certRequest = loaded.certRequest;

                CertError certError = new CertError(generateTempErrorId(), certId, note);
                certRequest.addError(certError);
                certRequest.markActionNeeded(note);

                updateCertRequestRecord(conn, certRequest, empId);
                insertCertError(conn, certError);

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Request marked as Action Needed and sent to Certification Errors.",
                        "Request Flagged",
                        JOptionPane.INFORMATION_MESSAGE);

                loadSubmittedRequests();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to send request to Certification Errors: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCertRequestRecord(Connection conn, CertRequest certRequest, int empId) throws Exception {
        String sql = """
                UPDATE cert_request
                                 SET status = ?,
                                     sco_note = ?,
                                     reviewed_by_emp_id = ?,
                                     cancel_requested = 0,
                                     last_updated_date = CURRENT_TIMESTAMP
                                 WHERE cert_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, toDatabaseStatus(certRequest.getStatus()));
            pstmt.setString(2, certRequest.getScoNote() == null || certRequest.getScoNote().isBlank() ? null : certRequest.getScoNote());
            pstmt.setInt(3, empId);
            pstmt.setInt(4, certRequest.getCertId());
            pstmt.executeUpdate();
        }
    }

    private void insertCertError(Connection conn, CertError certError) throws Exception {
        String sql = """
            INSERT INTO cert_error (cert_id, error_type, error_message, is_resolved)
            VALUES (?, ?, ?, 0)
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, certError.getCertId());
            pstmt.setString(2, "Missing Information");
            pstmt.setString(3, certError.getErrorMessage());
            pstmt.executeUpdate();
        }
    }

    private int generateTempErrorId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void syncStatusComboBox(RequestStatus status) {
        if (status == null) {
            statusComboBox.setSelectedIndex(0);
            return;
        }

        String label = formatStatus(status);
        ComboBoxModel<String> model = statusComboBox.getModel();

        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equalsIgnoreCase(label)) {
                statusComboBox.setSelectedIndex(i);
                return;
            }
        }

        statusComboBox.setSelectedIndex(0);
    }

    private void clearSelectedRequestDetails() {
        requestIdValue.setText("");
        studentNameValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
        noteArea.setText("");
        coursesTableModel.setRowCount(0);
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

    private String formatRequestId(int certId) {
        return String.format("REQ-%06d", certId);
    }

    private String formatTerm(int academicTermCode) {
        String value = String.valueOf(academicTermCode);
        if (value.length() != 6) {
            return value;
        }

        String year = value.substring(0, 4);
        String month = value.substring(4);

        return switch (month) {
            case "01" -> "Spring " + year;
            case "05" -> "Summer " + year;
            case "08" -> "Fall " + year;
            default -> year + " Term " + month;
        };
    }

    private String formatTrainingTime(String unitLoadCategory) {
        if (unitLoadCategory == null || unitLoadCategory.isBlank()) {
            return "N/A";
        }

        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4-Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> unitLoadCategory;
        };
    }

    private String formatDateTime(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        if (value.length() >= 10) {
            return value.substring(0, 10);
        }

        return value;
    }

    private String formatUnits(double units) {
        if (units == Math.floor(units)) {
            return String.valueOf((int) units);
        }
        return String.format("%.1f", units);
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

    private JPanel createLabeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        if (field instanceof JComboBox<?>) {
            field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        }

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(SCODashboard.DARK_TEXT);
        return label;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(SCODashboard.ACCENT_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(63, 107, 74), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        return button;
    }

    private JButton createErrorButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(178, 34, 34));
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBorder(new CompoundBorder(
                new LineBorder(new Color(120, 20, 20), 1, true),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        return button;
    }

    private static class LoadedRequest {
        private final CertRequest certRequest;
        private final String studentName;

        private LoadedRequest(CertRequest certRequest, String studentName) {
            this.certRequest = certRequest;
            this.studentName = studentName;
        }
    }
    private String toDatabaseStatus(RequestStatus status) {
        if (status == null) {
            return "Draft";
        }

        String statusText = status.toString().trim();

        if (statusText.equalsIgnoreCase("Cancellation Pending")) {
            return "Cancelled";
        }

        return switch (status) {
            case SUBMITTED -> "Submitted";
            case ACTION_NEEDED -> "Action Needed";
            case CERTIFIED -> "Certified";
            case CANCELLED -> "Cancelled";
            default -> "Draft";
        };
    }
}