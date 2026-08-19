package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping
    public List<InterviewResponse> getInterviews(
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) Long interviewerId,
            @RequestParam(required = false) InterviewStatus status) {
        List<Interview> interviews;
        if (applicationId != null) {
            interviews = interviewService.getInterviewsByApplication(applicationId);
        } else if (interviewerId != null) {
            interviews = interviewService.getInterviewsByInterviewer(interviewerId);
        } else if (status != null) {
            interviews = interviewService.getInterviewsByStatus(status);
        } else {
            interviews = interviewService.getAllInterviews();
        }

        return interviews.stream().map(InterviewResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public InterviewResponse getInterview(@PathVariable Long id) {
        return InterviewResponse.fromEntity(interviewService.getInterviewById(id));
    }

    @PostMapping
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
    public InterviewResponse updateInterview(
            @PathVariable Long id,
            @RequestBody UpdateInterviewRequest request) {
        Interview interview;
        if (request.getScheduledAt() != null || request.getMeetingLink() != null) {
            interview = interviewService.updateInterview(
                    id, null, request.getScheduledAt(), request.getMeetingLink());
        } else {
            interview = interviewService.getInterviewById(id);
        }

        if (request.getStatus() != null) {
            interview = interviewService.updateStatus(id, request.getStatus());
        }

        return InterviewResponse.fromEntity(interview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<InterviewResponse> cancelInterview(@PathVariable Long id) {
        return ResponseEntity.ok(
                InterviewResponse.fromEntity(interviewService.cancelInterview(id)));
    }

    @PutMapping("/{id}/complete")
    public InterviewResponse completeInterview(@PathVariable Long id) {
        return InterviewResponse.fromEntity(interviewService.completeInterview(id));
    }
}