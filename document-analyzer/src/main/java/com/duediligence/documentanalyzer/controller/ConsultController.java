package com.duediligence.documentanalyzer.controller;

import com.duediligence.documentanalyzer.model.ConsultRequest;
import com.duediligence.documentanalyzer.model.ConsultResponse;
import com.duediligence.documentanalyzer.service.ConsultService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for consultation services
 */
@RestController
@RequestMapping("/api/consult")
@PreAuthorize("isAuthenticated()")
@CrossOrigin(origins = "*") // Configure as needed for your frontend
public class ConsultController {

    private static final Logger logger = LoggerFactory.getLogger(ConsultController.class);

    private final ConsultService consultService;

    @Autowired
    public ConsultController(ConsultService consultService) {
        this.consultService = consultService;
    }

    /**
     * POST /api/consult
     * Process domain-specific consultation requests
     */
    @PostMapping
    public ResponseEntity<ConsultResponse> consultDomain(@Valid @RequestBody ConsultRequest request) {
        logger.info("Received consultation request for domain: {} with query: {}",
                request.getDomain(), request.getQuery());

        try {
            ConsultResponse response = consultService.processConsultation(
                    request.getDomain(),
                    request.getQuery()
            );

            logger.info("Consultation completed successfully for domain: {}", request.getDomain());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid consultation request: {}", e.getMessage());
            ConsultResponse errorResponse = new ConsultResponse(
                    request.getDomain(),
                    request.getQuery(),
                    "Invalid request: " + e.getMessage(),
                    "Error",
                    0
            );
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("Consultation failed with error: {}", e.getMessage());
            ConsultResponse errorResponse = new ConsultResponse(
                    request.getDomain(),
                    request.getQuery(),
                    "Service temporarily unavailable: " + e.getMessage(),
                    "Error",
                    0
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * GET /api/consult/domains
     * Get list of supported domains for consultation
     */
    @GetMapping("/domains")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String[]> getSupportedDomains() {
        String[] domains = {"finance", "healthcare", "legal"};
        return ResponseEntity.ok(domains);
    }

    /**
     * GET /api/consult/health
     * Health check endpoint for consultation service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Consultation service is running");
    }
}