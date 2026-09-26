package ksc.go.tz.job.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobChecklistItemDto {

    @NotNull(message = "Checklist ID must be provided")
    private String checklistId;

    @NotNull(message = "Task label must be provided")
    private String taskLabel;

    private Boolean complete;

    private String notes;

}
