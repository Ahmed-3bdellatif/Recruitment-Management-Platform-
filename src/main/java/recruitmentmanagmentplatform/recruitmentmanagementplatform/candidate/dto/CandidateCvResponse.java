package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateCv;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.dto.UserResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateCvResponse {

    private Long id;

    private CandidateResponse candidate;

    private String fileName;

    private String fileUrl;

    private String fileType;

    private UserResponse uploadedBy;

    private String parsedText;

    private LocalDateTime uploadedAt;

    public static CandidateCvResponse fromEntity(CandidateCv cv) {
        return CandidateCvResponse.builder()
                .id(cv.getId())
                .candidate(cv.getCandidate() != null
                        ? CandidateResponse.fromEntity(cv.getCandidate()) : null)
                .fileName(cv.getFileName())
                .fileUrl(cv.getFileUrl())
                .fileType(cv.getFileType())
                .uploadedBy(cv.getUploadedBy() != null
                        ? UserResponse.fromEntity(cv.getUploadedBy()) : null)
                .parsedText(cv.getParsedText())
                .uploadedAt(cv.getUploadedAt())
                .build();
    }
}
