package recruitmentmanagmentplatform.recruitmentmanagementplatform.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInterviewFeedbackRequest {

    @Min(value = 0, message = "Technical score must be at least 0")
    @Max(value = 10, message = "Technical score must not exceed 10")
    private Integer technicalScore;

    @Min(value = 0, message = "Communication score must be at least 0")
    @Max(value = 10, message = "Communication score must not exceed 10")
    private Integer communicationScore;

    @Min(value = 0, message = "Problem solving score must be at least 0")
    @Max(value = 10, message = "Problem solving score must not exceed 10")
    private Integer problemSolvingScore;

    @Min(value = 0, message = "Overall score must be at least 0")
    @Max(value = 10, message = "Overall score must not exceed 10")
    private BigDecimal overallScore;

    private String comments;
}
