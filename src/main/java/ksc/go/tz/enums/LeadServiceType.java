package ksc.go.tz.enums;

public enum LeadServiceType {
    DEEP_CLEANING("Deep Cleaning"),
    OFFICE_CLEANING("Office Cleaning"),
    RESIDENTIAL_CLEANING("Residential Cleaning"),
    COMMERCIAL_CLEANING("Commercial Cleaning"),
    OTHER("Other"),
    CLEANING("Cleaning"),
    PROPERTY_MANAGEMENT("Property Management"),;

    private final String displayName;
    LeadServiceType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

}
