package com.duediligence.documentanalyzer.model;

import java.util.List;

/**
 * Response model for news API
 */
public class NewsResponse {
    private String domain;
    private List<NewsArticle> news;
    private int totalArticles;
    private long responseTimeMs;

    public NewsResponse() {}

    public NewsResponse(String domain, List<NewsArticle> news, int totalArticles, long responseTimeMs) {
        this.domain = domain;
        this.news = news;
        this.totalArticles = totalArticles;
        this.responseTimeMs = responseTimeMs;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<NewsArticle> getNews() {
        return news;
    }

    public void setNews(List<NewsArticle> news) {
        this.news = news;
    }

    public int getTotalArticles() {
        return totalArticles;
    }

    public void setTotalArticles(int totalArticles) {
        this.totalArticles = totalArticles;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}

