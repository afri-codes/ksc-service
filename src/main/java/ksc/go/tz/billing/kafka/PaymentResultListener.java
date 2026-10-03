package ksc.go.tz.billing.kafka;

import afriUtils.responses.AfriException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ksc.go.tz.billing.services.PaymentService;
import ksc.go.tz.common.kafka.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Receives KSC's payment outcomes from the payment service on {@code ksc.payment.results}.
 * Delivery is at-least-once; {@link PaymentService#handleResult} ignores repeats.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultListener {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(
            topics = KafkaTopic.PAYMENT_RESULTS,
            groupId = "ksc-payment-results",
            containerFactory = PaymentKafkaConfig.PAYMENT_RESULTS_LISTENER_FACTORY
    )
    public void onPaymentResult(String payload) {
        PaymentResultMessage message;
        try {
            message = objectMapper.readValue(payload, PaymentResultMessage.class);
        } catch (JsonProcessingException e) {
            // A malformed message will never parse; log it and move on instead of retrying forever.
            log.error("[PAYMENT] Skipping unreadable payment result: {}", payload, e);
            return;
        }
        if (message.system() != null && !"KSC".equalsIgnoreCase(message.system())) {
            log.warn("[PAYMENT] Skipping result for system {} eventId={}", message.system(), message.eventId());
            return;
        }
        log.info("[PAYMENT] Result received eventId={} paymentId={} status={}",
                message.eventId(), message.paymentId(), message.status());
        try {
            paymentService.handleResult(message);
        } catch (AfriException | IllegalArgumentException e) {
            log.error("[PAYMENT] Skipping invalid payment result paymentId={}: {}", message.paymentId(), e.getMessage());
        }
    }
}
