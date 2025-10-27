package at.feedback_service.dto;

import at.feedback_service.dto.validation.NullOrNotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateFeedbackRequest {
    @Positive(message = "Course Id should be positive> 0")
    private Long courseId;
    @NullOrNotBlank(message = "Comment should not be empty")
    private String comment;
    @Min(value = 1, message = "Rating should be 1 to 5")
    @Max(value = 5, message = "Rating should be 1 to 5")
    private Integer rate;
}
