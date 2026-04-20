import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CertRequest {

    private int certId;
    private int academicTermCode;
    private BenefitType benefitType;
    private RequestStatus status;

    private LocalDateTime submissionDate;
    private LocalDateTime lastUpdatedDate;

    private double totalUnits;
    private String unitLoadCategory;
    private double estimatedMonthlyAllowance;
    private String scoNote;

    private boolean cancelRequested;

    private final List<Course> courses;
    private final List<CertError> errors;

    public CertRequest(int certId, int academicTermCode, BenefitType benefitType) {
        if (certId <= 0) {
            throw new IllegalArgumentException("Certification ID must be greater than 0.");
        }

        if (academicTermCode <= 0) {
            throw new IllegalArgumentException("Academic term code must be greater than 0.");
        }

        if (benefitType == null) {
            throw new IllegalArgumentException("Benefit type cannot be null.");
        }

        this.certId = certId;
        this.academicTermCode = academicTermCode;
        this.benefitType = benefitType;
        this.status = RequestStatus.DRAFT;
        this.submissionDate = null;
        this.lastUpdatedDate = LocalDateTime.now();
        this.totalUnits = 0.0;
        this.unitLoadCategory = "N/A";
        this.estimatedMonthlyAllowance = 0.0;
        this.scoNote = "";
        this.cancelRequested = false;
        this.courses = new ArrayList<>();
        this.errors = new ArrayList<>();
    }

    public int getCertId() {
        return certId;
    }

    public void setCertId(int certId) {
        if (certId <= 0) {
            throw new IllegalArgumentException("Certification ID must be greater than 0.");
        }
        this.certId = certId;
        touch();
    }

    public int getAcademicTermCode() {
        return academicTermCode;
    }

    public void setAcademicTermCode(int academicTermCode) {
        if (academicTermCode <= 0) {
            throw new IllegalArgumentException("Academic term code must be greater than 0.");
        }
        this.academicTermCode = academicTermCode;
        touch();
    }

    public BenefitType getBenefitType() {
        return benefitType;
    }

    public void setBenefitType(BenefitType benefitType) {
        if (benefitType == null) {
            throw new IllegalArgumentException("Benefit type cannot be null.");
        }
        this.benefitType = benefitType;
        updateEstimatedMonthlyAllowance();
        touch();
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public double getTotalUnits() {
        return totalUnits;
    }

    public String getUnitLoadCategory() {
        return unitLoadCategory;
    }

    public double getEstimatedMonthlyAllowance() {
        return estimatedMonthlyAllowance;
    }

    public String getFormattedEstimatedMonthlyAllowance() {
        return MonthlyAllowanceCalculator.formatAllowance(estimatedMonthlyAllowance);
    }

    public String getScoNote() {
        return scoNote;
    }

    public void setScoNote(String scoNote) {
        this.scoNote = scoNote != null ? scoNote : "";
        touch();
    }

    public boolean isCancelRequested() {
        return cancelRequested;
    }

    public List<Course> getCourses() {
        return new ArrayList<>(courses);
    }

    public List<CertError> getErrors() {
        return new ArrayList<>(errors);
    }

    public void addCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }

        courses.add(course);
        recalc();
    }

    public void removeCourse(Course course) {
        if (course == null) {
            return;
        }

        courses.remove(course);
        recalc();
    }

    public void removeCourse(int index) {
        if (index < 0 || index >= courses.size()) {
            throw new IndexOutOfBoundsException("Invalid course index.");
        }

        courses.remove(index);
        recalc();
    }

    public void addError(CertError error) {
        if (error == null) {
            throw new IllegalArgumentException("Certification error cannot be null.");
        }

        if (error.getCertId() != this.certId) {
            throw new IllegalArgumentException("Error certId does not match this request.");
        }

        errors.add(error);
        this.status = RequestStatus.ACTION_NEEDED;
        this.scoNote = error.getErrorMessage();
        touch();
    }

    public void resolveError(int errorId) {
        for (CertError error : errors) {
            if (error.getErrorId() == errorId) {
                error.markResolved();
                touch();
                return;
            }
        }

        throw new IllegalArgumentException("No error found with ID: " + errorId);
    }

    public void resolveAllErrors() {
        for (CertError error : errors) {
            error.markResolved();
        }

        this.scoNote = "";
        touch();
    }

    public boolean hasActiveErrors() {
        for (CertError error : errors) {
            if (!error.isResolved()) {
                return true;
            }
        }
        return false;
    }

    private void recalc() {
        calculateTotalUnits();
        updateUnitLoadCategory();
        updateEstimatedMonthlyAllowance();
        touch();
    }

    private void calculateTotalUnits() {
        double sum = 0.0;

        for (Course course : courses) {
            sum += course.getUnits();
        }

        this.totalUnits = sum;
    }

    private void updateUnitLoadCategory() {
        if (totalUnits >= 12) {
            unitLoadCategory = "FullTime";
        } else if (totalUnits >= 9) {
            unitLoadCategory = "ThreeQuarterTime";
        } else if (totalUnits >= 6) {
            unitLoadCategory = "HalfTime";
        } else if (totalUnits > 0) {
            unitLoadCategory = "LessThanHalfTime";
        } else {
            unitLoadCategory = "N/A";
        }
    }

    private void updateEstimatedMonthlyAllowance() {
        this.estimatedMonthlyAllowance =
                MonthlyAllowanceCalculator.calculateMonthlyAllowance(benefitType, unitLoadCategory);
    }

    public void submit() {
        if (courses.isEmpty()) {
            throw new IllegalStateException("Cannot submit with no courses.");
        }

        recalc();
        this.status = RequestStatus.SUBMITTED;
        this.submissionDate = LocalDateTime.now();
        touch();
    }

    public void markActionNeeded(String note) {
        this.status = RequestStatus.ACTION_NEEDED;
        this.scoNote = note != null ? note : "";
        touch();
    }

    public void markCertified() {
        if (hasActiveErrors()) {
            throw new IllegalStateException("Cannot certify request while unresolved errors still exist.");
        }

        this.status = RequestStatus.CERTIFIED;
        this.scoNote = "";
        touch();
    }

    public void cancel() {
        this.cancelRequested = true;
        this.status = RequestStatus.CANCELLATION_PENDING;
        touch();
    }

    public void markCancelled() {
        this.cancelRequested = false;
        this.status = RequestStatus.CANCELLED;
        touch();
    }

    private void touch() {
        this.lastUpdatedDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "CertRequest{" +
                "certId=" + certId +
                ", academicTermCode=" + academicTermCode +
                ", benefitType=" + benefitType +
                ", status=" + status +
                ", submissionDate=" + submissionDate +
                ", lastUpdatedDate=" + lastUpdatedDate +
                ", totalUnits=" + totalUnits +
                ", unitLoadCategory='" + unitLoadCategory + '\'' +
                ", estimatedMonthlyAllowance=" + estimatedMonthlyAllowance +
                ", scoNote='" + scoNote + '\'' +
                ", cancelRequested=" + cancelRequested +
                ", courses=" + courses +
                ", errors=" + errors +
                '}';
    }
}