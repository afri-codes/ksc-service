package ksc.go.tz.job.dto;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Contract ID must be provided")
    private String contractId;

    @NotNull(message = "Subscription ID must be provided")
    private String subscriptionId;

    @NotNull(message = "Site ID must be provided")
    private String siteId;

    @NotNull(message = "Crew ID must be provided")
    private String crewId;

    @NotNull(message = "Lead service type must be provided")
    private String serviceLine;

    @NotNull(message = "Job type must be provided")
    private String jobType;

    @NotNull(message = "Scheduled start time must be provided")
    private LocalDateTime scheduledStart;

    @NotNull(message = "Scheduled end time must be provided")
    private LocalDateTime scheduledEnd;

    private String status;
}
