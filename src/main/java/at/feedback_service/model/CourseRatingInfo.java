package at.feedback_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRatingInfo {
    private Long courseId;
    private Float wilsonScore;
    private Float avgStars;
}
