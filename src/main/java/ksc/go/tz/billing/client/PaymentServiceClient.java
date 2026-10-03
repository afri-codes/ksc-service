package ksc.go.tz.billing.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * The platform payment service (Eureka name payment-service). Results come back
 * on Kafka (ksc.payment.results), not through this client.
 */
@FeignClient(name = "payment-service", contextId = "kscPaymentServiceClient")
public interface PaymentServiceClient {

    /**
     * Starts a payment. Returns the payment service's response envelope; the
     * payment is under {@code data}. Rejections come back as HTTP 400 with
     * {@code error} and {@code message}.
     */
    @PostMapping("/api/v2/payments/{provider}")
    JsonNode createPayment(@RequestHeader("Authorization") String authorization,
                           @PathVariable("provider") String provider,
                           @RequestBody CreatePaymentRequest request);
}
