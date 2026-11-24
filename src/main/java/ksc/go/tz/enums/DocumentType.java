package ksc.go.tz.enums;

public enum DocumentType {
    CONTRACT("Contract"),;

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
