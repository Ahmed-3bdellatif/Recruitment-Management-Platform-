package recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.HiringDecisionStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHiringDecisionRequest {

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Hiring decision is required")
    private HiringDecisionStatus decision;

    private String reason;
}
