package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser;

public interface CvParser {

    ParsedCvData parse(String rawText);

    String getEngineName();

    boolean isAvailable();
}
