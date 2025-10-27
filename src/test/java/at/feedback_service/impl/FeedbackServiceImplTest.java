package at.feedback_service.impl;

import at.feedback_service.BaseIntegrationTest;
import at.feedback_service.dto.FeedbackResponse;
import at.feedback_service.dto.RatedFeedbackResponse;
import at.feedback_service.dto.SortBy;
import at.feedback_service.exception.FeedbackServiceException;
import at.feedback_service.model.Rating;
import at.feedback_service.repoository.RatingRepository;
import at.feedback_service.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static at.feedback_service.testutil.TestConstants.*;
import static at.feedback_service.testutil.TestConstants.FEEDBACK_DATE_COURSE_4;
import static at.feedback_service.testutil.TestData.createFeedbackRequest;
import static at.feedback_service.testutil.TestData.ratingCourseTen;
import static at.feedback_service.testutil.TestUtils.compareCourseInfo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FeedbackServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private FeedbackService feedbackService;
    @Autowired
    private RatingRepository ratingRepository;

    @Test
    void getRatedFeedbackForCourse_returnsCorrectResponse_whenCourseHasFeedback() {
        Rating ratingCourseTen = ratingCourseTen();
        RatedFeedbackResponse response = feedbackService.getRatedFeedbacksForCourse(COURSE_TEN, SortBy.DATE_ASC, 0, 5);
        var feedbacks = response.getFeedbacks();

        assertThat(feedbacks)
                .map(FeedbackResponse::getRate)
                .isEqualTo(List.of(5, 4, 3, 2, 1));

        assertThat(feedbacks)
                .map(FeedbackResponse::getCreatedAt)
                .isEqualTo(List.of(FEEDBACK_DATE_COURSE_4,
                        FEEDBACK_DATE_COURSE_5,
                        FEEDBACK_DATE_COURSE_6,
                        FEEDBACK_DATE_COURSE_7,
                        FEEDBACK_DATE_COURSE_8));

        compareCourseInfo(ratingCourseTen, response.getCourseRating());
    }

    @Test
    void getRatedFeedbacksForCourse_returnsEmptyListAndDefaultRating_whenCourseHasNoFeedback() {
        RatedFeedbackResponse response = feedbackService.getRatedFeedbacksForCourse(COURSE_UNKNOWN, SortBy.DATE_ASC, 0, 10);
        assertThat(response.getFeedbacks()).isEmpty();
        compareDefaultCourseInfo(COURSE_UNKNOWN, response.getCourseRating());
    }

    @Test
    void getFeedbacksOfUser_returnsCorrectList_whenUserHasFeedback() {
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbackOfUser(USER_NAME, SortBy.DATE_ASC, 0, 3);
        assertThat(feedbacks).hasSize(3);

        assertThat(feedbacks)
                .map(FeedbackResponse::getRate)
                .isEqualTo(List.of(5, 4, 3));

        assertThat(feedbacks)
                .map(FeedbackResponse::getCreatedAt)
                .isEqualTo(List.of(FEEDBACK_DATE_COURSE_4,
                        FEEDBACK_DATE_COURSE_5,
                        FEEDBACK_DATE_COURSE_6));
    }

    @Test
    void getFeedbacksOfUser_returnsEmptyList_whenUserHasNoFeedbacks() {
        List<FeedbackResponse> feedbacks = feedbackService
                .getFeedbackOfUser("Unknown", SortBy.DATE_ASC, 0, 10);
        assertThat(feedbacks).isEmpty();
    }

    @Test
    void getFeedback_throwsIfFeedbackIsNotPresent() {
        Long unknownId = 1000L;
        assertThrows(FeedbackServiceException.class,
                () -> feedbackService.getFeedback(unknownId));
    }

    @Test
    void getFeedback_returnsFeedbackIfFeedbackIsPresent() {
        var id = getFeedbackIdByCourseId(COURSE_ONE);
        FeedbackResponse response = feedbackService.getFeedback(id);
        assertThat(response.getRate()).isEqualTo(RATE_FIVE);
        assertThat(response.getComment()).isEqualTo(COMMENT_ONE);
        assertThat(response.getCourseId()).isEqualTo(COURSE_ONE);
        assertThat(response.getCreatedBy()).isEqualTo(USER_ONE);
        assertThat(response.getCreatedAt()).isEqualTo(FEEDBACK_DATE);
    }

    @Test
    void createFeedback_throwsWhenUserAlreadyPlacedFeedbackForCourse() {
        var request = createFeedbackRequest(COURSE_ONE, 5);
        assertThrows(FeedbackServiceException.class,
                () -> feedbackService.createFeedback(request, "UserOne"));
    }

    @Test
    void createFeedback_createsFeedbackAddsRatingWhenCourseHadNoRating() {
        var request = createFeedbackRequest(1000L, 5);
        FeedbackResponse response = feedbackService.createFeedback(request, "Alex");
        assertThat(response.getRate()).isEqualTo(request.getRate());
        assertThat(response.getCourseId()).isEqualTo(request.getCourseId());
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedBy()).isEqualTo("Alex");
        assertThat(response.getComment()).isEqualTo(request.getComment());

        var rating = ratingRepository.findByCourseId(request.getCourseId()).get();
        assertThat(rating.getRateFive()).isEqualTo(1);
    }

    @Test
    void createFeedback_createsFeedbackIncreasesRatingWhenCourseAlreadyHasRating() {
        var request = createFeedbackRequest(COURSE_ONE, 5);

        FeedbackResponse response = feedbackService.createFeedback(request, "Alex");
        assertThat(response.getRate()).isEqualTo(request.getRate());
        assertThat(response.getCourseId()).isEqualTo(request.getCourseId());
        assertThat(response.getId()).isNotNull();
        assertThat(response.getCreatedBy()).isEqualTo("Alex");
        assertThat(response.getComment()).isEqualTo(request.getComment());

        Rating rating = ratingRepository.findByCourseId(request.getCourseId()).get();
        assertThat(rating.getRateFive()).isEqualTo(2);
    }
}