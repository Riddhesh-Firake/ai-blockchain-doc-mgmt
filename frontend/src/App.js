import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import ApiService from './services/ApiService';
import AuthService from './services/AuthService';
import Navbar from './components/Navbar/Navbar';
import Sidebar from './components/Sidebar/Sidebar';
import ChatBot from './components/ChatBot/ChatBot.js'
import LandingPage from './pages/LandingPage/LandingPage.js';
import AuthPage from './pages/AuthPage/AuthPage';
import BlockchainPage from './pages/BlockchainPage/BlockchainPage.js';
import DocumentSummarizer from './pages/DocumentSummarizer/DocumentSummarizer.js';
import ConsultPage from './pages/ConsultPage/ConsultPage.js';
import NewsPage from './pages/NewsPage/NewsPage.js';
import AccountSettings from './pages/AccountSettings/AccountSettings.js';
import FAQPage from './pages/FAQPage/FAQPage.js';

function App() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = async () => {
      try {
        const token = AuthService.getToken();
        if (token) {
          const userData = await ApiService.getCurrentUser();
          setUser(userData);
        }
      } catch (error) {
        console.error('Auth initialization failed:', error);
        AuthService.logout();
      } finally {
        setLoading(false);
      }
    };

    initAuth();
  }, []);

  const handleLogin = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    AuthService.logout();
    setUser(null);
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner">Loading...</div>
      </div>
    );
  }

  return (
    <Router>
      <div className="app">
        {user && <Navbar user={user} onLogout={handleLogout} />}
        <div className={`app-content ${user ? 'with-sidebar' : ''}`}>
          {user && <Sidebar />}
          <div className={`main-content ${!user ? 'no-sidebar no-navbar' : 'with-navbar'}`}>
            <Routes>
              <Route 
                path="/" 
                element={<LandingPage />} 
              />
              <Route 
                path="/auth" 
                element={user ? <Navigate to="/blockchain" /> : <AuthPage onLogin={handleLogin} />} 
              />
              <Route 
                path="/blockchain" 
                element={user ? <BlockchainPage /> : <Navigate to="/auth" />} 
              />
              <Route 
                path="/summarizer" 
                element={user ? <DocumentSummarizer /> : <Navigate to="/auth" />} 
              />
              <Route 
                path="/consult" 
                element={user ? <ConsultPage /> : <Navigate to="/auth" />} 
              />
              <Route 
                path="/news" 
                element={user ? <NewsPage /> : <Navigate to="/auth" />} 
              />
              <Route 
                path="/settings" 
                element={user ? <AccountSettings /> : <Navigate to="/auth" />} 
              />
              <Route 
                path="/faq" 
                element={user ? <FAQPage /> : <Navigate to="/auth" />} 
              />
            </Routes>
          </div>
        </div>
        {/* Add the global ChatBot - only show when user is logged in */}
        {user && <ChatBot />}
      </div>
    </Router>
  );
}

export default App;