/**
 * AppShell Layout Component
 * Composes TopNavBar + SideNavPanel + main content area
 * 
 * Features:
 * - Fixed top navigation
 * - Collapsible side navigation
 * - Main content area with React Router outlet
 * - Breadcrumbs placeholder
 * - Skip-to-content link for accessibility
 */

import React, { useState } from 'react';
import { Outlet, useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { TopNavBar } from '../components/Navigation/TopNavBar';
import { SideNavPanel } from '../components/Navigation/SideNavPanel';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import { SIDE_NAV_ROUTES } from '../lib/navigationMap';
import './AppShell.css';

export const AppShell = () => {
  const navigate = useNavigate();
  const { roles } = useAuth();
  const { breadcrumbs } = useBreadcrumbs();
  const [isSideNavOpen, setIsSideNavOpen] = useState(false);

  // Derive role category from Keycloak roles
  const isAdmin = roles.some(r => r === 'ADMIN');
  const isSupervisor = roles.some(r => r.includes('SUPERVISOR'));
  const isPayroll = roles.some(r => r.includes('PAYROLL') || r.includes('TIMESHEET'));
  const computedRole = isAdmin ? 'admin' : isSupervisor ? 'supervisor' : 'standard';

  const handleTabChange = () => { /* Tab nav handled by TopNavBar */ };

  const handleSideNavNavigate = (linkId) => {
    const route = SIDE_NAV_ROUTES[linkId] || '/workspace';
    navigate(route);
  };

  const toggleSideNav = () => {
    setIsSideNavOpen(!isSideNavOpen);
  };

  return (
    <div className="app-shell">
      {/* Skip to content link for accessibility */}
      <a href="#main-content" className="skip-to-content">
        Skip to main content
      </a>

      {/* Top Navigation Bar - Fixed */}
      <TopNavBar onTabChange={handleTabChange} />

      {/* Main Layout Container */}
      <div className="app-shell-content">
        {/* Side Navigation Panel - Collapsible */}
        <SideNavPanel
          userRole={computedRole}
          roles={roles}
          isSupervisor={isSupervisor}
          isPayroll={isPayroll}
          onNavigate={handleSideNavNavigate}
          isExpanded={isSideNavOpen}
          onToggle={toggleSideNav}
        />

        {/* Main Content Area */}
        <main id="main-content" className="app-shell-main" role="main">
          {/* Dynamic Breadcrumbs */}
          <div className="breadcrumbs-container">
            <nav aria-label="Breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item">
                  <Link to="/workspace">Home</Link>
                </li>
                {breadcrumbs.map((crumb, idx) => (
                  <li key={idx} className="breadcrumb-item">
                    {crumb.path ? (
                      <Link to={crumb.path}>{crumb.label}</Link>
                    ) : (
                      <span>{crumb.label}</span>
                    )}
                  </li>
                ))}
              </ol>
            </nav>
          </div>

          {/* React Router Outlet - Renders routed page components */}
          <div className="page-outlet">
            <Outlet />
          </div>

          {/* Footer */}
          <footer className="app-shell-footer">
            <div className="container">
              <div className="footer-content">
                <p className="footer-text">
                  © 2026 California Department of Social Services. All rights reserved.
                </p>
                <div className="footer-links">
                  <a href="https://www.cdss.ca.gov/Privacy-Policy" target="_blank" rel="noopener noreferrer">
                    Privacy Policy
                  </a>
                  <span className="separator">|</span>
                  <a href="https://www.cdss.ca.gov/Accessibility" target="_blank" rel="noopener noreferrer">
                    Accessibility
                  </a>
                  <span className="separator">|</span>
                  <a href="https://www.cdss.ca.gov/Contact" target="_blank" rel="noopener noreferrer">
                    Contact Us
                  </a>
                </div>
              </div>
            </div>
          </footer>
        </main>
      </div>

      {/* Mobile Hamburger Menu Toggle */}
      <button 
        className="mobile-nav-toggle" 
        onClick={toggleSideNav}
        aria-label="Toggle navigation menu"
        aria-expanded={isSideNavOpen}
      >
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
        <span className="hamburger-line"></span>
      </button>
    </div>
  );
};
