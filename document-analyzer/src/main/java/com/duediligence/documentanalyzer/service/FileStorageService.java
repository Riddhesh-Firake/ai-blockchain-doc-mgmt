package com.duediligence.documentanalyzer.service;

import com.duediligence.documentanalyzer.model.Domain;
import com.duediligence.documentanalyzer.model.FileInfo;
import com.duediligence.documentanalyzer.repository.FileInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling file storage operations with PostgreSQL integration
 * Manages secure file uploads to local filesystem and database tracking
 */
@Service
@Transactional
public class FileStorageService {

    private final Path fileStorageLocation;
    private final FileInfoRepository fileInfoRepository;

    @Autowired
    public FileStorageService(@Value("${app.file-storage.upload-dir}") String uploadDir,
                              FileInfoRepository fileInfoRepository) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.fileInfoRepository = fileInfoRepository;

        try {
            // Create upload directory if it doesn't exist
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, ex);
        }
    }

    /**
     * Store multiple uploaded files securely with database tracking
     * @param files Array of uploaded files
     * @param domain Analysis domain for duplicate detection
     * @return List of FileInfo objects with storage details
     */
    public List<FileInfo> storeFiles(MultipartFile[] files, Domain domain) {
        List<FileInfo> fileInfos = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                FileInfo fileInfo = storeFile(file, domain);
                fileInfos.add(fileInfo);
            }
        }

        // Cleanup old files if necessary
        maintainFileLimit();

        return fileInfos;
    }

    /**
     * Store a single uploaded file securely with duplicate handling
     * @param file Uploaded file
     * @param domain Analysis domain
     * @return FileInfo object with storage details
     */
    private FileInfo storeFile(MultipartFile file, Domain domain) {
        try {
            String originalFilename = file.getOriginalFilename();

            // Check for existing file with same name and domain
            Optional<FileInfo> existingFile = fileInfoRepository.findByNameAndDomain(originalFilename, domain);

            if (existingFile.isPresent()) {
                // Delete existing file from filesystem
                deleteFileFromDisk(existingFile.get().getStoragePath());
                // Remove from database
                fileInfoRepository.delete(existingFile.get());
            }

            // Generate unique filename to prevent conflicts
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);

            // Copy file to target location
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Create and save FileInfo to database
            FileInfo fileInfo = new FileInfo(
                    originalFilename,
                    file.getSize(),
                    file.getContentType(),
                    targetLocation.toString(),
                    uniqueFileName,
                    domain
            );

            return fileInfoRepository.save(fileInfo);

        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), ex);
        }
    }

    /**
     * Maintain file limit by removing oldest files when count exceeds 50
     */
    private void maintainFileLimit() {
        long fileCount = fileInfoRepository.count();

        if (fileCount > 50) {
            int filesToDelete = (int) (fileCount - 50);
            List<FileInfo> oldestFiles = fileInfoRepository.findOldestFiles();

            // Delete oldest files
            for (int i = 0; i < filesToDelete && i < oldestFiles.size(); i++) {
                FileInfo fileToDelete = oldestFiles.get(i);

                // Delete from filesystem
                deleteFileFromDisk(fileToDelete.getStoragePath());

                // Delete from database
                fileInfoRepository.delete(fileToDelete);
            }
        }
    }

    /**
     * Delete file from disk safely
     * @param filePath Path to file to delete
     */
    private void deleteFileFromDisk(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log the error but don't throw to avoid breaking the transaction
            System.err.println("Failed to delete file from disk: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Check if file type is supported
     * @param contentType MIME type of the file
     * @return true if supported, false otherwise
     */
    public boolean isFileTypeSupported(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                        contentType.equals("application/msword") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        contentType.equals("application/zip") ||
                        contentType.equals("text/plain") ||
                        contentType.equals("text/csv") ||
                        contentType.equals("application/vnd.ms-excel") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
    }
}