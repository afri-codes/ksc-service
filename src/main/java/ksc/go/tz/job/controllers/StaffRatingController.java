package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import ksc.go.tz.job.dto.StaffRatingResponseDto;
import ksc.go.tz.job.services.StaffRatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StaffRatingController {
    private final StaffRatingService staffRatingService;
    private final ApiResponseUtil apiResponseUtil;

    // POST /api/v1/staff-ratings
    @Operation(summary = "Add a new staff rating")
    @Permission(name = "Add Staff Rating", code = "ADD_STAFF_RATING")
    @PostMapping("/staff-ratings")
    public ApiResponseUtil.ApiResponseEntity<StaffRatingResponseDto> addStaffRating(@RequestBody @Valid StaffRatingResponseDto staffRatingDto, Authentication authentication) {
        // Implementation for adding a new staff rating
        return apiResponseUtil.getResponse(null, staffRatingService.createStaffRating(staffRatingDto, authentication), "Staff rating added successfully", null);
    }

    // GET /api/v1/staff-ratings/{id}
    @Operation(summary = "Get a staff rating by ID")
    @Permission(name = "View Staff Rating By ID", code = "VIEW_STAFF_RATING_BY_ID")
    @GetMapping("/staff-ratings/{id}")
    public ApiResponseUtil.ApiResponseEntity<StaffRatingResponseDto> getStaffRatingById(@PathVariable("id") String ratingId) {
        return apiResponseUtil.getResponse(staffRatingService.getStaffRatingById(ratingId));
    }

    // GET /api/v1/staff-ratings
    @Operation(summary = "Get all staff ratings")
    @Permission(name = "View All Staff Ratings", code = "VIEW_ALL_STAFF_RATINGS")
    @GetMapping("/staff-ratings")
    public ApiResponseUtil.ApiResponseEntity<List<StaffRatingResponseDto>> getAllStaffRatings() {
        return apiResponseUtil.getResponse(staffRatingService.getAllStaffRatings());
    }

    // GET /api/v1/staff/{staffId}/ratings
    @Operation(summary = "Get all ratings for a specific staff member")
    @Permission(name = "View Staff Ratings", code = "VIEW_STAFF_RATINGS")
    @GetMapping("/staff/{staffId}/ratings")
    public ApiResponseUtil.ApiResponseEntity<List<StaffRatingResponseDto>> getRatingsByStaffId(@PathVariable("staffId") String staffId) {
        return apiResponseUtil.getResponse(staffRatingService.getRatingsByStaffId(staffId));
    }

    // GET /api/v1/staff/{staffId}/rating-summary
    @Operation(summary = "Get rating summary for a specific staff member")
    @Permission(name = "View Staff Rating Summary", code = "VIEW_STAFF_RATING_SUMMARY")
    @GetMapping("/staff/{staffId}/rating-summary")
    public ApiResponseUtil.ApiResponseEntity<StaffRatingResponseDto> getRatingSummaryByStaffId(@PathVariable("staffId") String staffId) {
        return apiResponseUtil.getResponse(staffRatingService.getRatingSummaryByStaffId(staffId));
    }

    // PUT /api/v1/staff-ratings/{id}
    @Operation(summary = "Update a staff rating by ID")
    @Permission(name = "Update Staff Rating", code = "UPDATE_STAFF_RATING")
    @PutMapping("/staff-ratings/{id}")
    public ApiResponseUtil.ApiResponseEntity<StaffRatingResponseDto> updateStaffRating(@PathVariable("id") String ratingId, @RequestBody @Valid StaffRatingResponseDto staffRatingDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, staffRatingService.updateStaffRating(ratingId, staffRatingDto, authentication), "Staff rating updated successfully", null);
    }

    // DELETE /api/v1/staff-ratings/{id}
    @Operation(summary = "Delete a staff rating by ID")
    @Permission(name = "Delete Staff Rating", code = "DELETE_STAFF_RATING")
    @DeleteMapping("/staff-ratings/{id}")
    public ApiResponseUtil.ApiResponseEntity<StaffRatingResponseDto> deleteStaffRating(@PathVariable("id") String ratingId, Authentication authentication) {

        return apiResponseUtil.getResponse(null,  staffRatingService.deleteStaffRating(ratingId,authentication), "Staff rating deleted successfully", null);
    }

    // GET /api/v1/jobs/{jobId}/staff-ratings
    @Operation(summary = "Get all staff ratings for a specific job")
    @Permission(name = "View Job Staff Ratings", code = "VIEW_JOB_STAFF_RATINGS")
    @GetMapping("/jobs/{jobId}/staff-ratings")
    public ApiResponseUtil.ApiResponseEntity<List<StaffRatingResponseDto>> getStaffRatingsByJobId(@PathVariable("jobId") String jobId) {
        return apiResponseUtil.getResponse(staffRatingService.getStaffRatingsByJobId(jobId));
    }

    // GET /api/v1/jobs/{jobId}/staff-ratings/summary
    @Operation(summary = "Get staff rating summary for a specific job")
    @Permission(name = "View Job Staff Rating Summary", code = "VIEW_JOB_STAFF_RATING_SUMMARY")
    @GetMapping("/jobs/{jobId}/staff-ratings/summary")
    public ApiResponseUtil.ApiResponseEntity<StaffRatingResponseDto> getStaffRatingSummaryByJobId(@PathVariable("jobId") String jobId) {
        return apiResponseUtil.getResponse(staffRatingService.getStaffRatingSummaryByJobId(jobId));
    }

}
