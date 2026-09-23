package ksc.go.tz.job.dto;


import jakarta.persistence.*;
import ksc.go.tz.common.BaseEntity;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.entities.Subscription;
import ksc.go.tz.enums.JobStatus;
import ksc.go.tz.enums.JobType;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.job.entities.Crew;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobDto {

    private String contractId;

    private String subscriptionId;

    private String siteId;

    private String crewId;

    private String serviceLine;

    private String jobType;

    private LocalDateTime scheduledStart;

    private LocalDateTime scheduledEnd;

    private String status;
}
