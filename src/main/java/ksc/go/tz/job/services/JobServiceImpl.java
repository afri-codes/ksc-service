package ksc.go.tz.job.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.contractAndSubscriptions.entities.Contract;
import ksc.go.tz.contractAndSubscriptions.entities.Subscription;
import ksc.go.tz.contractAndSubscriptions.repository.ContractRepository;
import ksc.go.tz.contractAndSubscriptions.repository.SubscriptionRepository;
import ksc.go.tz.enums.JobStatus;
import ksc.go.tz.enums.JobType;
import ksc.go.tz.enums.LeadServiceType;
import ksc.go.tz.enums.SubscriptionStatus;
import ksc.go.tz.job.dto.JobDto;
import ksc.go.tz.job.dto.JobResponseDto;
import ksc.go.tz.job.entities.Crew;
import ksc.go.tz.job.entities.Job;
import ksc.go.tz.job.repository.CrewRepository;
import ksc.go.tz.job.repository.JobRepository;
import ksc.go.tz.sitesAndAssests.entities.Sites;
import ksc.go.tz.sitesAndAssests.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final ContractRepository contractRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CrewRepository crewRepository;
    private final SiteRepository siteRepository;

    @Override
    public JobResponseDto createJob(JobDto jobDto, UUID createdBy) {

         Optional<Contract> contractOptional = contractRepository.findById(UUID.fromString(jobDto.getContractId()));
        if (contractOptional.isEmpty()) {
            throw new AfriException("Contract is required to create a job");
        }
        Optional<Subscription> subscriptionOptional = subscriptionRepository.findById(UUID.fromString(jobDto.getSubscriptionId()));
        if (subscriptionOptional.isEmpty()) {
            throw new AfriException("Subscription is required to create a job");
        }
        Optional<Sites> sitesOptional = siteRepository.findById(UUID.fromString(jobDto.getSiteId()));
        if (sitesOptional.isEmpty()) {
            throw new AfriException("Site is required to create a job");
        }

        Optional<Crew> crewOptional = crewRepository.findById(UUID.fromString(jobDto.getCrewId()));
        if (crewOptional.isEmpty()) {
            throw new AfriException("Crew is required to create a job");
        }

        Job job = new Job();
        job.setJobType(JobType.valueOf(jobDto.getJobType()));
        job.setContract(contractOptional.get());
        job.setSubscription(subscriptionOptional.get());
        job.setSite(sitesOptional.get());
        job.setCrew(crewOptional.get());
        job.setScheduledStart(jobDto.getScheduledStart());
        job.setScheduledEnd(jobDto.getScheduledEnd());
        job.setStatus(JobStatus.valueOf(jobDto.getStatus()));
        job.setServiceType(LeadServiceType.valueOf(jobDto.getServiceLine()));
        job.setCreatedBy(createdBy);
        return new JobResponseDto(jobRepository.save(job));
    }

    @Override
    public List<JobResponseDto> getAllJobs(Authentication authentication) {
        return jobRepository.findAll().stream().map(JobResponseDto::new).toList();
    }

    @Override
    public JobResponseDto getJobById(UUID jobId, Authentication authentication) {
        Optional<Job> jobOptional = jobRepository.findById(jobId);
        if (jobOptional.isEmpty()) {
            throw new AfriException("Job not found");
        }
        return new JobResponseDto(jobOptional.get());
    }

    @Override
    public JobResponseDto updateJobById(UUID jobId, JobDto jobDto, Authentication authentication) {
        Optional<Job> jobOptional = jobRepository.findById(jobId);
        if (jobOptional.isEmpty()) {
            throw new AfriException("Job not found");
        }
        Job job = jobOptional.get();
        job.setJobType(JobType.valueOf(jobDto.getJobType()));
        job.setScheduledStart(jobDto.getScheduledStart());
        job.setScheduledEnd(jobDto.getScheduledEnd());
        job.setStatus(JobStatus.valueOf(jobDto.getStatus()));
        job.setCrew(crewRepository.findById(UUID.fromString(jobDto.getCrewId())).orElseThrow(() -> new AfriException("Crew not found")));
        job.setSite(siteRepository.findById(UUID.fromString(jobDto.getSiteId())).orElseThrow(() -> new AfriException("Site not found")));
        job.setContract(contractRepository.findById(UUID.fromString(jobDto.getContractId())).orElseThrow(() -> new AfriException("Contract not found")));
        job.setSubscription(subscriptionRepository.findById(UUID.fromString(jobDto.getSubscriptionId())).orElseThrow(() -> new AfriException("Subscription not found")));
        job.setServiceType(LeadServiceType.valueOf(jobDto.getServiceLine()));
        job.setUpdatedBy(UUID.fromString(authentication.getName()));
        job.setUpdatedAt(LocalDateTime.now());
        return new JobResponseDto(jobRepository.save(job));
    }

}
