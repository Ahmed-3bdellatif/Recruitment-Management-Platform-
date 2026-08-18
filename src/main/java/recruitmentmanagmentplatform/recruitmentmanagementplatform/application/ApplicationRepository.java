package recruitmentmanagmentplatform.recruitmentmanagementplatform.application;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobId(Long jobId);

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByAssignedRecruiterId(Long assignedRecruiterId);

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);
}
