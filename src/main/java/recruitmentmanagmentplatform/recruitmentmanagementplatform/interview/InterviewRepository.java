package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationId(Long applicationId);

    List<Interview> findByInterviewerId(Long interviewerId);

    List<Interview> findByStatus(InterviewStatus status);

}
