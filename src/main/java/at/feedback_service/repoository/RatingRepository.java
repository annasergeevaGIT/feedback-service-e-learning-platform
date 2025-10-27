package at.feedback_service.repoository;

import at.feedback_service.model.CourseRatingInfo;
import at.feedback_service.model.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    //Returns rating summary information for a specific course.
    @Query("""
                SELECT new at.feedback_service.model.CourseRatingInfo(
                    r.courseId,
                    r.wilsonScore,
                    r.avgStars
                ) FROM Rating r WHERE r.courseId = :courseId
            """)
    Optional<CourseRatingInfo> findRatingInfoByCourseId(@Param("courseId") Long courseId);



    // Returns rating summary information for multiple courses.
    @Query(
            """
            SELECT new at.feedback_service.model.CourseRatingInfo(
                r.courseId,
                r.wilsonScore,
                r.avgStars
            ) FROM Rating r WHERE r.courseId in :courseIds
            """
    )
    List<CourseRatingInfo> findRatingInfosByCourseIdIn(@Param("courseIds") Set<Long> courseIds);



    //Increments the count of a specific rating (1–5 stars) for a course.
    @Modifying //Marks the query as changing data (insert, update, or delete). Without this, Spring expects a SELECT query.
    @Transactional //Ensures the update runs in a transaction, so it’s committed properly.
    @Query(value = """
            UPDATE ratings SET 
            rate_one = CASE WHEN :rating = 1 THEN rate_one + 1 ELSE rate_one END, 
            rate_two = CASE WHEN :rating = 2 THEN rate_two + 1 ELSE rate_two END, 
            rate_three = CASE WHEN :rating = 3 THEN rate_three + 1 ELSE rate_three END, 
            rate_four = CASE WHEN :rating = 4 THEN rate_four + 1 ELSE rate_four END, 
            rate_five = CASE WHEN :rating = 5 THEN rate_five + 1 ELSE rate_five END 
            WHERE course_id = :courseId
            """,
            nativeQuery = true)
    void incrementRating(@Param("courseId") Long courseId, @Param("rating") Integer rating);



    // Inserts a rating record for a course if it does not already exist.
    @Modifying
    @Transactional
    @Query(value = """
                INSERT INTO ratings(course_id) values(:courseId) ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    void insertNoConflict(@Param("courseId") Long courseId);



    // Finds Rating entity by course ID
    Optional<Rating> findByCourseId(Long courseId);
}
