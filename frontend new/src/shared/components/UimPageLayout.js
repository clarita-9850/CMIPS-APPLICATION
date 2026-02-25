import React from 'react';
import { useNavigate } from 'react-router-dom';
import './UimPage.css';

/**
 * UimPageLayout
 * Wrapper for every generated UIM page.
 * Renders an optional placeholder banner, page title, content areas, and navigation.
 *
 * Props:
 *   pageId                - original UIM PAGE_ID
 *   title                 - human-readable title
 *   children              - section/cluster content
 *   navLinks              - array of { label, route } for outgoing navigation
 *   hidePlaceholderBanner - when true, suppresses the placeholder info banner
 */
export function UimPageLayout({ pageId, title, children, navLinks = [], hidePlaceholderBanner = false }) {
  const navigate = useNavigate();

  return (
    <div className="uim-page">
      <div className="container">
        {!hidePlaceholderBanner && (
          <div className="uim-info-banner" style={{ marginTop: '0.75rem' }}>
            <strong>Placeholder</strong> — generated from <code>{pageId}.uim</code>.
            Backend integration pending.
          </div>
        )}

        <h1 className="page-title">{title || pageId}</h1>

        {children}

        {navLinks.length > 0 && (
          <div className="uim-cluster" style={{ marginTop: '1.5rem' }}>
            <h3 className="uim-cluster-title">Navigation</h3>
            <div className="uim-cluster-body">
              <div className="uim-action-bar">
                {navLinks.map(({ label, route }, idx) => (
                  <button
                    key={`${route}-${idx}`}
                    className="uim-btn uim-btn-secondary"
                    onClick={() => navigate(route)}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}

        <div className="uim-action-bar" style={{ marginTop: '1rem' }}>
          <button className="uim-btn uim-btn-secondary" onClick={() => navigate(-1)}>
            &larr; Back
          </button>
        </div>
      </div>
    </div>
  );
}

export default UimPageLayout;
