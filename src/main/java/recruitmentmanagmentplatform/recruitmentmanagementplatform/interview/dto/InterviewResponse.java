package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto.ApplicationResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.Interview;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.InterviewStatus;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponse {

    private Long id;

    private ApplicationResponse application;

    private UserResponse interviewer;

    private LocalDateTime scheduledAt;

    private InterviewStatus status;

    private String meetingLink;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static InterviewResponse fromEntity(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .application(interview.getApplication() != null ? ApplicationResponse.fromEntity(interview.getApplication()) : null)
                .interviewer(interview.getInterviewer() != null ? UserResponse.fromEntity(interview.getInterviewer()) : null)
                .scheduledAt(interview.getScheduledAt())
                .status(interview.getStatus())
                .meetingLink(interview.getMeetingLink())
                .createdAt(interview.getCreatedAt())
                .updatedAt(interview.getUpdatedAt())
                .build();
    }
}
