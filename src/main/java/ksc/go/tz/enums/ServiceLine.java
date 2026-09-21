package ksc.go.tz.enums;

public enum ServiceLine {
    CLEANING("Cleaning"),
    IRRIGATION("Irrigation"),
    PROPERTY_MANAGEMENT("Property Management"),;

    private final String displayName;
    ServiceLine(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

}
