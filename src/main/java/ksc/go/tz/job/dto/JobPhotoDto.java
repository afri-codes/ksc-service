package ksc.go.tz.job.dto;

import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.job.entities.Job;
import lombok.*;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobPhotoDto extends BaseEntity<UUID> {

    private String jobId;

    private String image;

    private String storageUrl;

    private LocalDateTime capturedAt;

    private Boolean synced;


}
