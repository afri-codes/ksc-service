package ksc.go.tz.job.dto;


import ksc.go.tz.job.entities.JobChecklistItem;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobChecklistItemResponseDto {


    private String checklistId;

    private String taskLabel;

    private Boolean complete;

    private String notes;

    public JobChecklistItemResponseDto(JobChecklistItem checklistItem) {
        this.checklistId = checklistItem.getId().toString();
        this.taskLabel = checklistItem.getTaskLabel();
        this.complete = checklistItem.getComplete();
        this.notes = checklistItem.getNotes();
    }

}
