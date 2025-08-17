import React, { useState, useCallback, useRef } from 'react';
import './DocumentSummarizer.css';
import ApiService from '../../services/ApiService'; // Import your existing ApiService

function DocumentSummarizer() {
  const [files, setFiles] = useState([]);
  const [domain, setDomain] = useState('finance');
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState('');
  const [dragActive, setDragActive] = useState(false);
  const [supportedDomains] = useState(['finance', 'healthcare', 'legal']);
  const fileInputRef = useRef(null);

  const handleDrag = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  }, []);

  const handleDrop = useCallback((e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const droppedFiles = Array.from(e.dataTransfer.files);
      setFiles(prev => [...prev, ...droppedFiles]);
    }
  }, []);

  const handleFileSelect = (e) => {
    if (e.target.files) {
      const selectedFiles = Array.from(e.target.files);
      setFiles(prev => [...prev, ...selectedFiles]);
    }
  };

  const removeFile = (index) => {
    setFiles(prev => prev.filter((_, i) => i !== index));
  };

  const validateFiles = () => {
    if (files.length === 0) {
      throw new Error('Please select at least one file to analyze');
    }

    const maxSize = 50 * 1024 * 1024; // 50MB
    const supportedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];

    files.forEach(file => {
      if (file.size > maxSize) {
        throw new Error(`File "${file.name}" is too large. Maximum size is 50MB.`);
      }
      
      if (!supportedTypes.some(type => file.type.includes(type.split('/')[1]) || file.name.toLowerCase().endsWith('.pdf') || file.name.toLowerCase().endsWith('.doc') || file.name.toLowerCase().endsWith('.docx'))) {
        console.warn(`File type might not be supported: ${file.type} for ${file.name}`);
      }
    });
  };

  const handleAnalyze = async () => {
    try {
      setError('');
      setAnalysisResult(null);
      validateFiles();
      
      setIsAnalyzing(true);
      
      const result = await ApiService.analyzeDocuments(files, domain);
      
      if (result.status && result.status.startsWith('ERROR')) {
        throw new Error(result.status.replace('ERROR: ', ''));
      }
      
      setAnalysisResult(result);
    } catch (err) {
      console.error('Analysis failed:', err);
      setError(err.message || 'Analysis failed. Please try again.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const handleUploadClick = () => {
    fileInputRef.current?.click();
  };

  const resetForm = () => {
    setFiles([]);
    setAnalysisResult(null);
    setError('');
    setDomain('finance');
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getDomainIcon = (domainValue) => {
    const icons = {
      finance: '💰',
      healthcare: '🏥',
      legal: '⚖️'
    };
    return icons[domainValue] || '📄';
  };

  const formatDateTime = (dateTime) => {
    if (!dateTime) return 'N/A';
    return new Date(dateTime).toLocaleString();
  };

  const getRiskLevelColor = (riskText) => {
    if (!riskText) return '#4a5568';
    const text = riskText.toLowerCase();
    if (text.includes('low') || text.includes('minimal')) return '#38a169';
    if (text.includes('medium') || text.includes('moderate')) return '#d69e2e';
    if (text.includes('high') || text.includes('significant')) return '#e53e3e';
    return '#4a5568';
  };

  const getRatingColor = (rating) => {
    if (!rating) return '#4a5568';
    const text = rating.toLowerCase();
    if (text.includes('acceptable') || text.includes('standard') || text.includes('good')) return '#38a169';
    if (text.includes('high risk') || text.includes('critical')) return '#e53e3e';
    if (text.includes('review') || text.includes('caution')) return '#d69e2e';
    return '#4a5568';
  };

  const parseDueDiligenceReport = (report) => {
    if (!report) return null;
    
    const sections = report.split('\n').filter(line => line.trim() !== '');
    const parsedSections = [];
    let currentSection = null;

    sections.forEach(line => {
      const trimmed = line.trim();
      
      // Check if it's a main section header (numbered)
      if (/^\d+\./.test(trimmed)) {
        if (currentSection) {
          parsedSections.push(currentSection);
        }
        currentSection = {
          title: trimmed.replace(/^\d+\.\s*/, '').replace(':', ''),
          items: []
        };
      } else if (trimmed.startsWith('•') && currentSection) {
        // Bullet point item
        currentSection.items.push(trimmed.substring(1).trim());
      } else if (trimmed.includes(':') && currentSection && !trimmed.startsWith('=')) {
        // Key-value pair
        const [key, ...valueParts] = trimmed.split(':');
        const value = valueParts.join(':').trim();
        currentSection.items.push({ key: key.trim(), value });
      } else if (trimmed.length > 0 && !trimmed.startsWith('=')) {
        // Regular text
        if (currentSection) {
          currentSection.items.push(trimmed);
        }
      }
    });

    if (currentSection) {
      parsedSections.push(currentSection);
    }

    return parsedSections;
  };

  return (
    <div className="document-summarizer">
      <div className="container">
        <header className="header">
          <div className="header-content">
            <div className="header-icon">🤖</div>
            <div>
              <h1 className="header-title">AI Document Analyzer</h1>
              <p className="header-subtitle">
                Upload documents and get intelligent analysis powered by advanced AI
              </p>
            </div>
          </div>
        </header>

        {!analysisResult ? (
          <div className="upload-section">
            <div className="domain-selector">
              <label className="domain-label">Analysis Domain</label>
              <div className="domain-options">
                {supportedDomains.map((domainOption) => (
                  <button
                    key={domainOption}
                    type="button"
                    className={`domain-option ${domain === domainOption ? 'active' : ''}`}
                    onClick={() => setDomain(domainOption)}
                  >
                    <span className="domain-icon">{getDomainIcon(domainOption)}</span>
                    <span className="domain-text">
                      {domainOption.charAt(0).toUpperCase() + domainOption.slice(1)}
                    </span>
                  </button>
                ))}
              </div>
            </div>

            <div
              className={`file-upload-area ${dragActive ? 'drag-active' : ''}`}
              onDragEnter={handleDrag}
              onDragLeave={handleDrag}
              onDragOver={handleDrag}
              onDrop={handleDrop}
              onClick={handleUploadClick}
            >
              <div className="upload-content">
                <div className="upload-icon">📁</div>
                <h3 className="upload-title">Drop your files here</h3>
                <p className="upload-subtitle">
                  or <span className="upload-link">click to browse</span>
                </p>
                <p className="upload-info">
                  Supports PDF, DOC, DOCX files up to 50MB each
                </p>
              </div>
              <input
                ref={fileInputRef}
                type="file"
                multiple
                accept=".pdf,.doc,.docx"
                onChange={handleFileSelect}
                className="file-input"
              />
            </div>

            {files.length > 0 && (
              <div className="file-list">
                <h4 className="file-list-title">Selected Files ({files.length})</h4>
                <div className="files">
                  {files.map((file, index) => (
                    <div key={index} className="file-item">
                      <div className="file-info">
                        <span className="file-icon">📄</span>
                        <div className="file-details">
                          <span className="file-name">{file.name}</span>
                          <span className="file-size">{formatFileSize(file.size)}</span>
                        </div>
                      </div>
                      <button
                        onClick={() => removeFile(index)}
                        className="remove-file-btn"
                        aria-label="Remove file"
                      >
                        ✕
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {error && (
              <div className="error-message">
                <span className="error-icon">⚠️</span>
                {error}
              </div>
            )}

            <div className="action-buttons">
              <button
                onClick={handleAnalyze}
                disabled={files.length === 0 || isAnalyzing}
                className={`analyze-btn ${isAnalyzing ? 'loading' : ''}`}
              >
                {isAnalyzing ? (
                  <>
                    <div className="spinner"></div>
                    Analyzing...
                  </>
                ) : (
                  <>
                    <span>🔍</span>
                    Analyze Documents
                  </>
                )}
              </button>
              
              {files.length > 0 && (
                <button onClick={resetForm} className="reset-btn">
                  Clear All
                </button>
              )}
            </div>
          </div>
        ) : (
          <div className="results-section">
            <div className="results-header">
              <h2 className="results-title">Analysis Results</h2>
              <div className="results-meta">
                <span className="results-domain">
                  {getDomainIcon(analysisResult.domain)} {analysisResult.domain}
                </span>
                <span className="processing-time">
                  ⏱️ {analysisResult.processingTimeMs}ms
                </span>
              </div>
            </div>

            {analysisResult.files && (
              <div className="processed-files">
                <h3 className="section-title">📁 Processed Files</h3>
                <div className="processed-files-grid">
                  {analysisResult.files.map((fileInfo, index) => (
                    <div key={index} className="processed-file">
                      <span className="file-icon">📄</span>
                      <div className="processed-file-info">
                        <span className="processed-file-name">{fileInfo.name}</span>
                        <div className="processed-file-details">
                          <span className="processed-file-size">{formatFileSize(fileInfo.size || 0)}</span>
                          <span className="processed-file-time">{formatDateTime(fileInfo.uploadTime)}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {analysisResult.summary && (
              <div className="analysis-section summary-section">
                <h3 className="section-title">📊 Executive Summary</h3>
                <div className="summary-content">
                  <div className="summary-text">{analysisResult.summary}</div>
                  <div className="summary-stats">
                    <div className="stat-item">
                      <span className="stat-label">Processing Time</span>
                      <span className="stat-value">{analysisResult.processingTimeMs}ms</span>
                    </div>
                    <div className="stat-item">
                      <span className="stat-label">Domain</span>
                      <span className="stat-value">
                        {getDomainIcon(analysisResult.domain)} {analysisResult.domain}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {analysisResult.dueDiligence && (
              <div className="analysis-section due-diligence-section">
                <h3 className="section-title">🔍 Due Diligence Report</h3>
                <div className="due-diligence-content">
                  {parseDueDiligenceReport(analysisResult.dueDiligence) ? (
                    <div className="parsed-report">
                      {parseDueDiligenceReport(analysisResult.dueDiligence).map((section, index) => (
                        <div key={index} className="report-section">
                          <h4 className="report-section-title">{section.title}</h4>
                          <div className="report-section-content">
                            {section.items.map((item, itemIndex) => (
                              <div key={itemIndex} className="report-item">
                                {typeof item === 'string' ? (
                                  <span className="report-text">{item}</span>
                                ) : (
                                  <div className="report-key-value">
                                    <span className="report-key">{item.key}:</span>
                                    <span 
                                      className="report-value"
                                      style={{
                                        color: item.key.toLowerCase().includes('risk') 
                                          ? getRiskLevelColor(item.value)
                                          : item.key.toLowerCase().includes('rating') || item.key.toLowerCase().includes('assessment')
                                          ? getRatingColor(item.value)
                                          : '#4a5568'
                                      }}
                                    >
                                      {item.value}
                                    </span>
                                  </div>
                                )}
                              </div>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <pre className="report-content">{analysisResult.dueDiligence}</pre>
                  )}
                </div>
              </div>
            )}

            <div className="action-buttons">
              <button onClick={resetForm} className="new-analysis-btn">
                <span>🔄</span>
                New Analysis
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default DocumentSummarizer;