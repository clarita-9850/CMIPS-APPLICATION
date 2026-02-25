'use client';

import React from 'react';
import CmipsTopNavBar from './CmipsTopNavBar';
import CmipsSideNavPanel from './CmipsSideNavPanel';
import styles from './CmipsAppShell.module.css';

interface CmipsAppShellProps {
  children: React.ReactNode;
}

export default function CmipsAppShell({ children }: CmipsAppShellProps) {
  return (
    <div className={styles.appShell}>
      <a href="#main-content" className={styles.skipToContent}>
        Skip to main content
      </a>
      <CmipsTopNavBar />
      <div className={styles.appShellContent}>
        <CmipsSideNavPanel />
        <main id="main-content" className={styles.appShellMain} role="main">
          <div className={styles.breadcrumbsContainer}>
            <nav aria-label="Breadcrumb">
              <ol className={styles.breadcrumb}>
                <li className={styles.breadcrumbItem}>
                  <a href="/">Home</a>
                </li>
              </ol>
            </nav>
          </div>
          <div className={styles.pageOutlet}>{children}</div>
          <footer className={styles.appShellFooter}>
            <div className={styles.footerContent}>
              <p className={styles.footerText}>
                © 2026 California Department of Social Services. All rights reserved.
              </p>
              <div className={styles.footerLinks}>
                <a href="https://www.cdss.ca.gov/Privacy-Policy" target="_blank" rel="noopener noreferrer">
                  Privacy Policy
                </a>
                <span className={styles.separator}>|</span>
                <a href="https://www.cdss.ca.gov/Accessibility" target="_blank" rel="noopener noreferrer">
                  Accessibility
                </a>
                <span className={styles.separator}>|</span>
                <a href="https://www.cdss.ca.gov/Contact" target="_blank" rel="noopener noreferrer">
                  Contact Us
                </a>
              </div>
            </div>
          </footer>
        </main>
      </div>
    </div>
  );
}
