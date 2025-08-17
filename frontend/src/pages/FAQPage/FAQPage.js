import React, { useState } from 'react';
import './FAQPage.css';

function FAQPage() {
  const [openItems, setOpenItems] = useState({});

  const toggleItem = (index) => {
    setOpenItems(prev => ({
      ...prev,
      [index]: !prev[index]
    }));
  };

  const faqData = [
    {
      category: "Getting Started",
      questions: [
        {
          question: "What is DocAnalyzer?",
          answer: "DocAnalyzer is a comprehensive document management platform that combines blockchain technology, AI-powered analysis, and secure storage to help you manage your documents efficiently and securely."
        },
        {
          question: "How do I create an account?",
          answer: "Click on 'Get Started' or 'Sign Up' and provide your username, email, password, and Ethereum wallet address. Your wallet address is required for blockchain operations and document ownership verification."
        },
        {
          question: "What wallet addresses are supported?",
          answer: "Currently, we support Ethereum wallet addresses (0x format). Make sure you have control of the wallet address you provide, as it's used for document ownership and blockchain operations."
        }
      ]
    },
    {
      category: "Blockchain Storage",
      questions: [
        {
          question: "How does blockchain storage work?",
          answer: "When you upload a document, it's stored securely and a hash is recorded on the blockchain. This creates an immutable record of your document's existence and integrity, providing proof of ownership and authenticity."
        },
        {
          question: "What file types are supported?",
          answer: "We support PDF, DOC, DOCX, TXT, JPG, and PNG files up to 50MB in size. Additional file types may be added in future updates."
        },
        {
          question: "How do I share documents?",
          answer: "Go to the 'Share Document' tab, select your document, enter the recipient's wallet address, and choose the access level (read, write, or admin). The recipient will then be able to access the shared document."
        },
        {
          question: "Can I revoke document access?",
          answer: "Yes, as the document owner, you can revoke access at any time. This is recorded on the blockchain and the user will immediately lose access to your document."
        }
      ]
    },
    {
      category: "Security & Privacy",
      questions: [
        {
          question: "How secure are my documents?",
          answer: "Documents are stored with enterprise-grade encryption and blockchain verification. Your files are protected both in transit and at rest, with immutable audit trails for all activities."
        },
        {
          question: "Who can see my documents?",
          answer: "Only you and users you explicitly share with can access your documents. We use JWT authentication and wallet-based authorization to ensure proper access control."
        },
        {
          question: "What is document verification?",
          answer: "Document verification checks the integrity of your file against the blockchain hash. This ensures your document hasn't been tampered with or corrupted since upload."
        }
      ]
    },
    {
      category: "Features",
      questions: [
        {
          question: "What is the audit trail?",
          answer: "The audit trail shows a complete history of all actions performed on a document - uploads, updates, shares, access grants/revokes, and downloads. All activities are recorded with timestamps and transaction hashes."
        },
        {
          question: "When will AI features be available?",
          answer: "AI Document Summarizer and AI Consultation features are currently in development and will be available in upcoming releases. Stay tuned for updates!"
        },
        {
          question: "What about IPFS integration?",
          answer: "IPFS (InterPlanetary File System) integration is optional. If you have already uploaded your document to IPFS, you can provide the hash during upload for additional decentralized storage."
        }
      ]
    },
    {
      category: "Troubleshooting",
      questions: [
        {
          question: "I can't upload my document. What's wrong?",
          answer: "Check that your file is under 50MB and in a supported format (PDF, DOC, DOCX, TXT, JPG, PNG). Also ensure you're logged in and have a valid wallet address associated with your account."
        },
        {
          question: "I forgot my password. How do I reset it?",
          answer: "Password reset functionality is currently being developed. For now, please contact support if you need assistance with your account."
        },
        {
          question: "Why can't I access a shared document?",
          answer: "Ensure the document owner has shared it with your exact wallet address. Access permissions are wallet-specific and case-sensitive."
        }
      ]
    }
  ];

  return (
    <div className="faq-page">
      <div className="faq-container">
        <div className="faq-header">
          <h1 className="faq-title">Frequently Asked Questions</h1>
          <p className="faq-description">
            Find answers to common questions about DocAnalyzer and its features
          </p>
        </div>

        <div className="faq-content">
          {faqData.map((category, categoryIndex) => (
            <div key={categoryIndex} className="faq-category">
              <h2 className="category-title">{category.category}</h2>
              <div className="faq-list">
                {category.questions.map((item, questionIndex) => {
                  const itemKey = `${categoryIndex}-${questionIndex}`;
                  const isOpen = openItems[itemKey];

                  return (
                    <div key={questionIndex} className="faq-item">
                      <button
                        className={`faq-question ${isOpen ? 'open' : ''}`}
                        onClick={() => toggleItem(itemKey)}
                      >
                        <span className="question-text">{item.question}</span>
                        <span className="question-icon">
                          {isOpen ? '−' : '+'}
                        </span>
                      </button>
                      {isOpen && (
                        <div className="faq-answer">
                          <p>{item.answer}</p>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        {/* Contact Section */}
        <div className="faq-contact">
          <div className="contact-card">
            <h3>Still have questions?</h3>
            <p>
              Can't find the answer you're looking for? We're here to help you get the most out of DocAnalyzer.
            </p>
            <div className="contact-actions">
              <a href="mailto:pkmehta1011@gmail.com" className="btn btn-primary">
                Contact Support
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default FAQPage;