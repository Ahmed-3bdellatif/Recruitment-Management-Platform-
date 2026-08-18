package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateService {

    private final CandidateRepository candidateRepository;

    public Candidate createCandidate(Candidate candidate) {
        String email = normalizeEmail(candidate.getEmail());

        if (candidateRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate email already exists");
        }

        candidate.setId(null);
        candidate.setEmail(email);

        return candidateRepository.save(candidate);
    }

    @Transactional(readOnly = true)
    public Candidate getCandidateById(Long id) {
        return findCandidateById(id);
    }

    @Transactional(readOnly = true)
    public Candidate getCandidateByEmail(String email) {
        return candidateRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    @Transactional(readOnly = true)
    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Candidate> searchCandidatesByName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            return candidateRepository.findAll();
        }

        return candidateRepository.findByFullNameContainingIgnoreCase(fullName.trim());
    }

    @Transactional(readOnly = true)
    public List<Candidate> getCandidatesBySource(String source) {
        if (!StringUtils.hasText(source)) {
            return candidateRepository.findAll();
        }

        return candidateRepository.findBySourceContainingIgnoreCase(source.trim());
    }

    public Candidate updateCandidate(Long id, Candidate updatedCandidate) {
        Candidate candidate = findCandidateById(id);
        String email = normalizeEmail(updatedCandidate.getEmail());

        candidateRepository.findByEmail(email)
                .filter(existingCandidate -> !Objects.equals(existingCandidate.getId(), id))
                .ifPresent(existingCandidate -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate email already exists");
                });

        candidate.setFullName(updatedCandidate.getFullName());
        candidate.setEmail(email);
        candidate.setPhone(updatedCandidate.getPhone());
        candidate.setLinkedinUrl(updatedCandidate.getLinkedinUrl());
        candidate.setGithubUrl(updatedCandidate.getGithubUrl());
        candidate.setCurrentTitle(updatedCandidate.getCurrentTitle());
        candidate.setYearsOfExperience(updatedCandidate.getYearsOfExperience());
        candidate.setLocation(updatedCandidate.getLocation());
        candidate.setSource(updatedCandidate.getSource());

        return candidateRepository.save(candidate);
    }

    public void deleteCandidate(Long id) {
        Candidate candidate = findCandidateById(id);
        candidateRepository.delete(candidate);
    }

    private Candidate findCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate email is required");
        }

        return email.trim().toLowerCase();
    }
}
