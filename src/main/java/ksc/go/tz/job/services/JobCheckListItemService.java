package ksc.go.tz.job.services;


import ksc.go.tz.job.dto.JobChecklistItemDto;
import ksc.go.tz.job.dto.JobChecklistItemResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface JobCheckListItemService {

    JobChecklistItemResponseDto createChecklistItemForChecklist(JobChecklistItemDto jobChecklistItemDto, Authentication authentication);

    List<JobChecklistItemResponseDto> getAllChecklistItemsForChecklist(Authentication authentication);

    JobChecklistItemResponseDto getChecklistItemById(String checklistItemId, Authentication authentication);

    JobChecklistItemResponseDto updateChecklistItemById(String checklistItemId, JobChecklistItemDto jobChecklistItemDto, Authentication authentication);

    JobChecklistItemResponseDto deleteChecklistItemById(String checklistItemId, Authentication authentication);
}