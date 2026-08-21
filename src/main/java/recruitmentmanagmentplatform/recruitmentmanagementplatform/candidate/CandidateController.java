package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

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
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.CandidateResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.CreateCandidateRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.UpdateCandidateRequest;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public List<CandidateResponse> getCandidates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String source) {
        List<Candidate> candidates;
        if (name != null) {
            candidates = candidateService.searchCandidatesByName(name);
        } else if (source != null) {
            candidates = candidateService.getCandidatesBySource(source);
        } else {
            candidates = candidateService.getAllCandidates();
        }

        return candidates.stream().map(CandidateResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public CandidateResponse getCandidate(@PathVariable Long id) {
        return CandidateResponse.fromEntity(candidateService.getCandidateById(id));
    }

    @GetMapping("/email/{email}")
    public CandidateResponse getCandidateByEmail(@PathVariable String email) {
        return CandidateResponse.fromEntity(candidateService.getCandidateByEmail(email));
    }

    @PostMapping
    public ResponseEntity<CandidateResponse> createCandidate(
            @Valid @RequestBody CreateCandidateRequest request) {
        Candidate candidate = candidateService.createCandidate(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(CandidateResponse.fromEntity(candidate));
    }

    @PutMapping("/{id}")
    public CandidateResponse updateCandidate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCandidateRequest request) {
        Candidate candidate = candidateService.updateCandidate(id, request.toEntity());
        return CandidateResponse.fromEntity(candidate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidate(@PathVariable Long id) {
        candidateService.deleteCandidate(id);
        return ResponseEntity.noContent().build();
    }
}