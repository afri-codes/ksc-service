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
@ToString
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


}
