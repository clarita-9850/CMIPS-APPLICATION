'use client';

import React, { useState, useMemo } from 'react';
import Link from 'next/link';
import { useAuth } from '@/contexts/AuthContext';
import { getVisibleShortcuts } from '@/lib/roleDashboardMapping';
import styles from './CmipsSideNavPanel.module.css';

export default function CmipsSideNavPanel() {
  const [isExpanded, setIsExpanded] = useState(false);
  const { user } = useAuth();
  const roles = user?.roles || [];
  const shortcutLinks = useMemo(() => getVisibleShortcuts(roles), [roles]);

  return (
    <div className={`${styles.sideNavPanel} ${isExpanded ? styles.expanded : styles.collapsed}`}>
      <button
        className={styles.sideNavToggle}
        onClick={() => setIsExpanded(!isExpanded)}
        title={isExpanded ? 'Collapse' : 'Expand'}
        type="button"
      >
        {isExpanded ? '«' : '»'}
      </button>
      {isExpanded ? (
        <div className={styles.sideNavContent}>
          <div className={styles.sideNavHeader}>
            <h3>Shortcuts</h3>
          </div>
          <div className={styles.sideNavLinks}>
            {shortcutLinks.map((link) => (
              <Link key={link.id} href={link.href} className={styles.sideNavLink}>
                <span className={styles.linkIcon}>{link.icon}</span>
                <span className={styles.linkLabel}>{link.label}</span>
              </Link>
            ))}
          </div>
        </div>
      ) : (
        <div className={styles.sideNavIcons}>
          {shortcutLinks.map((link) => (
            <Link
              key={link.id}
              href={link.href}
              className={styles.sideNavIconButton}
              title={link.label}
            >
              <span className={styles.iconOnly}>{link.icon}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
