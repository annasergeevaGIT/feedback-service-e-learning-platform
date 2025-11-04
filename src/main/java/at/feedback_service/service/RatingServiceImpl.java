package at.feedback_service.service;

import at.feedback_service.dto.GetRatingsRequest;
import at.feedback_service.dto.RatingsResponse;
import at.feedback_service.model.CourseRatingInfo;
import at.feedback_service.repoository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService{

    private final RatingRepository repository;

    @Transactional
    @Override
    public void saveRating(Long courseId, Integer rate) {
        ensureRatingExists(courseId);
        repository.incrementRating(courseId, rate);

    }
    //do not throw an error if the course has no feedbacks. Instead, the response returns default values — wilsonScore = 0.0, avgStars = 0.0.
    @Override
    public CourseRatingInfo getRatingOfCourse(Long courseId) {
        return repository.findRatingInfoByCourseId(courseId)
                .orElse(defaultRating(courseId));
    }

    @Override
    public RatingsResponse getRatingOfCourses(GetRatingsRequest request) {
        var courseIdToRatings = repository
                .findRatingInfosByCourseIdIn(request.getCourseIds())
                .stream()
                .collect(Collectors.toMap(CourseRatingInfo::getCourseId, Function.identity()));

        List<CourseRatingInfo> result = request.getCourseIds().stream()
                .map(id -> courseIdToRatings.getOrDefault(id, defaultRating(id)))
                .collect(Collectors.toList());

        return RatingsResponse.builder()
                .courseRatings(result)
                .build();
    }

    private void ensureRatingExists(Long courseId) {
        repository.insertNoConflict(courseId);
    }

    private CourseRatingInfo defaultRating(Long courseId) {
        return CourseRatingInfo.builder()
                .courseId(courseId)
                .wilsonScore(0.0f)
                .avgStars(0.0f)
                .build();
    }
}
