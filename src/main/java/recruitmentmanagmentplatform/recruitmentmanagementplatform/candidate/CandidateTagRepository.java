package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateTagRepository extends JpaRepository<CandidateTag, Long> {

    Optional<CandidateTag> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
