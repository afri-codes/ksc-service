package ksc.go.tz.enums;

public enum LeadStatus {
    NEW("New"),
    CONTACTED("Contacted"),
    CONVERTED("Converted"),
    LOST("Lost");

    private final String displayName;
    LeadStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}

