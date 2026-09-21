package ksc.go.tz.enums;

public enum LeadSource {
    WEBSITE("Website"),
    REFERRAL("Referral"),
    SOCIAL_MEDIA("Social Media"),
    WALK_IN("Walk-in"),
    OTHER("Other");

    private final String displayName;
    LeadSource(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}


