'use client';

import { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { MAIN_DASHBOARD_URL } from '@/lib/roleDashboardMapping';

/**
 * CMIPS Portal Login Page
 * Based on CMIPS3.0-main design with CDSS branding, unified header, and two-column layout.
 */
export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [mounted, setMounted] = useState(false);
  const [formErrors, setFormErrors] = useState<{ username?: string; password?: string }>({});
  const { login, user, loading: authLoading } = useAuth();

  // Redirect to main dashboard if already authenticated
  useEffect(() => {
    if (!authLoading && user) {
      window.location.href = MAIN_DASHBOARD_URL + '?t=' + Date.now();
    }
  }, [user, authLoading]);

  useEffect(() => {
    setMounted(true);
    const VERSION = '3.0.0';
    const storedVersion = sessionStorage.getItem('login-page-version');
    if (storedVersion && storedVersion !== VERSION) {
      sessionStorage.setItem('login-page-version', VERSION);
      window.location.reload();
    } else if (!storedVersion) {
      sessionStorage.setItem('login-page-version', VERSION);
    }
  }, []);

  const validateForm = () => {
    const errors: { username?: string; password?: string } = {};
    if (!username.trim()) {
      errors.username = 'Username is required';
    }
    if (!password.trim()) {
      errors.password = 'Password is required';
    }
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFormErrors({});

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const result = await login(username, password);

      if (result?.success) {
        const roles = result.userData?.roles || [];
        const { getDashboardForRoles, DASHBOARD_URLS } = await import('@/lib/roleDashboardMapping');
        const dashboard = getDashboardForRoles(roles);
        const url = DASHBOARD_URLS[dashboard];
        window.location.href = url + '?t=' + Date.now();
      } else {
        setError(result?.error || 'Login failed. Please try again.');
        setLoading(false);
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Login failed. Please try again.');
      setLoading(false);
    }
  };

  if (!mounted || authLoading || user) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#f5f5f5]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[var(--color-p2)] mx-auto"></div>
          <p className="mt-4 text-gray-600">{user ? 'Redirecting to dashboard...' : 'Loading...'}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-[#f5f5f5]">
      {/* Unified Header - CDSS Branding */}
      <header
        className="bg-gradient-to-br from-[#153554] to-[#0d2a3a] px-8 py-0 shadow-[0_2px_8px_rgba(0,0,0,0.15)] relative z-[100]"
        style={{
          background: 'linear-gradient(135deg, #153554 0%, #0d2a3a 100%)',
        }}
      >
        <div className="max-w-full mx-auto flex justify-between items-center h-[70px] px-0">
          <div className="flex items-center gap-4">
            <img
              src="https://www.cdss.ca.gov/Portals/13/Images/cdss-logo-v3.png?ver=clYTY_iqlcDpaW8FClTMww%3d%3d"
              alt="California Department of Social Services"
              className="h-[50px] w-auto object-contain"
            />
            <div className="flex flex-col gap-1">
              <span className="text-xs text-white/80 font-normal uppercase tracking-wider">
                Welcome to CMIPS
              </span>
              <span className="text-sm text-white font-medium block mt-0.5">
                California Department of Social Services
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 py-12">
        <div className="container max-w-[1200px] mx-auto px-4">
          <div className="flex flex-wrap -mx-2">
            {/* Login Form Section */}
            <div className="w-full md:w-7/12 px-2 mb-8">
              <div className="bg-white p-10 rounded shadow-[0_2px_8px_rgba(0,0,0,0.1)]">
                <h2 className="text-[var(--color-p2)] text-[1.75rem] font-semibold mb-8 pb-4 border-b-[3px] border-[var(--color-p2)]">
                  Login to Your Account
                </h2>

                {error && (
                  <div
                    className="rounded px-4 py-3 mb-6 border border-transparent bg-[#f8d7da] border-[#f5c6cb] text-[#721c24]"
                    role="alert"
                  >
                    <strong>Error:</strong> {error}
                  </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                  <div className="mb-6">
                    <label htmlFor="username" className="block font-semibold text-[#333] mb-2 text-[0.95rem]">
                      Username <span className="text-red-600">*</span>
                    </label>
                    <input
                      type="text"
                      id="username"
                      name="username"
                      className={`w-full h-[45px] px-4 py-2 border-2 rounded text-base transition-colors ${
                        formErrors.username
                          ? 'border-red-500'
                          : 'border-[#d4d4d7] focus:border-[var(--color-p2)] focus:shadow-[0_0_0_0.2rem_rgba(21,53,84,0.15)]'
                      }`}
                      value={username}
                      onChange={(e) => setUsername(e.target.value)}
                      disabled={loading}
                      autoComplete="username"
                      aria-required
                      placeholder="Enter your username"
                    />
                    {formErrors.username && (
                      <div className="block mt-1 text-sm text-red-600">{formErrors.username}</div>
                    )}
                  </div>

                  <div className="mb-6">
                    <label htmlFor="password" className="block font-semibold text-[#333] mb-2 text-[0.95rem]">
                      Password <span className="text-red-600">*</span>
                    </label>
                    <input
                      type="password"
                      id="password"
                      name="password"
                      className={`w-full h-[45px] px-4 py-2 border-2 rounded text-base transition-colors ${
                        formErrors.password
                          ? 'border-red-500'
                          : 'border-[#d4d4d7] focus:border-[var(--color-p2)] focus:shadow-[0_0_0_0.2rem_rgba(21,53,84,0.15)]'
                      }`}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      disabled={loading}
                      autoComplete="current-password"
                      aria-required
                      placeholder="Enter your password"
                    />
                    {formErrors.password && (
                      <div className="block mt-1 text-sm text-red-600">{formErrors.password}</div>
                    )}
                  </div>

                  <div className="mb-0">
                    <button
                      type="submit"
                      disabled={loading}
                      className="w-full h-[50px] text-lg font-semibold rounded bg-[var(--color-p2)] border-[var(--color-p2)] text-white hover:bg-[var(--color-p2-darker)] hover:border-[var(--color-p2-darker)] disabled:opacity-60 disabled:cursor-not-allowed transition-all hover:-translate-y-px hover:shadow-[0_4px_12px_rgba(21,53,84,0.3)]"
                      style={{ borderWidth: 1 }}
                    >
                      {loading ? 'Logging in...' : 'Login'}
                    </button>
                  </div>
                </form>
              </div>
            </div>

            {/* Information Section */}
            <div className="w-full md:w-5/12 px-2">
              <div className="bg-white p-8 rounded shadow-[0_2px_8px_rgba(0,0,0,0.1)] h-full">
                <h3 className="text-[var(--color-p2)] text-[1.4rem] font-semibold mb-4">
                  Welcome to CMIPS
                </h3>
                <p className="text-[#666] leading-relaxed mb-8">
                  The CMIPS Portal provides secure access to Case Management Information and Payroll
                  System for the In-Home Supportive Services (IHSS) program.
                </p>

                <div
                  className="rounded p-6"
                  style={{ backgroundColor: 'var(--color-s1, #eef8fb)' }}
                >
                  <h4 className="text-[var(--color-p2)] text-[1.1rem] font-semibold mb-4">
                    Need Help?
                  </h4>
                  <p className="text-[#555] leading-relaxed m-0">
                    <strong>Technical Support:</strong>
                    <br />
                    Contact the CMIPS Help Desk
                    <br />
                    Monday - Friday, 8:00 AM - 5:00 PM PST
                  </p>
                </div>

                {/* Test Users */}
                <div className="mt-6 pt-6 border-t border-gray-200">
                  <p className="font-semibold text-sm text-gray-700 mb-2">Test Users:</p>
                  <p className="text-xs text-gray-500 leading-relaxed">
                    CASEMANAGEMENTROLE_user1 / password123
                    <br />
                    PROVIDERMANAGEMENTROLE_user1 / password123
                    <br />
                    PAYROLLROLE_user1 / password123
                    <br />
                    provider1 / password123 (if created in Keycloak)
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="bg-[#e9ecef] border-t border-[#dee2e6] py-8 mt-auto">
        <div className="container max-w-[1200px] mx-auto px-4 text-center">
          <p className="text-[#666] text-sm mb-4">
            © 2026 California Department of Social Services. All rights reserved.
          </p>
          <div className="text-sm">
            <a
              href="https://www.cdss.ca.gov/Privacy-Policy"
              target="_blank"
              rel="noopener noreferrer"
              className="text-[var(--color-p2)] no-underline hover:underline"
            >
              Privacy Policy
            </a>
            <span className="mx-3 text-gray-400">|</span>
            <a
              href="https://www.cdss.ca.gov/Accessibility"
              target="_blank"
              rel="noopener noreferrer"
              className="text-[var(--color-p2)] no-underline hover:underline"
            >
              Accessibility
            </a>
            <span className="mx-3 text-gray-400">|</span>
            <a
              href="https://www.cdss.ca.gov/Contact"
              target="_blank"
              rel="noopener noreferrer"
              className="text-[var(--color-p2)] no-underline hover:underline"
            >
              Contact Us
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
