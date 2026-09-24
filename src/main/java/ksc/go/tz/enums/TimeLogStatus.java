package ksc.go.tz.enums;

public enum TimeLogStatus {
    IN_SITE("In Site"),
    OUT_OF_SITE("OUT Of Site");

    private final String displayName;
    TimeLogStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}


