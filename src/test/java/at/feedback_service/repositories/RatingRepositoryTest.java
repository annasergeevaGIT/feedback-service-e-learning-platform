package at.feedback_service.repositories;

import at.feedback_service.BaseTest;
import at.feedback_service.model.CourseRatingInfo;
import at.feedback_service.model.Rating;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static at.feedback_service.model.Rating.newRating;
import static at.feedback_service.testutil.TestConstants.*;
import static at.feedback_service.testutil.TestData.ratingCourseOne;
import static at.feedback_service.testutil.TestData.ratingCourseTwo;
import static at.feedback_service.testutil.TestUtils.*;

@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use application-test.yml db, don't replace with in memory
public class RatingRepositoryTest extends BaseTest {

    @Autowired
    private PlatformTransactionManager transactionManager;
    /*
    * We create a pool consisting of 8 threads in which tasks for updating the rating of the course with the identifier COURSE_ONE will be executed.
    * The course rating will be updated by 100 concurrent requests. As a result of their execution,
    * we expect that each rating type will increase by 20. However, since rateFive initially has a value of 1,
    * after the test completes, it should be 21. Next, we use ExecutorService.invokeAll to wait for all rating update tasks to finish,
    * and only after that do we verify that no exceptions occurred in any of the threads.
    */
    @Test
    void incrementRating_incrementsRatingCorrectlyForConcurrentRequests() throws Exception {
        Long courseId = COURSE_ONE;
        ExecutorService executor = Executors.newFixedThreadPool(8); //12 if 6 cores

        List<Callable<Void>> incrementors = new ArrayList<>();
        int numIncrementors = 100;
        for (int i = 1; i <= numIncrementors; i++) {
            // as a reisult to each rating adds 20. If beginning rateFive was 1 > it will be 21
            final int rate = (i % 5 == 0) ? 5 : i % 5;
            incrementors.add(() -> {
                ratingRepository.incrementRating(courseId, rate);
                return null;
            });
        }

        var results = executor.invokeAll(incrementors);
        executor.shutdown();

        for (var result : results) {
            assertDoesNotThrow(() -> result.get());
        }
        Rating expectedRating = ratingCourseOne();
        incrementExpectedRating(expectedRating, 20, 20, 20, 20, 20);

        Rating actualRating = ratingRepository.findByCourseId(courseId).get();
        assertRatesEqual(actualRating, expectedRating);
    }
    /*
    * The insertNoConflict method executes successfully when it is called simultaneously by two threads,
    * with one thread rolling back its transaction while the other completes it. In this test, we manually manage transactions
    * using the standard PlatformTransactionManager bean, along with the TransactionDefinition interface,
    * which describes the standard Spring transaction properties, and its default implementation DefaultTransactionDefinition.
    * To ensure the correct order of transaction start, a CountDownLatch is used. As in the previous test,
    * we do not expect any exceptions — if any occur, the test should fail.
    */
    @Test
    void insertNoConflict_succeeds_whenOneTransactionCommitsAndOneRollsBack() throws Exception {
        Long courseId = COURSE_UNKNOWN;
        TransactionDefinition def = new DefaultTransactionDefinition();
        var latch = new CountDownLatch(1);
        Callable<Void> t1 = () -> {
            // start transaction
            TransactionStatus status = transactionManager.getTransaction(def);
            // let the second thread to start transaction
            latch.countDown();

            ratingRepository.insertNoConflict(courseId);
            transactionManager.rollback(status);
            return null;
        };

        Callable<Void> t2 = () -> {
            // wait till 1st thread starts transaction
            latch.await();
            // start transaction
            TransactionStatus status = transactionManager.getTransaction(def);
            ratingRepository.insertNoConflict(courseId);
            transactionManager.commit(status);
            return null;
        };

        var results = Executors.newFixedThreadPool(2).invokeAll(Arrays.asList(t2, t1));
        assertDoesNotThrow(() -> {
            results.get(1).get();
            results.get(0).get();
        });

        assertThat(ratingRepository.findByCourseId(courseId)).isPresent().hasValueSatisfying(rating -> {
            assertThat(rating.getCourseId()).isEqualTo(courseId);
        });
    }
    /*
    * insertNoConflict method executes successfully when two concurrent threads attempt to call it.
    * In this test, we do not manage transactions manually but allow the database to handle the conflict on its own.
    */
    @Test
    void insertNoConflict_succeeds_whenTwoConcurrentThreadsPerformInsert() throws Exception {
        Long courseId = COURSE_UNKNOWN;
        var latch = new CountDownLatch(1);

        Callable<Void> t1 = () -> {
            latch.countDown();
            ratingRepository.insertNoConflict(courseId);
            return null;
        };

        Callable<Void> t2 = () -> {
            latch.await();
            ratingRepository.insertNoConflict(courseId);
            return null;
        };
        var results = Executors.newFixedThreadPool(2).invokeAll(Arrays.asList(t2, t1));

        assertDoesNotThrow(() -> {
            results.get(1).get();
            results.get(0).get();
        });

        assertThat(ratingRepository.findByCourseId(courseId)).isPresent().hasValueSatisfying(rating -> {
            assertThat(rating.getCourseId()).isEqualTo(courseId);
        });
    }

