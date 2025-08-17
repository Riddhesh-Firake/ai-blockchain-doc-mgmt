// DocumentShareRequest.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for document sharing
 */
public class DocumentShareRequest {

    @NotBlank(message = "Owner wallet address is required")
    private String ownerAddress;

    @NotBlank(message = "Recipient address is required")
    private String recipientAddress;

    @NotBlank(message = "Access level is required")
    @Pattern(regexp = "read|write|admin", message = "Access level must be one of: read, write, admin")
    private String accessLevel;

    // Constructors
    public DocumentShareRequest() {}

    public DocumentShareRequest(String ownerAddress, String recipientAddress, String accessLevel) {
        this.ownerAddress = ownerAddress;
        this.recipientAddress = recipientAddress;
        this.accessLevel = accessLevel;
    }

    // Getters and Setters
    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String ownerAddress) { this.ownerAddress = ownerAddress; }

    public String getRecipientAddress() { return recipientAddress; }
    public void setRecipientAddress(String recipientAddress) { this.recipientAddress = recipientAddress; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }
}