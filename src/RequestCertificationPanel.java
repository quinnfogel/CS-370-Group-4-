import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class RequestCertificationPanel extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:database.sqlite";

    private final HomePagePanel homePagePanel;

    private JTable coursesTable;
    private DefaultTableModel tableModel;

    private JComboBox<String> termComboBox;
    private JComboBox<String> benefitTypeComboBox;

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

    private final List<Course> pendingCourses = new ArrayList<>();

    public RequestCertificationPanel(HomePagePanel homePagePanel) {
        this.homePagePanel = homePagePanel;

        setBackground(StudentDashboard.LIGHT_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel pageTitle = new JLabel("Request Certification");
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

        upperSection.add(createCertificationDetailsPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCourseEntryPanel());
        upperSection.add(Box.createRigidArea(new Dimension(0, 20)));
        upperSection.add(createCoursesTablePanel());
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

        centerContent.add(scrollPane, BorderLayout.CENTER);
        add(centerContent, BorderLayout.CENTER);
    }

    private JPanel createCertificationDetailsPanel() {
        JPanel detailsPanel = createCardPanel("Certification Details");
        detailsPanel.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 20, 12));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        termComboBox = new JComboBox<>(new String[]{
                "Fall 2025", "Spring 2026", "Summer 2026"
        });

        benefitTypeComboBox = new JComboBox<>(new String[]{
                BenefitType.CH33.name(),
                BenefitType.CH33D.name(),
                BenefitType.CH31.name(),
                BenefitType.CH35.name()
        });

        formPanel.add(createLabeledField("Term:", termComboBox));
        formPanel.add(createLabeledField("Benefit Type:", benefitTypeComboBox));

        detailsPanel.add(formPanel, BorderLayout.CENTER);
        return detailsPanel;
    }

    private JPanel createCourseEntryPanel() {
        JPanel entryPanel = createCardPanel("Course Entry");
        entryPanel.setLayout(new BorderLayout());

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

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);

        JButton addClassButton = createActionButton("Add Class");
        JButton removeClassButton = createActionButton("Remove Selected Class");

        addClassButton.addActionListener(e -> addClassToTable());
        removeClassButton.addActionListener(e -> removeSelectedClass());

        buttonPanel.add(addClassButton);
        buttonPanel.add(removeClassButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(formPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        entryPanel.add(wrapper, BorderLayout.CENTER);
        return entryPanel;
    }

    private JPanel createCoursesTablePanel() {
        JPanel tablePanel = createCardPanel("Courses Added");
        tablePanel.setLayout(new BorderLayout());

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

        tablePanel.add(content, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = createCardPanel("Certification Summary");
        summaryPanel.setLayout(new BorderLayout());

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

        JButton submitButton = createSubmitButton("Submit Certification Request");
        submitButton.addActionListener(e -> submitCertificationRequest());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoPanel, BorderLayout.CENTER);
        wrapper.add(buttonPanel, BorderLayout.SOUTH);

        summaryPanel.add(wrapper, BorderLayout.CENTER);
        return summaryPanel;
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
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 6));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setForeground(Color.GRAY);

        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledValue(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BorderLayout(0, 6));

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

    private void addClassToTable() {
        try {
            Course course = buildCourseFromFields();

            if (isDuplicateCourse(course)) {
                JOptionPane.showMessageDialog(this,
                        "That course already exists in this certification request.",
                        "Duplicate Course",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            pendingCourses.add(course);
            tableModel.addRow(new Object[]{
                    course.getSectionNumber(),
                    course.getCoursePrefix(),
                    course.getCourseNumber(),
                    course.getTitle(),
                    String.valueOf(course.getCrn()),
                    stripTrailingZero(course.getUnits()),
                    course.getCourseLengthWeeks()
            });

            clearCourseEntryFields();
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

        if (sectionNumber.length() > 5) {
            throw new IllegalArgumentException("Section Number should be short, like 01 or 11A.");
        }

        if (prefix.length() > 6) {
            throw new IllegalArgumentException("Course Prefix is too long.");
        }

        if (title.length() > 60) {
            throw new IllegalArgumentException("Course Name is too long.");
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
        for (Course existingCourse : pendingCourses) {
            if (existingCourse.getSectionNumber().equalsIgnoreCase(newCourse.getSectionNumber())
                    && existingCourse.getCoursePrefix().equalsIgnoreCase(newCourse.getCoursePrefix())
                    && existingCourse.getCourseNumber() == newCourse.getCourseNumber()
                    && existingCourse.getCrn().equals(newCourse.getCrn())) {
                return true;
            }
        }
        return false;
    }

    private void removeSelectedClass() {
        int selectedRow = coursesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a class to remove.",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        tableModel.removeRow(selectedRow);
        pendingCourses.remove(selectedRow);
        updateSummary();
    }

    private void updateSummary() {
        CertRequest previewRequest = buildPreviewRequest();

        totalClassesValue.setText(String.valueOf(previewRequest.getCourses().size()));
        totalUnitsValue.setText(stripTrailingZero(previewRequest.getTotalUnits()));
        trainingTimeValue.setText(formatTrainingTime(previewRequest.getUnitLoadCategory()));
        allowanceValue.setText(previewRequest.getFormattedEstimatedMonthlyAllowance());
    }

    private CertRequest buildPreviewRequest() {
        BenefitType benefitType = getSelectedBenefitType();
        int academicTermCode = getAcademicTermCode(termComboBox.getSelectedItem().toString());

        CertRequest previewRequest = new CertRequest(1, academicTermCode, benefitType);

        for (Course course : pendingCourses) {
            previewRequest.addCourse(course);
        }

        return previewRequest;
    }

    private String formatTrainingTime(String unitLoadCategory) {
        return switch (unitLoadCategory) {
            case "FullTime" -> "Full-Time";
            case "ThreeQuarterTime" -> "3/4-Time";
            case "HalfTime" -> "Half-Time";
            case "LessThanHalfTime" -> "Less Than Half-Time";
            default -> "N/A";
        };
    }

    private BenefitType getSelectedBenefitType() {
        return BenefitType.valueOf(benefitTypeComboBox.getSelectedItem().toString());
    }

    private int getAcademicTermCode(String selectedTerm) {
        return switch (selectedTerm) {
            case "Fall 2025" -> 202508;
            case "Spring 2026" -> 202601;
            case "Summer 2026" -> 202605;
            default -> 202508;
        };
    }

    private Student loadStudent(Connection conn, int userId) throws SQLException {
        String sql = """
                SELECT u.user_id, u.first_name, u.last_name, u.email, u.password_hash,
                       u.is_active, u.last_login, s.student_id, s.benefit_type
                FROM "user" u
                JOIN student s ON u.user_id = s.user_id
                WHERE u.user_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDateTime lastLogin = parseDateTime(rs.getString("last_login"));

                    BenefitType benefitType;
                    String dbBenefitType = rs.getString("benefit_type");
                    try {
                        benefitType = BenefitType.valueOf(dbBenefitType);
                    } catch (Exception ex) {
                        benefitType = getSelectedBenefitType();
                    }

                    return new Student(
                            rs.getInt("user_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getBoolean("is_active"),
                            lastLogin,
                            rs.getInt("student_id"),
                            benefitType
                    );
                }
            }
        }

        throw new SQLException("Student record not found for user.");
    }

    private LocalDateTime parseDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeText.replace(" ", "T"));
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private void submitCertificationRequest() {
        if (pendingCourses.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one class before submitting your certification request.",
                    "No Classes Added",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "No active session found.",
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        BenefitType selectedBenefitType = getSelectedBenefitType();
        int academicTermCode = getAcademicTermCode(termComboBox.getSelectedItem().toString());

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);

            try {
                Student student = loadStudent(conn, Session.getUserId());
                student.setBenefitType(selectedBenefitType);

                CertRequest certRequest = createRequestFromObjects(student, academicTermCode);

                saveStudentBenefitType(conn, student);
                int certId = saveCertRequest(conn, student, certRequest);
                saveCourses(conn, certId, certRequest.getCourses());
                saveMonthlyAllowance(conn, certId, certRequest);

                conn.commit();

                JOptionPane.showMessageDialog(this,
                        "Certification request submitted successfully.",
                        "Submission Complete",
                        JOptionPane.INFORMATION_MESSAGE);

                if (homePagePanel != null) {
                    homePagePanel.refreshSummary();
                }

                clearForm();

            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to submit certification request.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private CertRequest createRequestFromObjects(Student student, int academicTermCode) {
        int generatedCertId = generateTempCertId();
        CertRequest certRequest = student.createCertRequest(generatedCertId, academicTermCode);

        for (Course course : pendingCourses) {
            certRequest.addCourse(course);
        }

        certRequest.submit();
        return certRequest;
    }

    private int generateTempCertId() {
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private void saveStudentBenefitType(Connection conn, Student student) throws SQLException {
        String sql = """
                UPDATE student
                SET benefit_type = ?
                WHERE user_id = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getBenefitType().name());
            ps.setInt(2, student.getUserId());
            ps.executeUpdate();
        }
    }

    private int saveCertRequest(Connection conn, Student student, CertRequest certRequest) throws SQLException {
        String sql = """
                INSERT INTO cert_request (
                    student_id,
                    academic_term_code,
                    status,
                    submission_date,
                    last_updated_date,
                    total_units,
                    unit_load_category,
                    is_draft
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 0)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, student.getStudentId());
            ps.setInt(2, certRequest.getAcademicTermCode());
            ps.setString(3, certRequest.getStatus().name());
            ps.setString(4, certRequest.getSubmissionDate() != null ? certRequest.getSubmissionDate().toString() : null);
            ps.setString(5, certRequest.getLastUpdatedDate() != null ? certRequest.getLastUpdatedDate().toString() : null);
            ps.setDouble(6, certRequest.getTotalUnits());
            ps.setString(7, certRequest.getUnitLoadCategory());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        throw new SQLException("Failed to retrieve generated cert_id.");
    }

    private void saveCourses(Connection conn, int certId, List<Course> courses) throws SQLException {
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

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Course course : courses) {
                ps.setInt(1, certId);
                ps.setString(2, course.getSectionNumber());
                ps.setString(3, course.getCoursePrefix());
                ps.setInt(4, course.getCourseNumber());
                ps.setString(5, course.getTitle());
                ps.setString(6, String.valueOf(String.valueOf(course.getCrn())));
                ps.setDouble(7, course.getUnits());
                ps.setInt(8, course.getCourseLengthWeeks());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void saveMonthlyAllowance(Connection conn, int certId, CertRequest certRequest) throws SQLException {
        String sql = """
                INSERT INTO monthly_allowance_calculator (
                    cert_id,
                    estimated_monthly_allowance
                ) VALUES (?, ?)
                ON CONFLICT(cert_id) DO UPDATE SET
                    estimated_monthly_allowance = excluded.estimated_monthly_allowance
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);
            ps.setDouble(2, certRequest.getEstimatedMonthlyAllowance());
            ps.executeUpdate();
        }
    }

    private String stripTrailingZero(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void clearCourseEntryFields() {
        sectionNumberField.setText("");
        prefixField.setText("");
        courseNumberField.setText("");
        titleField.setText("");
        crnField.setText("");
        unitsField.setText("");
        lengthField.setText("");
    }

    private void clearForm() {
        clearCourseEntryFields();
        tableModel.setRowCount(0);
        pendingCourses.clear();
        totalClassesValue.setText("0");
        totalUnitsValue.setText("0");
        trainingTimeValue.setText("N/A");
        allowanceValue.setText("$0.00 / month");
    }
}