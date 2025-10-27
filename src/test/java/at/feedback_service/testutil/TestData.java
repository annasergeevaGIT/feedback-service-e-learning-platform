package at.feedback_service.testutil;


import at.feedback_service.dto.CreateFeedbackRequest;
import at.feedback_service.model.Rating;

import java.util.List;

import static at.feedback_service.testutil.TestConstants.*;

public class TestData {
    public static Rating ratingCourseOne() {
        return Rating.newRating(COURSE_ONE, 0, 0, 0, 0, 1);
    }

    public static Rating ratingCourseTwo() {
        return Rating.newRating(COURSE_TWO);
    }

    public static Rating ratingCourseFour() {
        return Rating.newRating(COURSE_FOUR, 0, 0, 0, 0, 1);
    }

    public static Rating ratingCourseFive() {
        return Rating.newRating(COURSE_FIVE, 0, 0, 0, 1, 0);
    }

    public static Rating ratingCourseSix() {
        return Rating.newRating(COURSE_SIX, 0, 0, 1, 0, 0);
    }

    public static Rating ratingCourseSeven() {
        return Rating.newRating(COURSE_SEVEN, 0, 1, 0, 0, 0);
    }

    public static Rating ratingCourseEight() {
        return Rating.newRating(COURSE_EIGHT, 1, 0, 0, 0, 0);
    }

    public static Rating ratingCourseTen() {
        return Rating.newRating(COURSE_TEN, 1, 1, 1, 1, 1);
    }

    public static List<Rating> allRatingsHaveFeedbacks() {
        return List.of(
                ratingCourseFour(),
                ratingCourseFive(),
                ratingCourseSix(),
                ratingCourseSeven(),
                ratingCourseEight()
        );
    }

    public static CreateFeedbackRequest createFeedbackRequest(Long courseId, Integer rate) {
        return CreateFeedbackRequest.builder()
                .courseId(courseId)
                .comment("This is a feedback")
                .rate(rate)
                .build();
    }
}