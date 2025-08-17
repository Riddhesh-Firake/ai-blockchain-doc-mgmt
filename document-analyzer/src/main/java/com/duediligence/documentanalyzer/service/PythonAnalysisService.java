package com.duediligence.documentanalyzer.service;

import com.duediligence.documentanalyzer.model.AIAnalysisResult;
import com.duediligence.documentanalyzer.model.Domain;
import com.duediligence.documentanalyzer.model.FileInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.Duration;
import java.util.List;

/**
 * Service for communicating with Python backend AI models
 * Integrates with FastAPI backend for real AI analysis with mock fallback
 */
@Service
public class PythonAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(PythonAnalysisService.class);

    @Value("${app.python.service.url:http://localhost:5000/analyze}")
    private String pythonServiceUrl;

    @Value("${app.python.service.timeout:30000}")
    private int timeoutMs;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PythonAnalysisService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();

        // Configure RestTemplate timeout
        configureRestTemplate();
    }

    /**
     * Analyze documents using Python NLP models
     * Attempts to call Python backend first, falls back to mock data on failure
     *
     * @param files List of uploaded files
     * @param domain Analysis domain (finance, healthcare, legal)
     * @return AI analysis results
     */
    public AIAnalysisResult analyzeDocuments(List<FileInfo> files, Domain domain) {
        logger.info("Starting document analysis for domain: {} with {} files", domain, files.size());

        try {
            // Attempt to call Python backend
            AIAnalysisResult result = callPythonBackend(files, domain);
            logger.info("Successfully received analysis from Python backend");
            return result;

        } catch (Exception e) {
            logger.warn("Python backend call failed, falling back to mock response. Error: {}", e.getMessage());
            logger.debug("Full stack trace for Python backend failure:", e);

            // Fall back to mock response
            return generateMockResponse(files, domain);
        }
    }

    /**
     * Call Python FastAPI backend for document analysis
     *
     * @param files List of files to analyze
     * @param domain Analysis domain
     * @return AI analysis result from Python backend
     * @throws Exception if the call fails
     */
    private AIAnalysisResult callPythonBackend(List<FileInfo> files, Domain domain) throws Exception {
        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Add domain field
        body.add("domain", domain.getValue());

        // Add files
        for (FileInfo fileInfo : files) {
            File file = new File(fileInfo.getStoragePath());
            if (file.exists() && file.canRead()) {
                FileSystemResource fileResource = new FileSystemResource(file);
                body.add("files", fileResource);
                logger.debug("Added file to request: {}", fileInfo.getName());
            } else {
                logger.warn("File not found or not readable: {}", fileInfo.getStoragePath());
                throw new RuntimeException("File not accessible: " + fileInfo.getName());
            }
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        logger.info("Sending request to Python backend: {}", pythonServiceUrl);

        // Make the request
        ResponseEntity<String> response = restTemplate.postForEntity(
                pythonServiceUrl,
                requestEntity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Python backend returned non-OK status: " + response.getStatusCode());
        }

        String responseBody = response.getBody();
        if (responseBody == null || responseBody.trim().isEmpty()) {
            throw new RuntimeException("Empty response from Python backend");
        }

        logger.debug("Received response from Python backend: {}", responseBody);

        // Parse JSON response
        return parseAnalysisResponse(responseBody);
    }

    /**
     * Parse JSON response from Python backend into AIAnalysisResult
     *
     * @param jsonResponse JSON response string
     * @return Parsed AIAnalysisResult
     * @throws Exception if parsing fails
     */
    private AIAnalysisResult parseAnalysisResponse(String jsonResponse) throws Exception {
        try {
            logger.info("Attempting to parse response: {}", jsonResponse); // Debug log
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            logger.info("Parsed JSON root: {}", rootNode); // Debug log

            // The Python response has a nested structure: analysis.summary and analysis.due_diligence
            JsonNode analysisNode = rootNode.path("analysis");

            if (analysisNode.isMissingNode()) {
                throw new RuntimeException("Missing 'analysis' node in Python response");
            }

            String summary = analysisNode.path("summary").asText();
            String dueDiligence = analysisNode.path("due_diligence").asText();

            logger.info("Extracted summary: '{}'", summary); // Debug log
            logger.info("Extracted due_diligence: '{}'", dueDiligence); // Debug log

            if (summary.isEmpty() || dueDiligence.isEmpty()) {
                throw new RuntimeException("Missing required fields in Python response");
            }

            return new AIAnalysisResult(summary, dueDiligence);

        } catch (Exception e) {
            logger.error("Failed to parse Python backend response: {}", e.getMessage());
            throw new RuntimeException("Invalid response format from Python backend", e);
        }
    }

    /**
     * Configure RestTemplate with timeout settings
     */
    private void configureRestTemplate() {
        restTemplate.getMessageConverters().add(0, new org.springframework.http.converter.FormHttpMessageConverter());
        restTemplate.getMessageConverters().add(new org.springframework.http.converter.StringHttpMessageConverter());

        // Set timeout using request factory
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        restTemplate.setRequestFactory(factory);
    }

    /**
     * Generate mock AI analysis response
     * Returns deterministic data based on input parameters for consistent testing
     */
    private AIAnalysisResult generateMockResponse(List<FileInfo> files, Domain domain) {
        logger.info("Generating mock response for domain: {} with {} files", domain, files.size());

        // Simulate processing delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int fileCount = files.size();
        long totalSize = files.stream().mapToLong(FileInfo::getSize).sum();

        String summary;
        String dueDiligence;

        switch (domain) {
            case FINANCE:
                summary = String.format("Financial Analysis Summary: Analyzed %d document(s) totaling %.1fKB. " +
                                "Key findings include revenue projections, risk assessments, and compliance indicators. " +
                                "The documents show standard financial reporting practices with moderate risk exposure.",
                        fileCount, totalSize / 1024.0);

                dueDiligence = "Financial Due Diligence Report:\n" +
                        "• Revenue Streams: Multiple diversified sources identified\n" +
                        "• Risk Assessment: Moderate financial risk detected\n" +
                        "• Compliance Status: All regulatory requirements appear to be met\n" +
                        "• Recommendations: Continue monitoring quarterly reports\n" +
                        "• Overall Rating: ACCEPTABLE with minor concerns";
                break;

            case HEALTHCARE:
                summary = String.format("Healthcare Analysis Summary: Processed %d healthcare document(s) " +
                                "totaling %.1fKB. Analysis covers patient safety protocols, regulatory compliance, " +
                                "and clinical trial data. Overall compliance appears satisfactory.",
                        fileCount, totalSize / 1024.0);

                dueDiligence = "Healthcare Due Diligence Report:\n" +
                        "• Patient Safety: Protocols meet industry standards\n" +
                        "• Regulatory Compliance: FDA/HIPAA requirements satisfied\n" +
                        "• Clinical Data: Statistically significant results observed\n" +
                        "• Risk Factors: Low to moderate safety concerns identified\n" +
                        "• Overall Rating: COMPLIANT with standard monitoring required";
                break;

            case LEGAL:
                summary = String.format("Legal Analysis Summary: Reviewed %d legal document(s) " +
                                "totaling %.1fKB. Contract analysis reveals standard commercial terms " +
                                "with acceptable risk levels. No major legal red flags identified.",
                        fileCount, totalSize / 1024.0);

                dueDiligence = "Legal Due Diligence Report:\n" +
                        "• Contract Terms: Standard commercial agreements identified\n" +
                        "• Legal Risks: Low to moderate exposure detected\n" +
                        "• Compliance Issues: No significant violations found\n" +
                        "• Intellectual Property: Rights properly documented\n" +
                        "• Overall Rating: ACCEPTABLE for standard business operations";
                break;

            default:
                summary = "Generic document analysis completed successfully.";
                dueDiligence = "Standard due diligence review completed with no major issues identified.";
        }

        return new AIAnalysisResult(summary, dueDiligence);
    }
}