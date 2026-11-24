package ksc.go.tz.billing.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * How KSC uses the platform payment service.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ksc.payment")
public class PaymentServiceProperties {

    /**
     * CLICKPESA or SELCOM (Selcom also needs the payer's email).
     */
    private String provider = "CLICKPESA";

    /**
     * KSC's system code at the payment service; results arrive on ksc.payment.results.
     */
    private String system = "KSC";

    /**
     * A payment with no result from the payment service after this long is marked FAILED.
     * The payment service settles unanswered mobile money prompts after ~30 minutes, so
     * reaching this means the request most likely never arrived.
     */
    private long resultTimeoutMinutes = 120;

    /**
     * Same, for payments the client completes later via a link or control number (CARD, CHECKOUT,
     * BILLPAY, BANK). The payment service fails those after 12 hours unpaid, so this waits a bit longer.
     */
    private long hostedResultTimeoutMinutes = 780;
}
