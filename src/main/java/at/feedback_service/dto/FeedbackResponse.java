package at.feedback_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long courseId;
    private String createdBy;
    private String comment;
    private Integer rate;
    private LocalDateTime createdAt;
}
