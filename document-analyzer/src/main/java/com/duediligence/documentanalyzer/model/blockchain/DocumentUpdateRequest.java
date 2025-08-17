// DocumentUpdateRequest.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for document updates
 */
public class DocumentUpdateRequest {

    @NotBlank(message = "Wallet address is required")
    private String walletAddress;

    private String updateReason;

    // Constructors
    public DocumentUpdateRequest() {}

    public DocumentUpdateRequest(String walletAddress, String updateReason) {
        this.walletAddress = walletAddress;
        this.updateReason = updateReason;
    }

    // Getters and Setters
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }
}
