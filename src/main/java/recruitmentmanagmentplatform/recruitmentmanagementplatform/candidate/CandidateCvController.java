package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

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
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.CandidateCvResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.CreateCandidateCvRequest;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.UpdateCandidateCvRequest;

@RestController
@RequestMapping("/api/candidate-cvs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'HR', 'INTERVIEWER')")
public class CandidateCvController {

    private final CandidateCvService candidateCvService;

    @GetMapping
    public List<CandidateCvResponse> getCvs(
            @RequestParam(required = false) Long candidateId) {
        List<CandidateCv> cvs = candidateId == null
                ? candidateCvService.getAllCvs()
                : candidateCvService.getCvsByCandidate(candidateId);

        return cvs.stream().map(CandidateCvResponse::fromEntity).toList();
    }

    @GetMapping("/{id}")
    public CandidateCvResponse getCv(@PathVariable Long id) {
        return CandidateCvResponse.fromEntity(candidateCvService.getCvById(id));
    }

    @PostMapping
    public ResponseEntity<CandidateCvResponse> createCv(
            @Valid @RequestBody CreateCandidateCvRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        CandidateCv cv = candidateCvService.createCv(
                request.getCandidateId(),
                request.getFileName(),
                request.getFileUrl(),
                request.getFileType(),
                principal.getUsername(),
                request.getParsedText());

        return ResponseEntity.status(HttpStatus.CREATED).body(CandidateCvResponse.fromEntity(cv));
    }

    @PutMapping("/{id}")
    public CandidateCvResponse updateCv(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCandidateCvRequest request) {
        CandidateCv cv = candidateCvService.updateCv(
                id,
                request.getFileName(),
                request.getFileUrl(),
                request.getFileType(),
                request.getParsedText());

        return CandidateCvResponse.fromEntity(cv);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCv(@PathVariable Long id) {
        candidateCvService.deleteCv(id);
        return ResponseEntity.noContent().build();
    }
}