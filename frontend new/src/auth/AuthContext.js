/**
 * Authentication Context Provider
 * 
 * Manages authentication state and provides auth-related utilities
 * - User information from Keycloak token
 * - Login/logout functions
 * - Token management
 * - Role-based access control
 */

import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  getToken,
  isAuthenticated,
  getParsedToken,
  login,
  logout,
  hasRole,
  getUserRoles,
  getKeycloakInstance
} from './keycloak';
import { keycloakLogout } from '../api/authApi';

// Create Auth Context
const AuthContext = createContext(null);

/**
 * Auth Provider Component
 */
function getStoredBackendUser() {
  if (typeof window === 'undefined') return null;
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  if (!token || !userStr) return null;
  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
}

export const AuthProvider = ({ children, keycloakInstance, noAuthMode = false }) => {
  const storedUser = getStoredBackendUser();
  // In noAuthMode: only authenticated if user has a valid stored session (token + user in localStorage).
  // Previously noAuthMode=true always set authenticated=true, which meant logout could never stick
  // because a page reload would immediately re-authenticate.
  const [user, setUser] = useState(storedUser || null);
  const [authenticated, setAuthenticated] = useState(!!storedUser);
  const [loading, setLoading] = useState(!noAuthMode && !keycloakInstance);
  const [roles, setRoles] = useState(storedUser?.roles || []);

  /**
   * Extract user information from token
   */
  const extractUserInfo = () => {
    const token = getParsedToken();
    
    if (!token) {
      return null;
    }

    return {
      username: token.preferred_username || token.sub,
      name: token.name || token.preferred_username,
      email: token.email,
      firstName: token.given_name,
      lastName: token.family_name,
      sub: token.sub,
      emailVerified: token.email_verified,
      // Additional custom claims
      ...token
    };
  };

  /**
   * Initialize auth state from Keycloak
   */
  useEffect(() => {
    const initAuthState = () => {
      try {
        const auth = isAuthenticated();
        setAuthenticated(auth);

        if (auth) {
          const userInfo = extractUserInfo();
          setUser(userInfo);
          
          const userRoles = getUserRoles();
          setRoles(userRoles);

          console.log('Auth state initialized:', {
            authenticated: auth,
            user: userInfo,
            roles: userRoles
          });
        }
      } catch (error) {
        console.error('Failed to initialize auth state:', error);
      } finally {
        setLoading(false);
      }
    };

    if (noAuthMode) {
      const stored = getStoredBackendUser();
      if (stored) {
        setUser(stored);
        setRoles(stored.roles || []);
        setAuthenticated(true);
      }
      const onBackendLogin = (e) => {
        const u = e?.detail?.user || getStoredBackendUser();
        if (u) {
          setUser(u);
          setRoles(u.roles || []);
          setAuthenticated(true);
        }
      };
      window.addEventListener('cmips-auth-login', onBackendLogin);
      setLoading(false);
      return () => window.removeEventListener('cmips-auth-login', onBackendLogin);
    }
    if (keycloakInstance) {
      initAuthState();

      // Listen for token updates
      const keycloak = getKeycloakInstance();
      if (keycloak) {
        // Update user info when token is refreshed
        const originalOnTokenExpired = keycloak.onTokenExpired;
        keycloak.onTokenExpired = () => {
          originalOnTokenExpired?.();
          // Update user info after token refresh
          setTimeout(() => {
            const userInfo = extractUserInfo();
            setUser(userInfo);
          }, 100);
        };
      }
    }
  }, [keycloakInstance, noAuthMode]);

  /**
   * Handle login
   */
  const handleLogin = (options = {}) => {
    login(options);
  };

  /**
   * Handle logout
   */
  const handleLogout = (options = {}) => {
    // Grab refresh token BEFORE clearing localStorage — needed for server-side session cleanup
    const savedRefreshToken = typeof window !== 'undefined' ? localStorage.getItem('refreshToken') : null;

    // Clear local state immediately
    if (typeof window !== 'undefined') {
      localStorage.removeItem('isAuthenticated');
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
    }

    setUser(null);
    setAuthenticated(false);
    setRoles([]);

    if (keycloakInstance) {
      logout(options);
      return;
    }

    // Best-effort server-side session cleanup (fire and forget — don't block redirect)
    if (savedRefreshToken) {
      keycloakLogout(savedRefreshToken).catch(() => {});
    }

    // Redirect immediately — full page reload clears all React state
    window.location.href = (options.redirectUri || window.location.origin + '/login');
  };

  /**
   * Get current access token
   */
  const getAccessToken = () => {
    return getToken();
  };

  /**
   * Check if user has specific role
   * Checks Keycloak token first, falls back to local roles state (for noAuthMode)
   */
  const userHasRole = (role) => {
    if (hasRole(role)) return true;
    return roles.includes(role);
  };

  /**
   * Check if user has any of the specified roles
   */
  const userHasAnyRole = (roleList = []) => {
    return roleList.some((role) => userHasRole(role));
  };

  /**
   * Check if user has all of the specified roles
   */
  const userHasAllRoles = (roleList = []) => {
    return roleList.every((role) => userHasRole(role));
  };

  /**
   * Refresh user information
   */
  const refreshUserInfo = () => {
    const userInfo = extractUserInfo();
    setUser(userInfo);
    const userRoles = getUserRoles();
    setRoles(userRoles);
  };

  const contextValue = {
    // State
    user,
    authenticated,
    loading,
    roles,

    // Methods
    login: handleLogin,
    logout: handleLogout,
    getToken: getAccessToken,
    hasRole: userHasRole,
    hasAnyRole: userHasAnyRole,
    hasAllRoles: userHasAllRoles,
    refreshUserInfo,

    // Keycloak instance
    keycloak: keycloakInstance
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * Custom hook to use Auth Context
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  
  return context;
};

/**
 * Higher-Order Component to protect routes
 */
export const withAuth = (Component) => {
  return (props) => {
    const { authenticated, loading } = useAuth();

    if (loading) {
      return (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh' 
        }}>
          <div>Loading...</div>
        </div>
      );
    }

    if (!authenticated) {
      return (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh',
          flexDirection: 'column',
          gap: '1rem'
        }}>
          <h2>Authentication Required</h2>
          <p>Please log in to access this page.</p>
        </div>
      );
    }

    return <Component {...props} />;
  };
};

/**
 * Component to protect routes with role check
 */
export const ProtectedRoute = ({ 
  children, 
  roles = [], 
  requireAll = false,
  fallback = null 
}) => {
  const { authenticated, loading, hasAnyRole, hasAllRoles } = useAuth();

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh' 
      }}>
        <div>Loading...</div>
      </div>
    );
  }

  if (!authenticated) {
    return fallback || (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column',
        gap: '1rem'
      }}>
        <h2>Authentication Required</h2>
        <p>Please log in to access this page.</p>
      </div>
    );
  }

  // Check roles if specified
  if (roles.length > 0) {
    const hasRequiredRoles = requireAll 
      ? hasAllRoles(roles)
      : hasAnyRole(roles);

    if (!hasRequiredRoles) {
      return fallback || (
        <div style={{ 
          display: 'flex', 
          justifyContent: 'center', 
          alignItems: 'center', 
          height: '100vh',
          flexDirection: 'column',
          gap: '1rem'
        }}>
          <h2>Access Denied</h2>
          <p>You do not have permission to access this page.</p>
        </div>
      );
    }
  }

  return children;
};

export default AuthContext;
