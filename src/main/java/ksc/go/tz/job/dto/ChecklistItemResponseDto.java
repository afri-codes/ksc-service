package ksc.go.tz.job.dto;


import ksc.go.tz.job.entities.ChecklistItem;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ChecklistItemResponseDto {


    private String checklistId;

    private String taskLabel;

    private Boolean complete;

    private String notes;

    public ChecklistItemResponseDto(ChecklistItem checklistItem) {
        this.checklistId = checklistItem.getId().toString();
        this.taskLabel = checklistItem.getTaskLabel();
        this.complete = checklistItem.getComplete();
        this.notes = checklistItem.getNotes();
    }

}
