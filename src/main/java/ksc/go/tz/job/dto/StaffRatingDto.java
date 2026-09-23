package ksc.go.tz.job.dto;


import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class StaffRatingDto  {

    private String feedbackId;

    private String staff;

    private Integer rating;

    private String comment;
}
