package ksc.go.tz.job.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobCheckListDto {

    @NotNull(message = "Job ID must be provided")
    private String jobId;


    @NotNull(message = "Template name must be provided")
    private String templateName;

}
