package recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring;

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
import recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.dto.CreateHiringDecisionRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.dto.HiringDecisionResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring.dto.UpdateHiringDecisionRequest;

@RestController
@RequestMapping("/api/hiring-decisions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class HiringDecisionController {

    private final HiringDecisionService hiringDecisionService;

    @GetMapping
    public List<HiringDecisionResponse> getDecisions(
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) HiringDecisionStatus decision,
            @RequestParam(required = false) Long decidedByUserId) {
        List<HiringDecision> decisions;
        if (applicationId != null) {
            decisions = List.of(hiringDecisionService.getDecisionByApplication(applicationId));
        } else if (decision != null) {
            decisions = hiringDecisionService.getDecisionsByStatus(decision);
        } else if (decidedByUserId != null) {
            decisions = hiringDecisionService.getDecisionsByUser(decidedByUserId);
        } else {
            decisions = hiringDecisionService.getAllDecisions();
        }

        return decisions.stream().map(HiringDecisionResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public HiringDecisionResponse getDecision(@PathVariable Long id) {
        return HiringDecisionResponse.fromEntity(hiringDecisionService.getDecisionById(id));
    }

    @PostMapping
    public ResponseEntity<HiringDecisionResponse> createDecision(
            @Valid @RequestBody CreateHiringDecisionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        HiringDecision decision = hiringDecisionService.createDecision(
                request.getApplicationId(),
                request.getDecision(),
                principal.getUsername(),
                request.getReason());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HiringDecisionResponse.fromEntity(decision));
    }

    @PutMapping("/{id}")
    public HiringDecisionResponse updateDecision(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHiringDecisionRequest request) {
        HiringDecision decision = hiringDecisionService.updateDecision(
                id, request.getDecision(), request.getReason());
        return HiringDecisionResponse.fromEntity(decision);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDecision(@PathVariable Long id) {
        hiringDecisionService.deleteDecision(id);
        return ResponseEntity.noContent().build();
    }
}