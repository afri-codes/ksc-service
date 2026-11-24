package ksc.go.tz.billing.services;

import ksc.go.tz.billing.client.PaymentServiceProperties;
import ksc.go.tz.billing.entities.Payment;
import ksc.go.tz.billing.repository.PaymentRepository;
import ksc.go.tz.enums.PaymentMethod;
import ksc.go.tz.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentResultTimeoutJobTest {

    @Test
    void mobileMoneyAndHostedPaymentsHaveTheirOwnTimeouts() {
        PaymentRepository repository = mock(PaymentRepository.class);
        Payment staleMobile = new Payment();
        staleMobile.setStatus(PaymentStatus.PROCESSING);
        staleMobile.setMethod(PaymentMethod.MOBILE_MONEY);
        Payment staleBillPay = new Payment();
        staleBillPay.setStatus(PaymentStatus.PENDING);
        staleBillPay.setMethod(PaymentMethod.BILLPAY);
        when(repository.findByStatusInAndMethodAndCreatedAtBefore(eq(PaymentServiceImpl.IN_PROGRESS), eq(PaymentMethod.MOBILE_MONEY), any()))
                .thenReturn(List.of(staleMobile));
        when(repository.findByStatusInAndMethodNotAndCreatedAtBefore(eq(PaymentServiceImpl.IN_PROGRESS), eq(PaymentMethod.MOBILE_MONEY), any()))
                .thenReturn(List.of(staleBillPay));

        new PaymentResultTimeoutJob(repository, new PaymentServiceProperties()).failPaymentsWithoutResult();

        ArgumentCaptor<LocalDateTime> mobileCutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> hostedCutoff = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findByStatusInAndMethodAndCreatedAtBefore(eq(PaymentServiceImpl.IN_PROGRESS), eq(PaymentMethod.MOBILE_MONEY), mobileCutoff.capture());
        verify(repository).findByStatusInAndMethodNotAndCreatedAtBefore(eq(PaymentServiceImpl.IN_PROGRESS), eq(PaymentMethod.MOBILE_MONEY), hostedCutoff.capture());
        // Mobile money: 120 minutes. Links / control numbers: 780 minutes (the payment service gives up after 12 hours).
        assertTrue(mobileCutoff.getValue().isBefore(LocalDateTime.now().minusMinutes(119)));
        assertTrue(mobileCutoff.getValue().isAfter(LocalDateTime.now().minusMinutes(121)));
        assertTrue(hostedCutoff.getValue().isBefore(LocalDateTime.now().minusMinutes(779)));
        assertTrue(hostedCutoff.getValue().isAfter(LocalDateTime.now().minusMinutes(781)));

        for (Payment stale : List.of(staleMobile, staleBillPay)) {
            assertEquals(PaymentStatus.FAILED, stale.getStatus());
            assertEquals(PaymentResultTimeoutJob.REASON, stale.getFailureReason());
            verify(repository).save(stale);
        }
    }
}
