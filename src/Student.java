public class Student extends User {

    private int studentId;
    private BenefitType benefitType;

    public Student(int userId,
                   String firstName,
                   String lastName,
                   String email,
                   String passwordHash,
                   java.time.LocalDateTime lastLogin,
                   int studentId,
                   BenefitType benefitType) {

        super(userId, firstName, lastName, email, passwordHash, UserRole.STUDENT, lastLogin);

        this.studentId = studentId;
        this.benefitType = benefitType;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be greater than 0.");
        }
        this.studentId = studentId;
    }

    public BenefitType getBenefitType() {
        return benefitType;
    }

    public void setBenefitType(BenefitType benefitType) {
        if (benefitType == null) {
            throw new IllegalArgumentException("Benefit type cannot be null.");
        }
        this.benefitType = benefitType;
    }

    public CertRequest createCertRequest(int certId, int academicTermCode) {
        return new CertRequest(certId, academicTermCode, benefitType);
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId=" + getUserId() +
                ", studentId=" + studentId +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", benefitType=" + benefitType +
                '}';
    }
}