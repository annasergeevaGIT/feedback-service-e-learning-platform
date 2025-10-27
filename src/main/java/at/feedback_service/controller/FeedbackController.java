package at.feedback_service.controller;

import at.feedback_service.dto.*;
import at.feedback_service.service.FeedbackService;
import at.feedback_service.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FeedbackController", description = "REST API for feedbacks.")
@Slf4j
@RestController
@RequestMapping("/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
    public static final String USER_HEADER = "X-User-Name";

    private final FeedbackService feedbackService;
    private final RatingService ratingService;

    @Operation(
            summary = "${api.feedback-create.summary}",
            description = "${api.feedback-create.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "${api.response.createOk}"),
            @ApiResponse(
                    responseCode = "409",
                    description = "${api.response.createConflict}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.createBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse createFeedback(@RequestBody
                                       @Valid
                                       CreateFeedbackRequest request,
                                       @RequestHeader(USER_HEADER)
                                       @NotBlank(message = "Username can not be empty")
                                       String username) {
        log.info("Received POST request to create Feedback: {} by user: {}",
                request, username);
        return feedbackService.createFeedback(request, username);
    }

    @Operation(
            summary = "${api.feedback-get.summary}",
            description = "${api.feedback-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getOk}"),
            @ApiResponse(
                    responseCode = "404",
                    description = "${api.response.notFound}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
    })
    @GetMapping("/{id}")
    public FeedbackResponse getFeedback(@PathVariable("id") @Positive Long feedbackId) {
        log.info("Received request to GET feedback with id={}", feedbackId);
        return feedbackService.getFeedback(feedbackId);
    }

    @Operation(
            summary = "${api.user-feedbacks-get.summary}",
            description = "${api.user-feedbacks-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getUserFeedbacksOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getUserFeedbacksBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/my")
    public List<FeedbackResponse> getFeedbacksOfUser(@RequestHeader(USER_HEADER)
                                                 @NotBlank(message = "Username can not be empty")
                                                 String username,
                                                 @RequestParam(value = "from", defaultValue = "0")
                                                 @PositiveOrZero(message = "Page must be >= 0")
                                                 int from,
                                                 @RequestParam(value = "size", defaultValue = "10")
                                                 @Positive(message = "Page size must be > 0")
                                                 int size,
                                                 @RequestParam(value = "sortBy", defaultValue = "date_asc")
                                                 @NotBlank(message = "Sorting parameter must not be empty")
                                                 String sortBy) {
        log.info("Received request to GET list of Feedbacks made by user: {}", username);
        return feedbackService.getFeedbackOfUser(username, SortBy.fromString(sortBy), from, size);
    }

    @Operation(
            summary = "${api.course-feedbacks-get.summary}",
            description = "${api.course-feedbacks-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getCourseFeedbacksOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getCourseFeedbacksBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping("/course/{courseId}")
    public RatedFeedbackResponse getFeedbacksOfCourse(@PathVariable("courseId")
                                                 @Positive(message = "Course ID must be > 0.")
                                                 Long courseId,
                                                 @RequestParam(value = "from", defaultValue = "0")
                                                 @PositiveOrZero(message = "Page must be >= 0.")
                                                 int from,
                                                 @RequestParam(value = "size", defaultValue = "10")
                                                 @Positive(message = "Page size must be > 0.")
                                                 int size,
                                                 @RequestParam(value = "sortBy", defaultValue = "date_asc")
                                                 @NotBlank(message = "Sorting parameter must not be empty")
                                                 String sortBy) {
        log.info("Received request to GET list of feedbacks with ratings of course with id={}", courseId);
        return feedbackService.getRatedFeedbacksForCourse(courseId, SortBy.fromString(sortBy), from, size);
    }

    @Operation(
            summary = "${api.ratings-get.summary}",
            description = "${api.ratings-get.description}"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.response.getRatingsOk}"),
            @ApiResponse(
                    responseCode = "400",
                    description = "${api.response.getRatingsBadRequest}",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping("/ratings")
    public RatingsResponse getRatingsOfCourses(@RequestBody @Valid GetRatingsRequest request) {
        log.info("Received POST request to get ratings of courses: {}", request.getCourseIds());
        return ratingService.getRatingOfCourses(request);
    }
}