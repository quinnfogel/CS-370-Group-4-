public class SCO extends User {

    private int empId;

    public SCO(int userId,
               String firstName,
               String lastName,
               String email,
               String passwordHash,
               boolean isActive,
               java.time.LocalDateTime lastLogin,
               int empId) {

        super(userId, firstName, lastName, email, passwordHash, UserRole.SCO, isActive, lastLogin);

        this.empId = empId;
    }

    public int getEmpId() {
        return empId;
    }

    public void setEmpId(int empId) {
        if (empId <= 0) {
            throw new IllegalArgumentException("Employee ID must be greater than 0.");
        }
        this.empId = empId;
    }

    public void markRequestActionNeeded(CertRequest certRequest, String note) {
        if (certRequest == null) {
            throw new IllegalArgumentException("Certification request cannot be null.");
        }

        certRequest.markActionNeeded(note);
    }

    public void certifyRequest(CertRequest certRequest) {
        if (certRequest == null) {
            throw new IllegalArgumentException("Certification request cannot be null.");
        }

        certRequest.markCertified();
    }

    public void cancelRequest(CertRequest certRequest) {
        if (certRequest == null) {
            throw new IllegalArgumentException("Certification request cannot be null.");
        }

        certRequest.cancel();
    }

    @Override
    public String toString() {
        return "SCO{" +
                "userId=" + getUserId() +
                ", empId=" + empId +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", isActive=" + isActive() +
                '}';
    }
}