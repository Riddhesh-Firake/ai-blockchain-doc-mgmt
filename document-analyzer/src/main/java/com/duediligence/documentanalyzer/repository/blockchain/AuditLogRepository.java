// AuditLogRepository.java
package com.duediligence.documentanalyzer.repository.blockchain;

import com.duediligence.documentanalyzer.model.blockchain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entities
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by document ID ordered by timestamp (newest first)
     */
    List<AuditLog> findByDocumentIdOrderByTimestampDesc(String documentId);

    /**
     * Find audit logs by actor
     */
    List<AuditLog> findByActorOrderByTimestampDesc(String actor);

    /**
     * Find audit logs by action type
     */
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    /**
     * Find audit logs within date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.documentId = :documentId AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    List<AuditLog> findByDocumentIdAndDateRange(@Param("documentId") String documentId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find recent activity for a user
     */
    @Query("SELECT a FROM AuditLog a WHERE a.actor = :actor AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentActivityByActor(@Param("actor") String actor,
                                             @Param("since") LocalDateTime since);

    /**
     * Count actions by type for a document
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.documentId = :documentId AND a.action = :action")
    long countByDocumentIdAndAction(@Param("documentId") String documentId,
                                    @Param("action") String action);

    /**
     * Find integrity violations
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'integrity_violation' ORDER BY a.timestamp DESC")
    List<AuditLog> findIntegrityViolations();

    /**
     * Get activity summary for a user
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a WHERE a.actor = :actor GROUP BY a.action")
    List<Object[]> getActivitySummaryByActor(@Param("actor") String actor);
}