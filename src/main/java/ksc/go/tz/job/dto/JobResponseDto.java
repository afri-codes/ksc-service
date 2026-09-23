package ksc.go.tz.job.dto;


import ksc.go.tz.contractAndSubscriptions.dto.ContractResponseDto;
import ksc.go.tz.job.entities.Job;
import ksc.go.tz.sitesAndAssests.dto.SiteResponseDto;
import lombok.*;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobResponseDto {

    private String contractId;

    private ContractResponseDto contract;

    private String subscriptionId;

    private String siteId;

    private SiteResponseDto site;

    private String crewId;

    private String serviceLine;

    private String jobType;

    private LocalDateTime scheduledStart;

    private LocalDateTime scheduledEnd;

    private String status;

    public JobResponseDto (Job job){
        this.contractId = job.getContract().getId().toString();
        this.contract = new ContractResponseDto(job.getContract());
        this.subscriptionId = job.getSubscription() != null ? job.getSubscription().getId().toString() : null;
        this.siteId = job.getSite().getId().toString();
        this.site = new SiteResponseDto(job.getSite());
        this.crewId = job.getCrew() != null ? job.getCrew().getId().toString() : null;
        this.serviceLine = job.getServiceType() != null ? job.getServiceType().toString() : null;
        this.jobType = job.getJobType() != null ? job.getJobType().toString() : null;
        this.scheduledStart = job.getScheduledStart();
        this.scheduledEnd = job.getScheduledEnd();
        this.status = job.getStatus() != null ? job.getStatus().toString() : null;
    }
}