    @Test
    void insertNoConflict_doesNothing_whenRowWithThatCourseIdExistsInDb() {
        Long existingCourseId = COURSE_ONE;
        ratingRepository.insertNoConflict(existingCourseId);

        assertThat(ratingRepository.findByCourseId(existingCourseId)).isPresent().hasValueSatisfying(rating -> {
            assertThat(rating.getCourseId()).isEqualTo(existingCourseId);
            assertThat(rating.getRateFive()).isEqualTo(1);
        });
    }

    @Test
    void insertNoConflict_insertsRowWithCourseId_whenNoRowWithThatCourseIdInDb() {
        Long courseId = COURSE_UNKNOWN;
        ratingRepository.insertNoConflict(courseId);

        assertThat(ratingRepository.findByCourseId(courseId)).isPresent().hasValueSatisfying(rating -> assertThat(rating.getCourseId()).isEqualTo(courseId));
    }

    @Test
    void findRatingInfosByCourseIdIn_returnsCorrectListWhenSomeCoursesHaveRatings() {
        Rating ratingCourseOne = ratingCourseOne();
        Rating ratingCourseTwo = ratingCourseTwo();

        var courseIds = Set.of(COURSE_ONE, COURSE_TWO, COURSE_UNKNOWN);
        incrementRatingsForCourseId(ratingCourseOne, 10, 10, 10, 10, 10);
        incrementRatingsForCourseId(ratingCourseTwo, 11, 11, 11, 11, 11);

        List<CourseRatingInfo> courseRatingInfos = ratingRepository.findRatingInfosByCourseIdIn(courseIds);
        compareCourseInfos(List.of(ratingCourseOne, ratingCourseTwo), courseRatingInfos);
    }

    @Test
    void findRatingInfosByCourseIdIn_returnsCorrectListWhenAllCoursesHaveRatings() {
        Rating ratingCourseOne = ratingCourseOne();
        Rating ratingCourseTwo = ratingCourseTwo();
        Rating ratingCourseThree = newRating(COURSE_THREE);

        var courseIds = Set.of(COURSE_ONE, COURSE_TWO, COURSE_THREE);
        incrementRatingsForCourseId(ratingCourseOne, 10, 10, 10, 10, 10);
        incrementRatingsForCourseId(ratingCourseTwo, 11, 11, 11, 11, 11);
        incrementRatingsForCourseId(ratingCourseThree, 12, 12, 12, 12, 12);

        var courseRatingInfos = ratingRepository.findRatingInfosByCourseIdIn(courseIds);
        compareCourseInfos(List.of(ratingCourseOne, ratingCourseTwo, ratingCourseThree), courseRatingInfos);
    }

    @Test
    void findRatingInfosByCourseIdIn_returnsEmptyList_whenNoCoursesHaveRatings() {
        var unknown = Set.of(1000L, 2000L, 3000L);
        var ratings = ratingRepository.findRatingInfosByCourseIdIn(unknown);
        assertThat(ratings).isEmpty();
    }

    @Test
    void findRatingInfoByCourseId_returnsCorrectInfo() {
        Rating ratingCourseOne = ratingCourseOne();
        Long courseOne = ratingCourseOne.getCourseId();
        incrementRatingsForCourseId(ratingCourseOne, 10, 10, 10, 10, 10);

        var actual = ratingRepository.findRatingInfoByCourseId(courseOne).get();
        compareCourseInfo(ratingCourseOne, actual);
    }

    @Test
    void findRatingInfoByCourseId_returnsEmptyOptionalWhenNoRatingForCourse() {
        var opt = ratingRepository.findRatingInfoByCourseId(COURSE_UNKNOWN);
        assertThat(opt).isEmpty();
    }

