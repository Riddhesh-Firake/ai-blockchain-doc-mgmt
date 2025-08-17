package com.duediligence.documentanalyzer.model;

/**
 * Model representing a news article
 */
public class NewsArticle {
    private String title;
    private String url;
    private String source;
    private String description;
    private String publishedAt;
    private String imageUrl;

    public NewsArticle() {
    }

    public NewsArticle(String title, String url, String source, String description, String publishedAt, String imageUrl) {
        this.title = title;
        this.url = url;
        this.source = source;
        this.description = description;
        this.publishedAt = publishedAt;
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
