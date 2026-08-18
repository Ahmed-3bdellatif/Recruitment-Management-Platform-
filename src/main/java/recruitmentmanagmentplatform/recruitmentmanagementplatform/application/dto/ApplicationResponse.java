package recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.Application;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.ApplicationStatus;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.CandidateResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.dto.JobResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;

    private CandidateResponse candidate;

    private JobResponse job;

    private ApplicationStatus status;

    private LocalDateTime appliedAt;

    private UserResponse assignedRecruiter;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static ApplicationResponse fromEntity(Application application) {
        return ApplicationResponse.builder()
                .id(application.getId())
                .candidate(application.getCandidate() != null ? CandidateResponse.fromEntity(application.getCandidate()) : null)
                .job(application.getJob() != null ? JobResponse.fromEntity(application.getJob()) : null)
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .assignedRecruiter(application.getAssignedRecruiter() != null ? UserResponse.fromEntity(application.getAssignedRecruiter()) : null)
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .build();
    }
}
