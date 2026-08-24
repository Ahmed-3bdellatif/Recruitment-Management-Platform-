package recruitmentmanagmentplatform.recruitmentmanagementplatform.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt", "rtf", "zip"
    );

    private final Path baseStorageLocation;

    public LocalFileStorageService(
            @Value("${app.upload.base-dir:uploads}") String uploadDir) {
        this.baseStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseStorageLocation);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize file storage directory: " + uploadDir, exception);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot store empty or null file");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed_file");

        validateExtension(originalFilename);

        try (InputStream inputStream = file.getInputStream()) {
            return saveToDisk(inputStream, originalFilename, subDirectory);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file: " + originalFilename, exception);
        }
    }

    @Override
    public String storeFile(byte[] content, String originalFilename, String contentType, String subDirectory) {
        if (content == null || content.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot store empty content");
        }

        String cleanFilename = StringUtils.cleanPath(
                originalFilename != null ? originalFilename : "unnamed_file");

        validateExtension(cleanFilename);

        try (InputStream inputStream = new ByteArrayInputStream(content)) {
            return saveToDisk(inputStream, cleanFilename, subDirectory);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to store file: " + cleanFilename, exception);
        }
    }

    @Override
    public Resource loadFileAsResource(String filePathOrName) {
        try {
            Path filePath = resolveFilePath(filePathOrName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found or not readable: " + filePathOrName);
            }
        } catch (MalformedURLException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + filePathOrName, exception);
        }
    }

    @Override
    public void deleteFile(String filePathOrName) {
        if (!StringUtils.hasText(filePathOrName)) {
            return;
        }

        try {
            Path filePath = resolveFilePath(filePathOrName);
            Files.deleteIfExists(filePath);
        } catch (IOException exception) {
            log.warn("Failed to delete stored file: {}", filePathOrName, exception);
        }
    }

    @Override
    public String extractTextIfPossible(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String filename = file.getOriginalFilename();
        if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            try {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                log.debug("Could not read text content from file: {}", filename, exception);
            }
        }
        return null;
    }

    @Override
    public String extractTextIfPossible(byte[] content, String filename) {
        if (content == null || content.length == 0) {
            return null;
        }

        if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".txt")) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String saveToDisk(InputStream inputStream, String originalFilename, String subDirectory) throws IOException {
        String sanitizedFilename = sanitizeFilename(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "_" + sanitizedFilename;

        Path targetDirectory = this.baseStorageLocation;
        if (StringUtils.hasText(subDirectory)) {
            String cleanSubDirectory = StringUtils.cleanPath(subDirectory);
            if (cleanSubDirectory.contains("..")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sub-directory path");
            }
            targetDirectory = this.baseStorageLocation.resolve(cleanSubDirectory).normalize();
            Files.createDirectories(targetDirectory);
        }

        Path targetLocation = targetDirectory.resolve(uniqueFilename).normalize();
        if (!targetLocation.startsWith(this.baseStorageLocation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot store file outside current storage directory");
        }

        Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String relativePath = this.baseStorageLocation.relativize(targetLocation).toString().replace('\\', '/');
        return relativePath;
    }

    private Path resolveFilePath(String filePathOrName) {
        String cleanPath = StringUtils.cleanPath(filePathOrName).replace('\\', '/');
        if (cleanPath.contains("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path reference");
        }

        Path resolved = this.baseStorageLocation.resolve(cleanPath).normalize();
        if (!resolved.startsWith(this.baseStorageLocation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File path is outside storage directory");
        }
        return resolved;
    }

    private void validateExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File must have a valid extension (.pdf, .doc, .docx, .txt, .rtf, .zip)");
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File extension '." + extension + "' is not supported. Allowed formats: " + ALLOWED_EXTENSIONS);
        }
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
