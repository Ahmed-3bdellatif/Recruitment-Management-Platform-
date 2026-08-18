package recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.dto;

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
public class UpdateHiringDecisionRequest {

    private HiringDecisionStatus decision;

    private String reason;
}
