'use client';

import React from 'react';
import styles from './SiteHeader.module.css';

interface SiteHeaderProps {
  stateText?: string;
  departmentText?: string;
}

export default function SiteHeader({
  stateText = 'State of California',
  departmentText = 'Timesheet Reporting System',
}: SiteHeaderProps) {
  return (
    <div className={styles.siteHeader}>
      <div className="container">
        <div className={styles.flexRow}>
          <div className={styles.headerCagovLogo}>
            <a href="https://ca.gov">
              <span className={styles.srOnly}>CA.gov</span>
              <span className={styles.caGovLogoSvg}></span>
            </a>
          </div>
          <div className={styles.headerText}>
            <h1 className={styles.siteTitle}>{stateText}</h1>
            <p className={styles.siteSubtitle}>{departmentText}</p>
          </div>
        </div>
      </div>
    </div>
  );
}
