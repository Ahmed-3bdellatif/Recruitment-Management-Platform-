package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RuleBasedCvParser implements CvParser {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("(?i)\\b[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}\\b");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("(?:\\+?\\d{1,3}[-.\s]?)?\\(?\\d{2,4}\\)?[-.\s]?\\d{3,4}[-.\s]?\\d{3,4}");

    private static final Pattern LINKEDIN_PATTERN =
            Pattern.compile("(?i)(https?://)?(www\\.)?linkedin\\.com/in/[a-zA-Z0-9_-]+/?");

    private static final Pattern GITHUB_PATTERN =
            Pattern.compile("(?i)(https?://)?(www\\.)?github\\.com/[a-zA-Z0-9_-]+/?");

    private static final Pattern EXPERIENCE_PATTERN =
            Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*\\+?\\s*(?:years?|yrs?)(?:\\s+of)?\\s+(?:experience|exp)");

    private static final Pattern EXPERIENCE_ALT_PATTERN =
            Pattern.compile("(?i)(?:experience|exp)\\s*[:\\-]\\s*(\\d+(?:\\.\\d+)?)\\s*(?:years?|yrs?)");

    private static final Pattern YEAR_RANGE_PATTERN =
            Pattern.compile("\\b(20\\d{2}|19\\d{2})\\s*(?:-|–|to)\\s*(20\\d{2}|present|current|now)\\b", Pattern.CASE_INSENSITIVE);

    private static final List<String> KNOWN_TITLES = List.of(
            "Principal Software Engineer", "Staff Software Engineer", "Lead Software Engineer",
            "Senior Software Engineer", "Software Engineer", "Junior Software Engineer",
            "Full Stack Developer", "Full Stack Engineer", "Frontend Developer", "Frontend Engineer",
            "Backend Developer", "Backend Engineer", "DevOps Engineer", "Cloud Engineer", "Cloud Architect",
            "Site Reliability Engineer", "SRE", "Solutions Architect", "System Architect", "Enterprise Architect",
            "Data Scientist", "Data Engineer", "Machine Learning Engineer", "ML Engineer", "AI Engineer", "MLOps Engineer",
            "QA Engineer", "Software QA", "Test Automation Engineer", "Security Engineer",
            "Engineering Manager", "Technical Lead", "Tech Lead",
            "Product Manager", "Project Manager", "Scrum Master",
            "Mobile Developer", "iOS Developer", "Android Developer", "Flutter Developer"
    );

    private static final List<String> SKILLS_DICTIONARY = List.of(
            "Java", "Kotlin", "Scala", "Spring", "Spring Boot", "Spring Security", "Hibernate", "JPA", "Microservices", "REST", "GraphQL", "gRPC",
            "Python", "Django", "Flask", "FastAPI", "Pandas", "NumPy", "TensorFlow", "PyTorch",
            "C++", "C#", ".NET", ".NET Core", "Go", "Golang", "Rust", "Ruby", "Ruby on Rails", "PHP", "Laravel",
            "JavaScript", "TypeScript", "React", "React Native", "Angular", "Vue", "Vue.js", "Node.js", "Express", "Next.js", "Nuxt.js", "Svelte",
            "HTML", "CSS", "Tailwind", "Tailwind CSS", "Bootstrap", "Redux", "Sass",
            "SQL", "MySQL", "PostgreSQL", "Oracle", "MongoDB", "Redis", "Elasticsearch", "Cassandra", "DynamoDB", "Snowflake", "BigQuery",
            "Docker", "Kubernetes", "AWS", "Azure", "GCP", "Google Cloud", "Terraform", "Ansible", "CI/CD", "Jenkins", "GitHub Actions", "Git", "GitHub", "GitLab",
            "Kafka", "RabbitMQ", "ActiveMQ", "Linux", "Unix", "Bash", "Shell",
            "JUnit", "Mockito", "Selenium", "Cypress", "Playwright", "Postman", "Agile", "Scrum", "Jira", "Maven", "Gradle"
    );

    @Override
    public ParsedCvData parse(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            return ParsedCvData.builder()
                    .parserEngine(getEngineName())
                    .build();
        }

        String email = extractEmail(rawText);
        String phone = extractPhone(rawText);
        String linkedin = extractLinkedIn(rawText);
        String github = extractGitHub(rawText);
        String title = extractTitle(rawText);
        BigDecimal experience = extractExperience(rawText);
        Set<String> skills = extractSkills(rawText);
        String education = extractEducation(rawText);
        String fullName = extractFullName(rawText, email, phone);
        String location = extractLocation(rawText);
        String summary = extractSummary(rawText);

        return ParsedCvData.builder()
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .linkedinUrl(linkedin)
                .githubUrl(github)
                .currentTitle(title)
                .yearsOfExperience(experience)
                .location(location)
                .skills(skills)
                .education(education)
                .summary(summary)
                .rawText(rawText)
                .parserEngine(getEngineName())
                .build();
    }

    @Override
    public String getEngineName() {
        return "RULE_BASED";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group().toLowerCase(Locale.ROOT) : null;
    }

    private String extractPhone(String text) {
        Matcher matcher = PHONE_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group().trim();
            long digitCount = candidate.chars().filter(Character::isDigit).count();
            if (digitCount >= 7 && digitCount <= 15) {
                return candidate;
            }
        }
        return null;
    }

    private String extractLinkedIn(String text) {
        Matcher matcher = LINKEDIN_PATTERN.matcher(text);
        if (matcher.find()) {
            String url = matcher.group();
            return url.startsWith("http") ? url : "https://" + url;
        }
        return null;
    }

    private String extractGitHub(String text) {
        Matcher matcher = GITHUB_PATTERN.matcher(text);
        if (matcher.find()) {
            String url = matcher.group();
            return url.startsWith("http") ? url : "https://" + url;
        }
        return null;
    }

    private String extractTitle(String text) {
        String bestTitle = null;
        int earliestIndex = Integer.MAX_VALUE;

        for (String title : KNOWN_TITLES) {
            Pattern pattern = Pattern.compile("(?i)\\b" + Pattern.quote(title) + "\\b");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                if (matcher.start() < earliestIndex) {
                    earliestIndex = matcher.start();
                    bestTitle = title;
                }
            }
        }
        return bestTitle;
    }

    private BigDecimal extractExperience(String text) {
        Matcher expMatcher = EXPERIENCE_PATTERN.matcher(text);
        if (expMatcher.find()) {
            try {
                return new BigDecimal(expMatcher.group(1)).setScale(1, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
            }
        }

        Matcher altMatcher = EXPERIENCE_ALT_PATTERN.matcher(text);
        if (altMatcher.find()) {
            try {
                return new BigDecimal(altMatcher.group(1)).setScale(1, RoundingMode.HALF_UP);
            } catch (Exception ignored) {
            }
        }

        // Calculate from year ranges (e.g. 2018 - Present)
        int currentYear = Year.now().getValue();
        int minYear = currentYear;
        int maxYear = 0;
        boolean foundRanges = false;

        Matcher rangeMatcher = YEAR_RANGE_PATTERN.matcher(text);
        while (rangeMatcher.find()) {
            try {
                int start = Integer.parseInt(rangeMatcher.group(1));
                String endStr = rangeMatcher.group(2).toLowerCase(Locale.ROOT);
                int end = (endStr.contains("present") || endStr.contains("current") || endStr.contains("now"))
                        ? currentYear : Integer.parseInt(endStr);

                if (start <= end && start >= 1970 && end <= currentYear + 1) {
                    minYear = Math.min(minYear, start);
                    maxYear = Math.max(maxYear, end);
                    foundRanges = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (foundRanges && minYear < currentYear) {
            int diff = maxYear - minYear;
            if (diff > 0 && diff <= 50) {
                return BigDecimal.valueOf(diff).setScale(1, RoundingMode.HALF_UP);
            }
        }

        return null;
    }

    private Set<String> extractSkills(String text) {
        Set<String> matchedSkills = new HashSet<>();
        for (String skill : SKILLS_DICTIONARY) {
            String escaped = Pattern.quote(skill);
            Pattern pattern = Pattern.compile("(?i)(?:^|[^a-zA-Z0-9#+])" + escaped + "(?:$|[^a-zA-Z0-9#+])");
            if (pattern.matcher(text).find()) {
                matchedSkills.add(skill);
            }
        }
        return matchedSkills;
    }

    private static final List<String> SECTION_HEADERS = List.of(
            "summary", "professional summary", "profile", "about me", "objective",
            "experience", "work experience", "employment history", "professional experience",
            "education", "academic background", "qualifications",
            "skills", "technical skills", "core competencies",
            "projects", "certifications", "languages", "references", "contact", "contact information"
    );

    private String extractEducation(String text) {
        List<String> eduKeywords = List.of(
                "Bachelor", "Master", "B.Sc", "M.Sc", "Ph.D", "Doctorate", "B.S.", "M.S.", "B.Tech", "M.Tech", "B.E.", "M.E.", "MBA",
                "Computer Science", "Engineering", "Software Engineering", "Information Technology", "Computer Engineering"
        );

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            for (String kw : eduKeywords) {
                if (trimmed.toLowerCase(Locale.ROOT).contains(kw.toLowerCase(Locale.ROOT)) && trimmed.length() < 140) {
                    return trimmed;
                }
            }
        }
        return null;
    }

    private String extractFullName(String text, String email, String phone) {
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }

            String lower = trimmed.toLowerCase(Locale.ROOT);
            if (email != null && trimmed.contains(email)) {
                continue;
            }
            if (phone != null && trimmed.contains(phone)) {
                continue;
            }
            if (lower.contains("resume")
                    || lower.contains("curriculum vitae")
                    || lower.contains("cv")
                    || lower.startsWith("page ")
                    || SECTION_HEADERS.contains(lower)) {
                continue;
            }

            // A candidate name typically has 2 to 4 words and is under 50 characters
            String[] words = trimmed.split("\\s+");
            if (words.length >= 2 && words.length <= 4 && trimmed.length() <= 50
                    && trimmed.matches("^[a-zA-Z\\s.'-]+$")) {
                return trimmed;
            }
        }
        return null;
    }

    private String extractLocation(String text) {
        Pattern locPattern = Pattern.compile("(?i)(?:location|address|city)\\s*[:\\-]\\s*([^\\r\\n]{2,60})");
        Matcher matcher = locPattern.matcher(text);
        if (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (StringUtils.hasText(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String extractSummary(String text) {
        Pattern summaryHeader = Pattern.compile("(?i)(?:summary|professional summary|profile|about me)\\s*[:\\-\\n]+\\s*(.{20,300})", Pattern.DOTALL);
        Matcher matcher = summaryHeader.matcher(text);
        if (matcher.find()) {
            String matched = matcher.group(1).split("\\r?\\n\\r?\\n")[0].trim();
            return matched.replaceAll("\\s+", " ");
        }
        return null;
    }
}
