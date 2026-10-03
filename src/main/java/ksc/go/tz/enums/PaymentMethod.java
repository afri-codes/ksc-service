package ksc.go.tz.enums;

public enum PaymentMethod {
    CASH("Cash"),
    BANK("Bank"),
    MOBILE_MONEY("Mobile Money"),
    CARD("Card"),
    /** Hosted payment page where the client chooses how to pay (payment service CHECKOUT). */
    CHECKOUT("Checkout"),
    /** Control number payable via mobile money, bank or agent (payment service BILLPAY). */
    BILLPAY("BillPay"),
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
