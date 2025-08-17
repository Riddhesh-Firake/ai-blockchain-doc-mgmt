// BlockchainDocumentRepository.java
package com.duediligence.documentanalyzer.repository.blockchain;

import com.duediligence.documentanalyzer.model.blockchain.BlockchainDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BlockchainDocument entities
 */
@Repository
public interface BlockchainDocumentRepository extends JpaRepository<BlockchainDocument, String> {

    /**
     * Find documents by owner address and active status
     */
    List<BlockchainDocument> findByOwnerAddressAndIsActive(String ownerAddress, boolean isActive);

    /**
     * Find documents by domain
     */
    List<BlockchainDocument> findByDomainAndIsActive(String domain, boolean isActive);

    /**
     * Find documents by owner and domain
     */
    List<BlockchainDocument> findByOwnerAddressAndDomainAndIsActive(
            String ownerAddress, String domain, boolean isActive);

    /**
     * Count documents by owner
     */
    long countByOwnerAddressAndIsActive(String ownerAddress, boolean isActive);

    /**
     * Find documents by file hash (for duplicate detection)
     */
    @Query("SELECT d FROM BlockchainDocument d WHERE d.fileHash = :fileHash AND d.isActive = true")
    List<BlockchainDocument> findByFileHashAndActive(@Param("fileHash") String fileHash);
}