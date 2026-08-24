package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
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
class CandidateCvUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private CandidateCvRepository candidateCvRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Candidate testCandidate;
    private User hrUser;

    @BeforeEach
    void setUp() {
        candidateCvRepository.deleteAll();
        candidateRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role hrRole = roleRepository.save(Role.builder().name(RoleName.HR).description("HR Role").build());
        roleRepository.save(Role.builder().name(RoleName.ADMIN).description("Admin Role").build());
        roleRepository.save(Role.builder().name(RoleName.INTERVIEWER).description("Interviewer Role").build());

        hrUser = userRepository.save(User.builder()
                .fullName("HR Manager")
                .email("hr@example.com")
                .passwordHash("password")
                .status(UserStatus.ACTIVE)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(hrRole))
                .build());

        testCandidate = candidateRepository.save(Candidate.builder()
                .fullName("Jane Candidate")
                .email("jane.candidate@example.com")
                .phone("555-0123")
                .build());
    }

    @Test
    void singleCvUploadSucceeds() throws Exception {
        byte[] pdfContent = "%PDF-1.4 Mock PDF CV Content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "jane_resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                pdfContent);

        mockMvc.perform(multipart("/api/candidate-cvs/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString())
                        .with(user("hr@example.com").roles("HR")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.candidate.id").value(testCandidate.getId()))
                .andExpect(jsonPath("$.fileName").value("jane_resume.pdf"))
                .andExpect(jsonPath("$.fileType").value(MediaType.APPLICATION_PDF_VALUE));

        assertEquals(1, candidateCvRepository.count());
    }

    @Test
    void downloadCvReturnsFileContentAndHeaders() throws Exception {
        byte[] textContent = "Resume content for plain text file".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                MediaType.TEXT_PLAIN_VALUE,
                textContent);

        String responseJson = mockMvc.perform(multipart("/api/candidate-cvs/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString())
                        .with(user("hr@example.com").roles("HR")))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long cvId = candidateCvRepository.findAll().get(0).getId();

        mockMvc.perform(get("/api/candidate-cvs/" + cvId + "/download")
                        .with(user("interviewer@example.com").roles("INTERVIEWER")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.txt\""))
                .andExpect(content().bytes(textContent));
    }

    @Test
    void bulkUploadMultipleFilesSucceeds() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "cv1.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Content 1".getBytes(StandardCharsets.UTF_8));

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "cv2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Skills: Java, Spring Boot, SQL".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/candidate-cvs/bulk-upload")
                        .file(file1)
                        .file(file2)
                        .param("candidateId", testCandidate.getId().toString())
                        .with(user("hr@example.com").roles("HR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles").value(2))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failureCount").value(0))
                .andExpect(jsonPath("$.successfulUploads.length()").value(2));

        assertEquals(2, candidateCvRepository.count());
    }

    @Test
    void bulkUploadZipArchiveSucceedsAndAutoResolvesCandidates() throws Exception {
        byte[] zipBytes = createZipArchive(
                new String[]{"john_doe_john.doe@example.com.pdf", "alice_smith_alice.smith@example.com.txt"},
                new String[]{"PDF CV content", "Alice Smith Skills: React, Node.js"}
        );

        MockMultipartFile zipFile = new MockMultipartFile(
                "files",
                "candidates_batch.zip",
                "application/zip",
                zipBytes);

        mockMvc.perform(multipart("/api/candidate-cvs/bulk-upload")
                        .file(zipFile)
                        .with(user("hr@example.com").roles("HR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles").value(2))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failureCount").value(0));

        assertTrue(candidateRepository.findByEmail("john.doe@example.com").isPresent());
        assertTrue(candidateRepository.findByEmail("alice.smith@example.com").isPresent());
        assertEquals(2, candidateCvRepository.count());
    }

    @Test
    void uploadRejectsUnsupportedExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "binary-data".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/candidate-cvs/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString())
                        .with(user("hr@example.com").roles("HR")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadRequiresHrOrAdminRole() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cv.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "data".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/candidate-cvs/upload")
                        .file(file)
                        .param("candidateId", testCandidate.getId().toString())
                        .with(user("interviewer@example.com").roles("INTERVIEWER")))
                .andExpect(status().isForbidden());
    }

    private byte[] createZipArchive(String[] fileNames, String[] contents) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream)) {
            for (int i = 0; i < fileNames.length; i++) {
                ZipEntry entry = new ZipEntry(fileNames[i]);
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(contents[i].getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();
            }
        }
        return byteArrayOutputStream.toByteArray();
    }
}
