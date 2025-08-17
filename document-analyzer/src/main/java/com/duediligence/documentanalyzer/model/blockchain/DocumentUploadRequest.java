// DocumentUploadRequest.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for document upload
 */
public class DocumentUploadRequest {

    @NotBlank(message = "Wallet address is required")
    private String walletAddress;

    @NotBlank(message = "Domain is required")
    @Pattern(regexp = "finance|healthcare|legal|general", message = "Domain must be one of: finance, healthcare, legal, general")
    private String domain;

    private String ipfsHash; // Optional IPFS hash

    // Constructors
    public DocumentUploadRequest() {}

    public DocumentUploadRequest(String walletAddress, String domain, String ipfsHash) {
        this.walletAddress = walletAddress;
        this.domain = domain;
        this.ipfsHash = ipfsHash;
    }

    // Getters and Setters
    public String getWalletAddress() { return walletAddress; }
    public void setWalletAddress(String walletAddress) { this.walletAddress = walletAddress; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getIpfsHash() { return ipfsHash; }
    public void setIpfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; }
}
