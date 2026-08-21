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
import org.springframework.web.bind.annotation.RestController;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto.CreateInterviewFeedbackRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto.InterviewFeedbackResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto.UpdateInterviewFeedbackRequest;

@RestController
@RequiredArgsConstructor
public class InterviewFeedbackController {

    private final InterviewService interviewService;

    @GetMapping("/api/interviews/{interviewId}/feedback")
        @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'INTERVIEWER')")
        public List<InterviewFeedbackResponse> getFeedback(
                        @PathVariable Long interviewId,
                        @AuthenticationPrincipal UserDetails principal) {
                return interviewService.getFeedbackByInterview(interviewId, principal.getUsername()).stream()
                .map(InterviewFeedbackResponse::fromEntity)
                .toList();
    }

    @PostMapping("/api/interviews/{interviewId}/feedback")
    public ResponseEntity<InterviewFeedbackResponse> submitFeedback(
            @PathVariable Long interviewId,
            @Valid @RequestBody CreateInterviewFeedbackRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        InterviewFeedback feedback = InterviewFeedback.builder()
                .technicalScore(request.getTechnicalScore())
                .communicationScore(request.getCommunicationScore())
                .problemSolvingScore(request.getProblemSolvingScore())
                .overallScore(request.getOverallScore())
                .comments(request.getComments())
                .build();

        InterviewFeedback savedFeedback = interviewService.addFeedback(interviewId, feedback, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(InterviewFeedbackResponse.fromEntity(savedFeedback));
    }

    @PutMapping("/api/feedback/{id}")
    public InterviewFeedbackResponse updateFeedback(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInterviewFeedbackRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        InterviewFeedback feedback = InterviewFeedback.builder()
                .technicalScore(request.getTechnicalScore())
                .communicationScore(request.getCommunicationScore())
                .problemSolvingScore(request.getProblemSolvingScore())
                .overallScore(request.getOverallScore())
                .comments(request.getComments())
                .build();

        return InterviewFeedbackResponse.fromEntity(
                interviewService.updateFeedback(id, feedback, principal.getUsername()));
    }

    @DeleteMapping("/api/feedback/{id}")
        public ResponseEntity<Void> deleteFeedback(
                        @PathVariable Long id,
                        @AuthenticationPrincipal UserDetails principal) {
                interviewService.deleteFeedback(id, principal.getUsername());
        return ResponseEntity.noContent().build();
    }
}