package recruitmentmanagmentplatform.recruitmentmanagementplatform.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String storeFile(MultipartFile file, String subDirectory);

    String storeFile(byte[] content, String originalFilename, String contentType, String subDirectory);

    Resource loadFileAsResource(String filePathOrName);

    void deleteFile(String filePathOrName);

    String extractTextIfPossible(MultipartFile file);

    String extractTextIfPossible(byte[] content, String filename);
}
