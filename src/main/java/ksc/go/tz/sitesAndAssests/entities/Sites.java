package ksc.go.tz.sitesAndAssests.entities;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;
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


    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "plot_coordinates", columnDefinition = "jsonb")
    private Map<String, Object> plotCoordinates;

    @Column(name = "longitude",nullable = false)
    private String longitude;

    @Column(name = "latitude",nullable = false)
    private String latitude;

    @Column(name = "is_secured")
    private Boolean secured;

    @Column(name = "access_type")
    private String accessType;

}
