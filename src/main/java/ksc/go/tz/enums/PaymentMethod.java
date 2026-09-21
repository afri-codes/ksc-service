package ksc.go.tz.enums;

public enum PaymentMethod {
    CASH("Cash"),
    BANK("Bank"),
    MOBILE_MONEY("Mobile Money"),
    CARD("Card"),
    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    OTHER("Other");


    private final String displayName;
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
