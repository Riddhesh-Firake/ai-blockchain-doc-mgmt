import React, { useState, useEffect } from 'react';
import ApiService from '../../services/ApiService.js';
import './ConsultPage.css';

function ConsultPage() {
  const [selectedDomain, setSelectedDomain] = useState('');
  const [query, setQuery] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [consultHistory, setConsultHistory] = useState([]);
  const [supportedDomains, setSupportedDomains] = useState([]);

  const domainInfo = {
    finance: {
      icon: '💰',
      title: 'Finance',
      description: 'Investment advice, tax planning, financial analysis',
      color: 'var(--finance-color)'
    },
    healthcare: {
      icon: '🏥',
      title: 'Healthcare',
      description: 'Medical guidance, insurance, healthcare planning',
      color: 'var(--healthcare-color)'
    },
    legal: {
      icon: '⚖️',
      title: 'Legal',
      description: 'Contract review, compliance, legal consultation',
      color: 'var(--legal-color)'
    }
  };

  useEffect(() => {
    loadSupportedDomains();
  }, []);

  const loadSupportedDomains = async () => {
    try {
      const response = await ApiService.getConsultationDomains();
      setSupportedDomains(response || ['finance', 'healthcare', 'legal']);
    } catch (error) {
      console.error('Failed to load domains:', error);
      setSupportedDomains(['finance', 'healthcare', 'legal']);
    }
  };

  const handleConsultSubmit = async (e) => {
    e.preventDefault();
    
    if (!selectedDomain || !query.trim()) {
      alert('Please select a domain and enter your query.');
      return;
    }

    setIsLoading(true);
    
    try {
      const response = await ApiService.submitConsultation({
        domain: selectedDomain,
        query: query
      });

      const newConsult = {
        id: Date.now(),
        domain: selectedDomain,
        query: query,
        response: response.response,
        respondent: response.respondent,
        responseTime: response.responseTime,
        timestamp: new Date()
      };

      setConsultHistory(prev => [newConsult, ...prev]);
      setQuery('');
      
    } catch (error) {
      console.error('Consultation failed:', error);
      alert('Failed to process consultation: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const formatResponseText = (text) => {
    if (!text) return text;
    
    // Convert markdown-style formatting to proper line breaks and formatting
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>') // Bold text
      .replace(/\*(.*?)\*/g, '<em>$1</em>') // Italic text
      .replace(/(\d+\.\s)/g, '\n$1') // Add line breaks before numbered lists
      .replace(/(\*\s)/g, '\n• ') // Convert asterisk lists to bullet points
      .trim();
  };

  const formatTime = (date) => {
    return new Date(date).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="consult-page">
      <div className="consult-container">
        {/* Header */}
        <div className="consult-header">
          <div className="header-content">
            <h1 className="page-title">
              <span className="title-icon">🧠</span>
              AI Consultation Service
            </h1>
            <p className="page-subtitle">
              Get expert AI-powered consultation across multiple domains
            </p>
          </div>
        </div>

        {/* Domain Selection */}
        <div className="domain-section">
          <h2 className="section-title">Select Consultation Domain</h2>
          <div className="domain-grid">
            {supportedDomains.map(domain => (
              <div
                key={domain}
                className={`domain-card ${selectedDomain === domain ? 'selected' : ''}`}
                onClick={() => setSelectedDomain(domain)}
              >
                <div className="domain-icon">
                  {domainInfo[domain]?.icon || '📋'}
                </div>
                <h3 className="domain-title">
                  {domainInfo[domain]?.title || domain}
                </h3>
                <p className="domain-description">
                  {domainInfo[domain]?.description || `${domain} consultation services`}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Consultation Form */}
        <div className="consultation-form-section">
          <h2 className="section-title">Submit Your Query</h2>
          <form onSubmit={handleConsultSubmit} className="consultation-form">
            <div className="form-group">
              <label htmlFor="query" className="form-label">
                Your Question or Request
              </label>
              <textarea
                id="query"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Describe your consultation needs in detail..."
                className="form-textarea"
                rows="4"
                disabled={isLoading}
              />
            </div>
            <button
              type="submit"
              className={`submit-button ${isLoading ? 'loading' : ''}`}
              disabled={isLoading || !selectedDomain || !query.trim()}
            >
              {isLoading ? (
                <span className="loading-content">
                  <span className="spinner"></span>
                  Processing...
                </span>
              ) : (
                <span className="button-content">
                  <span className="button-icon">🚀</span>
                  Get Consultation
                </span>
              )}
            </button>
          </form>
        </div>

        {/* Consultation History */}
        {consultHistory.length > 0 && (
          <div className="history-section">
            <h2 className="section-title">Consultation History</h2>
            <div className="history-list">
              {consultHistory.map(consult => (
                <div key={consult.id} className="history-item">
                  <div className="history-header">
                    <div className="history-domain">
                      <span className="domain-badge" style={{backgroundColor: domainInfo[consult.domain]?.color}}>
                        {domainInfo[consult.domain]?.icon} {domainInfo[consult.domain]?.title}
                      </span>
                      <span className="history-time">
                        {formatTime(consult.timestamp)}
                      </span>
                    </div>
                    <div className="response-info">
                      <span className="respondent">By {consult.respondent}</span>
                      <span className="response-time">{consult.responseTime}ms</span>
                    </div>
                  </div>
                  <div className="history-content">
                    <div className="query-section">
                      <h4>Your Query:</h4>
                      <p>{consult.query}</p>
                    </div>
                    <div className="response-section">
                      <h4>Response:</h4>
                      <div 
                        dangerouslySetInnerHTML={{ 
                          __html: formatResponseText(consult.response) 
                        }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default ConsultPage;