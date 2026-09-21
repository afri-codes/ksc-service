package ksc.go.tz.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity<U> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid DEFAULT uuid_generate_v4()")
    private UUID id;
    @CreatedBy
    @Basic(optional = true)
    @Column(name = "created_by")
    private U createdBy;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @LastModifiedBy
    @Column(name = "updated_by")
    private U updatedBy;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(columnDefinition = "boolean default false")
    private boolean deleted;

    @Column(columnDefinition = "boolean default true")
    private boolean active = true;
}
