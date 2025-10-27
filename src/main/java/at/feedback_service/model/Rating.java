package at.feedback_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name = "ratings")
@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "course_id", nullable = false)
    private Long courseId;
    @Column(name = "rate_one", nullable = false)
    private Integer rateOne;
    @Column(name = "rate_two", nullable = false)
    private Integer rateTwo;
    @Column(name = "rate_three", nullable = false)
    private Integer rateThree;
    @Column(name = "rate_four", nullable = false)
    private Integer rateFour;
    @Column(name = "rate_five", nullable = false)
    private Integer rateFive;
    @Column(name = "wilson_score", nullable = false)
    private Float wilsonScore;
    @Column(name = "avg_stars", nullable = false)
    private Float avgStars;

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
        return getId() != null && getId().equals(((Rating) o).getId());
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
