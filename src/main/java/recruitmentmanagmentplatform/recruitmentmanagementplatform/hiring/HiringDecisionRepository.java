package recruitmentmanagmentplatform.recruitmentmanagementplatform.hiring;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HiringDecisionRepository extends JpaRepository<HiringDecision, Long> {

    Optional<HiringDecision> findByApplicationId(Long applicationId);

    List<HiringDecision> findByDecision(HiringDecisionStatus decision);

    List<HiringDecision> findByDecidedById(Long decidedById);

    boolean existsByApplicationId(Long applicationId);
}
