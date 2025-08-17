import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './Navbar.css';

function Navbar({ user, onLogout }) {
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const toggleDropdown = () => {
    setDropdownOpen(!dropdownOpen);
  };

  const handleLogout = () => {
    onLogout();
    setDropdownOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <Link to="/" className="navbar-logo">
            <div className="logo-icon">
              <svg width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="4" y="4" width="24" height="24" rx="4" fill="currentColor"/>
                <rect x="8" y="8" width="16" height="2" rx="1" fill="white"/>
                <rect x="8" y="12" width="16" height="2" rx="1" fill="white"/>
                <rect x="8" y="16" width="12" height="2" rx="1" fill="white"/>
                <rect x="8" y="20" width="10" height="2" rx="1" fill="white"/>
              </svg>
            </div>
            <span className="navbar-title">DocAnalyzer</span>
          </Link>
        </div>

        <div className="navbar-user">
          <div className="user-dropdown">
            <button 
              className="user-button"
              onClick={toggleDropdown}
            >
              <div className="user-avatar">
                {user.username.charAt(0).toUpperCase()}
              </div>
              <div className="user-info">
                <span className="user-name">{user.username}</span>
                <span className="user-wallet">
                  {user.walletAddress.slice(0, 10)}...{user.walletAddress.slice(-10)}
                </span>
              </div>
              <svg 
                className={`dropdown-icon ${dropdownOpen ? 'open' : ''}`}
                width="20" 
                height="20" 
                viewBox="0 0 20 20" 
                fill="currentColor"
              >
                <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
              </svg>
            </button>

            {dropdownOpen && (
              <div className="dropdown-menu">
                <Link 
                  to="/settings" 
                  className="dropdown-item"
                  onClick={() => setDropdownOpen(false)}
                >
                  <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M11.49 3.17c-.38-1.56-2.6-1.56-2.98 0a1.532 1.532 0 01-2.286.948c-1.372-.836-2.942.734-2.106 2.106.54.886.061 2.042-.947 2.287-1.561.379-1.561 2.6 0 2.978a1.532 1.532 0 01.947 2.287c-.836 1.372.734 2.942 2.106 2.106a1.532 1.532 0 012.287.947c.379 1.561 2.6 1.561 2.978 0a1.533 1.533 0 012.287-.947c1.372.836 2.942-.734 2.106-2.106a1.533 1.533 0 01.947-2.287c1.561-.379 1.561-2.6 0-2.978a1.532 1.532 0 01-.947-2.287c.836-1.372-.734-2.942-2.106-2.106a1.532 1.532 0 01-2.287-.947zM10 13a3 3 0 100-6 3 3 0 000 6z" clipRule="evenodd" />
                  </svg>
                  Account Settings
                </Link>
                <div className="dropdown-divider"></div>
                <button 
                  className="dropdown-item logout-item"
                  onClick={handleLogout}
                >
                  <svg width="16" height="16" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clipRule="evenodd" />
                  </svg>
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;