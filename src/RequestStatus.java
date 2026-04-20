public enum RequestStatus {
    DRAFT("Draft"),
    SUBMITTED("Submitted"),
    IN_REVIEW("In Review"),
    ACTION_NEEDED("Action Needed"),
    APPROVED("Approved"),
    CERTIFIED("Certified"),
    ERROR("Error"),
    CANCELLATION_PENDING("Cancellation Pending"),
    CANCELLED("Cancelled");

    private final String dbValue;

    RequestStatus(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static RequestStatus fromDbValue(String value) {
        for (RequestStatus status : values()) {
            if (status.dbValue.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown request status: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}