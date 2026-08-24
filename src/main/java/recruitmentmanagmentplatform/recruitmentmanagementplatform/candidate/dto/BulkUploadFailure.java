package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadFailure {

    private String fileName;
    private String errorMessage;
}
