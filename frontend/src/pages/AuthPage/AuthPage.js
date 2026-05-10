import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import ApiService from '../../services/ApiService';
import './AuthPage.css';

function AuthPage({ onLogin }) {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    walletAddress: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear specific error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (isLogin) {
      if (!formData.email.trim()) {
        newErrors.email = 'Email is required';
      } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
        newErrors.email = 'Email is invalid';
      }
      
      if (!formData.password.trim()) {
        newErrors.password = 'Password is required';
      }
    } else {
      if (!formData.username.trim()) {
        newErrors.username = 'Username is required';
      } else if (formData.username.length < 3) {
        newErrors.username = 'Username must be at least 3 characters';
      }

      if (!formData.email.trim()) {
        newErrors.email = 'Email is required';
      } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
        newErrors.email = 'Email is invalid';
      }

      if (!formData.password.trim()) {
        newErrors.password = 'Password is required';
      } else if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/.test(formData.password)) {
        newErrors.password = 'Password must be at least 8 characters long and include an uppercase letter, a lowercase letter, a number, and a special character.';
      }

      if (!formData.walletAddress.trim()) {
        newErrors.walletAddress = 'Wallet address is required';
      } else if (!/^0x[a-fA-F0-9]{40}$/.test(formData.walletAddress)) {
        newErrors.walletAddress = 'Invalid Ethereum wallet address';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setMessage('');

    try {
      let response;
      
      if (isLogin) {
        response = await ApiService.login({
          email: formData.email,
          password: formData.password
        });
      } else {
        response = await ApiService.signup({
          username: formData.username,
          email: formData.email,
          password: formData.password,
          walletAddress: formData.walletAddress
        });
      }

      // Get user data after successful auth
      const userData = await ApiService.getCurrentUser();
      onLogin(userData);

    } catch (error) {
      console.error('Auth error:', error);
      setMessage(error.message || `${isLogin ? 'Login' : 'Registration'} failed`);
    } finally {
      setLoading(false);
    }
  };

  const switchMode = () => {
    setIsLogin(!isLogin);
    setFormData({
      username: '',
      email: '',
      password: '',
      walletAddress: ''
    });
    setErrors({});
    setMessage('');
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        {/* Header */}
        <div className="auth-header">
          <Link to="/" className="auth-logo">
            <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="4" y="4" width="24" height="24" rx="4" fill="currentColor"/>
              <rect x="8" y="8" width="16" height="2" rx="1" fill="white"/>
              <rect x="8" y="12" width="16" height="2" rx="1" fill="white"/>
              <rect x="8" y="16" width="12" height="2" rx="1" fill="white"/>
              <rect x="8" y="20" width="10" height="2" rx="1" fill="white"/>
            </svg>
            <span>DocAnalyzer</span>
          </Link>
        </div>

        {/* Auth Card */}
        <div className="auth-card">
          <div className="auth-tabs">
            <button
              type="button"
              className={`auth-tab ${isLogin ? 'active' : ''}`}
              onClick={() => setIsLogin(true)}
            >
              Sign In
            </button>
            <button
              type="button"
              className={`auth-tab ${!isLogin ? 'active' : ''}`}
              onClick={() => setIsLogin(false)}
            >
              Sign Up
            </button>
          </div>

          <div className="auth-form-container">
            <div className="auth-form-header">
              <h2>{isLogin ? 'Welcome back' : 'Create your account'}</h2>
              <p>
                {isLogin 
                  ? 'Sign in to your account to continue'
                  : 'Join DocAnalyzer and start managing your documents securely'
                }
              </p>
            </div>

            {message && (
              <div className="alert alert-error">
                {message}
              </div>
            )}

            <form onSubmit={handleSubmit} className="auth-form">
              {!isLogin && (
                <div className="form-group">
                  <label htmlFor="username" className="form-label">
                    Username <span className="required-star">*</span>
                  </label>
                  <input
                    type="text"
                    id="username"
                    name="username"
                    className={`form-input ${errors.username ? 'error' : ''}`}
                    value={formData.username}
                    onChange={handleInputChange}
                    placeholder="Enter your username"
                  />
                  {errors.username && (
                    <div className="form-error">{errors.username}</div>
                  )}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="email" className="form-label">
                  Email Address <span className="required-star">*</span>
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  className={`form-input ${errors.email ? 'error' : ''}`}
                  value={formData.email}
                  onChange={handleInputChange}
                  placeholder="Enter your email"
                />
                {errors.email && (
                  <div className="form-error">{errors.email}</div>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="password" className="form-label">
                  Password <span className="required-star">*</span>
                </label>
                <input
                  type="password"
                  id="password"
                  name="password"
                  className={`form-input ${errors.password ? 'error' : ''}`}
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="Enter your password"
                />
                {errors.password && (
                  <div className="form-error">{errors.password}</div>
                )}
              </div>

              {!isLogin && (
                <div className="form-group">
                  <label htmlFor="walletAddress" className="form-label">
                    Wallet Address <span className="required-star">*</span>
                  </label>
                  <input
                    type="text"
                    id="walletAddress"
                    name="walletAddress"
                    className={`form-input ${errors.walletAddress ? 'error' : ''}`}
                    value={formData.walletAddress}
                    onChange={handleInputChange}
                    placeholder="0x..."
                  />
                  {errors.walletAddress && (
                    <div className="form-error">{errors.walletAddress}</div>
                  )}
                  <div className="form-help">
                    Enter your Ethereum wallet address for blockchain operations
                  </div>
                </div>
              )}

              <button
                type="submit"
                className="btn btn-primary w-full"
                disabled={loading}
              >
                {loading ? (
                  <span className="loading-text">
                    {isLogin ? 'Signing in...' : 'Creating account...'}
                  </span>
                ) : (
                  isLogin ? 'Sign In' : 'Create Account'
                )}
              </button>
            </form>

            <div className="auth-switch">
              <p>
                {isLogin ? "Don't have an account?" : "Already have an account?"}
                {' '}
                <button
                  type="button"
                  className="auth-switch-link"
                  onClick={switchMode}
                >
                  {isLogin ? 'Sign up' : 'Sign in'}
                </button>
              </p>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="auth-footer">
          <p>&copy; 2024 DocAnalyzer. All rights reserved.</p>
        </div>
      </div>
    </div>
  );
}

export default AuthPage;