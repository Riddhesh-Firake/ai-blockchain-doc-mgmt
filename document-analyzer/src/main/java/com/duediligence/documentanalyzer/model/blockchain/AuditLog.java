// AuditLog.java
package com.duediligence.documentanalyzer.model.blockchain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing audit log entries
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private String actor; // wallet address

    @Column(nullable = false)
    private String action; // upload, update, share, revoke, access

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String details;

    @Column(nullable = true)
    private String transactionHash;

    // Constructors
    public AuditLog() {}

    public AuditLog(String documentId, String actor, String action,
                    String details, String transactionHash) {
        this.documentId = documentId;
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.transactionHash = transactionHash;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getTransactionHash() { return transactionHash; }
    public void setTransactionHash(String transactionHash) { this.transactionHash = transactionHash; }
}