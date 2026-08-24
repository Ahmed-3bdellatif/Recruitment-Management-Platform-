package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
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
public class ParsedCvData {

    private String fullName;
    private String email;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String currentTitle;
    private BigDecimal yearsOfExperience;
    private String location;

    @Builder.Default
    private Set<String> skills = new HashSet<>();

    private String education;
    private String summary;
    private String rawText;
    private String parserEngine;
}
