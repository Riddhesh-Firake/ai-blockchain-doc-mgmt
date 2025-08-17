package com.duediligence.documentanalyzer.model;

import java.util.List;

/**
 * Response model for document analysis API
 * Contains file information, domain, and AI analysis results
 */
public class AnalysisResponse {
    private List<FileInfo> files;
    private String domain;
    private String summary;
    private String dueDiligence;
    private String status;
    private long processingTimeMs;

    public AnalysisResponse() {}

    public AnalysisResponse(List<FileInfo> files, String domain, String summary,
                            String dueDiligence, String status, long processingTimeMs) {
        this.files = files;
        this.domain = domain;
        this.summary = summary;
        this.dueDiligence = dueDiligence;
        this.status = status;
        this.processingTimeMs = processingTimeMs;
    }

    // Getters and Setters
    public List<FileInfo> getFiles() {
        return files;
    }

    public void setFiles(List<FileInfo> files) {
        this.files = files;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDueDiligence() {
        return dueDiligence;
    }

    public void setDueDiligence(String dueDiligence) {
        this.dueDiligence = dueDiligence;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}