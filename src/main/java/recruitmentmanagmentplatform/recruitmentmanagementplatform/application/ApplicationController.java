package recruitmentmanagmentplatform.recruitmentmanagementplatform.application;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto.ApplicationResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto.CreateApplicationRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.dto.UpdateApplicationRequest;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public List<ApplicationResponse> getApplications(
            @RequestParam(required = false) Long candidateId,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long recruiterId) {
        List<Application> applications;
        if (candidateId != null) {
            applications = applicationService.getApplicationsByCandidate(candidateId);
        } else if (jobId != null) {
            applications = applicationService.getApplicationsByJob(jobId);
        } else if (status != null) {
            applications = applicationService.getApplicationsByStatus(status);
        } else if (recruiterId != null) {
            applications = applicationService.getApplicationsByRecruiter(recruiterId);
        } else {
            applications = applicationService.getAllApplications();
        }

        return applications.stream().map(ApplicationResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public ApplicationResponse getApplication(@PathVariable Long id) {
        return ApplicationResponse.fromEntity(applicationService.getApplicationById(id));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        Application application = applicationService.applyToJob(
                request.getCandidateId(), request.getJobId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApplicationResponse.fromEntity(application));
    }

    @PutMapping("/{id}")
    public ApplicationResponse updateApplication(
            @PathVariable Long id,
            @RequestBody UpdateApplicationRequest request) {
        Application application = applicationService.getApplicationById(id);

        if (request.getStatus() != null) {
            application = applicationService.updateStatus(id, request.getStatus());
        }
        if (request.getAssignedRecruiterId() != null) {
            application = applicationService.assignRecruiter(id, request.getAssignedRecruiterId());
        }

        return ApplicationResponse.fromEntity(application);
    }

    @PutMapping("/{id}/assign")
    public ApplicationResponse assignRecruiter(
            @PathVariable Long id,
            @RequestParam Long recruiterId) {
        return ApplicationResponse.fromEntity(
                applicationService.assignRecruiter(id, recruiterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}