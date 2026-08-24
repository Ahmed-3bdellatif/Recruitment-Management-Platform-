package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCvUploadResponse {

    private int totalFiles;
    private int successCount;
    private int failureCount;
    private List<CandidateCvResponse> successfulUploads;
    private List<BulkUploadFailure> failedUploads;
}
