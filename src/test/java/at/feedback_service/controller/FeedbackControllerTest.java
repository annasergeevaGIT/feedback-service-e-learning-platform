package at.feedback_service.controller;

import at.feedback_service.BaseIntegrationTest;
import at.feedback_service.dto.FeedbackResponse;
import at.feedback_service.dto.GetRatingsRequest;
import at.feedback_service.dto.RatedFeedbackResponse;
import at.feedback_service.dto.RatingsResponse;
import at.feedback_service.model.CourseRatingInfo;
import at.feedback_service.testutil.AuthToken;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Set;

import static at.feedback_service.testutil.TestConstants.*;
import static at.feedback_service.testutil.TestData.*;
import static at.feedback_service.testutil.TestUtils.compareCourseInfo;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

@AutoConfigureMockMvc
class FeedbackControllerTest extends BaseIntegrationTest {
    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
            .withRealmImportFile("/cloud-java-realm.json");

    static {
        KEYCLOAK.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java/protocol/openid-connect/certs");
    }

    private static AuthToken ADMIN;
    private static AuthToken USER_NO_FEEDBACKS;
    private static AuthToken USER_WITH_FEEDBACKS;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeAll
    static void setup() {
        WebClient webClient = WebClient.builder()
                .baseUrl(KEYCLOAK.getAuthServerUrl() + "/realms/cloud-java/protocol/openid-connect/token")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        ADMIN = createToken(webClient, ADMIN_NAME, "password");
        USER_NO_FEEDBACKS = createToken(webClient, USER_NO_FEEDBACKS_NAME, "password");
        USER_WITH_FEEDBACKS = createToken(webClient, USER_NAME, "password");
    }

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
                    var allRatingsHaveFeedbacks = allRatingsHaveFeedbacks();
                    for (int i = 0; i <courseRatingInfos.size(); i++) {
                        compareCourseInfo(allRatingsHaveFeedbacks.get(i), courseRatingInfos.get(i));
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
    void getFeedbacksOfCourse_returnsEmptyListWithDefaultRating_whenCourseHasNoRFeedbacks() {
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
    void getFeedbacksOfUser_returnsForbidden_whenUserHasNoRights() {
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(ADMIN.getAccessToken()))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getFeedbacksOfUser_returns401_whenUserNotAuthenticated() {
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getFeedbacksOfUser_returnsCorrectList_whenUserHasFeedbacks() {
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(USER_WITH_FEEDBACKS.getAccessToken()))
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
        webTestClient.get()
                .uri(BASE_URL + "/my?" + "from=0&size=10&sortBy=date_asc")
                .headers(h -> h.setBearerAuth(USER_NO_FEEDBACKS.getAccessToken()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FeedbackResponse.class)
                .value(feedbackResponses -> {
                    assertThat(feedbackResponses).isEmpty();
                });
    }

    @Test
    void getFeedback_returnsFeedback() {
        var feedbackId = getFeedbackIdByCourseId(COURSE_ONE);

        webTestClient.get()
                .uri(BASE_URL + "/" + feedbackId)
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
    void createFeedback_returnsUnauthorized_whenUserNotAuthenticated() {
        var request = createFeedbackRequest(COURSE_ONE, 5);

        webTestClient.post()
                .uri(BASE_URL)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void createFeedback_returnsForbidden_whenUserHasNoRights() {
        var request = createFeedbackRequest(COURSE_ONE, 5);

        webTestClient.post()
                .uri(BASE_URL)
                .headers(h -> h.setBearerAuth(ADMIN.getAccessToken()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void createFeedback_createsFeedback_whenUserAuthenticatedAndAuthorized() {
        var request = createFeedbackRequest(COURSE_ONE, 5);
        var username = USER_NO_FEEDBACKS_NAME;

        LocalDateTime now = LocalDateTime.now().minusNanos(1000);

        webTestClient.post()
                .uri(BASE_URL)
                .headers(h -> h.setBearerAuth(USER_NO_FEEDBACKS.getAccessToken()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FeedbackResponse.class)
                .value(response -> {
                    AssertionsForClassTypes.assertThat(response.getId()).isNotNull();
                    AssertionsForClassTypes.assertThat(response.getCourseId()).isEqualTo(request.getCourseId());
                    AssertionsForClassTypes.assertThat(response.getCreatedBy()).isEqualTo(username);
                    AssertionsForClassTypes.assertThat(response.getRate()).isEqualTo(request.getRate());
                    AssertionsForClassTypes.assertThat(response.getCreatedAt()).isAfter(now);
                });
    }

    @Test
    void createFeedback_returnsConflictWhenUserTriesToSendSecondFeedbackToSameCourse() {
        var request = createFeedbackRequest(COURSE_FIVE, 5);

        webTestClient.post()
                .uri(BASE_URL)
                .headers(h -> h.setBearerAuth(USER_WITH_FEEDBACKS.getAccessToken()))
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    private static AuthToken createToken(WebClient webClient, String username, String password) {
        return webClient.post()
                .body(fromFormData("grant_type", "password")
                        .with("client_id", "cloud-java-gateway")
                        .with("username", username)
                        .with("password", password)
                        .with("client_secret", "RleFn4MVPDKtGTXIZv4Opyfuwfx2fFLL")
                )
                .retrieve()
                .bodyToMono(AuthToken.class)
                .block();
    }
}