package ksc.go.tz.billing.kafka;

import ksc.go.tz.billing.services.PaymentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PaymentResultListenerTest {

    /** A payment service PaymentEvent exactly as it appears on ksc.payment.results. */
    private static final String EVENT = """
            {"eventId":"5c0f8e1e-6a8e-4d55-9a8f-2f8c3b1d7e21","system":"KSC",
             "reference":"2b1d6f0e-8a40-4c1e-9d55-3f2a7c9e1b10","attempt":1,
             "paymentReference":"PAYS5XADEP6HELUDUY47","status":"SUCCESS","amount":15000,"currency":"TZS",
             "provider":"CLICKPESA","transactionId":"LCPCAD614J16EY","providerReference":"MP261001.1234.A12345",
             "channel":"AIRTEL-MONEY","message":null,"method":"MOBILE_MONEY",
             "paidAt":"2026-10-01T00:12:02","failureReason":null,"timestamp":"2026-10-01T00:12:03.512"}
            """;

    private final PaymentService paymentService = mock(PaymentService.class);
    private final PaymentResultListener listener =
            new PaymentResultListener(Jackson2ObjectMapperBuilder.json().build(), paymentService);

    @Test
    void readsThePaymentServiceEvent() {
        listener.onPaymentResult(EVENT);

        ArgumentCaptor<PaymentResultMessage> message = ArgumentCaptor.forClass(PaymentResultMessage.class);
        verify(paymentService).handleResult(message.capture());

        PaymentResultMessage result = message.getValue();
        assertEquals("2b1d6f0e-8a40-4c1e-9d55-3f2a7c9e1b10", result.paymentId());
        assertEquals("SUCCESS", result.status());
        assertEquals(0, new BigDecimal("15000").compareTo(result.amount()));
        assertEquals("MP261001.1234.A12345", result.providerRef());
        assertEquals("MOBILE_MONEY", result.method());
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 12, 2), result.paidAt());
    }

    @Test
    void ignoresOtherSystemsAndUnreadableMessages() {
        listener.onPaymentResult(EVENT.replace("\"system\":\"KSC\"", "\"system\":\"AFRITRANS\""));
        listener.onPaymentResult("not json");

        verify(paymentService, never()).handleResult(any());
    }
}
