package ksc.go.tz.job.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.enums.DisputeStatus;
import ksc.go.tz.job.dto.FeedbackDto;
import ksc.go.tz.job.dto.FeedbackResponseDto;
import ksc.go.tz.job.entities.Feedback;
import ksc.go.tz.job.entities.Job;
import ksc.go.tz.job.repository.FeedbackRepository;
import ksc.go.tz.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedBackServiceImpl implements FeedBackService {
    private final JobRepository jobRepository;
    private final FeedbackRepository feedbackRepository;


    @Override
    public FeedbackResponseDto addFeedBack(FeedbackDto feedbackDto, Authentication authentication) {

        Optional<Job> optionalJob = jobRepository.findById(UUID.fromString(feedbackDto.getJobId()));
        if (optionalJob.isEmpty()) {
            throw new AfriException("Job not found");
        }
        Feedback feedback = new Feedback();
        feedback.setJob(optionalJob.get());
        feedback.setComment(feedbackDto.getComment());
        feedback.setServiceRating(feedbackDto.getServiceRating());
        feedback.setCreatedBy(UUID.fromString(authentication.getName()));
        feedback.setDisputeStatus(DisputeStatus.valueOf(feedbackDto.getDisputeStatus()));
        feedbackRepository.save(feedback);
        return new FeedbackResponseDto( feedbackRepository.save(feedback));
    }

    @Override
    public List<FeedbackResponseDto> getAllFeedback() {
        return feedbackRepository.findAll().stream().map(FeedbackResponseDto::new).toList();
    }

    @Override
    public FeedbackResponseDto getFeedbackById(String feedbackId, Authentication authentication) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(UUID.fromString(feedbackId));
        if (optionalFeedback.isEmpty()) {
            throw new AfriException("Feedback not found");
        }
        return new FeedbackResponseDto(optionalFeedback.get());
    }

    @Override
    public FeedbackResponseDto updateFeedback(String feedbackId, FeedbackDto feedbackDto, Authentication authentication) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(UUID.fromString(feedbackId));
        if (optionalFeedback.isEmpty()) {
            throw new AfriException("Feedback not found");
        }
        Optional<Job> optionalJob = jobRepository.findById(UUID.fromString(feedbackDto.getJobId()));
        if (optionalJob.isEmpty()) {
            throw new AfriException("Job not found");
        }
        Feedback feedback = optionalFeedback.get();
        feedback.setComment(feedbackDto.getComment());
        feedback.setServiceRating(feedbackDto.getServiceRating());
        feedback.setJob(optionalJob.get());
        feedback.setDisputeStatus(DisputeStatus.valueOf(feedbackDto.getDisputeStatus()));
        feedback.setUpdatedBy(UUID.fromString(authentication.getName()));
        return new FeedbackResponseDto( feedbackRepository.save(feedback));
    }

    @Override
    public FeedbackResponseDto deleteFeedback(String feedbackId, Authentication authentication) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(UUID.fromString(feedbackId));
        if (optionalFeedback.isEmpty()) {
            throw new AfriException("Feedback not found");
        }
       Feedback feedback = optionalFeedback.get();
        feedback.setDeleted(true);
        feedback.setDeletedAt(LocalDateTime.now());
        return null;
    }


    @Override
    public List<FeedbackResponseDto> getFeedbackByJobId(String jobId, Authentication authentication) {
        UUID jobUuid;
        try {
            jobUuid = UUID.fromString(jobId);
        } catch (IllegalArgumentException e) {
            throw new AfriException("Invalid job ID");
        }

        Job job = jobRepository.findById(jobUuid).orElseThrow(() -> new AfriException("Job not found"));

        return feedbackRepository.findByJob(job).stream().map(FeedbackResponseDto::new).toList();
    }
}
