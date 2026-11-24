package ksc.go.tz.job.dto;

import ksc.go.tz.job.entities.JobCheckList;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class JobCheckListResponseDto {

    private String jobId;

    private JobResponseDto job;

    private String templateName;

    public JobCheckListResponseDto(JobCheckList jobChecklist) {
      this.jobId = jobChecklist.getId().toString();
      this.job = new JobResponseDto(jobChecklist.getJob());
      this.templateName = jobChecklist.getTemplateName();
    }

}
