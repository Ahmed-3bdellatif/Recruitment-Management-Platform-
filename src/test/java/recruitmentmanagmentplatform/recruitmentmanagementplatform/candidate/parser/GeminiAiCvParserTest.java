package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GeminiAiCvParserTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void isAvailableReturnsTrueWhenEnabledAndKeySet() {
        GeminiAiCvParser parser = new GeminiAiCvParser(true, "mock-api-key", "gemini-2.5-flash", objectMapper);
        assertTrue(parser.isAvailable());
        assertEquals("AI_GEMINI", parser.getEngineName());
    }

    @Test
    void isAvailableReturnsFalseWhenDisabledOrKeyEmpty() {
        GeminiAiCvParser disabledParser = new GeminiAiCvParser(false, "mock-api-key", "gemini-2.5-flash", objectMapper);
        assertFalse(disabledParser.isAvailable());

        GeminiAiCvParser noKeyParser = new GeminiAiCvParser(true, "", "gemini-2.5-flash", objectMapper);
        assertFalse(noKeyParser.isAvailable());
    }

    @Test
    void parseGeminiResponseExtractsStructuredDataFromJson() throws Exception {
        GeminiAiCvParser parser = new GeminiAiCvParser(true, "mock-api-key", "gemini-2.5-flash", objectMapper);

        String sampleGeminiResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{\\"fullName\\":\\"Emily Watson\\",\\"email\\":\\"emily.watson@example.com\\",\\"phone\\":\\"+1-555-4321\\",\\"linkedinUrl\\":\\"https://linkedin.com/in/emily-watson\\",\\"githubUrl\\":\\"https://github.com/emilywatson\\",\\"currentTitle\\":\\"Principal Software Engineer\\",\\"yearsOfExperience\\":8.5,\\"location\\":\\"New York, USA\\",\\"skills\\":[\\"Java\\",\\"Spring Boot\\",\\"Kubernetes\\",\\"Kafka\\"],\\"education\\":\\"M.S. in Computer Science, Stanford\\",\\"summary\\":\\"Experienced cloud backend architect.\\"}"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        ParsedCvData data = parser.parseGeminiResponse(sampleGeminiResponse, "raw text");

        assertNotNull(data);
        assertEquals("Emily Watson", data.getFullName());
        assertEquals("emily.watson@example.com", data.getEmail());
        assertEquals("+1-555-4321", data.getPhone());
        assertEquals("https://linkedin.com/in/emily-watson", data.getLinkedinUrl());
        assertEquals("https://github.com/emilywatson", data.getGithubUrl());
        assertEquals("Principal Software Engineer", data.getCurrentTitle());
        assertEquals(new BigDecimal("8.5"), data.getYearsOfExperience());
        assertEquals("New York, USA", data.getLocation());
        assertTrue(data.getSkills().contains("Java"));
        assertTrue(data.getSkills().contains("Spring Boot"));
        assertTrue(data.getSkills().contains("Kubernetes"));
        assertTrue(data.getSkills().contains("Kafka"));
        assertEquals("M.S. in Computer Science, Stanford", data.getEducation());
        assertEquals("Experienced cloud backend architect.", data.getSummary());
        assertEquals("AI_GEMINI", data.getParserEngine());
    }

    @Test
    void parseGeminiResponseStripsMarkdownCodeFences() throws Exception {
        GeminiAiCvParser parser = new GeminiAiCvParser(true, "mock-api-key", "gemini-2.5-flash", objectMapper);

        String sampleWithMarkdown = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "```json\\n{\\"fullName\\":\\"David Miller\\",\\"email\\":\\"david.miller@example.com\\",\\"skills\\":[\\"Python\\",\\"FastAPI\\"]}\\n```"
                          }
                        ]
                      }
                    }
                  ]
                }
                """;

        ParsedCvData data = parser.parseGeminiResponse(sampleWithMarkdown, "raw text");

        assertNotNull(data);
        assertEquals("David Miller", data.getFullName());
        assertEquals("david.miller@example.com", data.getEmail());
        assertTrue(data.getSkills().contains("Python"));
        assertTrue(data.getSkills().contains("FastAPI"));
    }

    @Test
    void parseThrowsWhenNotAvailable() {
        GeminiAiCvParser parser = new GeminiAiCvParser(false, null, "gemini-2.5-flash", objectMapper);
        assertThrows(IllegalStateException.class, () -> parser.parse("some cv text"));
    }
}
