// DocumentVersion.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a document version/update
 */
@Entity
@Table(name = "document_versions")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private String fileHash;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String updatedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private String updateReason;

    @Column(nullable = false)
    private String transactionHash;

    // Constructors
    public DocumentVersion() {}

    public DocumentVersion(String documentId, String fileHash, Integer version,
                           String updatedBy, String updateReason, String transactionHash) {
        this.documentId = documentId;
        this.fileHash = fileHash;
        this.version = version;
        this.updatedBy = updatedBy;
        this.updateReason = updateReason;
        this.transactionHash = transactionHash;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getFileHash() { return fileHash; }
    public void setFileHash(String fileHash) { this.fileHash = fileHash; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
}