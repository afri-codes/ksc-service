package ksc.go.tz.job.dto;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.job.entities.Job;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobChecklistDto  {

    private String jobId;

    private String templateName;

}
