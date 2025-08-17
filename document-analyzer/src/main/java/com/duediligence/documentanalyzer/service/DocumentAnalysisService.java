package com.duediligence.documentanalyzer.service;

import com.duediligence.documentanalyzer.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Main service orchestrating document analysis workflow
 * Coordinates file storage and AI analysis operations
 */
@Service
public class DocumentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalysisService.class);

    private final FileStorageService fileStorageService;
    private final PythonAnalysisService pythonAnalysisService;

    @Autowired
    public DocumentAnalysisService(FileStorageService fileStorageService,
                                   PythonAnalysisService pythonAnalysisService) {
        this.fileStorageService = fileStorageService;
        this.pythonAnalysisService = pythonAnalysisService;
    }

    /**
     * Process uploaded documents for analysis
     *
     * @param files Array of uploaded files
     * @param domain Analysis domain
     * @return Complete analysis response
     */
    public AnalysisResponse processDocuments(MultipartFile[] files, Domain domain) {
        long startTime = System.currentTimeMillis();

        logger.info("Starting document analysis process for domain: {} with {} files",
                domain, files != null ? files.length : 0);

        try {
            // Validate files
            validateFiles(files);
            logger.debug("File validation completed successfully");

            // Store files securely with domain information for duplicate detection
            List<FileInfo> fileInfos = fileStorageService.storeFiles(files, domain);
            logger.info("Successfully stored {} files", fileInfos.size());

            // Analyze documents using Python service (with fallback to mock)
            AIAnalysisResult analysisResult = pythonAnalysisService.analyzeDocuments(fileInfos, domain);
            logger.info("Document analysis completed successfully");

            long processingTime = System.currentTimeMillis() - startTime;

            // Build response
            AnalysisResponse response = new AnalysisResponse(
                    fileInfos,
                    domain.getValue(),
                    analysisResult.getSummary(),
                    analysisResult.getDueDiligence(),
                    "SUCCESS",
                    processingTime
            );

            logger.info("Analysis response built successfully in {}ms", processingTime);
            return response;

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;

            logger.error("Document analysis failed after {}ms. Error: {}", processingTime, e.getMessage());
            logger.debug("Full stack trace for analysis failure:", e);

            AnalysisResponse errorResponse = new AnalysisResponse(
                    null,
                    domain.getValue(),
                    null,
                    null,
                    "ERROR: " + e.getMessage(),
                    processingTime
            );

            return errorResponse;
        }
    }

    /**
     * Validate uploaded files
     * @param files Array of files to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No files provided for analysis");
        }

        logger.debug("Validating {} files", files.length);

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Empty file detected: " + file.getOriginalFilename());
            }

            if (!fileStorageService.isFileTypeSupported(file.getContentType())) {
                throw new IllegalArgumentException("Unsupported file type: " + file.getContentType() +
                        " for file: " + file.getOriginalFilename());
            }

            // Check file size (additional validation beyond Spring's multipart limits)
            if (file.getSize() > 50 * 1024 * 1024) { // 50MB per file
                throw new IllegalArgumentException("File too large: " + file.getOriginalFilename() +
                        " (size: " + file.getSize() + " bytes, max: 50MB)");
            }

            logger.debug("File validation passed: {} (size: {} bytes, type: {})",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
        }

        logger.info("All files validated successfully");
    }
}