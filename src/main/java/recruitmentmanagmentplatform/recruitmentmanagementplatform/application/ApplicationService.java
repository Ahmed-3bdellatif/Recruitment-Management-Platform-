package recruitmentmanagmentplatform.recruitmentmanagementplatform.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.Candidate;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.Job;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.JobRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.job.JobStatus;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final CandidateRepository candidateRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public Application applyToJob(Long candidateId, Long jobId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate can only apply to published jobs");
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate already applied to this job");
        }

        Application application = Application.builder()
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.APPLIED)
                .build();

        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public Application getApplicationById(Long id) {
        return findApplicationById(id);
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByCandidate(Long candidateId) {
        ensureCandidateExists(candidateId);
        return applicationRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByJob(Long jobId) {
        ensureJobExists(jobId);
        return applicationRepository.findByJobId(jobId);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Application> getApplicationsByRecruiter(Long recruiterId) {
        ensureUserExists(recruiterId);
        return applicationRepository.findByAssignedRecruiterId(recruiterId);
    }

    public Application updateStatus(Long applicationId, ApplicationStatus status) {
        Application application = findApplicationById(applicationId);

        if (application.getStatus() == ApplicationStatus.HIRED
                || application.getStatus() == ApplicationStatus.DISQUALIFIED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a completed application");
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public Application assignRecruiter(Long applicationId, Long recruiterId) {
        Application application = findApplicationById(applicationId);
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recruiter not found"));

        application.setAssignedRecruiter(recruiter);
        return applicationRepository.save(application);
    }

    public void deleteApplication(Long id) {
        Application application = findApplicationById(id);
        applicationRepository.delete(application);
    }

    private Application findApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private void ensureCandidateExists(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
        }
    }

    private void ensureJobExists(Long jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
        }
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }
}
