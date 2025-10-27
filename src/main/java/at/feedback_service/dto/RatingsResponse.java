package at.feedback_service.dto;

import at.feedback_service.model.CourseRatingInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RatingsResponse {
    private List<CourseRatingInfo> courseRatings;
}
