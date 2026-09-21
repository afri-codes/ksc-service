package ksc.go.tz.enums;

public enum PaymentStatus {

    CREATED("Created"),

    PENDING("Pending"),

    PROCESSING("Processing"),

    SUCCESS("Success"),

    FAILED("Failed"),

    CANCELLED("Cancelled"),

    REFUND_PENDING("Refund Pending"),

    REFUNDED("Refunded");



    private final String displayName;
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }


}