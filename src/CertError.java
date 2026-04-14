import java.time.LocalDateTime;

public class CertError {

    private int errorId;
    private int certId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private boolean resolved;

    public CertError(int errorId, int certId, String errorMessage) {
        if (errorId <= 0) {
            throw new IllegalArgumentException("Error ID must be greater than 0.");
        }

        if (certId <= 0) {
            throw new IllegalArgumentException("Certification ID must be greater than 0.");
        }

        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("Error message cannot be blank.");
        }

        this.errorId = errorId;
        this.certId = certId;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
        this.resolved = false;
    }

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        if (errorId <= 0) {
            throw new IllegalArgumentException("Error ID must be greater than 0.");
        }
        this.errorId = errorId;
    }

    public int getCertId() {
        return certId;
    }

    public void setCertId(int certId) {
        if (certId <= 0) {
            throw new IllegalArgumentException("Certification ID must be greater than 0.");
        }
        this.certId = certId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("Error message cannot be blank.");
        }
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void markResolved() {
        this.resolved = true;
    }

    public void reopen() {
        this.resolved = false;
    }

    @Override
    public String toString() {
        return "CertError{" +
                "errorId=" + errorId +
                ", certId=" + certId +
                ", errorMessage='" + errorMessage + '\'' +
                ", createdAt=" + createdAt +
                ", resolved=" + resolved +
                '}';
    }
}