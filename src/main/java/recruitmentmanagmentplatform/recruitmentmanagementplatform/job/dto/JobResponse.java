package recruitmentmanagmentplatform.recruitmentmanagementplatform.job.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.EmploymentType;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.Job;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.JobStatus;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private Long id;

    private String title;

    private String description;

    private String department;

    private String location;

    private EmploymentType employmentType;

    private JobStatus status;

    private UserResponse createdBy;

    private LocalDateTime publishedAt;

    private LocalDateTime closedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static JobResponse fromEntity(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .department(job.getDepartment())
                .location(job.getLocation())
                .employmentType(job.getEmploymentType())
                .status(job.getStatus())
                .createdBy(job.getCreatedBy() != null ? UserResponse.fromEntity(job.getCreatedBy()) : null)
                .publishedAt(job.getPublishedAt())
                .closedAt(job.getClosedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
