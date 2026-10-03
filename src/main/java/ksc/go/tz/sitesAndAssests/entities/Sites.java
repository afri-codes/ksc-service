package ksc.go.tz.sitesAndAssests.entities;


import ksc.go.tz.masterData.entities.Service;
import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.masterData.entities.AddOn;
import ksc.go.tz.masterData.entities.CleaningDepth;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "sites")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"service", "cleaningDepth", "addOns"})
@Getter
@Setter
@Where(clause = " deleted_at is null")
public class Sites extends BaseEntity<UUID> {

    @Column(name = "site_owner_id", nullable = false)
    private String site_owner;

    private String propertyType; // house, apartment, office, warehouse, etc. with price

    @Column(name = "site_type")
    private String siteType; // service, residential, commercial, industrial

    @Column(name = "area_sqm")
    private BigDecimal areaSqm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    // Required for cleaning services; optional for Fumigation and Property Management.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleaning_depth_id")
    private CleaningDepth cleaningDepth;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "site_add_ons",
            joinColumns = @JoinColumn(name = "site_id"),
            inverseJoinColumns = @JoinColumn(name = "add_on_id")
    )
    private Set<AddOn> addOns = new HashSet<>();

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

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