    @Test
    void incrementRating_alsoUpdatesWilsonScore_andAvgStars3() {
        Rating ratingCourseOne = ratingCourseOne();
        Rating ratingCourseTwo = ratingCourseTwo();
        Rating ratingCourseThree = newRating(COURSE_THREE);

        incrementRatingsForCourseId(ratingCourseOne, 0, 0, 0, 10, 1);
        incrementRatingsForCourseId(ratingCourseTwo, 0, 0, 2, 0, 0);
        incrementRatingsForCourseId(ratingCourseThree, 0, 0, 0, 0, 1);

        CourseRatingInfo first = ratingRepository.findRatingInfoByCourseId(COURSE_ONE).get();
        CourseRatingInfo second = ratingRepository.findRatingInfoByCourseId(COURSE_TWO).get();
        CourseRatingInfo third = ratingRepository.findRatingInfoByCourseId(COURSE_THREE).get();

        compareCourseInfos(List.of(ratingCourseOne, ratingCourseTwo, ratingCourseThree), List.of(first, second, third));

        assertTrue(first.getWilsonScore() > third.getWilsonScore());
        assertTrue(third.getWilsonScore() > second.getWilsonScore());

        assertTrue(third.getAvgStars() > first.getAvgStars());
        assertTrue(first.getAvgStars() > second.getAvgStars());

        printWilsonScoreAndAvgStars(List.of(first, second, third));
    }

    @Test
    void incrementRating_alsoUpdatesWilsonScore_andAvgStars2() {
        Rating ratingCourseOne = ratingCourseOne();
        Rating ratingCourseTwo = ratingCourseTwo();
        Rating ratingCourseThree = newRating(COURSE_THREE);

        incrementRatingsForCourseId(ratingCourseOne, 0, 0, 0, 10, 1);
        incrementRatingsForCourseId(ratingCourseTwo, 0, 0, 2, 10, 0);
        incrementRatingsForCourseId(ratingCourseThree, 0, 1, 10, 0, 0);

        CourseRatingInfo first = ratingRepository.findRatingInfoByCourseId(COURSE_ONE).get();
        CourseRatingInfo second = ratingRepository.findRatingInfoByCourseId(COURSE_TWO).get();
        CourseRatingInfo third = ratingRepository.findRatingInfoByCourseId(COURSE_THREE).get();

        compareCourseInfos(List.of(ratingCourseOne, ratingCourseTwo, ratingCourseThree), List.of(first, second, third));

        assertTrue(first.getWilsonScore() > second.getWilsonScore());
        assertTrue(second.getWilsonScore() > third.getWilsonScore());

        assertTrue(first.getAvgStars() > second.getAvgStars());
        assertTrue(second.getAvgStars() > third.getAvgStars());

        printWilsonScoreAndAvgStars(List.of(first, second, third));
    }

    @Test
    void incrementRating_alsoUpdatesWilsonScore_andAvgStars() {
        incrementActualRatingsForCourseId(COURSE_ONE, 0, 0, 0, 2, 100);
        incrementActualRatingsForCourseId(COURSE_TWO, 0, 0, 2, 10, 0);
        incrementActualRatingsForCourseId(COURSE_THREE, 0, 0, 0, 0, 1);

        CourseRatingInfo first = ratingRepository.findRatingInfoByCourseId(COURSE_ONE).get();
        CourseRatingInfo second = ratingRepository.findRatingInfoByCourseId(COURSE_TWO).get();
        CourseRatingInfo third = ratingRepository.findRatingInfoByCourseId(COURSE_THREE).get();

        Assertions.assertTrue(first.getWilsonScore() > second.getWilsonScore());
        Assertions.assertTrue(second.getWilsonScore() > third.getWilsonScore());

        Assertions.assertTrue(first.getAvgStars() > second.getAvgStars());
        Assertions.assertTrue(third.getAvgStars() > first.getAvgStars());

        printWilsonScoreAndAvgStars(List.of(first, second, third));
    }

    @Test
    void incrementRating_incrementsCorrectRating() {
        Rating ratingCourseOne = ratingCourseOne();
        incrementRatingsForCourseId(ratingCourseOne, 5, 5, 5, 5, 4);

        Rating updated = ratingRepository.findByCourseId(COURSE_ONE).get();
        assertRatesEqual(updated, ratingCourseOne);
    }

    private void printWilsonScoreAndAvgStars(List<CourseRatingInfo> ratings) {
        ratings.forEach(r -> {
            System.out.println("Wilson Score: " + r.getWilsonScore());
            System.out.println("Average Stars: " + r.getAvgStars());
        });
    }
}
