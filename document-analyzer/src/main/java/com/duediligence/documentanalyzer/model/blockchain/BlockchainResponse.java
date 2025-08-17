// BlockchainResponse.java
package com.duediligence.documentanalyzer.model.blockchain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Generic response DTO for blockchain operations
 */
public class BlockchainResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String transactionHash;
    private LocalDateTime timestamp;
    private List<String> errors;

    // Constructors
    public BlockchainResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public BlockchainResponse(boolean success, String message, T data, String transactionHash) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
        this.transactionHash = transactionHash;
    }

    // Static factory methods
    public static <T> BlockchainResponse<T> success(String message, T data, String transactionHash) {
        return new BlockchainResponse<>(true, message, data, transactionHash);
    }

    public static <T> BlockchainResponse<T> error(String message) {
        return new BlockchainResponse<>(false, message, null, null);
    }

    public static <T> BlockchainResponse<T> error(String message, List<String> errors) {
        BlockchainResponse<T> response = new BlockchainResponse<>(false, message, null, null);
        response.setErrors(errors);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}