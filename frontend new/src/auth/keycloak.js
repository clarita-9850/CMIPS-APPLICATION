/**
 * Keycloak Authentication Configuration
 * 
 * Initializes and manages Keycloak authentication
 * - PKCE flow with S256 for security
 * - Automatic token refresh
 * - Token management utilities
 */

import Keycloak from 'keycloak-js';

let keycloakInstance = null;

/**
 * Get Keycloak configuration from environment variables
 */
const getKeycloakConfig = () => {
  const url = process.env.REACT_APP_KEYCLOAK_URL;
  const realm = process.env.REACT_APP_KEYCLOAK_REALM;
  const clientId = process.env.REACT_APP_KEYCLOAK_CLIENT_ID;

  if (!url || !realm || !clientId) {
    throw new Error(
      'Missing Keycloak configuration. Please set:\n' +
      '- REACT_APP_KEYCLOAK_URL\n' +
      '- REACT_APP_KEYCLOAK_REALM\n' +
      '- REACT_APP_KEYCLOAK_CLIENT_ID'
    );
  }

  return {
    url,
    realm,
    clientId
  };
};

/**
 * Initialize Keycloak instance
 * @returns {Promise<Keycloak>} Initialized Keycloak instance
 */
export const initKeycloak = async () => {
  try {
    const config = getKeycloakConfig();
    
    // Create Keycloak instance
    keycloakInstance = new Keycloak(config);

    // Configure token refresh on expiration
    keycloakInstance.onTokenExpired = () => {
      console.log('Token expired, refreshing...');
      keycloakInstance
        .updateToken(30) // Refresh if token expires in 30 seconds
        .then((refreshed) => {
          if (refreshed) {
            console.log('Token refreshed successfully');
          } else {
            console.log('Token still valid');
          }
        })
        .catch((error) => {
          console.error('Failed to refresh token:', error);
          // Force login if refresh fails
          keycloakInstance.login();
        });
    };

    // Initialize with PKCE (S256) for enhanced security
    const authenticated = await keycloakInstance.init({
      onLoad: 'check-sso',
      pkceMethod: 'S256',
      checkLoginIframe: false, // Disable iframe check for better performance
      silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html'
    });

    if (authenticated) {
      console.log('User authenticated successfully');
      console.log('Token:', keycloakInstance.token);
      console.log('Refresh Token:', keycloakInstance.refreshToken);
      console.log('ID Token:', keycloakInstance.idToken);
      console.log('Token Parsed:', keycloakInstance.tokenParsed);
    } else {
      console.log('User not authenticated');
    }

    return keycloakInstance;
  } catch (error) {
    console.error('Failed to initialize Keycloak:', error);
    throw error;
  }
};

/**
 * Get current access token
 * @returns {string|null} Access token or null if not authenticated
 */
export const getToken = () => {
  if (!keycloakInstance) {
    console.warn('Keycloak not initialized');
    return null;
  }
  return keycloakInstance.token || null;
};

/**
 * Check if user is authenticated
 * @returns {boolean} True if authenticated
 */
export const isAuthenticated = () => {
  if (!keycloakInstance) {
    return false;
  }
  return keycloakInstance.authenticated || false;
};

/**
 * Get parsed token with user claims
 * @returns {object|null} Parsed token or null
 */
export const getParsedToken = () => {
  if (!keycloakInstance) {
    console.warn('Keycloak not initialized');
    return null;
  }
  return keycloakInstance.tokenParsed || null;
};

/**
 * Get user profile information
 * @returns {Promise<object>} User profile
 */
export const getUserProfile = async () => {
  if (!keycloakInstance) {
    throw new Error('Keycloak not initialized');
  }
  
  try {
    return await keycloakInstance.loadUserProfile();
  } catch (error) {
    console.error('Failed to load user profile:', error);
    throw error;
  }
};

/**
 * Login with Keycloak
 * @param {object} options - Login options
 */
export const login = (options = {}) => {
  if (!keycloakInstance) {
    console.error('Keycloak not initialized');
    return;
  }
  keycloakInstance.login(options);
};

/**
 * Logout from Keycloak
 * @param {object} options - Logout options
 */
export const logout = (options = {}) => {
  if (!keycloakInstance) {
    console.error('Keycloak not initialized');
    return;
  }
  keycloakInstance.logout({
    redirectUri: window.location.origin,
    ...options
  });
};

/**
 * Update token if needed
 * @param {number} minValidity - Minimum validity in seconds
 * @returns {Promise<boolean>} True if token was refreshed
 */
export const updateToken = async (minValidity = 30) => {
  if (!keycloakInstance) {
    throw new Error('Keycloak not initialized');
  }
  
  try {
    return await keycloakInstance.updateToken(minValidity);
  } catch (error) {
    console.error('Failed to update token:', error);
    throw error;
  }
};

/**
 * Get Keycloak instance
 * @returns {Keycloak|null} Keycloak instance
 */
export const getKeycloakInstance = () => {
  return keycloakInstance;
};

/**
 * Check if user has specific role
 * @param {string} role - Role name
 * @returns {boolean} True if user has role
 */
export const hasRole = (role) => {
  if (!keycloakInstance || !keycloakInstance.tokenParsed) {
    return false;
  }

  const realmRoles = keycloakInstance.tokenParsed.realm_access?.roles || [];
  return realmRoles.includes(role);
};

/**
 * Get user roles
 * @returns {string[]} Array of role names
 */
export const getUserRoles = () => {
  if (!keycloakInstance || !keycloakInstance.tokenParsed) {
    return [];
  }

  return keycloakInstance.tokenParsed.realm_access?.roles || [];
};

const keycloakModule = {
  initKeycloak,
  getToken,
  isAuthenticated,
  getParsedToken,
  getUserProfile,
  login,
  logout,
  updateToken,
  getKeycloakInstance,
  hasRole,
  getUserRoles
};

export default keycloakModule;
