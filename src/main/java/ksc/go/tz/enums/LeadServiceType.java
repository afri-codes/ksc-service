package ksc.go.tz.enums;

public enum LeadServiceType {

    DEEP_CLEANING("Deep Cleaning"),
    OFFICE_CLEANING("Office Cleaning"),
    RESIDENTIAL_CLEANING("Residential Cleaning"),
    COMMERCIAL_CLEANING("Commercial Cleaning"),
    OTHER("Other"),
    CLEANING("Cleaning"),
    FUMIGATION("Fumigation"),
    PEST_CONTROL("Pest Control"),
    PROPERTY_MANAGEMENT("Property Management");

    private final String displayName;
    LeadServiceType(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Matches a service name such as "Property Management" to its constant (by display name or constant name,
     * ignoring case, spaces and dashes); OTHER when nothing matches.
     */
    public static LeadServiceType fromName(String name) {
        if (name == null) {
            return OTHER;
        }
        String key = name.trim().replaceAll("[\\s-]+", "_").toUpperCase();
        for (LeadServiceType type : values()) {
            if (type.name().equals(key) || type.displayName.equalsIgnoreCase(name.trim())) {
                return type;
            }
        }
        return OTHER;
    }

}
