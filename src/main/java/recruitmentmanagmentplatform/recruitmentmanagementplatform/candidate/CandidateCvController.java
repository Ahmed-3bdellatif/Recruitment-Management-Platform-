package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.BulkCvUploadResponse;
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

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id) {
        CandidateCv cv = candidateCvService.getCvById(id);
        Resource resource = candidateCvService.loadCvResource(id);

        MediaType mediaType;
        try {
            mediaType = cv.getFileType() != null
                    ? MediaType.parseMediaType(cv.getFileType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cv.getFileName() + "\"")
                .body(resource);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<CandidateCvResponse> uploadCv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("candidateId") Long candidateId,
            @AuthenticationPrincipal UserDetails principal) {
        CandidateCv cv = candidateCvService.uploadCv(candidateId, file, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(CandidateCvResponse.fromEntity(cv));
    }

    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<BulkCvUploadResponse> bulkUploadCvs(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "candidateId", required = false) Long candidateId,
            @AuthenticationPrincipal UserDetails principal) {
        BulkCvUploadResponse response = candidateCvService.bulkUploadCvs(files, candidateId, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<Void> deleteCv(@PathVariable Long id) {
        candidateCvService.deleteCv(id);
        return ResponseEntity.noContent().build();
    }
}