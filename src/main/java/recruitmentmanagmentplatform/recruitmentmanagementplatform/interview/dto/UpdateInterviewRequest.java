package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.InterviewStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInterviewRequest {

    private LocalDateTime scheduledAt;

    private InterviewStatus status;

    private String meetingLink;
}
