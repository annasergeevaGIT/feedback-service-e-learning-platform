package at.feedback_service.impl;

import at.feedback_service.BaseIntegrationTest;
import at.feedback_service.dto.GetRatingsRequest;
import at.feedback_service.dto.RatingsResponse;
import at.feedback_service.model.CourseRatingInfo;
import at.feedback_service.model.Rating;
import at.feedback_service.repoository.RatingRepository;
import at.feedback_service.service.RatingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static at.feedback_service.model.Rating.newRating;
import static at.feedback_service.testutil.TestConstants.*;
import static at.feedback_service.testutil.TestConstants.COURSE_TWO;
import static at.feedback_service.testutil.TestData.*;
import static at.feedback_service.testutil.TestData.ratingCourseOne;
import static at.feedback_service.testutil.TestUtils.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class RatingServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private RatingService ratingService;
    @Autowired
    private RatingRepository ratingRepository;


    @Test
    void saveRating_savesNewRatingAndUpdatesItCorrectly_whenMultipleConcurrentRequestsSaveDifferentRatingsToSameCourse() throws Exception {
        Rating nonExistentRating = newRating(COURSE_UNKNOWN);
        concurrentlyIncrementEachRating20Times_incrementsRatingsCorrectly(nonExistentRating);
    }

    //handles concurrent calls from multiple threads, updating different ratings for a course that already has user feedbacks
    @Test
    void saveRating_updatesRatingCorrectly_whenMultipleConcurrentRequestsSaveDifferentRatingsToSameCourse() throws Exception {
        Rating existentRating = ratingCourseOne();
        concurrentlyIncrementEachRating20Times_incrementsRatingsCorrectly(existentRating);
    }

    private void concurrentlyIncrementEachRating20Times_incrementsRatingsCorrectly(Rating expectedRating) throws InterruptedException {
        Long courseId = expectedRating.getCourseId();
        ExecutorService executor = Executors.newFixedThreadPool(12);
        List<Callable<Void>> workers = new ArrayList<>();
        int numWorkers = 100;
        for (int i = 0; i < numWorkers; i++) {
            final int rate = (i % 5 == 0) ? 5 : i % 5;
            workers.add(() -> {
                ratingService.saveRating(courseId, rate);
                        return null;
                    }
            );
        }
        executor.invokeAll(workers);
        executor.shutdown();

        incrementExpectedRating(expectedRating, 20, 20, 20, 20, 20);

        Rating rating = ratingRepository.findByCourseId(courseId).get();
        assertRatesEqual(rating, expectedRating);
    }
    // concurrent calls from multiple threads, updating a single rating for a course that already has user feedbacks
    @Test
    void saveRating_updatesRatingCorrectly_whenMultipleConcurrentRequestsSaveSameRatingToSameCourse() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(12);
        List<Callable<Void>> workers = new ArrayList<>();
        int numWorkers = 100;
        for (int i = 0; i < numWorkers; i++) {
            workers.add(() -> {
                ratingService.saveRating(COURSE_ONE, 4);
                        return null;
                    }
            );
        }

        executor.invokeAll(workers);
        executor.shutdown();

        Rating expectedRating = ratingCourseOne();
        Rating rating = ratingRepository.findByCourseId(COURSE_ONE).get();
        assertThat(rating.getRateFour()).isEqualTo(expectedRating.getRateFour() + numWorkers);
    }

    @Test
    void getRatingsOfCourses_returnsCorrectRatingsWhenSomeCoursesHaveRatings() {
        Rating ratingCourseOne = ratingCourseOne();
        Rating ratingCourseTwo = ratingCourseTwo();
        var courseIds = Set.of(COURSE_ONE, COURSE_TWO, COURSE_UNKNOWN);

        incrementRatingsForCourseId(ratingCourseOne, 0, 0, 0, 0, 10);
        incrementRatingsForCourseId(ratingCourseTwo, 0, 0, 0, 10, 0);

        var request = GetRatingsRequest.builder().courseIds(courseIds).build();
        List<CourseRatingInfo> courseRatingInfos = ratingService.getRatingOfCourses(request).getCourseRatings();
        courseRatingInfos.sort(Comparator.comparing(CourseRatingInfo::getCourseId));

        compareCourseInfo(ratingCourseOne, courseRatingInfos.get(0));
        compareCourseInfo(ratingCourseTwo, courseRatingInfos.get(1));
        compareDefaultCourseInfo(COURSE_UNKNOWN, courseRatingInfos.get(2));
    }

    @Test
    void getRatingsOfCourses_returnsCorrectRatingsWhenAllCoursesHaveRatings() {
        Rating ratingCourseOne = ratingCourseOne();
        Rating ratingCourseTwo = ratingCourseTwo();
        Rating ratingCourseThree = newRating(COURSE_THREE);

        var courseIds = Set.of(COURSE_ONE, COURSE_TWO, COURSE_THREE);
        incrementRatingsForCourseId(ratingCourseOne, 0, 0, 0, 0, 10);
        incrementRatingsForCourseId(ratingCourseTwo, 0, 0, 0, 10, 0);
        incrementRatingsForCourseId(ratingCourseThree, 10, 0, 0, 0, 0);

        var request = GetRatingsRequest.builder().courseIds(courseIds).build();
        RatingsResponse ratingsOfCourses = ratingService.getRatingOfCourses(request);
        compareCourseInfos(List.of(ratingCourseOne, ratingCourseTwo, ratingCourseThree), ratingsOfCourses.getCourseRatings());
    }

    @Test
    void getRatingsOfCourses_returnsDefaultRatingsWhenCoursesHaveNoRatings() {
        var noRatingCourses = Set.of(1000L, 2000L, 3000L);
        var request = GetRatingsRequest.builder().courseIds(noRatingCourses).build();
        RatingsResponse ratingsOfCourses = ratingService.getRatingOfCourses(request);
        for (var courseRatingInfo : ratingsOfCourses.getCourseRatings()) {
            compareDefaultCourseInfo(courseRatingInfo.getCourseId(), courseRatingInfo);
        }
    }

    @Test
    void getRatingOfCourse_returnsCorrectRatingInfo_whenCourseHasRating() {
        Rating ratingCourseOne = ratingCourseOne();
        incrementRatingsForCourseId(ratingCourseOne, 1, 1, 1, 1, 1);
        CourseRatingInfo rating = ratingService.getRatingOfCourse(COURSE_ONE);
        compareCourseInfo(ratingCourseOne, rating);
    }

    @Test
    void getRatingOfCourse_returnsDefaultRatingInfo_whenCourseHadNoRatingBefore() {
        CourseRatingInfo rating = ratingService.getRatingOfCourse(COURSE_UNKNOWN);
        compareDefaultCourseInfo(COURSE_UNKNOWN, rating);
    }

    @Test
    void saveRating_createsNewRatingAndUpdatesItCorrectly_whenCourseHadNoRatingBefore() {
        ratingService.saveRating(COURSE_UNKNOWN, 5);
        Rating rating = ratingRepository.findByCourseId(COURSE_UNKNOWN).get();
        assertThat(rating.getRateFive()).isEqualTo(1);
    }

    @Test
    void saveRating_updatesRating_whenCourseHasRatingAlready() {
        ratingService.saveRating(COURSE_ONE, 5);
        Rating rating = ratingRepository.findByCourseId(COURSE_ONE).get();
        assertThat(rating.getRateFive()).isEqualTo(2);
    }
}