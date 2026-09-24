package ksc.go.tz.job.services;


import ksc.go.tz.job.dto.JobDto;
import ksc.go.tz.job.dto.JobResponseDto;
import ksc.go.tz.job.dto.StaffRatingResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface StaffRatingService {


    StaffRatingResponseDto createStaffRating(StaffRatingResponseDto staffRatingDto, Authentication authentication);

    StaffRatingResponseDto getStaffRatingById(String ratingId);

    List<StaffRatingResponseDto> getAllStaffRatings();

    List<StaffRatingResponseDto> getRatingsByStaffId(String staffId);

    StaffRatingResponseDto getRatingSummaryByStaffId(String staffId);

    StaffRatingResponseDto updateStaffRating(String ratingId, StaffRatingResponseDto staffRatingDto, Authentication authentication);

    StaffRatingResponseDto deleteStaffRating(String ratingId, Authentication authentication);

    StaffRatingResponseDto getStaffRatingSummaryByJobId(String jobId);

    List<StaffRatingResponseDto> getStaffRatingsByJobId(String jobId);
}