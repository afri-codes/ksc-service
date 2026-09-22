package ksc.go.tz.quoting.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.Frequency;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.QuoteStatus;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "quotes")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Quote extends BaseEntity<UUID> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Sites site;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "client_tier")
    private String clientTier;

    @Column(name = "hourly_rate", precision = 19, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "estimated_hours", precision = 19, scale = 2)
    private BigDecimal estimatedHours;

    @Column(name = "price_min", precision = 19, scale = 2)
    private BigDecimal priceMin;

    @Column(name = "price_max", precision = 19, scale = 2)
    private BigDecimal priceMax;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private LeadServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency")
    private Frequency frequency;

    @Column(name = "area_sqm")
    private BigDecimal areaSqm;

    @Enumerated(EnumType.STRING)
    private QuoteStatus status;
}
