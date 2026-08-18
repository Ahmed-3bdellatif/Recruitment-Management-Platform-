package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.InterviewFeedback;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewFeedbackResponse {

    private Long id;

    private Long interviewId;

    private Integer technicalScore;

    private Integer communicationScore;

    private Integer problemSolvingScore;

    private BigDecimal overallScore;

    private String comments;

    private LocalDateTime submittedAt;

    public static InterviewFeedbackResponse fromEntity(InterviewFeedback feedback) {
        return InterviewFeedbackResponse.builder()
                .id(feedback.getId())
                .interviewId(feedback.getInterview() != null ? feedback.getInterview().getId() : null)
                .technicalScore(feedback.getTechnicalScore())
                .communicationScore(feedback.getCommunicationScore())
                .problemSolvingScore(feedback.getProblemSolvingScore())
                .overallScore(feedback.getOverallScore())
                .comments(feedback.getComments())
                .submittedAt(feedback.getSubmittedAt())
                .build();
    }
}
