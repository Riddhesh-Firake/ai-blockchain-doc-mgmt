package com.duediligence.documentanalyzer.repository;

import com.duediligence.documentanalyzer.model.Domain;
import com.duediligence.documentanalyzer.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * Find file by original name and domain
     */
    Optional<FileInfo> findByNameAndDomain(String name, Domain domain);

    /**
     * Get all files ordered by upload time (oldest first)
     */
    List<FileInfo> findAllByOrderByUploadTimeAsc();

    /**
     * Count total files in database
     */
    long count();

    /**
     * Get oldest files for cleanup (used when file count exceeds limit)
     */
    @Query("SELECT f FROM FileInfo f ORDER BY f.uploadTime ASC")
    List<FileInfo> findOldestFiles();
}