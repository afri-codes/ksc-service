package ksc.go.tz.job.controllers;

import afriSecurity.annotations.Permission;
import afriSecurity.security.AuthDetailsExtractor;
import afriUtils.responses.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import ksc.go.tz.contractAndSubscriptions.services.ContractService;
import ksc.go.tz.job.dto.FeedbackDto;
import ksc.go.tz.job.dto.FeedbackResponseDto;
import ksc.go.tz.job.services.FeedBackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedBackService feedBackService;
    private final AuthDetailsExtractor authDetailsExtractor;
    private final ApiResponseUtil apiResponseUtil;

   // POST /api/v1/feedback
    @Operation(summary = "Save or add new feedback")
    @Permission(name = "Add Feedback", code = "ADD_FEEDBACK")
    @PostMapping("/feedbacks")
    public ApiResponseUtil.ApiResponseEntity<FeedbackResponseDto> addFeedback(@RequestBody @Valid FeedbackDto feedbackDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, feedBackService.addFeedBack(feedbackDto, authentication), "Feedback added successfully", null);
    }

    // GET /api/v1/feedback
    @Operation(summary = "Get all feedback")
    @Permission(name = "View All Feedback", code = "VIEW_ALL_FEEDBACK")
    @GetMapping("/feedbacks")
    public ApiResponseUtil.ApiResponseEntity<List<FeedbackResponseDto>> getAllFeedback(Authentication authentication) {
        return apiResponseUtil.getResponse(feedBackService.getAllFeedback());
    }

    // GET /api/v1/feedback/{id}
    @Operation(summary = "Get feedback by ID")
    @Permission(name = "View Feedback By ID", code = "VIEW_FEEDBACK_BY_ID")
    @GetMapping("/feedbacks/{feedbackId}")
    public ApiResponseUtil.ApiResponseEntity<FeedbackResponseDto> getFeedbackById(@PathVariable String feedbackId, Authentication authentication) {
        return apiResponseUtil.getResponse(feedBackService.getFeedbackById(feedbackId, authentication));
    }

    // PUT /api/v1/feedback/{id}
    @Operation(summary = "Update feedback")
    @Permission(name = "Update Feedback", code = "UPDATE_FEEDBACK")
    @PutMapping("/feedbacks/{feedbackId}")
    public ApiResponseUtil.ApiResponseEntity<FeedbackResponseDto> updateFeedback(@PathVariable String feedbackId, @RequestBody FeedbackDto feedbackDto, Authentication authentication) {
        return apiResponseUtil.getResponse(null, feedBackService.updateFeedback(feedbackId, feedbackDto, authentication), "Feedback updated successfully", null);
    }

    // DELETE /api/v1/feedback/{id}
    @Operation(summary = "Delete feedback")
    @Permission(name = "Delete Feedback", code = "DELETE_FEEDBACK")
    @DeleteMapping("/feedbacks/{feedbackId}")
    public ApiResponseUtil.ApiResponseEntity<FeedbackResponseDto> deleteFeedback(@PathVariable String feedbackId, Authentication authentication) {
        return apiResponseUtil.getResponse(null, feedBackService.deleteFeedback(feedbackId, authentication), "Feedback deleted successfully", null);
    }

    // GET /api/v1/jobs/{jobId}/feedback
    @Operation(summary = "Get feedback by Job ID")
    @Permission(name = "View Feedback By Job ID", code = "VIEW_FEEDBACK_BY_JOB_ID")
    @GetMapping("/jobs/{jobId}/feedbacks")
    public ApiResponseUtil.ApiResponseEntity<List<FeedbackResponseDto>> getFeedbackByJobId(@PathVariable String jobId, Authentication authentication) {
        return apiResponseUtil.getResponse(feedBackService.getFeedbackByJobId(jobId, authentication));
    }

    // GET /api/v1/users/{userId}/feedback

}
