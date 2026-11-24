package ksc.go.tz.billing.client;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Body of the payment service's POST /api/v2/payments/{provider}.
 *
 * @param reference the KSC payment ID; results on Kafka carry it back as {@code reference}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreatePaymentRequest(
        String system,
        String reference,
        String method,
        BigDecimal amount,
        String currency,
        String description,
        String customerName,
        String customerEmail,
        String phoneNumber,
        String phone,
        Customer customer
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Customer(String name, String phone, String email) {
    }
}
