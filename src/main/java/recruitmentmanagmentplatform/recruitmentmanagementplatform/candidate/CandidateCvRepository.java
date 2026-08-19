package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateCvRepository extends JpaRepository<CandidateCv, Long> {

	List<CandidateCv> findByCandidateId(Long candidateId);
}
