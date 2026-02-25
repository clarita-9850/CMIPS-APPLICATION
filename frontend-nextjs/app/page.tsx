'use client';

import { useEffect, useState } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { getDashboardForRoles, DASHBOARD_URLS } from '@/lib/roleDashboardMapping';

export default function HomePage() {
  const { user, loading } = useAuth();
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (!mounted || loading) return; // Wait for mount and auth to initialize

    if (!user) {
      // No user logged in, redirect to login
      window.location.href = '/login';
      return;
    }

    // User is logged in - resolve dashboard from all Keycloak roles
    const roles = user.roles || [];
    const dashboard = getDashboardForRoles(roles);
    window.location.href = DASHBOARD_URLS[dashboard];
  }, [user, loading, mounted]);

  // Show loading state while checking auth
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#1e3a8a] mx-auto"></div>
        <p className="mt-4 text-gray-600">Redirecting...</p>
      </div>
    </div>
  );
}
