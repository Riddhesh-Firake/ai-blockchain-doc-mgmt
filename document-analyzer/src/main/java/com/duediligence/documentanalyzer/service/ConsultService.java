package com.duediligence.documentanalyzer.service;

import com.duediligence.documentanalyzer.model.ConsultResponse;
import com.duediligence.documentanalyzer.model.Domain;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
public class ConsultService {

    private static final Logger logger = LoggerFactory.getLogger(ConsultService.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Alternative: OpenAI-compatible free APIs
    @Value("${openai.api.key:}")
    private String openaiApiKey;

    @Value("${app.api.cohere.key:}")
    private String cohereApiKey;

    public ConsultService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public ConsultResponse processConsultation(String domainStr, String query) {
        long startTime = System.currentTimeMillis();

        // Validate domain
        Domain domain = Domain.fromValue(domainStr);
        if (domain == null) {
            throw new IllegalArgumentException("Unsupported domain: " + domainStr +
                    ". Supported domains are: finance, healthcare, legal");
        }

        try {
            String response = getAIResponse(domain, query);
            long responseTime = System.currentTimeMillis() - startTime;

            return new ConsultResponse(
                    domainStr,
                    query,
                    response,
                    "AI Assistant",
                    responseTime
            );

        } catch (Exception e) {
            logger.error("Error processing consultation for domain {}: {}", domainStr, e.getMessage());
            throw new RuntimeException("Failed to process consultation: " + e.getMessage());
        }
    }

    private String getAIResponse(Domain domain, String query) {
        // Try multiple free AI services in order of preference
        String response = null;

        // 1. Try OpenAI-compatible free services (Groq)
        if (!openaiApiKey.isEmpty()) {
            response = tryOpenAICompatibleAPI(domain, query);
        }

        // 2. Try free public APIs (Cohere)
        if (response == null && !cohereApiKey.isEmpty()) {
            response = tryFreePublicAPIs(domain, query);
        }

        // 3. Fallback to a basic response structure
        if (response == null) {
            response = generateBasicResponse(domain, query);
        }

        return response;
    }

    private String tryOpenAICompatibleAPI(Domain domain, String query) {
        try {
            // Using a free OpenAI-compatible service (like Together AI, Groq, etc.)
            String url = "https://api.groq.com/openai/v1/chat/completions";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey);

            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", formatDomainQuery(domain, query)
            );

            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", new Object[]{message},
                    "max_tokens", 300,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                return jsonResponse.get("choices").get(0).get("message").get("content").asText();
            }
        } catch (Exception e) {
            logger.warn("OpenAI-compatible API failed: {}", e.getMessage());
        }
        return null;
    }

    private String tryFreePublicAPIs(Domain domain, String query) {
        try {
            String url = "https://api.cohere.ai/v1/generate";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(cohereApiKey); // Use your API key instead of no auth

            Map<String, Object> requestBody = Map.of(
                    "model", "command-light",
                    "prompt", formatDomainQuery(domain, query),
                    "max_tokens", 200,
                    "temperature", 0.7
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                if (jsonResponse.has("generations") && jsonResponse.get("generations").isArray()) {
                    return jsonResponse.get("generations").get(0).get("text").asText().trim();
                }
            }
        } catch (Exception e) {
            logger.warn("Cohere API failed: {}", e.getMessage());
        }
        return null;
    }

    private String generateBasicResponse(Domain domain, String query) {
        // This method generates a structured response based on domain and query analysis
        // without hardcoding specific answers

        StringBuilder response = new StringBuilder();
        response.append("Thank you for your ").append(domain.getValue()).append(" consultation query. ");

        // Analyze query keywords to provide relevant guidance
        String lowerQuery = query.toLowerCase();

        switch (domain) {
            case FINANCE:
                if (containsAny(lowerQuery, "investment", "portfolio", "stocks", "bonds")) {
                    response.append("For investment-related queries, I recommend consulting with a certified financial advisor. ");
                } else if (containsAny(lowerQuery, "tax", "taxes", "deduction")) {
                    response.append("Tax matters require professional guidance from a qualified tax professional. ");
                } else if (containsAny(lowerQuery, "loan", "mortgage", "credit")) {
                    response.append("For loan and credit inquiries, please consult with banking professionals. ");
                }
                break;

            case HEALTHCARE:
                if (containsAny(lowerQuery, "symptom", "diagnosis", "treatment", "medication")) {
                    response.append("For medical concerns, please consult with qualified healthcare professionals. ");
                } else if (containsAny(lowerQuery, "insurance", "coverage", "claim")) {
                    response.append("Healthcare insurance matters should be discussed with your insurance provider. ");
                }
                break;

            case LEGAL:
                if (containsAny(lowerQuery, "contract", "agreement", "dispute")) {
                    response.append("Contract and legal disputes require consultation with qualified legal counsel. ");
                } else if (containsAny(lowerQuery, "rights", "liability", "compliance")) {
                    response.append("Legal rights and compliance matters should be reviewed by legal professionals. ");
                }
                break;
        }

        response.append("Your specific question about '").append(query).append("' ");
        response.append("requires detailed analysis by domain experts. I recommend seeking professional consultation ");
        response.append("for accurate and personalized advice tailored to your specific situation.");

        return response.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String formatDomainQuery(Domain domain, String query) {
        return String.format("As a %s consultant, please provide professional guidance on the following query: %s",
                domain.getValue(), query);
    }
}