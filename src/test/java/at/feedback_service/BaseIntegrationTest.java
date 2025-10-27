package at.feedback_service;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BaseIntegrationTest extends BaseTest {

    @Autowired
    private EntityManager em;

    protected Long getFeedbackIdByCourseId(Long courseId) {
        return em.createQuery("select r.id from Feedback r where r.courseId= ?1", Long.class)
                .setParameter(1, courseId)
                .getSingleResult();
    }
}
