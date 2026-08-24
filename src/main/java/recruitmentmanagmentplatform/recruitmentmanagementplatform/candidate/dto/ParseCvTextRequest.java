package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParseCvTextRequest {

    @NotBlank(message = "CV text is required")
    private String text;

    private Long candidateId;
}
