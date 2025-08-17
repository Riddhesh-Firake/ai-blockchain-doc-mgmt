package com.duediligence.documentanalyzer.model;

/**
 * Response model for consultation API
 */
public class ConsultResponse {
    private String domain;
    private String query;
    private String response;
    private String source;
    private long responseTimeMs;

    public ConsultResponse() {}

    public ConsultResponse(String domain, String query, String response, String source, long responseTimeMs) {
        this.domain = domain;
        this.query = query;
        this.response = response;
        this.source = source;
        this.responseTimeMs = responseTimeMs;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}