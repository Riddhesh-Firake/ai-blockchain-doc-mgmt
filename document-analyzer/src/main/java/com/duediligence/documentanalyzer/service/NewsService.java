package com.duediligence.documentanalyzer.service;

import com.duediligence.documentanalyzer.model.ArticleDetailsResponse;
import com.duediligence.documentanalyzer.model.Domain;
import com.duediligence.documentanalyzer.model.NewsArticle;
import com.duediligence.documentanalyzer.model.NewsResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching domain-specific news using free news APIs
 */
@Service
public class NewsService {

    private static final Logger logger = LoggerFactory.getLogger(NewsService.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // API Key for NewsAPI - sign up at newsapi.org for free
    @Value("${app.api.newsapi.key:}")
    private String newsApiKey;

    // Alternative: GNews API key - sign up at gnews.io for free
    @Value("${app.api.gnews.key:}")
    private String gNewsApiKey;

    public NewsService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch latest news for specified domain
     */
    public NewsResponse getLatestNews(String domainStr) {
        long startTime = System.currentTimeMillis();

        Domain domain = Domain.fromValue(domainStr);
        if (domain == null) {
            throw new IllegalArgumentException("Invalid domain: " + domainStr);
        }

        logger.info("Fetching latest news for domain: {}", domain);

        try {
            List<NewsArticle> articles = fetchNewsArticles(domain);
            long responseTime = System.currentTimeMillis() - startTime;

            return new NewsResponse(
                    domain.getValue(),
                    articles,
                    articles.size(),
                    responseTime
            );

        } catch (Exception e) {
            logger.error("Failed to fetch news for domain: {} with error: {}", domain, e.getMessage());
            long responseTime = System.currentTimeMillis() - startTime;

            return new NewsResponse(
                    domain.getValue(),
                    new ArrayList<>(),
                    0,
                    responseTime
            );
        }
    }

    /**
     * Fetch news articles based on domain
     */
    private List<NewsArticle> fetchNewsArticles(Domain domain) {
        // Try NewsAPI first, then fall back to GNews API, then to RSS feeds
        try {
            if (newsApiKey != null && !newsApiKey.isEmpty()) {
                return fetchFromNewsAPI(domain);
            }
        } catch (Exception e) {
            logger.warn("NewsAPI failed, trying GNews API: {}", e.getMessage());
        }

        try {
            if (gNewsApiKey != null && !gNewsApiKey.isEmpty()) {
                return fetchFromGNewsAPI(domain);
            }
        } catch (Exception e) {
            logger.warn("GNews API failed, trying free RSS feeds: {}", e.getMessage());
        }

        // Fallback to free RSS feeds or public APIs
        return fetchFromPublicSources(domain);
    }

    /**
     * Fetch news from NewsAPI.org
     */
    private List<NewsArticle> fetchFromNewsAPI(Domain domain) {
        String query = getDomainQuery(domain);
        String url = String.format(
                "https://newsapi.org/v2/everything?q=%s&sortBy=publishedAt&pageSize=10&apiKey=%s",
                query, newsApiKey
        );

        String jsonResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        return parseNewsAPIResponse(jsonResponse);
    }

    /**
     * Fetch news from GNews API
     */
    private List<NewsArticle> fetchFromGNewsAPI(Domain domain) {
        String query = getDomainQuery(domain);
        String url = String.format(
                "https://gnews.io/api/v4/search?q=%s&lang=en&country=us&max=10&apikey=%s",
                query, gNewsApiKey
        );

        String jsonResponse = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();

        return parseGNewsResponse(jsonResponse);
    }

    /**
     * Fetch news from free public sources (RSS feeds, public APIs)
     */
    private List<NewsArticle> fetchFromPublicSources(Domain domain) {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            switch (domain) {
                case FINANCE -> {
                    // Use Yahoo Finance RSS or Financial Modeling Prep free endpoints
                    articles.addAll(fetchFinanceNews());
                }
                case HEALTHCARE -> {
                    // Use CDC RSS feeds or health.gov APIs
                    articles.addAll(fetchHealthcareNews());
                }
                case LEGAL -> {
                    // Use government legal RSS feeds or court APIs
                    articles.addAll(fetchLegalNews());
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching from public sources: {}", e.getMessage());
            // Return sample articles to show the structure
            articles.addAll(getSampleArticles(domain));
        }

        return articles;
    }

    /**
     * Fetch finance news from free sources
     */
    private List<NewsArticle> fetchFinanceNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            // Using Financial Modeling Prep's free tier
            String url = "https://financialmodelingprep.com/api/v3/stock_news?tickers=AAPL,GOOGL,MSFT&limit=10&apikey=demo";

            String jsonResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isArray()) {
                for (JsonNode article : rootNode) {
                    NewsArticle newsArticle = new NewsArticle();
                    newsArticle.setTitle(article.get("title").asText(""));
                    newsArticle.setUrl(article.get("url").asText(""));
                    newsArticle.setSource("Financial Modeling Prep");
                    newsArticle.setDescription(article.get("text").asText("").substring(0,
                            Math.min(200, article.get("text").asText("").length())));
                    newsArticle.setPublishedAt(article.get("publishedDate").asText(""));
                    newsArticle.setImageUrl(article.get("image").asText(""));

                    articles.add(newsArticle);

                    if (articles.size() >= 10) break;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch finance news: {}", e.getMessage());
        }

        return articles;
    }

    /**
     * Fetch healthcare news from free sources
     */
    private List<NewsArticle> fetchHealthcareNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            // Using disease.sh API for COVID-related health news
            String url = "https://disease.sh/v3/covid-19/gov/usa";

            String jsonResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            if (rootNode.isArray()) {
                for (JsonNode item : rootNode) {
                    NewsArticle article = new NewsArticle();
                    article.setTitle(item.get("title").asText("Health Update"));
                    article.setUrl(item.get("url").asText(""));
                    article.setSource("Government Health Data");
                    article.setDescription("Healthcare information and updates");
                    article.setPublishedAt(item.get("date").asText(""));

                    articles.add(article);

                    if (articles.size() >= 10) break;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch healthcare news: {}", e.getMessage());
        }

        return articles;
    }

    /**
     * Get detailed article content by scraping the URL
     */
    public ArticleDetailsResponse getArticleDetails(String articleUrl) {
        long startTime = System.currentTimeMillis();

        if (articleUrl == null || articleUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Article URL cannot be empty");
        }

        logger.info("Fetching article details for URL: {}", articleUrl);

        try {
            // Use Jsoup to scrape article content
            String fullContent = scrapeArticleContent(articleUrl);
            String summary = generateSummary(fullContent);
            String[] keywords = extractKeywords(fullContent);
            int wordCount = fullContent.split("\\s+").length;
            String language = detectLanguage(fullContent);

            long responseTime = System.currentTimeMillis() - startTime;

            return new ArticleDetailsResponse(
                    articleUrl,
                    fullContent,
                    summary,
                    keywords,
                    wordCount,
                    language,
                    responseTime
            );

        } catch (Exception e) {
            logger.error("Failed to fetch article details for URL: {} with error: {}", articleUrl, e.getMessage());
            long responseTime = System.currentTimeMillis() - startTime;

            return new ArticleDetailsResponse(
                    articleUrl,
                    "Unable to fetch full content at this time.",
                    "Content unavailable",
                    new String[]{},
                    0,
                    "unknown",
                    responseTime
            );
        }
    }

    /**
     * Search news articles by keywords
     */
    public NewsResponse searchNews(String domainStr, String keywords, int page, int size) {
        long startTime = System.currentTimeMillis();

        Domain domain = Domain.fromValue(domainStr);
        if (domain == null) {
            throw new IllegalArgumentException("Invalid domain: " + domainStr);
        }

        logger.info("Searching news for domain: {}, keywords: {}", domain, keywords);

        try {
            List<NewsArticle> articles = searchNewsArticles(domain, keywords, page, size);
            long responseTime = System.currentTimeMillis() - startTime;

            return new NewsResponse(
                    domain.getValue(),
                    articles,
                    articles.size(),
                    responseTime
            );

        } catch (Exception e) {
            logger.error("Failed to search news for domain: {} with error: {}", domain, e.getMessage());
            long responseTime = System.currentTimeMillis() - startTime;

            return new NewsResponse(
                    domain.getValue(),
                    new ArrayList<>(),
                    0,
                    responseTime
            );
        }
    }

    /**
     * Scrape article content using Jsoup
     */
    private String scrapeArticleContent(String url) {
        try {
            // Add Jsoup dependency to your pom.xml:
            // <dependency>
            //     <groupId>org.jsoup</groupId>
            //     <artifactId>jsoup</artifactId>
            //     <version>1.16.1</version>
            // </dependency>

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

            // Common article content selectors
            String[] selectors = {
                    "article",
                    "[role='main'] p",
                    ".article-content",
                    ".post-content",
                    ".entry-content",
                    ".content p",
                    "main p"
            };

            for (String selector : selectors) {
                Elements elements = doc.select(selector);
                if (!elements.isEmpty()) {
                    return elements.text();
                }
            }

            // Fallback: get all paragraph text
            Elements paragraphs = doc.select("p");
            StringBuilder content = new StringBuilder();
            for (Element p : paragraphs) {
                String text = p.text();
                if (text.length() > 50) { // Filter out short paragraphs
                    content.append(text).append("\n\n");
                }
            }

            String result = content.toString().trim();
            return result.isEmpty() ? "Unable to extract article content" : result;

        } catch (Exception e) {
            logger.warn("Failed to scrape content from URL: {} - {}", url, e.getMessage());
            return "Content could not be retrieved from the source.";
        }
    }

    /**
     * Generate a simple summary from full content
     */
    private String generateSummary(String fullContent) {
        if (fullContent == null || fullContent.length() < 200) {
            return fullContent;
        }

        // Simple summary: first 300 characters + "..."
        String summary = fullContent.substring(0, Math.min(300, fullContent.length()));
        if (fullContent.length() > 300) {
            // Try to end at a sentence
            int lastSentence = summary.lastIndexOf('.');
            if (lastSentence > 200) {
                summary = summary.substring(0, lastSentence + 1);
            }
            summary += "...";
        }

        return summary;
    }

    /**
     * Extract keywords from content (simple implementation)
     */
    private String[] extractKeywords(String content) {
        if (content == null || content.isEmpty()) {
            return new String[]{};
        }

        // Simple keyword extraction: most common non-stop words
        String[] stopWords = {"the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "can", "may", "might", "this", "that", "these", "those"};
        Set<String> stopWordsSet = Set.of(stopWords);

        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = content.toLowerCase().replaceAll("[^a-zA-Z\\s]", "").split("\\s+");

        for (String word : words) {
            if (word.length() > 3 && !stopWordsSet.contains(word)) {
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }

        return wordCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Map.Entry::getKey)
                .toArray(String[]::new);
    }

    /**
     * Detect content language (simple implementation)
     */
    private String detectLanguage(String content) {
        // Simple language detection based on common English words
        String[] englishIndicators = {"the", "and", "is", "in", "to", "of", "a", "for", "are", "as"};
        String lowerContent = content.toLowerCase();

        int englishScore = 0;
        for (String indicator : englishIndicators) {
            if (lowerContent.contains(indicator)) {
                englishScore++;
            }
        }

        return englishScore >= 3 ? "en" : "unknown";
    }

    /**
     * Search news articles with keywords
     */
    private List<NewsArticle> searchNewsArticles(Domain domain, String keywords, int page, int size) {
        // This would integrate with your existing news fetching logic
        // For now, filter existing results by keywords
        List<NewsArticle> allArticles = fetchNewsArticles(domain);

        return allArticles.stream()
                .filter(article ->
                        article.getTitle().toLowerCase().contains(keywords.toLowerCase()) ||
                                article.getDescription().toLowerCase().contains(keywords.toLowerCase())
                )
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * Fetch legal news from free sources
     */
    private List<NewsArticle> fetchLegalNews() {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            // Using CourtListener API for legal news
            String url = "https://www.courtlistener.com/api/rest/v3/search/?type=o&order_by=-dateFiled&format=json";

            String jsonResponse = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode results = rootNode.get("results");

            if (results != null && results.isArray()) {
                for (JsonNode result : results) {
                    NewsArticle article = new NewsArticle();
                    article.setTitle("Legal Case: " + result.get("caseName").asText(""));
                    article.setUrl("https://www.courtlistener.com" + result.get("absolute_url").asText(""));
                    article.setSource("CourtListener");
                    article.setDescription("Legal case filing and court documents");
                    article.setPublishedAt(result.get("dateFiled").asText(""));

                    articles.add(article);

                    if (articles.size() >= 5) break;
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch legal news: {}", e.getMessage());
        }

        return articles;
    }

    /**
     * Parse NewsAPI response
     */
    private List<NewsArticle> parseNewsAPIResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode articlesNode = rootNode.get("articles");

            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode articleNode : articlesNode) {
                    NewsArticle article = new NewsArticle();
                    article.setTitle(articleNode.get("title").asText(""));
                    article.setUrl(articleNode.get("url").asText(""));
                    article.setSource(articleNode.get("source").get("name").asText(""));
                    article.setDescription(articleNode.get("description").asText(""));
                    article.setPublishedAt(articleNode.get("publishedAt").asText(""));
                    article.setImageUrl(articleNode.get("urlToImage").asText(""));

                    articles.add(article);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing NewsAPI response: {}", e.getMessage());
        }

        return articles;
    }

    /**
     * Parse GNews API response
     */
    private List<NewsArticle> parseGNewsResponse(String jsonResponse) {
        List<NewsArticle> articles = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode articlesNode = rootNode.get("articles");

            if (articlesNode != null && articlesNode.isArray()) {
                for (JsonNode articleNode : articlesNode) {
                    NewsArticle article = new NewsArticle();
                    article.setTitle(articleNode.get("title").asText(""));
                    article.setUrl(articleNode.get("url").asText(""));
                    article.setSource(articleNode.get("source").get("name").asText(""));
                    article.setDescription(articleNode.get("description").asText(""));
                    article.setPublishedAt(articleNode.get("publishedAt").asText(""));
                    article.setImageUrl(articleNode.get("image").asText(""));

                    articles.add(article);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing GNews response: {}", e.getMessage());
        }

        return articles;
    }

    /**
     * Get sample articles for demonstration
     */
    private List<NewsArticle> getSampleArticles(Domain domain) {
        List<NewsArticle> articles = new ArrayList<>();

        switch (domain) {
            case FINANCE -> {
                articles.add(new NewsArticle(
                        "Market Analysis: Tech Stocks Rise",
                        "https://example.com/finance/1",
                        "Financial Times",
                        "Technology stocks showed strong performance this quarter...",
                        "2025-08-12T10:00:00Z",
                        ""
                ));
                articles.add(new NewsArticle(
                        "Federal Reserve Interest Rate Decision",
                        "https://example.com/finance/2",
                        "Reuters",
                        "The Federal Reserve announced its latest interest rate decision...",
                        "2025-08-12T09:30:00Z",
                        ""
                ));
            }
            case HEALTHCARE -> {
                articles.add(new NewsArticle(
                        "New Medical Research Published",
                        "https://example.com/health/1",
                        "Medical Journal",
                        "Researchers published new findings on treatment effectiveness...",
                        "2025-08-12T11:00:00Z",
                        ""
                ));
                articles.add(new NewsArticle(
                        "Healthcare Policy Updates",
                        "https://example.com/health/2",
                        "Health News",
                        "Recent changes in healthcare policy affect millions...",
                        "2025-08-12T08:45:00Z",
                        ""
                ));
            }
            case LEGAL -> {
                articles.add(new NewsArticle(
                        "Supreme Court Ruling Impact",
                        "https://example.com/legal/1",
                        "Legal Tribune",
                        "Latest Supreme Court decision affects business regulations...",
                        "2025-08-12T12:00:00Z",
                        ""
                ));
                articles.add(new NewsArticle(
                        "Corporate Law Changes",
                        "https://example.com/legal/2",
                        "Law Journal",
                        "New corporate compliance requirements take effect...",
                        "2025-08-12T07:30:00Z",
                        ""
                ));
            }
        }

        return articles;
    }

    /**
     * Get search query for domain
     */
    private String getDomainQuery(Domain domain) {
        return switch (domain) {
            case FINANCE -> "finance OR stock OR market OR economy OR investment";
            case HEALTHCARE -> "health OR medical OR healthcare OR medicine OR hospital";
            case LEGAL -> "legal OR law OR court OR regulation OR compliance";
        };
    }
}