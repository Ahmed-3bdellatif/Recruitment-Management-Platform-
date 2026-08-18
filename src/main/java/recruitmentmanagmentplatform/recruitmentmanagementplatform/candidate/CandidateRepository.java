package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Candidate> findByFullNameContainingIgnoreCase(String fullName);

    List<Candidate> findBySourceContainingIgnoreCase(String source);
}
