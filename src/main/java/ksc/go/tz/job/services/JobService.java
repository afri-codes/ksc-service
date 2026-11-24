package ksc.go.tz.job.services;


import ksc.go.tz.contractAndSubscriptions.dto.SubscriptionResponseDto;
import ksc.go.tz.job.dto.JobDto;
import ksc.go.tz.job.dto.JobResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface JobService {

    JobResponseDto createJob(JobDto jobDto, UUID createdBy);

    List<JobResponseDto> getAllJobs(Authentication authentication);

    JobResponseDto getJobById(UUID jobId, Authentication authentication);

    JobResponseDto updateJobById(UUID jobId, JobDto jobDto, Authentication authentication);
}
