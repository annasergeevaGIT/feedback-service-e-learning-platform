package at.feedback_service.service;

import at.feedback_service.dto.CreateFeedbackRequest;
import at.feedback_service.dto.FeedbackResponse;
import at.feedback_service.dto.RatedFeedbackResponse;
import at.feedback_service.dto.SortBy;
import at.feedback_service.exception.FeedbackServiceException;
import at.feedback_service.mapper.FeedbackMapper;
import at.feedback_service.model.Feedback;
import at.feedback_service.repoository.FeedbackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService{

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;
    private final RatingService ratingService;

    @Transactional
    @Override
    public FeedbackResponse createFeedback(CreateFeedbackRequest request, String username) {
        try {
            var feedback = feedbackRepository.save(feedbackMapper.toDomain(request, username));
            saveRating(request);
            return feedbackMapper.toFeedbackResponse(feedback);
        } catch (DataIntegrityViolationException ex) {
            var msg =
                    "Failed to create Feedback to course with id %d by user with name: %s, because the user already placed Feedback to that course."
                            .formatted(request.getCourseId(), username);
            throw new FeedbackServiceException(msg, HttpStatus.CONFLICT);
        }
    }

    @Override
    public FeedbackResponse getFeedback(Long feedbackId) {
        return feedbackRepository.findById(feedbackId)
                .map(feedbackMapper::toFeedbackResponse)
                .orElseThrow(() -> {
                    var msg = "Feedback with id=%d not found.".formatted(feedbackId);
                    return new FeedbackServiceException(msg, HttpStatus.NOT_FOUND);
                });
    }

    @Override
    public List<FeedbackResponse> getFeedbackOfUser(String username, SortBy sort, int from, int size) {
        var pageable = getPageable(sort, from, size);
        List<Feedback> feedbacks = feedbackRepository.findAllByCreatedBy(username, pageable);
        return feedbackMapper.toFeedbackResponseList(feedbacks);
    }

    @Override
    public RatedFeedbackResponse getRatedFeedbacksForCourse(Long courseId, SortBy sort, int from, int size) {
        var pageable = getPageable(sort, from, size);
        var feedbacks = feedbackMapper
                .toFeedbackResponseList(feedbackRepository.findAllByCourseId(courseId, pageable));
        var ratingInfo = ratingService.getRatingOfCourse(courseId);

        return RatedFeedbackResponse.builder()
                .feedbacks(feedbacks)
                .courseRating(ratingInfo)
                .build();
    }

    private Pageable getPageable(SortBy sort, int from, int size) {
        return PageRequest.of(from, size)
                .withSort(sort.getSort());
    }

    private void saveRating(CreateFeedbackRequest request) {
        ratingService.saveRating(request.getCourseId(), request.getRate());
    }
}
