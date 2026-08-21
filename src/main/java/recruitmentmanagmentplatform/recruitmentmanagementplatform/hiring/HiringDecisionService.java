package recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring;

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
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class HiringDecisionService {

    private final HiringDecisionRepository hiringDecisionRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public HiringDecision createDecision(Long applicationId, HiringDecisionStatus decision, String decidedByEmail,
            String reason) {
        Application application = findApplicationById(applicationId);
        User decidedBy = findActiveUserByEmail(decidedByEmail);

        if (hiringDecisionRepository.existsByApplicationId(applicationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hiring decision already exists for this application");
        }

        if (isCompletedApplication(application.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot create decision for completed application");
        }

        HiringDecision hiringDecision = HiringDecision.builder()
                .application(application)
                .decision(requireDecision(decision))
                .decidedBy(decidedBy)
                .reason(normalizeOptionalText(reason))
                .build();

        applyDecisionToApplication(application, decision);
        applicationRepository.save(application);

        return hiringDecisionRepository.save(hiringDecision);
    }

    @Transactional(readOnly = true)
    public HiringDecision getDecisionById(Long id) {
        return findDecisionById(id);
    }

    @Transactional(readOnly = true)
    public HiringDecision getDecisionByApplication(Long applicationId) {
        return hiringDecisionRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hiring decision not found"));
    }

    @Transactional(readOnly = true)
    public List<HiringDecision> getAllDecisions() {
        return hiringDecisionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<HiringDecision> getDecisionsByStatus(HiringDecisionStatus decision) {
        return hiringDecisionRepository.findByDecision(decision);
    }

    @Transactional(readOnly = true)
    public List<HiringDecision> getDecisionsByUser(Long decidedByUserId) {
        ensureUserExists(decidedByUserId);
        return hiringDecisionRepository.findByDecidedById(decidedByUserId);
    }

    public HiringDecision updateDecision(Long id, HiringDecisionStatus decision, String reason) {
        HiringDecision hiringDecision = findDecisionById(id);
        Application application = hiringDecision.getApplication();

        hiringDecision.setDecision(requireDecision(decision));
        hiringDecision.setReason(normalizeOptionalText(reason));

        applyDecisionToApplication(application, decision);
        applicationRepository.save(application);

        return hiringDecisionRepository.save(hiringDecision);
    }

    public void deleteDecision(Long id) {
        HiringDecision hiringDecision = findDecisionById(id);
        hiringDecisionRepository.delete(hiringDecision);
    }

    private HiringDecision findDecisionById(Long id) {
        return hiringDecisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hiring decision not found"));
    }

    private Application findApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private User findActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Decision maker not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision maker must be active");
        }

        return user;
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    private HiringDecisionStatus requireDecision(HiringDecisionStatus decision) {
        if (decision == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hiring decision is required");
        }

        return decision;
    }

    private void applyDecisionToApplication(Application application, HiringDecisionStatus decision) {
        if (decision == HiringDecisionStatus.HIRED) {
            application.setStatus(ApplicationStatus.HIRED);
            return;
        }

        if (decision == HiringDecisionStatus.REJECTED) {
            application.setStatus(ApplicationStatus.DISQUALIFIED);
            return;
        }

        application.setStatus(ApplicationStatus.OFFER);
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
