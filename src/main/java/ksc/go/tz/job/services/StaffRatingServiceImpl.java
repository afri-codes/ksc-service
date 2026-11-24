package ksc.go.tz.job.services;

import afriUtils.responses.AfriException;
import ksc.go.tz.job.dto.StaffRatingResponseDto;
import ksc.go.tz.job.entities.Feedback;
import ksc.go.tz.job.entities.StaffRating;
import ksc.go.tz.job.repository.FeedbackRepository;
import ksc.go.tz.job.repository.StaffRatingRepository;
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
public class StaffRatingServiceImpl implements StaffRatingService {

    private final FeedbackRepository feedbackRepository;
    private final StaffRatingRepository staffRatingRepository;


    @Override
    public StaffRatingResponseDto createStaffRating(StaffRatingResponseDto staffRatingDto, Authentication authentication) {
        Optional<Feedback> optionalFeedback = feedbackRepository.findById(UUID.fromString(staffRatingDto.getFeedbackId()));
        if (optionalFeedback.isEmpty()) {
            throw new AfriException("Feedback not found");
        }
        StaffRating staffRating = new StaffRating();
        staffRating.setFeedback(optionalFeedback.get());
        staffRating.setRating(staffRatingDto.getRating());
        staffRating.setComment(staffRatingDto.getComment());
        staffRating.setCreatedBy(UUID.fromString(authentication.getName()));
        staffRating.setCreatedAt(LocalDateTime.now());

        return new StaffRatingResponseDto( staffRatingRepository.save(staffRating));
    }

    @Override
    public StaffRatingResponseDto getStaffRatingById(String ratingId) {
        Optional<StaffRating> optionalStaffRating = staffRatingRepository.findById(UUID.fromString(ratingId));
        if (optionalStaffRating.isEmpty()) {
            throw new AfriException("Staff rating not found");
        }
        return new StaffRatingResponseDto(optionalStaffRating.get());
    }

    @Override
    public List<StaffRatingResponseDto> getAllStaffRatings() {
        return staffRatingRepository.findAll().stream()
                .map(StaffRatingResponseDto::new)
                .toList();
    }

    @Override
    public List<StaffRatingResponseDto> getRatingsByStaffId(String staffId) {
        return List.of();
    }

    @Override
    public StaffRatingResponseDto getRatingSummaryByStaffId(String staffId) {
        return null;
    }

    @Override
    public StaffRatingResponseDto updateStaffRating(String ratingId, StaffRatingResponseDto staffRatingDto, Authentication authentication) {
        Optional<StaffRating> optionalStaffRating = staffRatingRepository.findById(UUID.fromString(ratingId));
        if (optionalStaffRating.isEmpty()) {
            throw new AfriException("Staff rating not found");
        }
        StaffRating staffRating = optionalStaffRating.get();
        staffRating.setRating(staffRatingDto.getRating());
        staffRating.setComment(staffRatingDto.getComment());
        staffRating.setUpdatedBy(UUID.fromString(authentication.getName()));
        staffRating.setUpdatedAt(LocalDateTime.now());
        return new StaffRatingResponseDto(staffRatingRepository.save(staffRating));
    }

    @Override
    public StaffRatingResponseDto deleteStaffRating(String ratingId, Authentication authentication) {
        Optional<StaffRating> optionalStaffRating = staffRatingRepository.findById(UUID.fromString(ratingId));
        if (optionalStaffRating.isEmpty()) {
            throw new AfriException("Staff rating not found");
        }
        StaffRating staffRating = optionalStaffRating.get();
        staffRating.setDeletedAt(LocalDateTime.now());
        staffRating.setDeleted(true);
        return new StaffRatingResponseDto(staffRatingRepository.save(staffRating));

    }

    @Override
    public StaffRatingResponseDto getStaffRatingSummaryByJobId(String jobId) {
        return null;
    }

    @Override
    public List<StaffRatingResponseDto> getStaffRatingsByJobId(String jobId) {
        return List.of();
    }
}
