package at.feedback_service.mapper;

import at.feedback_service.dto.CreateFeedbackRequest;
import at.feedback_service.dto.FeedbackResponse;
import at.feedback_service.model.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FeedbackMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "username", target = "createdBy")
    Feedback toDomain(CreateFeedbackRequest dto, String username);

    FeedbackResponse toFeedbackResponse(Feedback feedback);

    List<FeedbackResponse> toFeedbackResponseList(List<Feedback> feedbacks);
}
