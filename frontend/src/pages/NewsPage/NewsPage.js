import React, { useState, useEffect } from 'react';
import ApiService from '../../services/ApiService';
import './NewsPage.css';

function NewsPage() {
  const [news, setNews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedDomain, setSelectedDomain] = useState('finance');
  const [error, setError] = useState(null);
  const [expandedCard, setExpandedCard] = useState(null);
  const [supportedDomains, setSupportedDomains] = useState([]);
  const [responseTime, setResponseTime] = useState(0);

  const domains = [
    { value: 'finance', label: 'Finance', icon: '💰' },
    { value: 'healthcare', label: 'Healthcare', icon: '🏥' },
    { value: 'legal', label: 'Legal', icon: '⚖️' }
  ];

  useEffect(() => {
    fetchSupportedDomains();
    fetchNews(selectedDomain);
  }, []);

  useEffect(() => {
    if (selectedDomain) {
      fetchNews(selectedDomain);
    }
  }, [selectedDomain]);

  const fetchSupportedDomains = async () => {
    try {
      const domains = await ApiService.getNewsDomains();
      setSupportedDomains(domains);
    } catch (error) {
      console.error('Failed to fetch supported domains:', error);
    }
  };

  const fetchNews = async (domain) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await ApiService.getLatestNews(domain);
      setNews(response.news || []);
      setResponseTime(response.responseTimeMs || 0);
    } catch (error) {
      console.error('Failed to fetch news:', error);
      setError('Failed to load news. Please try again later.');
      setNews([]);
    } finally {
      setLoading(false);
    }
  };

  const handleDomainChange = (domain) => {
    setSelectedDomain(domain);
    setExpandedCard(null); // Close any expanded card
  };

  const handleCardClick = async (articleIndex) => {
    if (expandedCard === articleIndex) {
      setExpandedCard(null);
    } else {
      setExpandedCard(articleIndex);
      // Fetch detailed content for this article
      try {
        const detailedArticle = await ApiService.getArticleDetails(news[articleIndex].url);
        // Update the article with detailed content
        const updatedNews = [...news];
        updatedNews[articleIndex] = { ...updatedNews[articleIndex], ...detailedArticle };
        setNews(updatedNews);
      } catch (error) {
        console.error('Failed to fetch article details:', error);
      }
    }
  };

  const formatDate = (dateString) => {
    try {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (error) {
      return 'Unknown date';
    }
  };

  const handleRefresh = () => {
    fetchNews(selectedDomain);
  };

  return (
    <div className="news-page">
      <div className="news-container">
        {/* Header Section */}
        <div className="news-header">
          <div className="header-content">
            <div className="title-section">
              <h1 className="news-title">
                <span className="news-icon">📰</span>
                News & Updates
              </h1>
              <p className="news-subtitle">
                Stay informed with the latest developments in your industry
              </p>
            </div>
            
            <div className="header-stats">
              {news.length > 0 && (
                <div className="stats-item">
                  <span className="stats-number">{news.length}</span>
                  <span className="stats-label">Articles</span>
                </div>
              )}
              {responseTime > 0 && (
                <div className="stats-item">
                  <span className="stats-number">{responseTime}ms</span>
                  <span className="stats-label">Load Time</span>
                </div>
              )}
            </div>
          </div>

          {/* Domain Selection */}
          <div className="domain-selector">
            {domains.map((domain) => (
              <button
                key={domain.value}
                className={`domain-button ${selectedDomain === domain.value ? 'active' : ''}`}
                onClick={() => handleDomainChange(domain.value)}
                disabled={loading}
              >
                <span className="domain-icon">{domain.icon}</span>
                <span className="domain-label">{domain.label}</span>
              </button>
            ))}
            
            <button 
              className="refresh-button"
              onClick={handleRefresh}
              disabled={loading}
              title="Refresh news"
            >
              <span className={`refresh-icon ${loading ? 'spinning' : ''}`}>🔄</span>
            </button>
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p className="loading-text">Fetching latest {selectedDomain} news...</p>
          </div>
        )}

        {/* Error State */}
        {error && (
          <div className="error-container">
            <div className="error-content">
              <span className="error-icon">❌</span>
              <h3>Unable to Load News</h3>
              <p>{error}</p>
              <button className="retry-button" onClick={handleRefresh}>
                Try Again
              </button>
            </div>
          </div>
        )}

        {/* News Grid */}
        {!loading && !error && news.length > 0 && (
          <div className="news-grid">
            {news.map((article, index) => (
              <div
                key={index}
                className={`news-card ${expandedCard === index ? 'expanded' : ''}`}
                onClick={() => handleCardClick(index)}
              >
                {/* Card Header */}
                <div className="card-header">
                  {article.imageUrl && (
                    <div className="card-image">
                      <img 
                        src={article.imageUrl} 
                        alt={article.title}
                        onError={(e) => {
                          e.target.style.display = 'none';
                        }}
                      />
                      <div className="image-overlay"></div>
                    </div>
                  )}
                  
                  <div className="card-meta">
                    <span className="source-badge">{article.source}</span>
                    <span className="date-badge">{formatDate(article.publishedAt)}</span>
                  </div>
                </div>

                {/* Card Content */}
                <div className="card-content">
                  <h3 className="card-title">{article.title}</h3>
                  
                  <p className="card-description">
                    {article.description || 'No description available...'}
                  </p>

                  {/* Expanded Content */}
                  {expandedCard === index && (
                    <div className="expanded-content">
                      {article.fullContent && (
                        <div className="full-content">
                          <h4>Full Article</h4>
                          <p>{article.fullContent}</p>
                        </div>
                      )}
                      
                      <div className="article-actions">
                        <a 
                          href={article.url} 
                          target="_blank" 
                          rel="noopener noreferrer"
                          className="read-more-button"
                          onClick={(e) => e.stopPropagation()}
                        >
                          Read Full Article →
                        </a>
                      </div>
                    </div>
                  )}
                </div>

                {/* Card Footer */}
                <div className="card-footer">
                  <div className="card-tags">
                    <span className="domain-tag">{selectedDomain}</span>
                  </div>
                  
                  <div className="card-actions">
                    <button 
                      className="expand-button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleCardClick(index);
                      }}
                    >
                      {expandedCard === index ? '▲ Collapse' : '▼ Expand'}
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Empty State */}
        {!loading && !error && news.length === 0 && (
          <div className="empty-container">
            <div className="empty-content">
              <span className="empty-icon">📰</span>
              <h3>No News Available</h3>
              <p>No news articles found for {selectedDomain}. Try refreshing or selecting a different domain.</p>
              <button className="refresh-button-large" onClick={handleRefresh}>
                <span className="refresh-icon">🔄</span>
                Refresh News
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default NewsPage;