package ksc.go.tz.job.dto;

import ksc.go.tz.job.entities.Feedback;
import lombok.*;
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FeedbackResponseDto {

    private String jobId;

    private JobResponseDto job;

    private String submittedBy;

    private Integer serviceRating;

    private String comment;

    private String disputeStatus;

    public FeedbackResponseDto(Feedback feedback){
        this.jobId = feedback.getJob().getId().toString();
        this.job = new JobResponseDto(feedback.getJob());
        this.submittedBy = feedback.getSubmittedBy();
        this.serviceRating = feedback.getServiceRating();
        this.comment = feedback.getComment();
        this.disputeStatus = feedback.getDisputeStatus().toString();
    }

}
