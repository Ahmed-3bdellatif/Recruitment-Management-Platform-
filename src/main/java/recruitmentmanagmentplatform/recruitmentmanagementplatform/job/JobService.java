package recruitmentmanagmentplatform.recruitmentmanagementplatform.job;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public Job createJob(Job job, Long createdByUserId) {
        User createdBy = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        job.setId(null);
        job.setCreatedBy(createdBy);
        job.setStatus(JobStatus.DRAFT);
        job.setPublishedAt(null);
        job.setClosedAt(null);

        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public Job getJobById(Long id) {
        return findJobById(id);
    }

    @Transactional(readOnly = true)
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Job> getPublishedJobs() {
        return jobRepository.findByStatus(JobStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public Job updateJob(Long id, Job updatedJob) {
        Job job = findJobById(id);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a closed job");
        }

        job.setTitle(updatedJob.getTitle());
        job.setDescription(updatedJob.getDescription());
        job.setDepartment(updatedJob.getDepartment());
        job.setLocation(updatedJob.getLocation());
        job.setEmploymentType(updatedJob.getEmploymentType());

        return jobRepository.save(job);
    }

    public Job publishJob(Long id) {
        Job job = findJobById(id);

        if (job.getStatus() == JobStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job is already published");
        }

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot publish a closed job");
        }

        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now());
        job.setClosedAt(null);

        return jobRepository.save(job);
    }

    public Job closeJob(Long id) {
        Job job = findJobById(id);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job is already closed");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setClosedAt(LocalDateTime.now());

        return jobRepository.save(job);
    }

    public void deleteJob(Long id) {
        Job job = findJobById(id);
        jobRepository.delete(job);
    }

    private Job findJobById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }
}
