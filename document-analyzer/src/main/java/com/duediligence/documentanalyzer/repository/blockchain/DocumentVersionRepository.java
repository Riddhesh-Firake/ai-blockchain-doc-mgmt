package com.duediligence.documentanalyzer.repository.blockchain;

import com.duediligence.documentanalyzer.model.blockchain.DocumentVersion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DocumentVersion entities
 */
@Repository
public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {

    /**
     * Find versions by document ID ordered by version number
     */
    List<DocumentVersion> findByDocumentIdOrderByVersionDesc(String documentId);

    /**
     * Count versions for a document
     */
    int countByDocumentId(String documentId);

    /**
     * Find latest version for a document (no LIMIT keyword in JPQL)
     */
    @Query("SELECT v FROM DocumentVersion v WHERE v.documentId = :documentId ORDER BY v.version DESC")
    List<DocumentVersion> findLatestVersionByDocumentId(@Param("documentId") String documentId, Pageable pageable);

    /**
     * Find specific version
     */
    Optional<DocumentVersion> findByDocumentIdAndVersion(String documentId, Integer version);

    /**
     * Find versions by updater
     */
    List<DocumentVersion> findByUpdatedByOrderByCreatedAtDesc(String updatedBy);
}
