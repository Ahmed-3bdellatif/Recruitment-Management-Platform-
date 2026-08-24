package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class GeminiAiCvParser implements CvParser {

    private final boolean aiEnabled;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiAiCvParser(
            @Value("${app.cv-parser.ai.enabled:false}") boolean aiEnabled,
            @Value("${app.cv-parser.gemini.api-key:}") String apiKey,
            @Value("${app.cv-parser.gemini.model:gemini-2.5-flash}") String model,
            ObjectMapper objectMapper) {
        this.aiEnabled = aiEnabled;
        this.apiKey = apiKey;
        this.model = StringUtils.hasText(model) ? model : "gemini-2.5-flash";
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public ParsedCvData parse(String rawText) {
        if (!isAvailable() || !StringUtils.hasText(rawText)) {
            throw new IllegalStateException("Gemini AI parser is not enabled or available");
        }

        try {
            String prompt = buildPrompt(rawText);
            String endpoint = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    model, apiKey);

            Map<String, Object> requestBody = Map.of(
                    "contents", java.util.List.of(
                            Map.of("parts", java.util.List.of(Map.of("text", prompt)))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json",
                            "temperature", 0.1
                    )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Gemini API returned non-200 status: {} body: {}", response.statusCode(), response.body());
                throw new RuntimeException("Gemini API call failed with status: " + response.statusCode());
            }

            return parseGeminiResponse(response.body(), rawText);
        } catch (Exception exception) {
            log.warn("AI CV parsing failed, caller will fallback: {}", exception.getMessage());
            throw new RuntimeException("AI CV parsing error: " + exception.getMessage(), exception);
        }
    }

    @Override
    public String getEngineName() {
        return "AI_GEMINI";
    }

    @Override
    public boolean isAvailable() {
        return aiEnabled && StringUtils.hasText(apiKey);
    }

    private String buildPrompt(String resumeText) {
        return """
                You are an expert HR resume and CV parser.
                Extract structured candidate details from the following resume text.
                Return ONLY valid JSON matching this schema:
                {
                  "fullName": "Candidate full name",
                  "email": "candidate@example.com",
                  "phone": "+1-555-0123",
                  "linkedinUrl": "https://linkedin.com/in/...",
                  "githubUrl": "https://github.com/...",
                  "currentTitle": "e.g. Senior Software Engineer",
                  "yearsOfExperience": 5.5,
                  "location": "City, Country",
                  "skills": ["Skill1", "Skill2"],
                  "education": "Degree, Major, Institution",
                  "summary": "Brief professional summary"
                }

                Resume text:
                """ + resumeText;
    }

    ParsedCvData parseGeminiResponse(String responseJson, String rawText) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode candidates = root.path("candidates");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No candidates returned from Gemini API");
        }

        JsonNode contentParts = candidates.get(0).path("content").path("parts");
        if (contentParts.isEmpty()) {
            throw new IllegalStateException("No content parts in Gemini response");
        }

        String textJson = contentParts.get(0).path("text").asText().trim();
        if (textJson.startsWith("```json")) {
            textJson = textJson.substring(7);
        } else if (textJson.startsWith("```")) {
            textJson = textJson.substring(3);
        }
        if (textJson.endsWith("```")) {
            textJson = textJson.substring(0, textJson.length() - 3);
        }
        textJson = textJson.trim();

        ParsedCvData data = objectMapper.readValue(textJson, ParsedCvData.class);
        data.setRawText(rawText);
        data.setParserEngine(getEngineName());
        return data;
    }
}
