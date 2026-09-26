package ksc.go.tz.DocumentManagement.entities;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "uploads")
public class Upload extends BaseEntity<UUID> {

    @Column(name = "file_name",nullable = false)
    private String fileName;
    @Column(name = "file_type",nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentType fileType;
    @Column(name = "file_desc")
    private String fileDescription;
    private UUID createdBy;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "file_size_kb")
    private BigInteger kbSize;

}
