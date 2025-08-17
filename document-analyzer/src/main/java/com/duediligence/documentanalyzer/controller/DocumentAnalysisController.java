package com.duediligence.documentanalyzer.controller;

import com.duediligence.documentanalyzer.model.AnalysisResponse;
import com.duediligence.documentanalyzer.model.Domain;
import com.duediligence.documentanalyzer.service.DocumentAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST Controller for document analysis endpoints
 * Handles file uploads and document analysis requests
 */
@RestController
@RequestMapping("/api/documents")
@PreAuthorize("isAuthenticated()")
@CrossOrigin(origins = "*") // Enable CORS for frontend integration
public class DocumentAnalysisController {

    private final DocumentAnalysisService documentAnalysisService;

    @Autowired
    public DocumentAnalysisController(DocumentAnalysisService documentAnalysisService) {
        this.documentAnalysisService = documentAnalysisService;
    }

    /**
     * Upload and analyze documents
     *
     * @param files Array of uploaded files (PDF, Word, ZIP)
     * @param domain Analysis domain (finance, healthcare, legal)
     * @return Analysis results including summaries and due diligence reports
     */
    @PostMapping("/analyze")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnalysisResponse> analyzeDocuments(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("domain") String domain) {

        try {
            // Validate domain parameter
            Domain domainEnum = Domain.fromValue(domain);
            if (domainEnum == null) {
                return ResponseEntity.badRequest().body(
                        new AnalysisResponse(null, domain, null, null,
                                "ERROR: Invalid domain. Supported domains: finance, healthcare, legal", 0)
                );
            }

            // Process documents
            AnalysisResponse response = documentAnalysisService.processDocuments(files, domainEnum);

            // Return appropriate HTTP status based on processing result
            if (response.getStatus().startsWith("ERROR")) {
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    new AnalysisResponse(null, domain, null, null,
                            "ERROR: Internal server error - " + e.getMessage(), 0)
            );
        }
    }

    /**
     * Health check endpoint
     * @return Simple health status
     */
    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Document Analysis Service is running");
    }

    /**
     * Get supported domains
     * @return List of supported analysis domains
     */
    @GetMapping("/domains")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String[]> getSupportedDomains() {
        String[] domains = {"finance", "healthcare", "legal"};
        return ResponseEntity.ok(domains);
    }
}