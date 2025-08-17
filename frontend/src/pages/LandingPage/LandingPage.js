import React from 'react';
import { Link } from 'react-router-dom';
import './LandingPage.css';

function LandingPage() {
  const features = [
    {
      title: 'Blockchain Storage',
      description: 'Secure document storage with blockchain verification, sharing capabilities, and complete audit trails.',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M4 6h16v2H4zm0 5h16v2H4zm0 5h16v2H4z"/>
        </svg>
      ),
      color: 'var(--primary-color)'
    },
    {
      title: 'AI Document Summarizer',
      description: 'Intelligent document analysis and summarization using advanced AI to extract key insights.',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6z"/>
          <polyline points="14,2 14,8 20,8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
          <line x1="16" y1="17" x2="8" y2="17"/>
          <polyline points="10,9 9,9 8,9"/>
        </svg>
      ),
      color: 'var(--success-color)'
    },
    {
      title: 'AI Consultation',
      description: 'Expert AI consultation for document analysis, legal advice, and business insights.',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v10z"/>
        </svg>
      ),
      color: 'var(--accent-color)'
    },
    {
      title: 'Real-time News',
      description: 'Stay updated with the latest industry news, regulations, and blockchain developments.',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"/>
          <polyline points="22,6 12,13 2,6"/>
        </svg>
      ),
      color: 'var(--warning-color)'
    },
    {
      title: 'Secure Document Sharing',
      description: 'Share documents securely with granular permissions and time-based access controls.',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/>
          <polyline points="16,6 12,2 8,6"/>
          <line x1="12" y1="2" x2="12" y2="15"/>
          <circle cx="18" cy="8" r="3"/>
        </svg>
      ),
      color: '#8b5cf6'
    },
    {
      title: 'MetaMask Integration',
      description: 'Simplified wallet connection with MetaMask and Ganache for seamless blockchain interactions.',
      icon: (
        <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
          <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
          <polyline points="3.27,6.96 12,12.01 20.73,6.96"/>
          <line x1="12" y1="22.08" x2="12" y2="12"/>
        </svg>
      ),
      color: '#f59e0b'
    }
  ];

  return (
    <div className="landing-page">
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-container">
          <div className="hero-content">
            <h1 className="hero-title">
              Secure Document Management
              <span className="highlight"> with Blockchain</span>
            </h1>
            <p className="hero-description">
              Upload, analyze, and manage your documents with enterprise-grade security, 
              AI-powered insights, and blockchain verification. Join the future of document management.
            </p>
            <div className="hero-actions">
              <Link to="/auth" className="btn btn-primary btn-lg">
                Get Started
              </Link>
              <a href="#features" className="btn btn-secondary btn-lg">
                Learn More
              </a>
            </div>
          </div>
          <div className="hero-visual">
            <div className="hero-graphic">
              <div className="floating-card card-1">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6z"/>
                </svg>
                <span>Secure</span>
              </div>
              <div className="floating-card card-2">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 1l3 6 6 3-6 3-3 6-3-6-6-3 6-3z"/>
                </svg>
                <span>AI-Powered</span>
              </div>
              <div className="floating-card card-3">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M4 4h16v16H4z"/>
                </svg>
                <span>Verified</span>
              </div>
              <div className="floating-card card-4">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/>
                  <polyline points="16,6 12,2 8,6"/>
                  <line x1="12" y1="2" x2="12" y2="15"/>
                  <circle cx="18" cy="8" r="3"/>
                </svg>
                <span>Share</span>
              </div>
              <div className="floating-card card-5">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <rect x="3" y="11" width="18" height="10" rx="2" ry="2"/>
                  <circle cx="12" cy="16" r="1"/>
                  <path d="M7 11V7a5 5 0 0 1 9.9-1"/>
                  <line x1="17" y1="4" x2="17" y2="10"/>
                  <line x1="14" y1="7" x2="20" y2="7"/>
                </svg>
                <span>Revoke</span>
              </div>
              <div className="floating-card card-6">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"/>
                  <polyline points="3.27,6.96 12,12.01 20.73,6.96"/>
                  <line x1="12" y1="22.08" x2="12" y2="12"/>
                </svg>
                <span>MetaMask</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="features">
        <div className="features-container">
          <div className="section-header">
            <h2 className="section-title">Everything you need to manage documents</h2>
            <p className="section-description">
              Our comprehensive platform provides all the tools you need for secure, 
              intelligent document management with blockchain technology.
            </p>
          </div>

          <div className="features-grid">
            {features.map((feature, index) => (
              <div key={index} className="feature-card">
                <div className="feature-icon" style={{ color: feature.color }}>
                  {feature.icon}
                </div>
                <h3 className="feature-title">{feature.title}</h3>
                <p className="feature-description">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="benefits">
        <div className="benefits-container">
          <div className="benefits-content">
            <h2 className="benefits-title">Why choose DocAnalyzer?</h2>
            <div className="benefits-list">
              <div className="benefit-item">
                <div className="benefit-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/>
                  </svg>
                </div>
                <div className="benefit-content">
                  <h4>Enterprise Security</h4>
                  <p>Bank-grade encryption with blockchain verification ensures your documents are always secure and tamper-proof.</p>
                </div>
              </div>
              
              <div className="benefit-item">
                <div className="benefit-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M4 12v8a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-8"/>
                    <polyline points="16,6 12,2 8,6"/>
                    <line x1="12" y1="2" x2="12" y2="15"/>
                    <circle cx="18" cy="8" r="3"/>
                  </svg>
                </div>
                <div className="benefit-content">
                  <h4>Smart Sharing & Access Control</h4>
                  <p>Share documents securely with granular permissions and instantly revoke access when needed.</p>
                </div>
              </div>
              
              <div className="benefit-item">
                <div className="benefit-icon">
                  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
                  </svg>
                </div>
                <div className="benefit-content">
                  <h4>AI Intelligence & MetaMask Integration</h4>
                  <p>Advanced AI algorithms provide intelligent document analysis with seamless blockchain wallet integration.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta">
        <div className="cta-container">
          <div className="cta-content">
            <h2 className="cta-title">Ready to get started?</h2>
            <p className="cta-description">
              Join thousands of users who trust DocAnalyzer for their secure document management needs.
            </p>
            <Link to="/auth" className="btn btn-primary btn-lg">
              Start Free Today
            </Link>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="footer-container">
          <div className="footer-content">
            <div className="footer-brand">
              <div className="footer-logo">
                <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <rect x="4" y="4" width="24" height="24" rx="4" fill="currentColor"/>
                  <rect x="8" y="8" width="16" height="2" rx="1" fill="white"/>
                  <rect x="8" y="12" width="16" height="2" rx="1" fill="white"/>
                  <rect x="8" y="16" width="12" height="2" rx="1" fill="white"/>
                  <rect x="8" y="20" width="10" height="2" rx="1" fill="white"/>
                </svg>
                <span>DocAnalyzer</span>
              </div>
              <p className="footer-tagline">
                Secure, intelligent document management for the modern world.
              </p>
            </div>
          </div>
          <div className="footer-bottom">
            <p>&copy; 2024 DocAnalyzer. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}

export default LandingPage;