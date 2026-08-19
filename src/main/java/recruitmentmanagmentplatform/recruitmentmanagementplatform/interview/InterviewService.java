package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.Application;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.ApplicationRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.application.ApplicationStatus;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewFeedbackRepository interviewFeedbackRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public Interview scheduleInterview(Long applicationId, Long interviewerId, LocalDateTime scheduledAt,
            String meetingLink) {
        Application application = findApplicationById(applicationId);
        User interviewer = findActiveInterviewerById(interviewerId);

        if (isCompletedApplication(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot schedule interview for completed application");
        }

        Interview interview = Interview.builder()
                .application(application)
                .interviewer(interviewer)
                .scheduledAt(requireScheduledAt(scheduledAt))
                .status(InterviewStatus.SCHEDULED)
                .meetingLink(normalizeOptionalText(meetingLink))
                .build();

        application.setStatus(ApplicationStatus.INTERVIEW);
        applicationRepository.save(application);

        return interviewRepository.save(interview);
    }

    @Transactional(readOnly = true)
    public Interview getInterviewById(Long id) {
        return findInterviewById(id);
    }

    @Transactional(readOnly = true)
    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Interview> getInterviewsByApplication(Long applicationId) {
        ensureApplicationExists(applicationId);
        return interviewRepository.findByApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    public List<Interview> getInterviewsByInterviewer(Long interviewerId) {
        ensureUserExists(interviewerId);
        return interviewRepository.findByInterviewerId(interviewerId);
    }

    @Transactional(readOnly = true)
    public List<Interview> getInterviewsByStatus(InterviewStatus status) {
        return interviewRepository.findByStatus(status);
    }

 

    public Interview updateInterview(Long id, Long interviewerId, LocalDateTime scheduledAt, String meetingLink) {
        Interview interview = findInterviewById(id);

        if (interview.getStatus() == InterviewStatus.CANCELLED
                || interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot update a finished interview");
        }

        if (interviewerId != null) {
            interview.setInterviewer(findActiveInterviewerById(interviewerId));
        }

        if (scheduledAt != null) {
            interview.setScheduledAt(requireScheduledAt(scheduledAt));
        }

        interview.setMeetingLink(normalizeOptionalText(meetingLink));

        return interviewRepository.save(interview);
    }

    public Interview updateStatus(Long id, InterviewStatus status) {
        if (status == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interview status is required");
        }

        Interview interview = findInterviewById(id);
        interview.setStatus(status);

        return interviewRepository.save(interview);
    }

    public Interview completeInterview(Long id) {
        Interview interview = findInterviewById(id);

        if (interview.getStatus() == InterviewStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot complete a cancelled interview");
        }

        interview.setStatus(InterviewStatus.COMPLETED);

        return interviewRepository.save(interview);
    }

    public Interview cancelInterview(Long id) {
        Interview interview = findInterviewById(id);

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a completed interview");
        }

        interview.setStatus(InterviewStatus.CANCELLED);

        return interviewRepository.save(interview);
    }

    public InterviewFeedback addFeedback(Long interviewId, InterviewFeedback feedback) {
        Interview interview = findInterviewById(interviewId);

        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Feedback can only be submitted for a completed interview");
        }

        feedback.setId(null);
        feedback.setInterview(interview);

        return interviewFeedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<InterviewFeedback> getFeedbackByInterview(Long interviewId) {
        ensureInterviewExists(interviewId);
        return interviewFeedbackRepository.findByInterviewId(interviewId);
    }

    public InterviewFeedback updateFeedback(Long feedbackId, InterviewFeedback updatedFeedback) {
        InterviewFeedback feedback = findFeedbackById(feedbackId);

        feedback.setTechnicalScore(updatedFeedback.getTechnicalScore());
        feedback.setCommunicationScore(updatedFeedback.getCommunicationScore());
        feedback.setProblemSolvingScore(updatedFeedback.getProblemSolvingScore());
        feedback.setOverallScore(updatedFeedback.getOverallScore());
        feedback.setComments(updatedFeedback.getComments());

        return interviewFeedbackRepository.save(feedback);
    }

    public void deleteFeedback(Long feedbackId) {
        interviewFeedbackRepository.delete(findFeedbackById(feedbackId));
    }

    public void deleteInterview(Long id) {
        Interview interview = findInterviewById(id);
        interviewRepository.delete(interview);
    }

    private Interview findInterviewById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found"));
    }

    private InterviewFeedback findFeedbackById(Long id) {
        return interviewFeedbackRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview feedback not found"));
    }

    private Application findApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private User findActiveInterviewerById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interviewer not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interviewer must be active");
        }

        boolean hasInterviewerRole = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.INTERVIEWER);

        if (!hasInterviewerRole) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User must have interviewer role");
        }

        return user;
    }

    private void ensureApplicationExists(Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
        }
    }

    private void ensureInterviewExists(Long interviewId) {
        if (!interviewRepository.existsById(interviewId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview not found");
        }
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private LocalDateTime requireScheduledAt(LocalDateTime scheduledAt) {
        if (scheduledAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interview schedule time is required");
        }

        return scheduledAt;
    }

    private boolean isCompletedApplication(ApplicationStatus status) {
        return status == ApplicationStatus.HIRED || status == ApplicationStatus.DISQUALIFIED;
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}
