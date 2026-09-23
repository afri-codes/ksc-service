package ksc.go.tz.job.dto;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class FeedbackDto {


    private String jobId;

    private String submittedBy;

    private Integer serviceRating;

   private String comment;

    private String disputeStatus;

}
