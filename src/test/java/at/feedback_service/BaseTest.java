package at.feedback_service;
import at.feedback_service.model.CourseRatingInfo;
import at.feedback_service.model.Rating;
import at.feedback_service.repoository.RatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import static at.feedback_service.testutil.TestUtils.incrementExpectedRating;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@ActiveProfiles("test")
@SqlGroup(
        {
                @Sql(
                        scripts = "classpath:insert-data.sql",
                        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
                ),
                @Sql(
                        scripts = "classpath:delete-data.sql",
                        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
                )
        }
)
public abstract class BaseTest {
    @Autowired
    protected RatingRepository ratingRepository;

    protected void incrementRatingsForCourseId(Rating expected,
                                             int oneTimes,
                                             int twoTimes,
                                             int threeTimes,
                                             int fourTimes,
                                             int fiveTimes) {
        incrementActualRatingsForCourseId(expected.getCourseId(), oneTimes, twoTimes, threeTimes, fourTimes, fiveTimes);
        incrementExpectedRating(expected, oneTimes, twoTimes, threeTimes, fourTimes, fiveTimes);
    }

    protected void incrementActualRatingsForCourseId(Long courseId,
                                                     int oneTimes,
                                                     int twoTimes,
                                                     int threeTimes,
                                                     int fourTimes,
                                                     int fiveTimes) {
        incrementRatingForCourseId(courseId, 1, oneTimes);
        incrementRatingForCourseId(courseId, 2, twoTimes);
        incrementRatingForCourseId(courseId, 3, threeTimes);
        incrementRatingForCourseId(courseId, 4, fourTimes);
        incrementRatingForCourseId(courseId, 5, fiveTimes);
    }

    private void incrementRatingForCourseId(Long courseId,
                                            Integer rating,
                                            int times) {
        for (int i = 0; i < times; i++) {
            ratingRepository.incrementRating(courseId, rating);
        }
    }

    protected void compareDefaultCourseInfo(Long noRatingCourse, CourseRatingInfo rating) {
        Float zero = 0.0f;
        assertThat(rating.getAvgStars()).isEqualTo(zero);
        assertThat(rating.getWilsonScore()).isEqualTo(zero);
        assertThat(rating.getCourseId()).isEqualTo(noRatingCourse);
    }
}