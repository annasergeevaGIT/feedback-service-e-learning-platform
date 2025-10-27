package at.feedback_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static at.feedback_service.model.DateUtil.DATE_FORMAT;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "feedbacks")
@Entity
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    @Column(name = "comment")
    private String comment;
    @Column(name = "rate", nullable = false)
    private int rate; // 1 to 5
    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    @DateTimeFormat(pattern = DATE_FORMAT)
    private LocalDateTime createdAt;

    public static Rating newRating(Long courseId) {
        return newRating(courseId, 0, 0, 0, 0, 0);
    }

    public static Rating newRating(Long courseId, int one, int two, int three, int four, int five) {
        return Rating.builder()
                .id(null)
                .courseId(courseId)
                .rateOne(one)
                .rateTwo(two)
                .rateThree(three)
                .rateFour(four)
                .rateFive(five)
                .wilsonScore(0.0f)
                .avgStars(0.0f)
                .build();
    }

    /**
     * https://stackoverflow.com/a/78077907/548473
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return getId() != null && getId().equals(((Feedback) o).getId());
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
