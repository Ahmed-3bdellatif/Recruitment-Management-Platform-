package recruitmentmanagmentplatform.recruitmentmanagementplatform.job.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.EmploymentType;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.JobStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateJobRequest {

    private String title;

    private String description;

    private String department;

    private String location;

    private EmploymentType employmentType;

    private JobStatus status;
}
