package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.Candidate;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateTag;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateTagRepository;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class CvParsingService implements CvParser {

    private final RuleBasedCvParser ruleBasedCvParser;
    private final GeminiAiCvParser geminiAiCvParser;
    private final CandidateTagRepository candidateTagRepository;
    private final CandidateRepository candidateRepository;

    @Override
    public ParsedCvData parse(String rawText) {
        return parseText(rawText);
    }

    @Override
    public String getEngineName() {
        return geminiAiCvParser.isAvailable() ? geminiAiCvParser.getEngineName() : ruleBasedCvParser.getEngineName();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public ParsedCvData parseText(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return ParsedCvData.builder().parserEngine(ruleBasedCvParser.getEngineName()).build();
        }

        if (geminiAiCvParser.isAvailable()) {
            try {
                log.info("Attempting CV parsing via Gemini AI engine");
                return geminiAiCvParser.parse(rawText);
            } catch (Exception exception) {
                log.warn("Gemini AI CV parsing failed, falling back to rule-based engine: {}", exception.getMessage());
            }
        }

        log.info("Parsing CV using Rule-Based engine");
        return ruleBasedCvParser.parse(rawText);
    }

    @Transactional
    public void enrichCandidate(Candidate candidate, ParsedCvData data) {
        if (candidate == null || data == null) {
            return;
        }

        boolean[] modified = {false};

        setIfBlank(candidate.getFullName(), data.getFullName(), val -> { candidate.setFullName(val); modified[0] = true; });
        setIfBlank(candidate.getPhone(), data.getPhone(), val -> { candidate.setPhone(val); modified[0] = true; });
        setIfBlank(candidate.getLinkedinUrl(), data.getLinkedinUrl(), val -> { candidate.setLinkedinUrl(val); modified[0] = true; });
        setIfBlank(candidate.getGithubUrl(), data.getGithubUrl(), val -> { candidate.setGithubUrl(val); modified[0] = true; });
        setIfBlank(candidate.getCurrentTitle(), data.getCurrentTitle(), val -> { candidate.setCurrentTitle(val); modified[0] = true; });
        setIfBlank(candidate.getLocation(), data.getLocation(), val -> { candidate.setLocation(val); modified[0] = true; });

        if (candidate.getYearsOfExperience() == null && data.getYearsOfExperience() != null) {
            candidate.setYearsOfExperience(data.getYearsOfExperience());
            modified[0] = true;
        }

        if (data.getSkills() != null && !data.getSkills().isEmpty()) {
            if (candidate.getTags() == null) {
                candidate.setTags(new HashSet<>());
            }

            for (String skill : data.getSkills()) {
                if (StringUtils.hasText(skill)) {
                    String cleanSkill = skill.trim();
                    CandidateTag tag = candidateTagRepository.findByNameIgnoreCase(cleanSkill)
                            .orElseGet(() -> candidateTagRepository.save(CandidateTag.builder().name(cleanSkill).build()));
                    if (candidate.getTags().add(tag)) {
                        modified[0] = true;
                    }
                }
            }
        }

        if (modified[0]) {
            candidateRepository.save(candidate);
        }
    }

    private void setIfBlank(String currentVal, String newVal, Consumer<String> setter) {
        if (!StringUtils.hasText(currentVal) && StringUtils.hasText(newVal)) {
            setter.accept(newVal.trim());
        }
    }
}
