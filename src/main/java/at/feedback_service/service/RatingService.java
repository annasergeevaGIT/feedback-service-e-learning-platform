package at.feedback_service.service;

import at.feedback_service.dto.GetRatingsRequest;
import at.feedback_service.dto.RatingsResponse;
import at.feedback_service.model.CourseRatingInfo;

public interface RatingService {

    void saveRating(Long courseId, Integer rate);

    CourseRatingInfo getRatingOfCourse(Long courseId);

    RatingsResponse getRatingOfCourses(GetRatingsRequest request);
}
