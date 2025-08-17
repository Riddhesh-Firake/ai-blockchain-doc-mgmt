package com.duediligence.documentanalyzer.model;

public class ArticleDetailsResponse {
    private String url;
    private String fullContent;
    private String summary;
    private String[] keywords;
    private int wordCount;
    private String language;
    private long responseTimeMs;

    public ArticleDetailsResponse() {}

    public ArticleDetailsResponse(String url, String fullContent, String summary,
                                  String[] keywords, int wordCount, String language, long responseTimeMs) {
        this.url = url;
        this.fullContent = fullContent;
        this.summary = summary;
        this.keywords = keywords;
        this.wordCount = wordCount;
        this.language = language;
        this.responseTimeMs = responseTimeMs;
    }

    // Getters and setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getFullContent() { return fullContent; }
    public void setFullContent(String fullContent) { this.fullContent = fullContent; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String[] getKeywords() { return keywords; }
    public void setKeywords(String[] keywords) { this.keywords = keywords; }

    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}