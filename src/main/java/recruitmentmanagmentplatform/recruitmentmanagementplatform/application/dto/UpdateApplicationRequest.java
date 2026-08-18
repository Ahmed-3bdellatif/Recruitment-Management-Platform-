package recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.ApplicationStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationRequest {

    private ApplicationStatus status;

    private Long assignedRecruiterId;
}
