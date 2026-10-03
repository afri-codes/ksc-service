package ksc.go.tz.billing.services;

import ksc.go.tz.enums.PaymentMethod;
import ksc.go.tz.billing.client.PaymentServiceProperties;
import ksc.go.tz.billing.entities.Payment;
import ksc.go.tz.billing.repository.PaymentRepository;
import ksc.go.tz.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Releases invoices blocked by a payment the payment service never reported on (for
 * example, the request never reached it). A result that still arrives later is applied
 * by {@link PaymentService#handleResult}: a late SUCCESS overrides this FAILED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultTimeoutJob {

    static final String REASON = "No result from the payment service";

    private final PaymentRepository paymentRepository;
    private final PaymentServiceProperties properties;

    @Scheduled(fixedDelayString = "${ksc.payment.timeout-check-interval-ms:600000}",
            initialDelayString = "${ksc.payment.timeout-check-initial-delay-ms:120000}")
    @Transactional
    public void failPaymentsWithoutResult() {
        LocalDateTime now = LocalDateTime.now();
        // Mobile money prompts settle within ~30 minutes; links and control numbers can take up to 12 hours.
        fail(paymentRepository.findByStatusInAndMethodAndCreatedAtBefore(PaymentServiceImpl.IN_PROGRESS,
                PaymentMethod.MOBILE_MONEY, now.minusMinutes(properties.getResultTimeoutMinutes())),
                properties.getResultTimeoutMinutes());
        fail(paymentRepository.findByStatusInAndMethodNotAndCreatedAtBefore(PaymentServiceImpl.IN_PROGRESS,
                PaymentMethod.MOBILE_MONEY, now.minusMinutes(properties.getHostedResultTimeoutMinutes())),
                properties.getHostedResultTimeoutMinutes());
    }

    private void fail(List<Payment> stale, long minutes) {
        for (Payment payment : stale) {
            log.warn("[PAYMENT] paymentId={} ({}) had no result after {} minutes; marking FAILED",
                    payment.getId(), payment.getMethod(), minutes);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(REASON);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }
}
