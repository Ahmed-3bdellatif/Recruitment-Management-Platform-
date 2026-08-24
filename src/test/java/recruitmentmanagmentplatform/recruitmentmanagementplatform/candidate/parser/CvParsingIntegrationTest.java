package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.Candidate;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateCv;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateCvRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.CandidateTagRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.AuthProvider;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.Role;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleName;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.RoleRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserStatus;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CvParsingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateCvRepository candidateCvRepository;

    @Autowired
    private CandidateTagRepository candidateTagRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Candidate candidate;
    private User hrUser;

    private static final String SAMPLE_RESUME = """
            Sarah Connor
            sarah.connor@example.com | +1 555-987-6543
            https://linkedin.com/in/sarah-connor-lead
            https://github.com/sarahconnor

            Professional Summary:
            Technical Lead and Senior Software Engineer with 8 years of experience in Java, Spring Boot, Microservices, and Cloud infrastructure.

            Experience:
            Technical Lead | Cyberdyne Systems (2018 - Present)
            - Built scalable microservices using Java, Spring Boot, and PostgreSQL.
            - Deployed Kubernetes clusters on AWS using Docker and Terraform.

            Education:
            Master of Science in Software Engineering, MIT

            Skills:
            Java, Spring Boot, Docker, Kubernetes, AWS, PostgreSQL, Redis, React, Python
            """;

    @BeforeEach
    void setUp() {
        candidateCvRepository.deleteAll();
        candidateRepository.deleteAll();
        candidateTagRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role hrRole = roleRepository.save(Role.builder().name(RoleName.HR).description("HR").build());
        roleRepository.save(Role.builder().name(RoleName.ADMIN).description("Admin").build());
        roleRepository.save(Role.builder().name(RoleName.INTERVIEWER).description("Interviewer").build());

        hrUser = userRepository.save(User.builder()
                .fullName("HR Recruiter")
                .email("recruiter@example.com")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(hrRole))
                .build());

        candidate = candidateRepository.save(Candidate.builder()
                .fullName("Sarah Connor")
                .email("sarah.connor@example.com")
                .source("PORTAL")
                .build());
    }

    @Test
    void parseTextEndpointReturnsExtractedData() throws Exception {
        mockMvc.perform(post("/api/candidate-cvs/parse-text")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": %s
                                }
                                """.formatted(escapeJson(SAMPLE_RESUME)))
                        .with(user("recruiter@example.com").roles("HR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"))
                .andExpect(jsonPath("$.email").value("sarah.connor@example.com"))
                .andExpect(jsonPath("$.phone").value("+1 555-987-6543"))
                .andExpect(jsonPath("$.currentTitle").value("Technical Lead"))
                .andExpect(jsonPath("$.skills").isArray())
                .andExpect(jsonPath("$.parserEngine").isNotEmpty());
    }

    @Test
    void parseFileEndpointReturnsExtractedData() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sarah_resume.txt",
                MediaType.TEXT_PLAIN_VALUE,
                SAMPLE_RESUME.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/candidate-cvs/parse-file")
                        .file(file)
                        .with(user("recruiter@example.com").roles("INTERVIEWER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"))
                .andExpect(jsonPath("$.email").value("sarah.connor@example.com"));
    }

    @Test
    void parseStoredCvEnrichesCandidateProfileAndTags() throws Exception {
        CandidateCv cv = candidateCvRepository.save(CandidateCv.builder()
                .candidate(candidate)
                .fileName("sarah_resume.txt")
                .fileUrl("uploads/cvs/mock.txt")
                .fileType("text/plain")
                .parsedText(SAMPLE_RESUME)
                .uploadedBy(hrUser)
                .build());

        mockMvc.perform(post("/api/candidate-cvs/" + cv.getId() + "/parse?applyToCandidate=true")
                        .with(user("recruiter@example.com").roles("HR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cvId").value(cv.getId()))
                .andExpect(jsonPath("$.candidateId").value(candidate.getId()))
                .andExpect(jsonPath("$.fullName").value("Sarah Connor"));

        Candidate updated = candidateRepository.findById(candidate.getId()).orElseThrow();
        assertEquals("Sarah Connor", updated.getFullName());
        assertEquals("+1 555-987-6543", updated.getPhone());
        assertEquals("Technical Lead", updated.getCurrentTitle());
        assertEquals("https://linkedin.com/in/sarah-connor-lead", updated.getLinkedinUrl());
        assertEquals("https://github.com/sarahconnor", updated.getGithubUrl());
        assertNotNull(updated.getYearsOfExperience());
        assertTrue(candidateTagRepository.count() > 0);
    }

    @Test
    void uploadCvAutoEnrichesCandidateAndExtractsSkills() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sarah_resume.txt",
                MediaType.TEXT_PLAIN_VALUE,
                SAMPLE_RESUME.getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/candidate-cvs/upload")
                        .file(file)
                        .param("candidateId", candidate.getId().toString())
                        .with(user("recruiter@example.com").roles("HR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        Candidate updated = candidateRepository.findById(candidate.getId()).orElseThrow();
        assertEquals("Sarah Connor", updated.getFullName());
        assertEquals("+1 555-987-6543", updated.getPhone());
        assertTrue(candidateTagRepository.count() > 0);
    }

    private String escapeJson(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "") + "\"";
    }
}
