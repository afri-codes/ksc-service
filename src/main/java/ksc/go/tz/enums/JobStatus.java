package ksc.go.tz.enums;

public enum JobStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    FAILED("Failed");

    private final String displayName;
    JobStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
