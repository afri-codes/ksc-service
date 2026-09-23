package ksc.go.tz.job.dto;


import ksc.go.tz.job.entities.StaffRating;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class StaffRatingResponseDto {

    private String staffRatingId;

    private String feedbackId;

    private FeedbackResponseDto feedback;

    private String staff;

    private Integer rating;

    private String comment;


    public StaffRatingResponseDto(StaffRating staffRating) {
        this.staffRatingId = staffRating.getId().toString();
        this.feedbackId = staffRating.getFeedback().getId().toString();
        this.feedback = new FeedbackResponseDto(staffRating.getFeedback());
        this.staff = staffRating.getStaff();
        this.rating = staffRating.getRating();
        this.comment = staffRating.getComment();
    }
}
