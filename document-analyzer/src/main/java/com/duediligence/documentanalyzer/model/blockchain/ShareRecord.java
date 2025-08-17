// ShareRecord.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing document sharing records
 */
@Entity
@Table(name = "share_records")
public class ShareRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private String sharedBy;

    @Column(nullable = false)
    private String sharedWith; // wallet address or "public" for public access

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String accessLevel; // read, write, admin

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private String transactionHash;

    // Constructors
    public ShareRecord() {}

    public ShareRecord(String documentId, String sharedBy, String sharedWith,
                       String accessLevel, String transactionHash) {
        this.documentId = documentId;
        this.sharedBy = sharedBy;
        this.sharedWith = sharedWith;
        this.accessLevel = accessLevel;
        this.transactionHash = transactionHash;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getSharedBy() { return sharedBy; }
    public void setSharedBy(String sharedBy) { this.sharedBy = sharedBy; }

    public String getSharedWith() { return sharedWith; }
    public void setSharedWith(String sharedWith) { this.sharedWith = sharedWith; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
}