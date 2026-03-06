/**
 * TopNavBar - Horizontal tab navigation
 * Based on DSD Section 20 - Navigation Elements
 * 
 * Features:
 * - Workspace tabs that always display
 * - Case search field
 * - Application bar with user info from Keycloak token and logout
 */

import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import { TAB_TO_ROUTE, ROUTE_TO_TAB } from '../../lib/navigationMap';
import './TopNavBar.css';

export const TopNavBar = ({ onTabChange, activeTab: controlledActiveTab }) => {
  const [caseSearchQuery, setCaseSearchQuery] = useState('');
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const activeTab = controlledActiveTab ?? (ROUTE_TO_TAB[location.pathname] || 'My Workspace');

  // Get user display name from token or fall back to default
  const displayName = user?.name || user?.username || 'Demo User';
  const userEmail = user?.email;

  const tabs = [
    { id: 'my-workspace', label: 'My Workspace' },
    { id: 'my-cases', label: 'My Cases' },
    { id: 'inbox', label: 'Inbox' },
    { id: 'calendar', label: 'Calendar' },
    { id: 'reports', label: 'Reports' }
  ];

  const handleCaseSearch = (e) => {
    e.preventDefault();
    const q = caseSearchQuery.trim();
    navigate(q ? `/cases?search=${encodeURIComponent(q)}` : '/cases');
  };

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to log out?')) {
      logout({ redirectUri: window.location.origin + '/login' });
    }
  };

  const handleTabClick = (tabLabel) => {
    const route = TAB_TO_ROUTE[tabLabel];
    if (route) navigate(route);
    onTabChange?.(tabLabel);
  };

  return (
    <div className="top-nav-container">
      {/* Unified Header - CDSS Logo + Application Bar */}
      <div className="unified-header">
        <div className="container">
          <div className="unified-header-content">
            {/* Left: CDSS Logo and Text */}
            <div className="header-left">
              <img 
                src="https://www.cdss.ca.gov/Portals/13/Images/cdss-logo-v3.png?ver=clYTY_iqlcDpaW8FClTMww%3d%3d" 
                alt="California Department of Social Services" 
                className="cdss-logo"
              />
              <div className="cdss-text">
                <span className="cdss-label">Welcome to CMIPS</span>
                <span className="cdss-subtitle">California Department of Social Services</span>
              </div>
            </div>
            
            {/* Right: Application Bar */}
            <div className="header-right">
              <div className="app-bar-content">
                <div className="user-info" title={userEmail || displayName}>
                  <span className="user-icon">👤</span>
                  <span className="user-name">{displayName}</span>
                </div>
                <span className="separator">|</span>
                <Link to="/preferences" className="app-bar-link">Preferences</Link>
                <span className="separator">|</span>
                <button 
                  className="app-bar-link logout-link" 
                  onClick={handleLogout}
                  title="Log out"
                >
                  Log out
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Workspace Tabs and Case Search */}
      <div className="workspace-tabs-container">
        <div className="workspace-tabs">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              className={`workspace-tab ${activeTab === tab.label ? 'active' : ''}`}
              onClick={() => handleTabClick(tab.label)}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* Case Search - Top right */}
        <div className="case-search-container">
          <form onSubmit={handleCaseSearch} className="case-search-form">
            <label htmlFor="case-search" className="case-search-label">
              Case Search:
            </label>
            <input
              id="case-search"
              type="text"
              className="case-search-input"
              placeholder="Enter Case ID..."
              value={caseSearchQuery}
              onChange={(e) => setCaseSearchQuery(e.target.value)}
            />
            <button type="submit" className="case-search-button">
              Search
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
