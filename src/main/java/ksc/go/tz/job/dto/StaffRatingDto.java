package ksc.go.tz.job.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class StaffRatingDto  {


    @NotNull(message = "Feedback ID must be provided")
    private String feedbackId;

    private String staff;

    private Integer rating;

    private String comment;
}
