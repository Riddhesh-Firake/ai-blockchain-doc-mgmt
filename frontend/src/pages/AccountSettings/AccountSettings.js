import React, { useState, useEffect } from 'react';
import ApiService from '../../services/ApiService';
import './AccountSettings.css';

function AccountSettings() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');

  useEffect(() => {
    loadUserData();
  }, []);

  const loadUserData = async () => {
    try {
      const userData = await ApiService.getCurrentUser();
      setUser(userData);
    } catch (error) {
      console.error('Failed to load user data:', error);
      showMessage('Failed to load user data', 'error');
    } finally {
      setLoading(false);
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

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  return (
    <div className="settings-page">
      <div className="settings-container">
        <div className="settings-header">
          <h1 className="settings-title">Account Settings</h1>
          <p className="settings-description">
            Manage your account information and preferences
          </p>
        </div>

        {message && (
          <div className={`alert alert-${messageType}`}>
            {message}
          </div>
        )}

        <div className="settings-content">
          {/* Profile Information */}
          <div className="settings-section">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Profile Information</h3>
              </div>
              <div className="card-body">
                <div className="profile-info">
                  <div className="profile-avatar">
                    <div className="avatar-circle">
                      {user?.username?.charAt(0).toUpperCase()}
                    </div>
                  </div>
                  <div className="profile-details">
                    <div className="detail-item">
                      <label className="detail-label">Username</label>
                      <div className="detail-value">{user?.username}</div>
                    </div>
                    <div className="detail-item">
                      <label className="detail-label">Email</label>
                      <div className="detail-value">{user?.email}</div>
                    </div>
                    <div className="detail-item">
                      <label className="detail-label">Wallet Address</label>
                      <div className="detail-value wallet-address">
                        {user?.walletAddress}
                        <button
                          className="copy-btn"
                          onClick={() => {
                            navigator.clipboard.writeText(user.walletAddress);
                            showMessage('Wallet address copied!', 'success');
                          }}
                          title="Copy wallet address"
                        >
                          📋
                        </button>
                      </div>
                    </div>
                    <div className="detail-item">
                      <label className="detail-label">Member Since</label>
                      <div className="detail-value">
                        {new Date(user?.createdAt).toLocaleDateString('en-US', {
                          year: 'numeric',
                          month: 'long',
                          day: 'numeric'
                        })}
                      </div>
                    </div>
                    {user?.lastLogin && (
                      <div className="detail-item">
                        <label className="detail-label">Last Login</label>
                        <div className="detail-value">
                          {new Date(user.lastLogin).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Account Security */}
          <div className="settings-section">
            <div className="card">
              <div className="card-header">
                <h3 className="card-title">Account Security</h3>
              </div>
              <div className="card-body">
                <div className="security-items">

                  <div className="security-item">
                    <div className="security-info">
                      <h4>Wallet Verification</h4>
                      <p>Your wallet is verified and secure</p>
                    </div>
                    <div className="verification-status verified">
                      ✅ Verified
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AccountSettings;