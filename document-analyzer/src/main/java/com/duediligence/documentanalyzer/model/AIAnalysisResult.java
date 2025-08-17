package com.duediligence.documentanalyzer.model;

/**
 * Model class representing AI analysis results from the Python backend
 */
public class AIAnalysisResult {
    private String summary;
    private String dueDiligence;

    public AIAnalysisResult() {}

    public AIAnalysisResult(String summary, String dueDiligence) {
        this.summary = summary;
        this.dueDiligence = dueDiligence;
    }

    // Getters and Setters
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
}