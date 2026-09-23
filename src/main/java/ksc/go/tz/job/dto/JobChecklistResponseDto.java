package ksc.go.tz.job.dto;

import ksc.go.tz.job.entities.JobChecklist;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobChecklistResponseDto {

    private String jobId;

    private JobResponseDto job;

    private String templateName;

    public JobChecklistResponseDto(JobChecklist jobChecklist) {
      this.jobId = jobChecklist.getId().toString();
      this.job = new JobResponseDto(jobChecklist.getJob());
      this.templateName = jobChecklist.getTemplateName();
    }

}
