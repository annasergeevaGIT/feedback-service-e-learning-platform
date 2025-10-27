package at.feedback_service.repoository;

import org.springframework.data.domain.Pageable;
import at.feedback_service.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findAllByCreatedBy(String createdBy, Pageable pageable);
    List<Feedback> findAllByCourseId(Long courseId, Pageable pageable);
}
