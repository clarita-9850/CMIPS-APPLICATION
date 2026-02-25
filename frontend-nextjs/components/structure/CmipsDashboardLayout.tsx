'use client';

import React from 'react';
import Link from 'next/link';
import styles from './CmipsDashboardLayout.module.css';

export interface ShortcutItem {
  id: string;
  label: string;
  icon: string;
  href: string;
}

interface CmipsDashboardLayoutProps {
  title: string;
  subtitle?: string;
  children: React.ReactNode;
  shortcuts?: ShortcutItem[];
}

const DEFAULT_SHORTCUTS: ShortcutItem[] = [
  { id: 'new-referral', label: 'New Referral', icon: '🏠', href: '/recipients/new' },
  { id: 'new-application', label: 'New Application', icon: '📋', href: '/new-application' },
  { id: 'find-person', label: 'Find a Person', icon: '👤', href: '/recipients' },
  { id: 'find-hearing-case', label: 'Find a State Hearing Case', icon: '⚖️', href: '#' },
];

export default function CmipsDashboardLayout({
  title,
  subtitle,
  children,
  shortcuts = DEFAULT_SHORTCUTS,
}: CmipsDashboardLayoutProps) {
  return (
    <div className={styles.dashboardWrapper}>
      <h1 className={styles.pageTitle}>{title}</h1>
      {subtitle && <p className={styles.pageSubtitle}>{subtitle}</p>}
      <div className={styles.dashboardGrid}>
        <div className={styles.tasksSection}>{children}</div>
        <div className={styles.shortcutsSection}>
          <h2 className={styles.shortcutsHeader}>My Shortcuts</h2>
          <div className={styles.shortcutsList}>
            {shortcuts.map((link) => (
              <Link key={link.id} href={link.href} className={styles.shortcutButton}>
                <span className={styles.shortcutIcon}>{link.icon}</span>
                <span className={styles.shortcutText}>{link.label}</span>
              </Link>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
