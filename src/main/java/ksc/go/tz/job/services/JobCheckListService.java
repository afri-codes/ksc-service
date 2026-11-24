package ksc.go.tz.job.services;


import ksc.go.tz.job.dto.JobCheckListDto;
import ksc.go.tz.job.dto.JobCheckListResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface JobCheckListService {

    JobCheckListResponseDto createChecklistForJob(JobCheckListDto jobCheckListDto, Authentication authentication);

    List<JobCheckListResponseDto> getAllChecklistsForJob(Authentication authentication);

    JobCheckListResponseDto getChecklistById(String checklistId, Authentication authentication);

    JobCheckListResponseDto updateChecklistById(String checklistId, JobCheckListDto jobCheckListDto, Authentication authentication);

    JobCheckListResponseDto deleteChecklistById(String checklistId, Authentication authentication);
}