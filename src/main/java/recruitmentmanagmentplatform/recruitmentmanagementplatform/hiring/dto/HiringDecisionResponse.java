package recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto.ApplicationResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.HiringDecision;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.HiringDecisionStatus;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HiringDecisionResponse {

    private Long id;

    private ApplicationResponse application;

    private HiringDecisionStatus decision;

    private UserResponse decidedBy;

    private String reason;

    private LocalDateTime decidedAt;

    public static HiringDecisionResponse fromEntity(HiringDecision hiringDecision) {
        return HiringDecisionResponse.builder()
                .id(hiringDecision.getId())
                .application(hiringDecision.getApplication() != null ? ApplicationResponse.fromEntity(hiringDecision.getApplication()) : null)
                .decision(hiringDecision.getDecision())
                .decidedBy(hiringDecision.getDecidedBy() != null ? UserResponse.fromEntity(hiringDecision.getDecidedBy()) : null)
                .reason(hiringDecision.getReason())
                .decidedAt(hiringDecision.getDecidedAt())
                .build();
    }
}
