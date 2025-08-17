package com.duediligence.documentanalyzer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Model class representing uploaded file information with database persistence
 */
@Entity
@Table(name = "file_info")
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_name", nullable = false)
    private String name;

    @Column(name = "file_size", nullable = false)
    private long size;

    @Column(name = "file_type")
    private String type;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "unique_filename", nullable = false)
    private String uniqueFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain")
    private Domain domain;

    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;

    public FileInfo() {
        this.uploadTime = LocalDateTime.now();
    }

    public FileInfo(String name, long size, String type, String storagePath,
                    String uniqueFileName, Domain domain) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.storagePath = storagePath;
        this.uniqueFileName = uniqueFileName;
        this.domain = domain;
        this.uploadTime = LocalDateTime.now();
    }

    // Existing getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    // New getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueFileName() {
        return uniqueFileName;
    }

    public void setUniqueFileName(String uniqueFileName) {
        this.uniqueFileName = uniqueFileName;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
}