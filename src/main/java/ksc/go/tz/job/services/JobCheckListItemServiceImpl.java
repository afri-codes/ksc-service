package ksc.go.tz.job.services;

import ksc.go.tz.job.dto.JobChecklistItemDto;
import ksc.go.tz.job.dto.JobChecklistItemResponseDto;
import ksc.go.tz.job.entities.JobCheckList;
import ksc.go.tz.job.entities.JobChecklistItem;
import ksc.go.tz.job.repository.JobCheckListItemRepository;
import ksc.go.tz.job.repository.JobCheckListRepository;
import ksc.go.tz.job.repository.JobRepository;
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
public class JobCheckListItemServiceImpl implements JobCheckListItemService {
    private final JobRepository jobRepository;
    private final JobCheckListItemRepository jobCheckListItemRepository;
    private final JobCheckListRepository jobCheckListRepository;

    @Override
    public JobChecklistItemResponseDto createChecklistItemForChecklist(JobChecklistItemDto jobChecklistItemDto, Authentication authentication) {

        Optional<JobCheckList> optionalJobCheckList = jobCheckListRepository.findById(UUID.fromString(jobChecklistItemDto.getChecklistId()));
        if (optionalJobCheckList.isEmpty()) {
            throw new RuntimeException("Checklist not found");
        }
        JobChecklistItem jobChecklistItem = new JobChecklistItem();
        jobChecklistItem.setChecklist(optionalJobCheckList.get());
        jobChecklistItem.setTaskLabel(jobChecklistItemDto.getTaskLabel());
        jobChecklistItem.setNotes(jobChecklistItemDto.getNotes());
        jobChecklistItem.setComplete(false);
        jobChecklistItem.setCreatedBy(UUID.fromString(authentication.getName()));
        jobChecklistItem.setCreatedAt(java.time.LocalDateTime.now());
        return new JobChecklistItemResponseDto(jobCheckListItemRepository.save(jobChecklistItem));

    }

    @Override
    public List<JobChecklistItemResponseDto> getAllChecklistItemsForChecklist(Authentication authentication) {

        return jobCheckListItemRepository.findAll().stream().map(JobChecklistItemResponseDto::new).toList();
    }

    @Override
    public JobChecklistItemResponseDto getChecklistItemById(String checklistItemId, Authentication authentication) {
        Optional<JobChecklistItem> optionalJobChecklistItem = jobCheckListItemRepository.findById(UUID.fromString(checklistItemId));
        if (optionalJobChecklistItem.isEmpty()) {
            throw new RuntimeException("Checklist item not found");
        }
        return new JobChecklistItemResponseDto(optionalJobChecklistItem.get());
    }

    @Override
    public JobChecklistItemResponseDto updateChecklistItemById(String checklistItemId, JobChecklistItemDto jobChecklistItemDto, Authentication authentication) {
        Optional<JobChecklistItem> optionalJobChecklistItem = jobCheckListItemRepository.findById(UUID.fromString(checklistItemId));
        if (optionalJobChecklistItem.isEmpty()) {
            throw new RuntimeException("Checklist item not found");
        }
        JobChecklistItem jobChecklistItem = optionalJobChecklistItem.get();
        jobChecklistItem.setTaskLabel(jobChecklistItemDto.getTaskLabel());
        jobChecklistItem.setNotes(jobChecklistItemDto.getNotes());
        jobChecklistItem.setUpdatedAt(LocalDateTime.now());
        jobChecklistItem.setUpdatedBy(UUID.fromString(authentication.getName()));
        return new JobChecklistItemResponseDto(jobCheckListItemRepository.save(jobChecklistItem));
    }

    @Override
    public JobChecklistItemResponseDto deleteChecklistItemById(String checklistItemId, Authentication authentication) {
        Optional<JobChecklistItem> optionalJobChecklistItem = jobCheckListItemRepository.findById(UUID.fromString(checklistItemId));
        if (optionalJobChecklistItem.isEmpty()) {
            throw new RuntimeException("Checklist item not found");
        }
        JobChecklistItem jobChecklistItem = optionalJobChecklistItem.get();
        jobChecklistItem.setDeleted(true);
        jobChecklistItem.setDeletedAt(LocalDateTime.now());
        return new JobChecklistItemResponseDto(jobCheckListItemRepository.save(jobChecklistItem));
    }
}
