package at.feedback_service.controller;

import at.feedback_service.BaseIntegrationTest;
import at.feedback_service.dto.FeedbackResponse;
import at.feedback_service.dto.GetRatingsRequest;
import at.feedback_service.dto.RatedFeedbackResponse;
import at.feedback_service.dto.RatingsResponse;
import at.feedback_service.model.CourseRatingInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;

import static at.feedback_service.testutil.TestConstants.*;
import static at.feedback_service.testutil.TestData.*;
import static at.feedback_service.testutil.TestUtils.compareCourseInfo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@AutoConfigureMockMvc
class FeedbackControllerTest extends BaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getRatingsOfCourses_returnsCorrectRatings_whenSomeCoursesHaveFeedback() {
        var coursesWithFeedbacks = Set.of(4L, 5L, 6L, 7L, 8L, 11L, 12L);
        var request = GetRatingsRequest.builder()
                .courseIds(coursesWithFeedbacks)
                .build();

        webTestClient.post()
                .uri(BASE_URL + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatingsResponse.class)
                .value(response -> {
                    var courseRatingInfos = response.getCourseRatings();
                    courseRatingInfos.sort(Comparator.comparing(CourseRatingInfo::getCourseId));
                    assertThat(courseRatingInfos).hasSize(coursesWithFeedbacks.size());

                    var allRatingsHaveFeedbacks = allRatingsHaveFeedbacks();

                    for (int i = 0; i < courseRatingInfos.size(); i++) {
                        var rating = courseRatingInfos.get(i);
                        if (i < allRatingsHaveFeedbacks.size()) {
                            compareCourseInfo(allRatingsHaveFeedbacks.get(i), rating);
                        } else {
                            compareDefaultCourseInfo(rating.getCourseId(), rating);
                        }
                    }
                });
    }

    @Test
    void getRatingsOfCourses_returnsCorrectRatings_whenAllCoursesHaveFeedbacks() {
        var coursesWithFeedbacks = Set.of(4L, 5L, 6L, 7L, 8L);
        var request = GetRatingsRequest.builder()
                .courseIds(coursesWithFeedbacks)
                .build();

        webTestClient.post()
                .uri(BASE_URL + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatingsResponse.class)
                .value(response -> {
                    var courseRatingInfos = response.getCourseRatings();
                    assertThat(courseRatingInfos).hasSize(coursesWithFeedbacks.size());
                    courseRatingInfos.sort(Comparator.comparing(CourseRatingInfo::getCourseId));
                    var allRatingsHaveReviews = allRatingsHaveFeedbacks();
                    for (int i = 0; i <courseRatingInfos.size(); i++) {
                        compareCourseInfo(allRatingsHaveReviews.get(i), courseRatingInfos.get(i));
                    }
                });
    }

    @Test
    void getRatingsOfCourses_returnsDefaultRatingsWhenCourseHaveNoFeedback() {
        var coursesWithNoFeedbacks = Set.of(1000L, 2000L, 3000L);
        var request = GetRatingsRequest.builder()
                .courseIds(coursesWithNoFeedbacks)
                .build();

        webTestClient.post()
                .uri(BASE_URL + "/ratings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatingsResponse.class)
                .value(response -> {
                    var courseRatingInfos = response.getCourseRatings();
                    assertThat(courseRatingInfos).hasSize(coursesWithNoFeedbacks.size());
                    for (var courseRatingInfo : courseRatingInfos) {
                        compareDefaultCourseInfo(courseRatingInfo.getCourseId(), courseRatingInfo);
                    }
                });
    }

    @Test
    void getFeedbacksOfCourseu_returnsCorrectResponse_whenCourseHasFeedbacks() {
        webTestClient.get()
                .uri(BASE_URL + "/course/" + COURSE_TEN + "?from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatedFeedbackResponse.class)
                .value(response -> {
                    assertThat(response.getFeedbacks())
                            .hasSize(5)
                            .isSortedAccordingTo(Comparator.comparing(FeedbackResponse::getCreatedAt));
                    compareCourseInfo(ratingCourseTen(), response.getCourseRating());
                });
    }

    @Test
    void getFeedbacksOfCourse_returnsEmptyListWithDefaultRating_whenMenuHasNoRFeedbacks() {
        long courseWithNoFeedbacks = 1000;
        webTestClient.get()
                .uri(BASE_URL + "/course/" + courseWithNoFeedbacks + "?from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isOk()
                .expectBody(RatedFeedbackResponse.class)
                .value(response -> {
                    assertThat(response.getFeedbacks()).isEmpty();
                    compareDefaultCourseInfo(courseWithNoFeedbacks, response.getCourseRating());
                });

    }

    @Test
    void getFeedbacksOfUser_returnsCorrectList_whenUserHasFeedbacks() {
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .header(FeedbackController.USER_HEADER, USER_NAME)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FeedbackResponse.class)
                .value(feedbackResponses -> {
                    assertThat(feedbackResponses)
                            .hasSize(5)
                            .isSortedAccordingTo(Comparator.comparing(FeedbackResponse::getCreatedAt));
                });
    }

    @Test
    void getFeedbacksOfUser_returnsEmptyList_whenUserHasNoFeedbacks() {
        String userWithNoFeedbacks = "Unknown user";
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .header(FeedbackController.USER_HEADER, userWithNoFeedbacks)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FeedbackResponse.class)
                .value(feedbackResponses -> {
                    assertThat(feedbackResponses).isEmpty();
                });
    }

    @Test
    void getFeedback_returnsFeedback() {
        var reviewId = getFeedbackIdByCourseId(COURSE_ONE);

        webTestClient.get()
                .uri(BASE_URL + "/" + reviewId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FeedbackResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getCourseId()).isEqualTo(COURSE_ONE);
                    assertThat(response.getCreatedBy()).isEqualTo(USER_ONE);
                    assertThat(response.getRate()).isEqualTo(RATE_FIVE);
                    assertThat(response.getCreatedAt()).isEqualTo(FEEDBACK_DATE);
                    assertThat(response.getComment()).isEqualTo(COMMENT_ONE);
                });
    }

    @Test
    void getFeedback_returnsNotFoundWhenNoFeedbackWithThatId() {
        long unknown = 1000L;
        webTestClient.get()
                .uri(BASE_URL + "/" + unknown)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createFeedback_createsFeedback() {
        var request = createFeedbackRequest(COURSE_ONE, 5);
        var username = "Alex";

        LocalDateTime now = LocalDateTime.now().minusNanos(1000);

        webTestClient.post()
                .uri(BASE_URL)
                .header(FeedbackController.USER_HEADER, username)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FeedbackResponse.class)
                .value(response -> {
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getCourseId()).isEqualTo(request.getCourseId());
                    assertThat(response.getCreatedBy()).isEqualTo(username);
                    assertThat(response.getRate()).isEqualTo(request.getRate());
                    assertThat(response.getCreatedAt()).isAfter(now);
                });
    }

    @Test
    void createReview_returnsConflictWhenUserTriesToSendSecondReviewToSameMenu() {
        var request = createFeedbackRequest(COURSE_ONE, 5);

        webTestClient.post()
                .uri(BASE_URL)
                .header(FeedbackController.USER_HEADER, USER_ONE)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
}