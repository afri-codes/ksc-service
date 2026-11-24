package ksc.go.tz.billing.kafka;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment result published by the payment service on {@code ksc.payment.results} (its {@code PaymentEvent}).
 * Only the fields KSC uses are mapped; the rest are ignored.
 *
 * @param eventId       unique per event; redeliveries repeat it
 * @param system        calling system, always KSC on this topic
 * @param paymentId     KSC payment ID, sent to the payment service as {@code reference} (required)
 * @param invoiceNumber invoice number (informational, not sent by the payment service)
 * @param status        SUCCESS, FAILED, CANCELLED or REFUNDED
 * @param amount        amount requested; check {@code message} for AMOUNT_MISMATCH
 * @param currency      ISO currency code
 * @param providerRef   provider / mobile network receipt reference
 * @param method        MOBILE_MONEY, CARD, CHECKOUT or BILLPAY
 * @param paidAt        when the money was received (SUCCESS only)
 * @param failureReason why the payment failed or was cancelled
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentResultMessage(
        String eventId,
        String system,
        @JsonAlias("reference") String paymentId,
        String invoiceNumber,
        String status,
        BigDecimal amount,
        String currency,
        @JsonAlias("providerReference") String providerRef,
        String method,
        LocalDateTime paidAt,
        String failureReason
) {
}
