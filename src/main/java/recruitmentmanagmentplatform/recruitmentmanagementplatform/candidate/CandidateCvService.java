package recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.BulkCvUploadResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.BulkUploadFailure;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.CandidateCvResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.dto.ParsedCvResponse;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser.CvParsingService;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.candidate.parser.ParsedCvData;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.storage.FileStorageService;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.User;
import recruitmentmanagmentplatform.recruitmentmanagementplatform.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CandidateCvService {

    private static final Pattern STRICT_EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern FALLBACK_EMAIL_PATTERN = Pattern.compile("(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}");

    private final CandidateCvRepository candidateCvRepository;
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CvParsingService cvParsingService;

    public CandidateCv createCv(Long candidateId, String fileName, String fileUrl,
            String fileType, String uploadedByEmail, String parsedText) {
        Candidate candidate = findCandidateById(candidateId);

        CandidateCv cv = CandidateCv.builder()
                .candidate(candidate)
                .fileName(requireText(fileName, "CV file name is required"))
                .fileUrl(requireText(fileUrl, "CV file URL is required"))
                .fileType(normalizeOptionalText(fileType))
                .uploadedBy(findUserByEmail(uploadedByEmail))
                .parsedText(parsedText)
                .build();

        return candidateCvRepository.save(cv);
    }

    public CandidateCv uploadCv(Long candidateId, MultipartFile file, String uploadedByEmail) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File to upload cannot be empty");
        }

        Candidate candidate = findCandidateById(candidateId);
        User uploader = findUserByEmail(uploadedByEmail);

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "cv_file");
        String storedPath = fileStorageService.storeFile(file, "cvs");
        String fileType = determineFileType(originalFilename, file.getContentType());
        String extractedText = fileStorageService.extractTextIfPossible(file);

        if (StringUtils.hasText(extractedText)) {
            ParsedCvData parsedData = cvParsingService.parseText(extractedText);
            cvParsingService.enrichCandidate(candidate, parsedData);
        }

        CandidateCv cv = CandidateCv.builder()
                .candidate(candidate)
                .fileName(originalFilename)
                .fileUrl(storedPath)
                .fileType(fileType)
                .uploadedBy(uploader)
                .parsedText(extractedText)
                .build();

        return candidateCvRepository.save(cv);
    }

    public BulkCvUploadResponse bulkUploadCvs(List<MultipartFile> files, Long defaultCandidateId, String uploadedByEmail) {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No files provided for bulk upload");
        }

        User uploader = findUserByEmail(uploadedByEmail);
        List<CandidateCvResponse> successfulUploads = new ArrayList<>();
        List<BulkUploadFailure> failedUploads = new ArrayList<>();
        int totalFilesCount = 0;

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                totalFilesCount++;
                failedUploads.add(new BulkUploadFailure(file.getOriginalFilename(), "File is empty"));
                continue;
            }

            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
            if (filename.toLowerCase(Locale.ROOT).endsWith(".zip")) {
                totalFilesCount += processZipArchive(file, defaultCandidateId, uploader, successfulUploads, failedUploads);
            } else {
                totalFilesCount++;
                processSingleFile(file, defaultCandidateId, uploader, successfulUploads, failedUploads);
            }
        }

        return BulkCvUploadResponse.builder()
                .totalFiles(totalFilesCount)
                .successCount(successfulUploads.size())
                .failureCount(failedUploads.size())
                .successfulUploads(successfulUploads)
                .failedUploads(failedUploads)
                .build();
    }

    public ParsedCvResponse parseCv(Long cvId, boolean applyToCandidate) {
        CandidateCv cv = findCvById(cvId);
        String textToParse = cv.getParsedText();

        if (!StringUtils.hasText(textToParse)) {
            try {
                Resource resource = fileStorageService.loadFileAsResource(cv.getFileUrl());
                byte[] bytes = resource.getInputStream().readAllBytes();
                textToParse = fileStorageService.extractTextIfPossible(bytes, cv.getFileName());
                if (StringUtils.hasText(textToParse)) {
                    cv.setParsedText(textToParse);
                    candidateCvRepository.save(cv);
                }
            } catch (Exception exception) {
                log.warn("Could not load file content for parsing: {}", cv.getFileUrl(), exception);
            }
        }

        if (!StringUtils.hasText(textToParse)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No extractable text found in CV for parsing");
        }

        ParsedCvData parsedData = cvParsingService.parseText(textToParse);
        if (applyToCandidate && cv.getCandidate() != null) {
            cvParsingService.enrichCandidate(cv.getCandidate(), parsedData);
        }

        return ParsedCvResponse.fromParsedData(parsedData, cv.getId(),
                cv.getCandidate() != null ? cv.getCandidate().getId() : null);
    }

    public ParsedCvResponse parseFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
        }

        String extractedText = fileStorageService.extractTextIfPossible(file);
        if (!StringUtils.hasText(extractedText)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Could not extract readable text from uploaded file: " + file.getOriginalFilename());
        }

        ParsedCvData parsedData = cvParsingService.parseText(extractedText);
        return ParsedCvResponse.fromParsedData(parsedData, null, null);
    }

    public ParsedCvResponse parseText(String rawText, Long candidateId) {
        if (!StringUtils.hasText(rawText)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text to parse cannot be empty");
        }

        ParsedCvData parsedData = cvParsingService.parseText(rawText);
        if (candidateId != null) {
            Candidate candidate = findCandidateById(candidateId);
            cvParsingService.enrichCandidate(candidate, parsedData);
        }

        return ParsedCvResponse.fromParsedData(parsedData, null, candidateId);
    }

    @Transactional(readOnly = true)
    public Resource loadCvResource(Long id) {
        CandidateCv cv = findCvById(id);
        return fileStorageService.loadFileAsResource(cv.getFileUrl());
    }

    @Transactional(readOnly = true)
    public CandidateCv getCvById(Long id) {
        return findCvById(id);
    }

    @Transactional(readOnly = true)
    public List<CandidateCv> getCvsByCandidate(Long candidateId) {
        ensureCandidateExists(candidateId);
        return candidateCvRepository.findByCandidateId(candidateId);
    }

    @Transactional(readOnly = true)
    public List<CandidateCv> getAllCvs() {
        return candidateCvRepository.findAll();
    }

    public CandidateCv updateCv(Long id, String fileName, String fileUrl,
            String fileType, String parsedText) {
        CandidateCv cv = findCvById(id);

        cv.setFileName(requireText(fileName, "CV file name is required"));
        cv.setFileUrl(requireText(fileUrl, "CV file URL is required"));
        cv.setFileType(normalizeOptionalText(fileType));
        cv.setParsedText(parsedText);

        return candidateCvRepository.save(cv);
    }

    public void deleteCv(Long id) {
        CandidateCv cv = findCvById(id);
        fileStorageService.deleteFile(cv.getFileUrl());
        candidateCvRepository.delete(cv);
    }

    private void processSingleFile(MultipartFile file, Long defaultCandidateId, User uploader,
            List<CandidateCvResponse> successfulUploads, List<BulkUploadFailure> failedUploads) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "cv_file";
        try {
            Candidate candidate = resolveCandidate(defaultCandidateId, filename);
            String storedPath = fileStorageService.storeFile(file, "cvs");
            String fileType = determineFileType(filename, file.getContentType());
            String extractedText = fileStorageService.extractTextIfPossible(file);

            if (StringUtils.hasText(extractedText)) {
                ParsedCvData parsedData = cvParsingService.parseText(extractedText);
                cvParsingService.enrichCandidate(candidate, parsedData);
            }

            CandidateCv cv = candidateCvRepository.save(CandidateCv.builder()
                    .candidate(candidate)
                    .fileName(filename)
                    .fileUrl(storedPath)
                    .fileType(fileType)
                    .uploadedBy(uploader)
                    .parsedText(extractedText)
                    .build());

            successfulUploads.add(CandidateCvResponse.fromEntity(cv));
        } catch (Exception exception) {
            log.warn("Bulk upload failed for file: {}", filename, exception);
            failedUploads.add(new BulkUploadFailure(filename, exception.getMessage()));
        }
    }

    private int processZipArchive(MultipartFile zipFile, Long defaultCandidateId, User uploader,
            List<CandidateCvResponse> successfulUploads, List<BulkUploadFailure> failedUploads) {
        int count = 0;
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zipInputStream.closeEntry();
                    continue;
                }

                String entryName = StringUtils.cleanPath(entry.getName());
                if (entryName.startsWith("__MACOSX") || entryName.startsWith(".")) {
                    zipInputStream.closeEntry();
                    continue;
                }

                int lastSlash = Math.max(entryName.lastIndexOf('/'), entryName.lastIndexOf('\\'));
                String cleanEntryName = lastSlash >= 0 ? entryName.substring(lastSlash + 1) : entryName;
                if (!StringUtils.hasText(cleanEntryName) || cleanEntryName.startsWith(".")) {
                    zipInputStream.closeEntry();
                    continue;
                }

                count++;
                try {
                    byte[] content = readZipEntryContent(zipInputStream);
                    zipInputStream.closeEntry();

                    Candidate candidate = resolveCandidate(defaultCandidateId, cleanEntryName);
                    String storedPath = fileStorageService.storeFile(content, cleanEntryName, null, "cvs");
                    String fileType = determineFileType(cleanEntryName, null);
                    String extractedText = fileStorageService.extractTextIfPossible(content, cleanEntryName);

                    if (StringUtils.hasText(extractedText)) {
                        ParsedCvData parsedData = cvParsingService.parseText(extractedText);
                        cvParsingService.enrichCandidate(candidate, parsedData);
                    }

                    CandidateCv cv = candidateCvRepository.save(CandidateCv.builder()
                            .candidate(candidate)
                            .fileName(cleanEntryName)
                            .fileUrl(storedPath)
                            .fileType(fileType)
                            .uploadedBy(uploader)
                            .parsedText(extractedText)
                            .build());

                    successfulUploads.add(CandidateCvResponse.fromEntity(cv));
                } catch (Exception exception) {
                    log.warn("Failed processing zip entry: {}", cleanEntryName, exception);
                    failedUploads.add(new BulkUploadFailure(cleanEntryName, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            log.error("Failed to read zip archive: {}", zipFile.getOriginalFilename(), exception);
            failedUploads.add(new BulkUploadFailure(zipFile.getOriginalFilename(), "Invalid or corrupted zip archive: " + exception.getMessage()));
        }
        return count;
    }

    private byte[] readZipEntryContent(ZipInputStream zipInputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int bytesRead;
        while ((bytesRead = zipInputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    private Candidate resolveCandidate(Long defaultCandidateId, String filename) {
        if (defaultCandidateId != null) {
            return findCandidateById(defaultCandidateId);
        }

        String email = extractEmailFromFilename(filename);
        if (email != null) {
            return candidateRepository.findByEmail(email).orElseGet(() -> {
                String candidateName = extractCandidateNameFromFilename(filename, email);
                return candidateRepository.save(Candidate.builder()
                        .fullName(candidateName)
                        .email(email)
                        .source("CV_UPLOAD")
                        .build());
            });
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No candidateId provided and cannot extract email from filename: " + filename);
    }

    private String extractEmailFromFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return null;
        }

        int lastDot = filename.lastIndexOf('.');
        String baseName = lastDot > 0 ? filename.substring(0, lastDot) : filename;

        String[] tokens = baseName.split("[\\s_\\-\\[\\]\\(\\)]+");
        for (String token : tokens) {
            Matcher matcher = STRICT_EMAIL_PATTERN.matcher(token);
            if (matcher.matches()) {
                return token.toLowerCase(Locale.ROOT);
            }
        }

        Matcher fallbackMatcher = FALLBACK_EMAIL_PATTERN.matcher(baseName);
        if (fallbackMatcher.find()) {
            return fallbackMatcher.group().toLowerCase(Locale.ROOT);
        }

        return null;
    }

    private String extractCandidateNameFromFilename(String filename, String email) {
        int dotIndex = filename.lastIndexOf('.');
        String namePart = dotIndex >= 0 ? filename.substring(0, dotIndex) : filename;
        namePart = namePart.replace(email, "");
        namePart = namePart.replaceAll("[_-]+", " ").trim();
        return StringUtils.hasText(namePart) ? namePart : email;
    }

    private String determineFileType(String filename, String contentType) {
        if (StringUtils.hasText(contentType)) {
            return contentType;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
            String ext = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            return switch (ext) {
                case "pdf" -> "application/pdf";
                case "doc" -> "application/msword";
                case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "txt" -> "text/plain";
                case "rtf" -> "application/rtf";
                default -> "application/octet-stream";
            };
        }
        return "application/octet-stream";
    }

    private CandidateCv findCvById(Long id) {
        return candidateCvRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate CV not found"));
    }

    private Candidate findCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found"));
    }

    private void ensureCandidateExists(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Uploader not found"));
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}