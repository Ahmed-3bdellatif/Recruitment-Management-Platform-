package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto;

import java.math.BigDecimal;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser.ParsedCvData;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParsedCvResponse {

    private Long cvId;
    private Long candidateId;
    private String fullName;
    private String email;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String currentTitle;
    private BigDecimal yearsOfExperience;
    private String location;
    private Set<String> skills;
    private String education;
    private String summary;
    private String parserEngine;

    public static ParsedCvResponse fromParsedData(ParsedCvData data, Long cvId, Long candidateId) {
        return ParsedCvResponse.builder()
                .cvId(cvId)
                .candidateId(candidateId)
                .fullName(data.getFullName())
                .email(data.getEmail())
                .phone(data.getPhone())
                .linkedinUrl(data.getLinkedinUrl())
                .githubUrl(data.getGithubUrl())
                .currentTitle(data.getCurrentTitle())
                .yearsOfExperience(data.getYearsOfExperience())
                .location(data.getLocation())
                .skills(data.getSkills())
                .education(data.getEducation())
                .summary(data.getSummary())
                .parserEngine(data.getParserEngine())
                .build();
    }
}
