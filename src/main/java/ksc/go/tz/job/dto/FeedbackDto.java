package ksc.go.tz.job.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FeedbackDto {

    @NotNull(message = "Job ID must be provided")
    private String jobId;

    @NotNull(message = "Submitted by must be provided")
    private String submittedBy;

    @NotNull(message = "Rating must be provided")
    private Integer serviceRating;

    @NotNull(message = "Comment must be provided")
   private String comment;

    private String disputeStatus;

}
