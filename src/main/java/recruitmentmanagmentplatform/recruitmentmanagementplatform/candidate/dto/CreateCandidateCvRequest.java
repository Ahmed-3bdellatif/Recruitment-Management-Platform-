package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateCandidateCvRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    @NotBlank(message = "CV file name is required")
    private String fileName;

    @NotBlank(message = "CV file URL is required")
    private String fileUrl;

    private String fileType;

    private Long uploadedByUserId;

    private String parsedText;
}
