package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto.CreateInterviewRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto.InterviewResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto.UpdateInterviewRequest;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'INTERVIEWER')")
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public List<InterviewResponse> getInterviews(
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) Long interviewerId,
            @RequestParam(required = false) InterviewStatus status,
            @AuthenticationPrincipal UserDetails principal) {
        List<Interview> interviews;
        if (principal.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_HR"))) {
            if (applicationId != null) {
            interviews = interviewService.getInterviewsByApplication(applicationId);
            } else if (interviewerId != null) {
            interviews = interviewService.getInterviewsByInterviewer(interviewerId);
            } else if (status != null) {
            interviews = interviewService.getInterviewsByStatus(status);
            } else {
            interviews = interviewService.getAllInterviews();
            }
        } else {
            interviews = interviewService.getInterviewsForUser(principal.getUsername());
        }

        return interviews.stream().map(InterviewResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public InterviewResponse getInterview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return InterviewResponse.fromEntity(interviewService.getInterviewById(id, principal.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<InterviewResponse> scheduleInterview(
            @Valid @RequestBody CreateInterviewRequest request) {
        Interview interview = interviewService.scheduleInterview(
                request.getApplicationId(),
                request.getInterviewerId(),
                request.getScheduledAt(),
                request.getMeetingLink());

        return ResponseEntity.status(HttpStatus.CREATED).body(InterviewResponse.fromEntity(interview));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'INTERVIEWER')")
    public InterviewResponse updateInterview(
            @PathVariable Long id,
            @RequestBody UpdateInterviewRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        Interview interview;
        if (request.getScheduledAt() != null || request.getMeetingLink() != null) {
            interview = interviewService.updateInterview(
                    id, null, request.getScheduledAt(), request.getMeetingLink(), principal.getUsername());
        } else {
            interview = interviewService.getInterviewById(id, principal.getUsername());
        }

        if (request.getStatus() != null) {
            interview = interviewService.updateStatus(id, request.getStatus(), principal.getUsername());
        }

        return InterviewResponse.fromEntity(interview);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<InterviewResponse> cancelInterview(@PathVariable Long id) {
        return ResponseEntity.ok(
                InterviewResponse.fromEntity(interviewService.cancelInterview(id)));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'INTERVIEWER')")
    public InterviewResponse completeInterview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return InterviewResponse.fromEntity(interviewService.completeInterview(id, principal.getUsername()));
    }
}