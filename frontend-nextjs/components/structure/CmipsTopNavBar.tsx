'use client';

import React, { useState, useMemo } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/contexts/AuthContext';
import { getVisibleTabs } from '@/lib/roleDashboardMapping';
import styles from './CmipsTopNavBar.module.css';

interface CmipsTopNavBarProps {
  activeTab?: string;
  onTabChange?: (tab: string) => void;
}

export default function CmipsTopNavBar({ activeTab = 'My Workspace', onTabChange }: CmipsTopNavBarProps) {
  const router = useRouter();
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [caseSearchQuery, setCaseSearchQuery] = useState('');
  const roles = user?.roles || [];
  const workspaceTabs = useMemo(() => getVisibleTabs(roles), [roles]);

  const displayName = user?.name || user?.username || 'User';

  const handleCaseSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (caseSearchQuery.trim()) {
      router.push(`/cases?search=${encodeURIComponent(caseSearchQuery.trim())}`);
    }
  };

  const handleLogout = async () => {
    if (typeof window !== 'undefined' && window.confirm('Are you sure you want to log out?')) {
      await logout();
      router.replace('/login');
    }
  };

  return (
    <div className={styles.topNavContainer}>
      {/* Unified Header - CDSS Logo + Application Bar */}
      <div className={styles.unifiedHeader}>
        <div className={styles.container}>
          <div className={styles.unifiedHeaderContent}>
            <div className={styles.headerLeft}>
              <img
                src="https://www.cdss.ca.gov/Portals/13/Images/cdss-logo-v3.png?ver=clYTY_iqlcDpaW8FClTMww%3d%3d"
                alt="California Department of Social Services"
                className={styles.cdssLogo}
              />
              <div className={styles.cdssText}>
                <span className={styles.cdssLabel}>Welcome to CMIPS</span>
                <span className={styles.cdssSubtitle}>California Department of Social Services</span>
              </div>
            </div>
            <div className={styles.headerRight}>
              <div className={styles.appBarContent}>
                <div className={styles.userInfo} title={displayName}>
                  <span className={styles.userIcon}>👤</span>
                  <span className={styles.userName}>{displayName}</span>
                </div>
                <span className={styles.separator}>|</span>
                <Link href="#preferences" className={styles.appBarLink}>Preferences</Link>
                <span className={styles.separator}>|</span>
                <button className={styles.logoutLink} onClick={handleLogout} title="Log out">
                  Log out
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Workspace Tabs and Case Search */}
      <div className={styles.workspaceTabsContainer}>
        <div className={styles.workspaceTabs}>
          {workspaceTabs.map((tab) => {
            const isActive = tab.paths?.some((p) => pathname === p || (p !== '/' && pathname?.startsWith(p)));
            return (
              <Link
                key={tab.id}
                href={tab.href}
                className={`${styles.workspaceTab} ${isActive ? styles.active : ''}`}
              >
                {tab.label}
              </Link>
            );
          })}
        </div>
        <div className={styles.caseSearchContainer}>
          <form onSubmit={handleCaseSearch} className={styles.caseSearchForm}>
            <label htmlFor="case-search" className={styles.caseSearchLabel}>
              Case Search:
            </label>
            <input
              id="case-search"
              type="text"
              className={styles.caseSearchInput}
              placeholder="Enter Case ID..."
              value={caseSearchQuery}
              onChange={(e) => setCaseSearchQuery(e.target.value)}
            />
            <button type="submit" className={styles.caseSearchButton}>
              Search
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
