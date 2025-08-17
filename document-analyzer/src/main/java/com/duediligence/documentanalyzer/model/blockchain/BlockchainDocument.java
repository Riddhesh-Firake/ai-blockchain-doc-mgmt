// BlockchainDocument.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a document stored in blockchain
 * Stores metadata and references to blockchain transactions
 */
@Entity
@Table(name = "blockchain_documents")
public class BlockchainDocument {

    @Id
    private String documentId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileHash;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String ownerAddress;

    @Column(nullable = false)
    private String filePath; // Local storage path

    private String ipfsHash; // Optional IPFS hash

    @Column(nullable = false)
    private String transactionHash; // Blockchain transaction hash

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "documentId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DocumentVersion> versions;

    @OneToMany(mappedBy = "documentId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShareRecord> shareRecords;

    // Constructors
    public BlockchainDocument() {}

    public BlockchainDocument(String documentId, String fileName, String fileHash,
                              String domain, String ownerAddress, String filePath,
                              String transactionHash) {
        this.documentId = documentId;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.domain = domain;
        this.ownerAddress = ownerAddress;
        this.filePath = filePath;
        this.transactionHash = transactionHash;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String ownerAddress) { this.ownerAddress = ownerAddress; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getIpfsHash() { return ipfsHash; }
    public void setIpfsHash(String ipfsHash) { this.ipfsHash = ipfsHash; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<DocumentVersion> getVersions() { return versions; }
    public void setVersions(List<DocumentVersion> versions) { this.versions = versions; }

    public List<ShareRecord> getShareRecords() { return shareRecords; }
    public void setShareRecords(List<ShareRecord> shareRecords) { this.shareRecords = shareRecords; }
}