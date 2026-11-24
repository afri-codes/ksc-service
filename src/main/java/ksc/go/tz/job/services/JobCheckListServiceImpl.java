package ksc.go.tz.job.services;

import ksc.go.tz.job.dto.JobCheckListDto;
import ksc.go.tz.job.dto.JobCheckListResponseDto;
import ksc.go.tz.job.entities.Job;
import ksc.go.tz.job.entities.JobCheckList;
import ksc.go.tz.job.repository.JobCheckListRepository;
import ksc.go.tz.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobCheckListServiceImpl implements JobCheckListService {
    private final JobRepository jobRepository;
    private final JobCheckListRepository jobCheckListRepository;

    @Override
    public JobCheckListResponseDto createChecklistForJob(JobCheckListDto jobCheckListDto, Authentication authentication) {

        Optional<Job> optionalJob = jobRepository.findById(UUID.fromString(jobCheckListDto.getJobId()));
        if (optionalJob.isEmpty()) {
            throw new RuntimeException("Job not found");
        }
        JobCheckList jobCheckList = new JobCheckList();
        jobCheckList.setJob(optionalJob.get());
        jobCheckList.setTemplateName(jobCheckListDto.getTemplateName());
        jobCheckList.setCreatedBy(UUID.fromString(authentication.getName()));
        jobCheckList.setCreatedAt(java.time.LocalDateTime.now());
        return new JobCheckListResponseDto(jobCheckListRepository.save(jobCheckList));
    }

    @Override
    public List<JobCheckListResponseDto> getAllChecklistsForJob(Authentication authentication) {

        return jobCheckListRepository.findAll().stream().map(JobCheckListResponseDto::new).toList();
    }

    @Override
    public JobCheckListResponseDto getChecklistById(String checklistId, Authentication authentication) {
        Optional<JobCheckList> optionalJobCheckList = jobCheckListRepository.findById(UUID.fromString(checklistId));
        if (optionalJobCheckList.isEmpty()) {
            throw new RuntimeException("Checklist not found");
        }
        return new JobCheckListResponseDto(optionalJobCheckList.get());
    }

    @Override
    public JobCheckListResponseDto updateChecklistById(String checklistId, JobCheckListDto jobCheckListDto, Authentication authentication) {
        Optional<JobCheckList> optionalJobCheckList = jobCheckListRepository.findById(UUID.fromString(checklistId));
        if (optionalJobCheckList.isEmpty()) {
            throw new RuntimeException("Checklist not found");
        }
        JobCheckList jobCheckList = optionalJobCheckList.get();
        jobCheckList.setJob(optionalJobCheckList.get().getJob());
        jobCheckList.setTemplateName(jobCheckListDto.getTemplateName());
        jobCheckList.setUpdatedBy(UUID.fromString(authentication.getName()));
        jobCheckList.setUpdatedAt(java.time.LocalDateTime.now());
        return new JobCheckListResponseDto(jobCheckListRepository.save(jobCheckList));
    }

    @Override
    public JobCheckListResponseDto deleteChecklistById(String checklistId, Authentication authentication) {
        Optional<JobCheckList> optionalJobCheckList = jobCheckListRepository.findById(UUID.fromString(checklistId));
        if (optionalJobCheckList.isEmpty()) {
            throw new RuntimeException("Checklist not found");
        }
        JobCheckList jobCheckList = optionalJobCheckList.get();
        jobCheckList.setDeleted(true);
        jobCheckList.setDeletedAt(java.time.LocalDateTime.now());
        return new JobCheckListResponseDto(jobCheckListRepository.save(jobCheckList));
    }
}
