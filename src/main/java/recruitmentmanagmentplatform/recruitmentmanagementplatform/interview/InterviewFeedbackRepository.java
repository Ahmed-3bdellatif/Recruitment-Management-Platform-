package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewFeedbackRepository extends JpaRepository<InterviewFeedback, Long> {

    List<InterviewFeedback> findByInterviewId(Long interviewId);
}
