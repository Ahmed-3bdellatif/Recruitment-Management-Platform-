package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.Candidate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private String linkedinUrl;

    private String githubUrl;

    private String currentTitle;

    private BigDecimal yearsOfExperience;

    private String location;

    private String source;

    private Set<String> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static CandidateResponse fromEntity(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .fullName(candidate.getFullName())
                .email(candidate.getEmail())
                .phone(candidate.getPhone())
                .linkedinUrl(candidate.getLinkedinUrl())
                .githubUrl(candidate.getGithubUrl())
                .currentTitle(candidate.getCurrentTitle())
                .yearsOfExperience(candidate.getYearsOfExperience())
                .location(candidate.getLocation())
                .source(candidate.getSource())
                .tags(toTagNames(candidate))
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }

    private static Set<String> toTagNames(Candidate candidate) {
        if (candidate.getTags() == null) {
            return Set.of();
        }

        return candidate.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toSet());
    }
}
