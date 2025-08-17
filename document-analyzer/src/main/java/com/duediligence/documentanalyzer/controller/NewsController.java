package com.duediligence.documentanalyzer.controller;

import com.duediligence.documentanalyzer.model.NewsResponse;
import com.duediligence.documentanalyzer.service.NewsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.duediligence.documentanalyzer.model.ArticleDetailsRequest;
import com.duediligence.documentanalyzer.model.ArticleDetailsResponse;
/**
 * REST Controller for news services
 */
@RestController
@RequestMapping("/api/news")
@PreAuthorize("isAuthenticated()")
@CrossOrigin(origins = "*") // Configure as needed for your frontend
public class NewsController {

    private static final Logger logger = LoggerFactory.getLogger(NewsController.class);

    private final NewsService newsService;

    @Autowired
    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    /**
     * GET /api/news/{domain}
     * Get latest news for specified domain
     */
    @GetMapping("/{domain}")
    public ResponseEntity<NewsResponse> getLatestNews(@PathVariable String domain) {
        logger.info("Received news request for domain: {}", domain);

        try {
            NewsResponse response = newsService.getLatestNews(domain);

            logger.info("News fetched successfully for domain: {} - {} articles",
                    domain, response.getTotalArticles());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid news request for domain: {}", domain);
            NewsResponse errorResponse = new NewsResponse(domain, null, 0, 0);
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("News fetch failed for domain: {} with error: {}", domain, e.getMessage());
            NewsResponse errorResponse = new NewsResponse(domain, null, 0, 0);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * POST /api/news/article-details
     * Get detailed content for a specific article URL
     */
    @PostMapping("/article-details")
    public ResponseEntity<ArticleDetailsResponse> getArticleDetails(@RequestBody ArticleDetailsRequest request) {
        logger.info("Received article details request for URL: {}", request.getUrl());

        try {
            ArticleDetailsResponse response = newsService.getArticleDetails(request.getUrl());

            logger.info("Article details fetched successfully for URL: {}", request.getUrl());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid article details request for URL: {}", request.getUrl());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            logger.error("Article details fetch failed for URL: {} with error: {}",
                    request.getUrl(), e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/news/{domain}/search
     * Search news articles by keywords
     */
    @GetMapping("/{domain}/search")
    public ResponseEntity<NewsResponse> searchNews(
            @PathVariable String domain,
            @RequestParam String keywords,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        logger.info("Received news search request for domain: {}, keywords: {}", domain, keywords);

        try {
            NewsResponse response = newsService.searchNews(domain, keywords, page, size);

            logger.info("News search completed successfully for domain: {} - {} articles",
                    domain, response.getTotalArticles());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid news search request for domain: {}", domain);
            NewsResponse errorResponse = new NewsResponse(domain, null, 0, 0);
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            logger.error("News search failed for domain: {} with error: {}", domain, e.getMessage());
            NewsResponse errorResponse = new NewsResponse(domain, null, 0, 0);
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * GET /api/news/domains
     * Get list of supported domains for news
     */
    @GetMapping("/domains")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String[]> getSupportedDomains() {
        String[] domains = {"finance", "healthcare", "legal"};
        return ResponseEntity.ok(domains);
    }

    /**
     * GET /api/news/health
     * Health check endpoint for news service
     */
    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("News service is running");
    }
}