package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateCvService {

    private final CandidateCvRepository candidateCvRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    public CandidateCv createCv(Long candidateId, String fileName, String fileUrl,
            String fileType, Long uploadedByUserId, String parsedText) {
        Candidate candidate = findCandidateById(candidateId);

        CandidateCv cv = CandidateCv.builder()
                .candidate(candidate)
                .fileName(requireText(fileName, "CV file name is required"))
                .fileUrl(requireText(fileUrl, "CV file URL is required"))
                .fileType(normalizeOptionalText(fileType))
                .uploadedBy(findOptionalUserById(uploadedByUserId))
                .parsedText(parsedText)
                .build();

        return candidateCvRepository.save(cv);
    }

    @Transactional(readOnly = true)
    public CandidateCv getCvById(Long id) {
        return findCvById(id);
    }

    @Transactional(readOnly = true)
    public List<CandidateCv> getCvsByCandidate(Long candidateId) {
        ensureCandidateExists(candidateId);
        return candidateCvRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public List<CandidateCv> getAllCvs() {
        return candidateCvRepository.findAll();
    }

    public CandidateCv updateCv(Long id, String fileName, String fileUrl,
            String fileType, Long uploadedByUserId, String parsedText) {
        CandidateCv cv = findCvById(id);

        cv.setFileName(requireText(fileName, "CV file name is required"));
        cv.setFileUrl(requireText(fileUrl, "CV file URL is required"));
        cv.setFileType(normalizeOptionalText(fileType));
        cv.setUploadedBy(findOptionalUserById(uploadedByUserId));
        cv.setParsedText(parsedText);

        return candidateCvRepository.save(cv);
    }

    public void deleteCv(Long id) {
        candidateCvRepository.delete(findCvById(id));
    }

    private CandidateCv findCvById(Long id) {
        return candidateCvRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate CV not found"));
    }

    private Candidate findCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    private void ensureCandidateExists(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
        }
    }

    private User findOptionalUserById(Long id) {
        if (id == null) {
            return null;
        }

        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploader not found"));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}