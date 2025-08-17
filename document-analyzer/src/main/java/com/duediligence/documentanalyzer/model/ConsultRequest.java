package com.duediligence.documentanalyzer.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request model for consultation API
 */
public class ConsultRequest {

    @NotBlank(message = "Domain is required")
    private String domain;

    @NotBlank(message = "Query is required")
    private String query;

    public ConsultRequest() {}

    public ConsultRequest(String domain, String query) {
        this.domain = domain;
        this.query = query;
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
}