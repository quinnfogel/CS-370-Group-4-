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

public class CertificationErrorsPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private JTable errorsTable;
    private JTable coursesTable;

    private DefaultTableModel errorsTableModel;
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
    private JTextArea errorNoteArea;

    private final List<Integer> requestCertIds = new ArrayList<>();
    private final List<Integer> errorIds = new ArrayList<>();

    public CertificationErrorsPanel() {
        setBackground(SCODashboard.ADMIN_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Certification Errors");
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

        stacked.add(createErrorsTablePanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createSelectedRequestPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createCoursesPanel());
        stacked.add(Box.createRigidArea(new Dimension(0, 20)));
        stacked.add(createResolutionPanel());

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

        loadErrorRequests();
    }

    public void refreshData() {
        loadErrorRequests();
    }

    private JPanel createErrorsTablePanel() {
        JPanel panel = createCardPanel("Error Queue");
        panel.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student Name", "Term", "Benefit Type", "Status", "Issue"};
        errorsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        errorsTable = new JTable(errorsTableModel);
        errorsTable.setRowHeight(28);
        errorsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        errorsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        errorsTable.getTableHeader().setBackground(new Color(226, 235, 229));
        errorsTable.getTableHeader().setForeground(SCODashboard.DARK_TEXT);
        errorsTable.setSelectionBackground(new Color(214, 232, 220));
        errorsTable.setGridColor(SCODashboard.BORDER);
        errorsTable.setFillsViewportHeight(true);
        errorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        errorsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        errorsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = errorsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        updateSelectedErrorDetails(selectedRow);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(errorsTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(1100, 180));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

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
        coursesTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));
        scrollPane.setPreferredSize(new Dimension(1100, 180));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 0, 0, 0));
        content.add(scrollPane, BorderLayout.CENTER);

        panel.add(content, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private JPanel createResolutionPanel() {
        JPanel panel = createCardPanel("Error Resolution");
        panel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        statusComboBox = new JComboBox<>(new String[]{
                "Submitted", "Action Needed", "Certified"
        });

        errorNoteArea = new JTextArea(4, 30);
        errorNoteArea.setLineWrap(true);
        errorNoteArea.setWrapStyleWord(true);
        errorNoteArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane noteScroll = new JScrollPane(errorNoteArea);
        noteScroll.setBorder(new LineBorder(SCODashboard.BORDER, 1, true));

        formPanel.add(createLabeledField("Update Status:", statusComboBox));
        formPanel.add(createLabeledField("Resolution / Error Notes:", noteScroll));

        JButton updateButton = createActionButton("Update Request");
        JButton clearErrorButton = createActionButton("Resolve Error");

        updateButton.addActionListener(e -> updateErrorRequest());
        clearErrorButton.addActionListener(e -> resolveError());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearErrorButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        return panel;
    }

    private void loadErrorRequests() {
        errorsTableModel.setRowCount(0);
        coursesTableModel.setRowCount(0);
        requestCertIds.clear();
        errorIds.clear();

        String sql = """
            SELECT
                ce.error_id,
                cr.cert_id,
                u.first_name || ' ' || u.last_name AS student_name,
                cr.academic_term_code,
                s.benefit_type,
                cr.status,
                ce.error_message
            FROM cert_error ce
            JOIN cert_request cr ON ce.cert_id = cr.cert_id
            JOIN student s ON cr.student_id = s.student_id
            JOIN user u ON s.user_id = u.user_id
            WHERE ce.is_resolved = 0
              AND cr.status IN ('ACTION_NEEDED', 'Action Needed')
            ORDER BY ce.error_id DESC
            """;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int errorId = rs.getInt("error_id");
                int certId = rs.getInt("cert_id");
                BenefitType benefitType = parseBenefitType(rs.getString("benefit_type"));
                RequestStatus status = parseRequestStatus(rs.getString("status"));

                errorIds.add(errorId);
                requestCertIds.add(certId);

                errorsTableModel.addRow(new Object[]{
                        formatRequestId(certId),
                        rs.getString("student_name"),
                        formatTerm(rs.getInt("academic_term_code")),
                        benefitType != null ? benefitType.getDisplayName() : "N/A",
                        formatStatus(status),
                        rs.getString("error_message")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load certification errors: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (errorsTableModel.getRowCount() > 0) {
            errorsTable.setRowSelectionInterval(0, 0);
            updateSelectedErrorDetails(0);
        } else {
            clearSelectedErrorDetails();
        }
    }

    private void updateSelectedErrorDetails(int row) {
        if (row < 0 || row >= requestCertIds.size()) {
            clearSelectedErrorDetails();
            return;
        }

        int certId = requestCertIds.get(row);
        int errorId = errorIds.get(row);

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            LoadedErrorRequest loaded = loadErrorRequestObject(conn, certId, errorId);

            if (loaded == null) {
                clearSelectedErrorDetails();
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
            errorNoteArea.setText(loaded.errorNote != null ? loaded.errorNote : "");

            loadCourses(certRequest.getCourses());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load error request details: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private LoadedErrorRequest loadErrorRequestObject(Connection conn, int certId, int errorId) throws Exception {
        String sql = """
                SELECT
                    cr.cert_id,
                    cr.academic_term_code,
                    cr.status,
                    cr.sco_note,
                    s.benefit_type,
                    u.first_name || ' ' || u.last_name AS student_name,
                    ce.error_message
                FROM cert_request cr
                JOIN student s ON cr.student_id = s.student_id
                JOIN user u ON s.user_id = u.user_id
                JOIN cert_error ce ON ce.cert_id = cr.cert_id
                WHERE cr.cert_id = ?
                  AND ce.error_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, certId);
            pstmt.setInt(2, errorId);

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

                String errorMessage = rs.getString("error_message");
                CertError certError = new CertError(errorId, certId, errorMessage);
                certRequest.addError(certError);

                String scoNote = rs.getString("sco_note");
                if (scoNote != null && !scoNote.isBlank()) {
                    certRequest.setScoNote(scoNote);
                }

                return new LoadedErrorRequest(certRequest, rs.getString("student_name"), errorMessage);
            }
        }
    }

    private List<Course> loadCourseObjects(Connection conn, int certId) throws Exception {
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
                ORDER BY course_prefix, course_number
                """;

        List<Course> courses = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
                    course.getCrn(),
                    formatUnits(course.getUnits()),
                    String.valueOf(course.getCourseLengthWeeks())
            });
        }
    }

    private void updateErrorRequest() {
        int selectedRow = errorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SCO currentSco = Session.getSCO();
        if (currentSco == null) {
            JOptionPane.showMessageDialog(this,
                    "No active SCO session found.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int certId = requestCertIds.get(selectedRow);
        int errorId = errorIds.get(selectedRow);
        String note = errorNoteArea.getText().trim();
        RequestStatus selectedStatus = parseRequestStatus(statusComboBox.getSelectedItem().toString());

        if (selectedStatus == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid status selected.",
                    "Status Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                LoadedErrorRequest loaded = loadErrorRequestObject(conn, certId, errorId);
                if (loaded == null) {
                    throw new IllegalStateException("Selected error request could not be loaded.");
                }

                CertRequest certRequest = loaded.certRequest;

                if (selectedStatus == RequestStatus.SUBMITTED) {
                    certRequest.submit();
                    certRequest.setScoNote(note);
                } else if (selectedStatus == RequestStatus.ACTION_NEEDED) {
                    currentSco.markRequestActionNeeded(certRequest, note.isBlank() ? loaded.errorNote : note);
                } else if (selectedStatus == RequestStatus.CERTIFIED) {
                    certRequest.resolveAllErrors();
                    currentSco.certifyRequest(certRequest);
                }

                updateCertRequestRecord(conn, certRequest, currentSco.getEmpId());

                if (selectedStatus != RequestStatus.ACTION_NEEDED) {
                    markErrorResolved(conn, errorId);
                }

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Certification error request updated successfully.",
                        "Update Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                loadErrorRequests();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to update certification error request: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resolveError() {
        int selectedRow = errorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request first.",
                    "No Request Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SCO currentSco = Session.getSCO();
        if (currentSco == null) {
            JOptionPane.showMessageDialog(this,
                    "No active SCO session found.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int certId = requestCertIds.get(selectedRow);
        int errorId = errorIds.get(selectedRow);
        String note = errorNoteArea.getText().trim();

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                LoadedErrorRequest loaded = loadErrorRequestObject(conn, certId, errorId);
                if (loaded == null) {
                    throw new IllegalStateException("Selected error request could not be loaded.");
                }

                CertRequest certRequest = loaded.certRequest;
                certRequest.resolveAllErrors();
                certRequest.submit();
                certRequest.setScoNote(note);

                updateCertRequestRecord(conn, certRequest, currentSco.getEmpId());
                markErrorResolved(conn, errorId);

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Request moved out of error state and returned to Submitted.",
                        "Error Resolved",
                        JOptionPane.INFORMATION_MESSAGE);

                loadErrorRequests();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to resolve certification error: " + ex.getMessage(),
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
                    last_updated_date = CURRENT_TIMESTAMP
                WHERE cert_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, certRequest.getStatus().name());
            pstmt.setString(2, certRequest.getScoNote() == null || certRequest.getScoNote().isBlank() ? null : certRequest.getScoNote());
            pstmt.setInt(3, empId);
            pstmt.setInt(4, certRequest.getCertId());
            pstmt.executeUpdate();
        }
    }

    private void markErrorResolved(Connection conn, int errorId) throws Exception {
        String sql = """
                UPDATE cert_error
                SET is_resolved = 1
                WHERE error_id = ?
                """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, errorId);
            pstmt.executeUpdate();
        }
    }

    private void syncStatusComboBox(RequestStatus currentStatus) {
        if (currentStatus == null) {
            statusComboBox.setSelectedIndex(0);
            return;
        }

        String label = formatStatus(currentStatus);
        ComboBoxModel<String> model = statusComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (model.getElementAt(i).equalsIgnoreCase(label)) {
                statusComboBox.setSelectedIndex(i);
                return;
            }
        }
        statusComboBox.setSelectedIndex(0);
    }

    private void clearSelectedErrorDetails() {
        requestIdValue.setText("");
        studentNameValue.setText("");
        termValue.setText("");
        benefitTypeValue.setText("");
        statusValue.setText("");
        totalClassesValue.setText("");
        totalUnitsValue.setText("");
        trainingTimeValue.setText("");
        allowanceValue.setText("");
        errorNoteArea.setText("");
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

    private static class LoadedErrorRequest {
        private final CertRequest certRequest;
        private final String studentName;
        private final String errorNote;

        private LoadedErrorRequest(CertRequest certRequest, String studentName, String errorNote) {
            this.certRequest = certRequest;
            this.studentName = studentName;
            this.errorNote = errorNote;
        }
    }
}