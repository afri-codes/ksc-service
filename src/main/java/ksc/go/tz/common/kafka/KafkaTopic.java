package ksc.go.tz.common.kafka;

public class KafkaTopic {
    public static final String AUDIT_LOGS = "audit.logs";
    public static final String COMPANY_USER_REGISTRATION = "company.user_registration";
    /** Payment service -> KSC: final outcome (SUCCESS, FAILED, CANCELLED, REFUNDED) of KSC's payments. */
    public static final String PAYMENT_RESULTS = "ksc.payment.results";
}
