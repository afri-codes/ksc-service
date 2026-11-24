package ksc.go.tz.billing.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.PaymentMethod;
import ksc.go.tz.enums.PaymentStatus;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "invoice")
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Payment extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "provider_ref")
    private String providerRef;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "payer_phone")
    private String payerPhone;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "notes")
    private String notes;

    /** Payment service reference for this attempt (quote it to support). */
    @Column(name = "payment_reference")
    private String paymentReference;

    /** Hosted page the client opens to pay (CARD / CHECKOUT). */
    @Column(name = "payment_url", length = 1000)
    private String paymentUrl;

    /** Control number the client pays (BILLPAY / BANK). */
    @Column(name = "control_number")
    private String controlNumber;

    /** KSC receipt number, assigned when the payment succeeds (e.g. RCT-20261002-7K3Q9A). */
    @Column(name = "receipt_number", unique = true)
    private String receiptNumber;


}
