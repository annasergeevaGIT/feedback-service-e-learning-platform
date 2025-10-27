package at.feedback_service.testutil;

import java.time.LocalDateTime;
import java.time.Month;

public class TestConstants {

    public static final String BASE_URL = "/v1/feedbacks";
    public static final Long COURSE_ONE = 1L;
    public static final Long COURSE_TWO = 2L;
    public static final Long COURSE_THREE = 3L;
    public static final Long COURSE_FOUR = 4L;
    public static final Long COURSE_FIVE = 5L;
    public static final Long COURSE_SIX = 6L;
    public static final Long COURSE_SEVEN = 7L;
    public static final Long COURSE_EIGHT = 8L;
    public static final Long COURSE_TEN = 10L;
    public static final Long COURSE_UNKNOWN = 333L;

    public static final String USER_ONE = "UserOne";
    public static final String COMMENT_ONE = "CommentOne";
    public static final Integer RATE_FIVE = 5;
    public static final LocalDateTime FEEDBACK_DATE = LocalDateTime.of(2024, Month.MARCH, 14, 10, 23, 54);
    public static final String USER_NAME = "Username";
    public static final LocalDateTime FEEDBACK_DATE_COURSE_4 = LocalDateTime.of(2024, Month.MARCH, 14, 11, 23, 54);
    public static final LocalDateTime FEEDBACK_DATE_COURSE_5 = LocalDateTime.of(2024, Month.MARCH, 15, 12, 23, 54);
    public static final LocalDateTime FEEDBACK_DATE_COURSE_6 = LocalDateTime.of(2024, Month.MARCH, 16, 13, 23, 54);
    public static final LocalDateTime FEEDBACK_DATE_COURSE_7 = LocalDateTime.of(2024, Month.MARCH, 17, 14, 23, 54);
    public static final LocalDateTime FEEDBACK_DATE_COURSE_8 = LocalDateTime.of(2024, Month.MARCH, 18, 15, 23, 54);

}