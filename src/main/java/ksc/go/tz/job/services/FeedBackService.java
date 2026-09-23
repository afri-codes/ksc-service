package ksc.go.tz.job.services;

import ksc.go.tz.job.dto.FeedbackDto;
import ksc.go.tz.job.dto.FeedbackResponseDto;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface FeedBackService {


    FeedbackResponseDto addFeedBack(FeedbackDto feedbackDto, Authentication authentication);

    List<FeedbackResponseDto> getAllFeedback();

    FeedbackResponseDto getFeedbackById(String feedbackId, Authentication authentication);

    FeedbackResponseDto updateFeedback(String feedbackId, FeedbackDto feedbackDto, Authentication authentication);

    FeedbackResponseDto deleteFeedback(String feedbackId, Authentication authentication);

    List<FeedbackResponseDto> getFeedbackByJobId(String jobId, Authentication authentication);
}
