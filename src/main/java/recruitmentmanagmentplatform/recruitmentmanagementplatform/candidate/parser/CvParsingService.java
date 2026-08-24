package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.Candidate;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateTag;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateTagRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvParsingService {

    private final RuleBasedCvParser ruleBasedCvParser;
    private final GeminiAiCvParser geminiAiCvParser;
    private final CandidateTagRepository candidateTagRepository;
    private final CandidateRepository candidateRepository;

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

        boolean modified = false;

        if (!StringUtils.hasText(candidate.getFullName()) && StringUtils.hasText(data.getFullName())) {
            candidate.setFullName(data.getFullName());
            modified = true;
        }

        if (!StringUtils.hasText(candidate.getPhone()) && StringUtils.hasText(data.getPhone())) {
            candidate.setPhone(data.getPhone());
            modified = true;
        }

        if (!StringUtils.hasText(candidate.getLinkedinUrl()) && StringUtils.hasText(data.getLinkedinUrl())) {
            candidate.setLinkedinUrl(data.getLinkedinUrl());
            modified = true;
        }

        if (!StringUtils.hasText(candidate.getGithubUrl()) && StringUtils.hasText(data.getGithubUrl())) {
            candidate.setGithubUrl(data.getGithubUrl());
            modified = true;
        }

        if (!StringUtils.hasText(candidate.getCurrentTitle()) && StringUtils.hasText(data.getCurrentTitle())) {
            candidate.setCurrentTitle(data.getCurrentTitle());
            modified = true;
        }

        if (candidate.getYearsOfExperience() == null && data.getYearsOfExperience() != null) {
            candidate.setYearsOfExperience(data.getYearsOfExperience());
            modified = true;
        }

        if (!StringUtils.hasText(candidate.getLocation()) && StringUtils.hasText(data.getLocation())) {
            candidate.setLocation(data.getLocation());
            modified = true;
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
                    candidate.getTags().add(tag);
                    modified = true;
                }
            }
        }

        if (modified) {
            candidateRepository.save(candidate);
        }
    }
}
