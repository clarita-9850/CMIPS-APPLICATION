/**
 * RequireAuth Component
 * 
 * Protects routes by requiring authentication
 * - Checks if user is authenticated via AuthContext
 * - Redirects to Keycloak login if not authenticated
 * - Shows loading state during authentication check
 */

import React, { useEffect } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

/**
 * Loading component shown during authentication check
 */
const AuthLoadingScreen = () => (
  <div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    flexDirection: 'column',
    gap: '1rem',
    backgroundColor: '#f8f9fa'
  }}>
    <div style={{
      width: '60px',
      height: '60px',
      border: '4px solid #e9ecef',
      borderTop: '4px solid #153554',
      borderRadius: '50%',
      animation: 'spin 1s linear infinite'
    }}></div>
    <p style={{ color: '#153554', fontSize: '1.2rem', fontWeight: 500 }}>
      Checking authentication...
    </p>
    <style>{`
      @keyframes spin {
        0% { transform: rotate(0deg); }
        100% { transform: rotate(360deg); }
      }
    `}</style>
  </div>
);

/**
 * RequireAuth Component
 * Wraps protected routes and ensures user is authenticated
 */
export const RequireAuth = ({ children }) => {
  const { authenticated, loading, login, keycloak } = useAuth();

  useEffect(() => {
    // If not loading and not authenticated, trigger login
    if (!loading && !authenticated && keycloak) {
      console.log('User not authenticated, redirecting to login...');
      login();
    }
  }, [loading, authenticated, login, keycloak]);

  // No Keycloak: check context (no-auth mode) or localStorage (mock dev mode)
  if (!keycloak) {
    if (authenticated) return <>{children}</>;
    const isAuth = localStorage.getItem('isAuthenticated') === 'true';
    if (!isAuth) {
      return <Navigate to="/login" replace />;
    }
    return <>{children}</>;
  }

  // Show loading screen while checking authentication
  if (loading) {
    return <AuthLoadingScreen />;
  }

  // If not authenticated and Keycloak is configured, show loading
  // (login redirect will happen in useEffect)
  if (!authenticated && keycloak) {
    return <AuthLoadingScreen />;
  }

  // User is authenticated, render children
  return <>{children}</>;
};

export default RequireAuth;
