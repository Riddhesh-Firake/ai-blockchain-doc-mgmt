package com.duediligence.documentanalyzer.controller;

import com.duediligence.documentanalyzer.model.AnalysisResponse;
import com.duediligence.documentanalyzer.model.ConsultResponse;
import com.duediligence.documentanalyzer.model.NewsResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the application
 * Provides consistent error responses across all endpoints
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle file size exceeded exceptions
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<AnalysisResponse> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.badRequest().body(
                new AnalysisResponse(null, null, null, null,
                        "ERROR: File size exceeds maximum allowed limit of 50MB per file", 0)
        );
    }

    /**
     * Handle illegal argument exceptions
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException exc) {
        // Return different response types based on the request context
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid request");
        errorResponse.put("message", exc.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle validation exceptions for request bodies
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException exc) {
        Map<String, Object> errorResponse = new HashMap<>();
        Map<String, String> fieldErrors = new HashMap<>();

        exc.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errorResponse.put("error", "Validation failed");
        errorResponse.put("fields", fieldErrors);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle WebClient exceptions (API call failures)
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<Object> handleWebClientException(WebClientResponseException exc) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "External API error");
        errorResponse.put("message", "External service temporarily unavailable");
        errorResponse.put("statusCode", exc.getStatusCode().value());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handle runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException exc) {
        // Check if this is for document analysis
        if (exc.getMessage() != null && exc.getMessage().contains("document")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new AnalysisResponse(null, null, null, null,
                            "ERROR: Internal server error - " + exc.getMessage(), 0)
            );
        }

        // Generic error response for other services
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Service error");
        errorResponse.put("message", exc.getMessage());
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneral(Exception exc) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unexpected error");
        errorResponse.put("message", "An unexpected error occurred");
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}