package at.feedback_service.service;

import at.feedback_service.dto.CreateFeedbackRequest;
import at.feedback_service.dto.FeedbackResponse;
import at.feedback_service.dto.RatedFeedbackResponse;
import at.feedback_service.dto.SortBy;

import java.util.List;

public interface FeedbackService {

    FeedbackResponse createFeedback(CreateFeedbackRequest request, String username);

    FeedbackResponse getFeedback(Long feedbackId);

    List<FeedbackResponse> getFeedbackOfUser(String username, SortBy sort, int from, int size);

    RatedFeedbackResponse getRatedFeedbacksForCourse(Long courseId, SortBy sort, int from, int size);
}
