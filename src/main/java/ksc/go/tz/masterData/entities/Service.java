package ksc.go.tz.masterData.entities;


import java.math.BigDecimal;
import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.Status;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "services")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Service extends BaseEntity<UUID> {

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    // Fixed price charged once per site for this service (e.g. Fumigation). Null = no fixed charge;
    // Cleaning is priced by cleaning depth instead.
    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price;

}
