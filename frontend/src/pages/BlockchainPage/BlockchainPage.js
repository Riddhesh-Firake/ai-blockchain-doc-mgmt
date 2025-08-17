import React, { useState, useEffect } from 'react';
import ApiService from '../../services/ApiService.js';
import Web3Service from '../../services/Web3Service.js';
import './BlockchainPage.css';

function BlockchainPage() {
  const [activeTab, setActiveTab] = useState('upload');
  const [documents, setDocuments] = useState([]);
  const [sharedDocuments, setSharedDocuments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [documentShares, setDocumentShares] = useState({});
  const [walletConnected, setWalletConnected] = useState(false);
  const [walletAddress, setWalletAddress] = useState('');
  const [transactionInProgress, setTransactionInProgress] = useState(false);

  useEffect(() => {
    checkWalletConnection();
  }, []);

  const checkWalletConnection = async () => {
    try {
      const currentAccount = Web3Service.getCurrentAccount();
      if (currentAccount) {
        setWalletAddress(currentAccount);
        setWalletConnected(true);
      }
    } catch (error) {
      console.error('Failed to check wallet connection:', error);
    }
  };

  const connectWallet = async () => {
    try {
      setLoading(true);
      const result = await ApiService.connectWallet();

      if (result.walletAddress) {
        setWalletAddress(result.walletAddress);
        setWalletConnected(true);
        showMessage('Wallet connected successfully', 'success');
      }
    } catch (error) {
      console.error('Wallet connection failed:', error);
      showMessage(error.message || 'Failed to connect wallet', 'error');
    } finally {
      setLoading(false);
    }
  };

  // Upload form state
  const [uploadForm, setUploadForm] = useState({
    file: null,
    domain: 'general',
    ipfsHash: ''
  });

  // Update form state
  const [updateForm, setUpdateForm] = useState({
    documentId: '',
    file: null,
    updateReason: ''
  });

  // Share form state
  const [shareForm, setShareForm] = useState({
    documentId: '',
    recipientAddress: '',
    accessLevel: 'read'
  });

  // Selected document for actions
  const [selectedDocument, setSelectedDocument] = useState(null);
  const [auditTrail, setAuditTrail] = useState([]);
  const [showAuditModal, setShowAuditModal] = useState(false);

  useEffect(() => {
    loadDocuments();
  }, []);

  const loadDocuments = async () => {
    try {
      const [myDocsResponse, sharedDocsResponse] = await Promise.all([
        ApiService.getMyDocuments(),
        ApiService.getSharedDocuments()
      ]);

      if (myDocsResponse.success) {
        setDocuments(myDocsResponse.data || []);
      }

      if (sharedDocsResponse.success) {
        setSharedDocuments(sharedDocsResponse.data || []);
      }
    } catch (error) {
      console.error('Failed to load documents:', error);
      showMessage('Failed to load documents', 'error');
    }
  };

  const showMessage = (msg, type = 'info') => {
    setMessage(msg);
    setMessageType(type);
    setTimeout(() => {
      setMessage('');
      setMessageType('');
    }, 5000);
  };

  const handleUpload = async (e) => {
    e.preventDefault();

    if (!uploadForm.file) {
      showMessage('Please select a file', 'error');
      return;
    }

    if (!walletConnected) {
      showMessage('Please connect your wallet first', 'error');
      return;
    }

    setLoading(true);
    setTransactionInProgress(true);

    try {
      showMessage('Processing upload... Please confirm the transaction in MetaMask', 'info');

      const response = await ApiService.uploadDocument(
        uploadForm.file,
        uploadForm.domain,
        uploadForm.ipfsHash
      );

      if (response.success) {
        showMessage('Document uploaded and blockchain transaction confirmed!', 'success');
        setUploadForm({ file: null, domain: 'general', ipfsHash: '' });
        loadDocuments();
      } else {
        showMessage(response.message || 'Upload failed', 'error');
      }
    } catch (error) {
      console.error('Upload failed:', error);
      if (error.message.includes('User denied transaction signature')) {
        showMessage('Transaction was cancelled by user', 'error');
      } else if (error.message.includes('insufficient funds')) {
        showMessage('Insufficient ETH for gas fees', 'error');
      } else {
        showMessage(error.message || 'Upload failed', 'error');
      }
    } finally {
      setLoading(false);
      setTransactionInProgress(false);
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();

    if (!updateForm.documentId || !updateForm.file) {
      showMessage('Please select document and file', 'error');
      return;
    }

    if (!walletConnected) {
      showMessage('Please connect your wallet first', 'error');
      return;
    }

    setLoading(true);
    setTransactionInProgress(true);

    try {
      showMessage('Processing update... Please confirm the transaction in MetaMask', 'info');

      const response = await ApiService.updateDocument(
        updateForm.documentId,
        updateForm.file,
        updateForm.updateReason
      );

      if (response.success) {
        showMessage('Document updated and blockchain transaction confirmed!', 'success');
        setUpdateForm({ documentId: '', file: null, updateReason: '' });
        loadDocuments();
      } else {
        showMessage(response.message || 'Update failed', 'error');
      }
    } catch (error) {
      console.error('Update failed:', error);
      if (error.message.includes('User denied transaction signature')) {
        showMessage('Transaction was cancelled by user', 'error');
      } else {
        showMessage(error.message || 'Update failed', 'error');
      }
    } finally {
      setLoading(false);
      setTransactionInProgress(false);
    }
  };

  const handleShare = async (e) => {
    e.preventDefault();

    if (!shareForm.documentId || !shareForm.recipientAddress) {
      showMessage('Please fill all required fields', 'error');
      return;
    }

    if (!walletConnected) {
      showMessage('Please connect your wallet first', 'error');
      return;
    }

    setLoading(true);
    setTransactionInProgress(true);

    try {
      showMessage('Processing share... Please confirm the transaction in MetaMask', 'info');

      const response = await ApiService.shareDocument(shareForm.documentId, {
        ownerAddress: walletAddress,
        recipientAddress: shareForm.recipientAddress,
        accessLevel: shareForm.accessLevel
      });

      if (response.success) {
        showMessage('Document shared and blockchain transaction confirmed!', 'success');
        setShareForm({ documentId: '', recipientAddress: '', accessLevel: 'read' });
      } else {
        showMessage(response.message || 'Share failed', 'error');
      }
    } catch (error) {
      console.error('Share failed:', error);
      if (error.message.includes('User denied transaction signature')) {
        showMessage('Transaction was cancelled by user', 'error');
      } else {
        showMessage(error.message || 'Share failed', 'error');
      }
    } finally {
      setLoading(false);
      setTransactionInProgress(false);
    }
  };

  const handleVerify = async (documentId) => {
    setLoading(true);
    try {
      const response = await ApiService.verifyDocument(documentId);

      if (response.success) {
        showMessage(
          response.data ? 'Document integrity verified ✓' : 'Document integrity check failed ✗',
          response.data ? 'success' : 'error'
        );
      } else {
        showMessage(response.message || 'Verification failed', 'error');
      }
    } catch (error) {
      console.error('Verification failed:', error);
      showMessage(error.message || 'Verification failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (documentId, fileName) => {
    try {
      const blob = await ApiService.downloadDocument(documentId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.style.display = 'none';
      a.href = url;
      a.download = fileName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      showMessage('Download started', 'success');
    } catch (error) {
      console.error('Download failed:', error);
      showMessage(error.message || 'Download failed', 'error');
    }
  };

  const loadSharedUsers = async (documentId) => {
    try {
      const response = await ApiService.getDocumentAuditTrail(documentId);
      if (response.success) {
        const auditRecords = response.data || [];

        // Sort by timestamp so later actions overwrite earlier ones
        auditRecords.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

        const accessMap = {};

        for (const record of auditRecords) {
          const action = record.action?.toLowerCase();
          let address = record.actor;
          let accessLevel = 'read';

          if (typeof record.details === 'object' && record.details !== null) {
            address = record.details.recipientAddress || address;
            accessLevel = record.details.accessLevel || accessLevel;
          } else if (typeof record.details === 'string') {
            const match = record.details.match(/0x[a-fA-F0-9]{40}/);
            if (match) address = match[0];
            const accessMatch = record.details.match(/\(Access:\s*(\w+)\)/i);
            if (accessMatch) accessLevel = accessMatch[1];
          }

          if (!address) continue;

          if (['shared', 'document_shared', 'share'].includes(action)) {
            accessMap[address] = { address, timestamp: record.timestamp, accessLevel };
          } else if (['revoke', 'access_revoked', 'revoked'].includes(action)) {
            delete accessMap[address]; // Remove from active shares
          }
        }

        setDocumentShares(prev => ({
          ...prev,
          [documentId]: Object.values(accessMap)
        }));
      }
    } catch (error) {
      console.error('Failed to load shared users:', error);
    }
  };


  const handleRevoke = async (documentId, userAddress) => {
    if (!window.confirm('Are you sure you want to revoke access? This will require a blockchain transaction.')) {
      return;
    }

    if (!walletConnected) {
      showMessage('Please connect your wallet first', 'error');
      return;
    }

    setLoading(true);
    setTransactionInProgress(true);

    try {
      showMessage('Processing revocation... Please confirm the transaction in MetaMask', 'info');

      const response = await ApiService.revokeAccess(documentId, userAddress);

      if (response.success) {
        showMessage('Access revoked and blockchain transaction confirmed!', 'success');
        // Refresh the shared users list
        loadSharedUsers(documentId);
      } else {
        showMessage(response.message || 'Revoke failed', 'error');
      }
    } catch (error) {
      console.error('Revoke failed:', error);
      if (error.message.includes('User denied transaction signature')) {
        showMessage('Transaction was cancelled by user', 'error');
      } else {
        showMessage(error.message || 'Revoke failed', 'error');
      }
    } finally {
      setLoading(false);
      setTransactionInProgress(false);
    }
  };

  const showAuditTrail = async (documentId) => {
    setLoading(true);
    try {
      const response = await ApiService.getDocumentAuditTrail(documentId);

      if (response.success) {
        setAuditTrail(response.data || []);
        setShowAuditModal(true);
      } else {
        showMessage(response.message || 'Failed to load audit trail', 'error');
      }
    } catch (error) {
      console.error('Failed to load audit trail:', error);
      showMessage(error.message || 'Failed to load audit trail', 'error');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const tabs = [
    { id: 'upload', label: 'Upload Document', icon: '📤' },
    { id: 'update', label: 'Update Document', icon: '🔄' },
    { id: 'share', label: 'Share Document', icon: '🔗' },
    { id: 'documents', label: 'My Documents', icon: '📁' },
    { id: 'shared', label: 'Shared with Me', icon: '👥' },
    { id: 'manage', label: 'Manage Access', icon: '⚙️' }
  ];

  return (
    <div className="blockchain-page">
      <div className="page-header">
        <h1 className="page-title">Blockchain Document Storage</h1>
        <p className="page-description">
          Securely store, share, and manage your documents with blockchain verification
        </p>
      </div>

      {/* Wallet Connection Section */}
      <div className="wallet-section">
        {!walletConnected ? (
          <div className="wallet-connect">
            <p className="wallet-status">Connect your MetaMask wallet to get started</p>
            <button
              className="btn btn-primary"
              onClick={connectWallet}
              disabled={loading}
            >
              {loading ? 'Connecting...' : 'Connect Wallet'}
            </button>
          </div>
        ) : (
          <div className="wallet-connected">
            <div className="wallet-info">
              <span className="wallet-status">✅ Wallet Connected</span>
              <span className="wallet-address">
                {walletAddress}
              </span>
            </div>
          </div>
        )}
      </div>

      {transactionInProgress && (
        <div className="transaction-progress">
          <div className="progress-indicator">
            <span className="spinner">⏳</span>
            <span>Blockchain transaction in progress... Please check MetaMask</span>
          </div>
        </div>
      )}

      {/* Message Alert */}
      {message && (
        <div className={`alert alert-${messageType}`}>
          {message}
        </div>
      )}

      {/* Tab Navigation */}
      <div className="tab-navigation">
        {tabs.map(tab => (
          <button
            key={tab.id}
            className={`tab-button ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <span className="tab-icon">{tab.icon}</span>
            <span className="tab-label">{tab.label}</span>
          </button>
        ))}
      </div>

      {/* Tab Content */}
      <div className="tab-content">
        {/* Upload Tab */}
        {activeTab === 'upload' && (
          <div className="tab-panel">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Upload New Document</h3>
              </div>
              <div className="card-body">
                <form onSubmit={handleUpload} className="form">
                  <div className="form-group">
                    <label htmlFor="file" className="form-label">
                      Select File
                    </label>
                    <input
                      type="file"
                      id="file"
                      className="form-input"
                      onChange={(e) => setUploadForm(prev => ({ ...prev, file: e.target.files[0] }))}
                      accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png"
                    />
                    <div className="form-help">
                      Supported formats: PDF, DOC, DOCX, TXT, JPG, PNG (Max: 50MB)
                    </div>
                  </div>

                  <div className="form-group">
                    <label htmlFor="domain" className="form-label">
                      Domain
                    </label>
                    <select
                      id="domain"
                      className="form-select"
                      value={uploadForm.domain}
                      onChange={(e) => setUploadForm(prev => ({ ...prev, domain: e.target.value }))}
                    >
                      <option value="general">General</option>
                      <option value="finance">Finance</option>
                      <option value="healthcare">Healthcare</option>
                      <option value="legal">Legal</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="ipfsHash" className="form-label">
                      IPFS Hash (Optional)
                    </label>
                    <input
                      type="text"
                      id="ipfsHash"
                      className="form-input"
                      value={uploadForm.ipfsHash}
                      onChange={(e) => setUploadForm(prev => ({ ...prev, ipfsHash: e.target.value }))}
                      placeholder="QmX..."
                    />
                    <div className="form-help">
                      If you have already uploaded to IPFS, enter the hash here
                    </div>
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading || !uploadForm.file}
                  >
                    {loading ? 'Uploading...' : 'Upload Document'}
                  </button>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* Update Tab */}
        {activeTab === 'update' && (
          <div className="tab-panel">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Update Existing Document</h3>
              </div>
              <div className="card-body">
                <form onSubmit={handleUpdate} className="form">
                  <div className="form-group">
                    <label htmlFor="documentId" className="form-label">
                      Select Document
                    </label>
                    <select
                      id="documentId"
                      className="form-select"
                      value={updateForm.documentId}
                      onChange={(e) => setUpdateForm(prev => ({ ...prev, documentId: e.target.value }))}
                    >
                      <option value="">Choose document to update...</option>
                      {documents.map(doc => (
                        <option key={doc.documentId} value={doc.documentId}>
                          {doc.fileName}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="updateFile" className="form-label">
                      New File Version
                    </label>
                    <input
                      type="file"
                      id="updateFile"
                      className="form-input"
                      onChange={(e) => setUpdateForm(prev => ({ ...prev, file: e.target.files[0] }))}
                      accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="updateReason" className="form-label">
                      Update Reason (Optional)
                    </label>
                    <textarea
                      id="updateReason"
                      className="form-textarea"
                      value={updateForm.updateReason}
                      onChange={(e) => setUpdateForm(prev => ({ ...prev, updateReason: e.target.value }))}
                      placeholder="Brief description of changes..."
                    />
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading || !updateForm.documentId || !updateForm.file}
                  >
                    {loading ? 'Updating...' : 'Update Document'}
                  </button>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* Share Tab */}
        {activeTab === 'share' && (
          <div className="tab-panel">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Share Document</h3>
              </div>
              <div className="card-body">
                <form onSubmit={handleShare} className="form">
                  <div className="form-group">
                    <label htmlFor="shareDocumentId" className="form-label">
                      Select Document
                    </label>
                    <select
                      id="shareDocumentId"
                      className="form-select"
                      value={shareForm.documentId}
                      onChange={(e) => setShareForm(prev => ({ ...prev, documentId: e.target.value }))}
                    >
                      <option value="">Choose document to share...</option>
                      {documents.map(doc => (
                        <option key={doc.documentId} value={doc.documentId}>
                          {doc.fileName}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="form-group">
                    <label htmlFor="recipientAddress" className="form-label">
                      Recipient Wallet Address
                    </label>
                    <input
                      type="text"
                      id="recipientAddress"
                      className="form-input"
                      value={shareForm.recipientAddress}
                      onChange={(e) => setShareForm(prev => ({ ...prev, recipientAddress: e.target.value }))}
                      placeholder="0x..."
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="accessLevel" className="form-label">
                      Access Level
                    </label>
                    <select
                      id="accessLevel"
                      className="form-select"
                      value={shareForm.accessLevel}
                      onChange={(e) => setShareForm(prev => ({ ...prev, accessLevel: e.target.value }))}
                    >
                      <option value="read">Read Only</option>
                      <option value="write">Read & Write</option>
                      <option value="admin">Admin</option>
                    </select>
                  </div>

                  <button
                    type="submit"
                    className="btn btn-primary"
                    disabled={loading || !shareForm.documentId || !shareForm.recipientAddress}
                  >
                    {loading ? 'Sharing...' : 'Share Document'}
                  </button>
                </form>
              </div>
            </div>
          </div>
        )}

        {/* My Documents Tab */}
        {activeTab === 'documents' && (
          <div className="tab-panel">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">My Documents ({documents.length})</h3>
              </div>
              <div className="card-body">
                {documents.length === 0 ? (
                  <div className="empty-state">
                    <div className="empty-icon">📄</div>
                    <h4>No documents yet</h4>
                    <p>Upload your first document to get started</p>
                    <button
                      className="btn btn-primary"
                      onClick={() => setActiveTab('upload')}
                    >
                      Upload Document
                    </button>
                  </div>
                ) : (
                  <div className="documents-grid">
                    {documents.map(doc => (
                      <div key={doc.documentId} className="document-card">
                        <div className="document-header">
                          <div className="document-icon">📄</div>
                          <div className="document-info">
                            <h4 className="document-title">{doc.fileName}</h4>
                            <p className="document-meta">
                              {doc.domain} • {formatDate(doc.createdAt)}
                            </p>
                          </div>
                        </div>

                        <div className="document-actions">
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => handleDownload(doc.documentId, doc.fileName)}
                            title="Download"
                          >
                            📥
                          </button>
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => handleVerify(doc.documentId)}
                            title="Verify Integrity"
                          >
                            ✅
                          </button>
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => showAuditTrail(doc.documentId)}
                            title="Audit Trail"
                          >
                            📋
                          </button>
                          <button
                            className="btn btn-sm btn-info"
                            onClick={() => loadSharedUsers(doc.documentId)}
                            title="Manage Access"
                          >
                            👥
                          </button>
                        </div>

                        {/* Show shared users if loaded */}
                        {documentShares[doc.documentId] && (
                          <div className="shared-users-section">
                            <h5>Shared With:</h5>
                            {documentShares[doc.documentId].length === 0 ? (
                              <p className="no-shares">Not shared with anyone</p>
                            ) : (
                              <div className="shared-users-list">
                                {documentShares[doc.documentId].map((share, index) => (
                                  <div key={index} className="shared-user-item">
                                    <span className="user-address">
                                      {share.address.slice(0, 6)}...{share.address.slice(-4)}
                                    </span>
                                    <span className="access-level">({share.accessLevel})</span>
                                    <button
                                      className="btn btn-xs btn-danger revoke-btn"
                                      onClick={() => handleRevoke(doc.documentId, share.address)}
                                      title="Revoke Access"
                                    >
                                      🚫
                                    </button>
                                  </div>
                                ))}
                              </div>
                            )}
                          </div>
                        )}

                        <div className="document-details">
                          <div className="detail-item">
                            <span className="detail-label">Hash:</span>
                            <span className="detail-value">{doc.fileHash?.slice(0, 16) || 'Unknown'}...</span>
                          </div>
              
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Shared Documents Tab */}
        {activeTab === 'shared' && (
          <div className="tab-panel">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Shared with Me ({sharedDocuments.length})</h3>
              </div>
              <div className="card-body">
                {sharedDocuments.length === 0 ? (
                  <div className="empty-state">
                    <div className="empty-icon">👥</div>
                    <h4>No shared documents</h4>
                    <p>Documents shared with you will appear here</p>
                  </div>
                ) : (
                  <div className="documents-grid">
                    {sharedDocuments.map(doc => (
                      <div key={doc.documentId} className="document-card shared">
                        <div className="document-header">
                          <div className="document-icon">📄</div>
                          <div className="document-info">
                            <h4 className="document-title">{doc.fileName}</h4>
                            <p className="document-meta">
                              {doc.domain} • Shared by {doc.ownerAddress.slice(0, 6)}...{doc.ownerAddress.slice(-4)}
                            </p>
                          </div>
                        </div>

                        <div className="document-actions">
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => handleDownload(doc.documentId, doc.fileName)}
                            title="Download"
                          >
                            📥
                          </button>
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => handleVerify(doc.documentId)}
                            title="Verify Integrity"
                          >
                            ✅
                          </button>
                          <button
                            className="btn btn-sm btn-secondary"
                            onClick={() => showAuditTrail(doc.documentId)}
                            title="Audit Trail"
                          >
                            📋
                          </button>
                        </div>

                        <div className="document-details">
                          <div className="detail-item">
                            <span className="detail-label">Hash:</span>
                            <span className="detail-value">{doc.fileHash.slice(0, 16)}...</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>
        )}

        {/* Manage Access Tab */}
        {activeTab === 'manage' && (
          <div className="tab-panel">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Manage Document Access</h3>
              </div>
              <div className="card-body">
                <div className="form-group">
                  <label htmlFor="manageDocumentId" className="form-label">
                    Select Document
                  </label>
                  <select
                    id="manageDocumentId"
                    className="form-select"
                    onChange={(e) => setSelectedDocument(e.target.value)}
                  >
                    <option value="">Choose document to manage...</option>
                    {documents.map(doc => (
                      <option key={doc.documentId} value={doc.documentId}>
                        {doc.fileName}
                      </option>
                    ))}
                  </select>
                </div>

                {selectedDocument && (
                  <div className="revoke-section">
                    <h4>Revoke Access</h4>
                    <div className="form-group">
                      <label htmlFor="revokeAddress" className="form-label">
                        User Address to Revoke
                      </label>
                      <input
                        type="text"
                        id="revokeAddress"
                        className="form-input"
                        placeholder="0x..."
                      />
                      <button
                        className="btn btn-danger"
                        onClick={() => {
                          const address = document.getElementById('revokeAddress').value;
                          if (address) {
                            handleRevoke(selectedDocument, address);
                          }
                        }}
                      >
                        Revoke Access
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Audit Trail Modal */}
      {showAuditModal && (
        <div className="modal-overlay" onClick={() => setShowAuditModal(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Audit Trail</h3>
              <button
                className="modal-close"
                onClick={() => setShowAuditModal(false)}
              >
                ✕
              </button>
            </div>
            <div className="modal-body">
              {auditTrail.length === 0 ? (
                <p>No audit records found.</p>
              ) : (
                <div className="audit-list">
                  {auditTrail.map((record, index) => (
                    <div key={index} className="audit-item">
                      <div className="audit-action">{record.action}</div>
                      <div className="audit-details">
                        <div>By: {record.actor?.slice(0, 6) || 'Unknown'}...{record.actor?.slice(-4) || ''}</div>
                        <div>Time: {record.timestamp ? formatDate(record.timestamp) : 'Unknown'}</div>
                        {record.details && <div>Details: {record.details}</div>}
                    
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default BlockchainPage;