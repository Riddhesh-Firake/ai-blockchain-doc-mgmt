// ShareRecordRepository.java
package com.duediligence.documentanalyzer.repository.blockchain;

import com.duediligence.documentanalyzer.model.blockchain.ShareRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ShareRecord entities
 */
@Repository
public interface ShareRecordRepository extends JpaRepository<ShareRecord, Long> {

    /**
     * Find active shares for a document
     */
    @Query("SELECT s FROM ShareRecord s WHERE s.documentId = :documentId AND s.isActive = true")
    List<ShareRecord> findActiveSharesByDocumentId(@Param("documentId") String documentId);

    /**
     * Find active share between specific users
     */
    @Query("SELECT s FROM ShareRecord s WHERE s.documentId = :documentId AND s.sharedWith = :sharedWith AND s.isActive = true")
    Optional<ShareRecord> findActiveShareRecord(@Param("documentId") String documentId,
                                                @Param("sharedWith") String sharedWith);

    /**
     * Find documents shared with a user
     */
    @Query("SELECT DISTINCT s.documentId FROM ShareRecord s WHERE s.sharedWith = :userAddress AND s.isActive = true")
    List<String> findActiveSharedDocumentIds(@Param("userAddress") String userAddress);

    /**
     * Find shares by sharer
     */
    List<ShareRecord> findBySharedByOrderByTimestampDesc(String sharedBy);

    /**
     * Find shares by recipient
     */
    List<ShareRecord> findBySharedWithAndIsActiveOrderByTimestampDesc(String sharedWith, boolean isActive);

    /**
     * Count active shares for a document
     */
    @Query("SELECT COUNT(s) FROM ShareRecord s WHERE s.documentId = :documentId AND s.isActive = true")
    long countActiveSharesByDocumentId(@Param("documentId") String documentId);

    /**
     * Find shares by access level
     */
    @Query("SELECT s FROM ShareRecord s WHERE s.documentId = :documentId AND s.accessLevel = :accessLevel AND s.isActive = true")
    List<ShareRecord> findByDocumentIdAndAccessLevelAndActive(@Param("documentId") String documentId,
                                                              @Param("accessLevel") String accessLevel);


}