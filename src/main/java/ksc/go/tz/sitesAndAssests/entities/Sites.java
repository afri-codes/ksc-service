package ksc.go.tz.sitesAndAssests.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sites")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Sites extends BaseEntity<UUID> {

    @Column(name = "site_owner_id", nullable = false)
    private String site_owner;

    @Column(name = "site_type")
    private String siteType;

    @Column(name = "area_sqm")
    private BigDecimal areaSqm;

    @Column(name = "room_count")
    private Integer roomCount;

    @Column(name = "address_area")
    private String addressArea;

    @Column(name = "plot_coordinates", columnDefinition = "jsonb")
    private String plotCoordinates;

    @Column(name = "is_secured")
    private Boolean secured;

    @Column(name = "access_type")
    private String accessType;

}
