package recruitmentmanagmentplatform.recruitmentmanagementplatform.job;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.dto.CreateJobRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.dto.JobResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.dto.UpdateJobRequest;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class JobController {

    private final JobService jobService;

    @GetMapping
    public List<JobResponse> getJobs(@RequestParam(required = false) JobStatus status) {
        List<Job> jobs = status == null
                ? jobService.getAllJobs()
                : jobService.getJobsByStatus(status);
        return jobs.stream().map(JobResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public JobResponse getJob(@PathVariable Long id) {
        return JobResponse.fromEntity(jobService.getJobById(id));
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody CreateJobRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .build();

        Job createdJob = jobService.createJob(job, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(JobResponse.fromEntity(createdJob));
    }

    @PutMapping("/{id}")
    public JobResponse updateJob(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobRequest request) {
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .build();

        return JobResponse.fromEntity(jobService.updateJob(id, job));
    }

    @PutMapping("/{id}/publish")
    public JobResponse publishJob(@PathVariable Long id) {
        return JobResponse.fromEntity(jobService.publishJob(id));
    }

    @PutMapping("/{id}/close")
    public JobResponse closeJob(@PathVariable Long id) {
        return JobResponse.fromEntity(jobService.closeJob(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id) {
        jobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}