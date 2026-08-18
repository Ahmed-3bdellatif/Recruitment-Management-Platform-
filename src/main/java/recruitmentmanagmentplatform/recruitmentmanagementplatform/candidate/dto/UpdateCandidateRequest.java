package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.Candidate;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateTag;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCandidateRequest {

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

    public Candidate toEntity() {
        return Candidate.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .linkedinUrl(linkedinUrl)
                .githubUrl(githubUrl)
                .currentTitle(currentTitle)
                .yearsOfExperience(yearsOfExperience)
                .location(location)
                .source(source)
                .tags(toCandidateTags(tags))
                .build();
    }

    private Set<CandidateTag> toCandidateTags(Set<String> tags) {
        if (tags == null) {
            return null;
        }

        return tags.stream()
                .map(tag -> CandidateTag.builder().name(tag).build())
                .collect(Collectors.toSet());
    }
}
