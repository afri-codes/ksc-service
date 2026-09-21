package ksc.go.tz.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntityCustomId<T extends Serializable,U> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private T id;
    @CreatedBy
    @Column(name = "created_by")
    private U createdBy;
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @LastModifiedBy
    @Column(name = "updated_by")
    private U updatedBy;
    @Column(name = "deleted_by")
    private U deletedBy;
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @Column(columnDefinition = "boolean default false")
    private Boolean deleted = false;
}
