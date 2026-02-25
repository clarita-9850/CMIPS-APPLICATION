'use client';

import React, { useState, useEffect } from 'react';
import SiteHeader from './SiteHeader';
import SiteNavigation, { type NavItem } from './SiteNavigation';
import ActiveSearch from './ActiveSearch';
import LanguageSwitcher from '../LanguageSwitcher';
import { useTheme } from '@/lib/contexts/ThemeContext';
import { useAuth } from '@/lib/contexts/AuthContext';
import { usePathname, useRouter } from 'next/navigation';
import styles from './Header.module.css';

export default function Header() {
  const pathname = usePathname();
  const router = useRouter();
  const { selectedCounty } = useTheme();
  const { user, isAuthenticated, logout } = useAuth();
  const [showDashboard, setShowDashboard] = useState(false);

  const isAdmin = user?.role?.toUpperCase() === 'ADMIN';
  const isCaseWorker = user?.role?.toUpperCase() === 'CASE_WORKER';
  
  // Debug logging for admin check
  if (typeof window !== 'undefined' && process.env.NODE_ENV === 'development') {
    if (user) {
      console.log('🔍 Header - User role check:', {
        userRole: user.role,
        roleUpperCase: user.role?.toUpperCase(),
        isAdmin: isAdmin,
        userObject: user
      });
    }
  }
  const isLoginPage = pathname === '/login';

  // Define supervisor/timesheet-frontend pages
  const supervisorPages = [
    '/dashboard',
    '/analytics',
    '/visualization',
    '/admin/field-masking',
  ];

  // Check if current path is a supervisor page
  const isSupervisorPage = supervisorPages.some(page => pathname.startsWith(page));

  // Check for dashboard query parameter on client side
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const params = new URLSearchParams(window.location.search);
      setShowDashboard(params.get('dashboard') === 'true');
    }
  }, [pathname]);

  // Case worker navigation (demo-screens structure)
  const caseWorkerNavigation: NavItem[] = [
    { href: '/', navHeader: 'SW-Home' },
    { href: '/sw-team-workloads', navHeader: 'SW-Team & Workloads' },
    { href: '/my-workspace', navHeader: 'My Workspace' },
    { href: '/my-cases', navHeader: 'My Cases' },
    { href: '/inbox', navHeader: 'Inbox' },
    { href: '/caseload-by-user', navHeader: 'Caseload by User' },
  ];

  // Supervisor/timesheet-frontend navigation
  // When on supervisor pages or dashboard=true, Home should maintain the dashboard view
  const supervisorNavigation: NavItem[] = [
    { href: (showDashboard || isSupervisorPage) ? '/?dashboard=true' : '/', navHeader: 'Home' },
    { href: '/dashboard', navHeader: 'Dashboard' },
    { href: '/analytics', navHeader: 'Analytics' },
    { href: '/visualization', navHeader: 'Visualization' },
    ...(isAdmin
      ? [
          {
            href: '/admin/field-masking',
            navHeader: 'Field Masking',
          } as NavItem,
        ]
      : []),
  ];

  // Select navigation based on role, dashboard parameter, and current page
  // If case worker is on supervisor pages or has dashboard=true, show supervisor navigation
  const shouldShowSupervisorNav = isCaseWorker && (showDashboard || isSupervisorPage);
  const navigationContent: NavItem[] = shouldShowSupervisorNav ? supervisorNavigation : (isCaseWorker ? caseWorkerNavigation : supervisorNavigation);

  const handleLogout = async () => {
    await logout();
    router.replace('/login');
  };

  return (
    <header role="banner" id="header" className={styles.globalHeader}>
      <div id="skip-to-content" className={styles.skipToContent}>
        <a href="#main-content">Skip to Main Content</a>
      </div>

      {/* Utility Header */}
      <div className={styles.utilityHeader}>
        <div className="container">
          <div className={styles.flexRow}>
            <div className={styles.socialMediaLinks}>
              <div className={styles.headerCagovLogo}>
                <a href="https://www.ca.gov" className={styles.cagovLogoLink}>
                  <span className="ca-gov-logo-svg" aria-label="CA.gov"></span>
                  <span className={styles.srOnly}>CA.gov</span>
                </a>
                <p className={styles.officialTag}>
                  <span className={styles.desktopOnly}>Official website of the </span>
                  State of California
                </p>
              </div>
            </div>
            <div className={styles.settingsLinks}>
              <LanguageSwitcher />
            </div>
            {isAuthenticated && (
              <div className={styles.userActions}>
                {/* Show Dashboard/Home toggle button for case workers */}
                {user?.role?.toUpperCase() === 'CASE_WORKER' && (
                  <button
                    type="button"
                    className={styles.dashboardButton}
                    onClick={() => {
                      if (showDashboard || isSupervisorPage) {
                        // Go back to SW-Home (remove dashboard parameter)
                        router.push('/');
                      } else {
                        // Go to Dashboard view
                        router.push('/?dashboard=true');
                      }
                    }}
                  >
                    {(showDashboard || isSupervisorPage) ? 'Home' : 'Dashboard'}
                  </button>
                )}
                <span className={styles.welcomeText}>
                  {user?.name || user?.username || 'User'}
                </span>
                <button type="button" className={styles.logoutButton} onClick={handleLogout}>
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Site Header */}
      <SiteHeader
        stateText="State of California"
        departmentText={shouldShowSupervisorNav ? 'Timesheet Reporting System' : (isCaseWorker ? 'Social Work Management System' : 'Timesheet Reporting System')}
      />

      {/* Navigation and Search */}
      {!isLoginPage && (
        <div className={styles.navigationWrapper}>
          <div className="container">
            <div className={styles.navigationSearch}>
              <div id="head-search" className={styles.searchContainer}>
                <ActiveSearch />
              </div>
              <div className={styles.navControls}>
                <SiteNavigation type="dropdown" contentArr={navigationContent} />
              </div>
            </div>
          </div>
        </div>
      )}
    </header>
  );
}

