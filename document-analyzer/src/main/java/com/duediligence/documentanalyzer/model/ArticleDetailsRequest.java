package com.duediligence.documentanalyzer.model;

public class ArticleDetailsRequest {
    private String url;

    public ArticleDetailsRequest() {}

    public ArticleDetailsRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}