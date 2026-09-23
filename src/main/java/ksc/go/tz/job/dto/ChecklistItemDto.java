package ksc.go.tz.job.dto;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class ChecklistItemDto  {


    private String checklistId;

    private String taskLabel;

    private Boolean complete;

    private String notes;

}
