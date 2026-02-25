/**
 * HTTP Client with Keycloak Token Injection
 * 
 * Provides a fetch wrapper that:
 * - Automatically injects Authorization: Bearer <token>
 * - Handles 401 by refreshing token and retrying once
 * - Logs out on refresh failure
 */

import { getKeycloakInstance } from '../auth/keycloak';

/**
 * @typedef {Object} HttpClientOptions
 * @property {string} [method] - HTTP method (GET, POST, PUT, DELETE, etc.)
 * @property {Object} [headers] - Additional headers
 * @property {any} [body] - Request body (will be JSON stringified if object)
 * @property {boolean} [skipAuth] - Skip Authorization header injection
 * @property {AbortSignal} [signal] - AbortSignal for request cancellation
 */

/**
 * @typedef {Object} HttpClientResponse
 * @property {boolean} ok - Response success status
 * @property {number} status - HTTP status code
 * @property {Object} data - Parsed response data
 * @property {Response} raw - Raw fetch response
 */

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081';

/**
 * Makes an authenticated HTTP request with automatic token injection
 * @param {string} url - API endpoint (relative or absolute)
 * @param {HttpClientOptions} options - Request options
 * @returns {Promise<HttpClientResponse>}
 */
export async function httpClient(url, options = {}) {
  const {
    method = 'GET',
    headers = {},
    body,
    skipAuth = false,
    signal,
    ...fetchOptions
  } = options;

  // Build full URL (api path typically starts with /api)
  const base = API_BASE_URL.endsWith('/api') ? API_BASE_URL.replace(/\/api$/, '') : API_BASE_URL;
  const path = url.startsWith('/') ? url : `/${url}`;
  const fullUrl = url.startsWith('http') ? url : `${base}${path.startsWith('/api') ? path : '/api' + path}`;

  // Prepare headers
  const requestHeaders = {
    'Content-Type': 'application/json',
    ...headers,
  };

  // Inject Authorization token (Keycloak or stored backend token)
  if (!skipAuth) {
    let token = null;
    const keycloak = getKeycloakInstance();
    if (keycloak && keycloak.token) {
      token = keycloak.token;
    } else if (typeof window !== 'undefined') {
      token = localStorage.getItem('token');
    }
    if (token) {
      requestHeaders['Authorization'] = `Bearer ${token}`;
    }
  }

  // Prepare request config with timeout
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 15000); // 15 second timeout
  
  const config = {
    method,
    headers: requestHeaders,
    signal: signal || controller.signal,
    ...fetchOptions,
  };

  // Add body for non-GET requests
  if (body && method !== 'GET' && method !== 'HEAD') {
    config.body = typeof body === 'string' ? body : JSON.stringify(body);
  }

  try {
    const response = await fetch(fullUrl, config);
    clearTimeout(timeoutId);

    // Handle 401 Unauthorized - attempt token refresh and retry
    if (response.status === 401 && !skipAuth) {
      console.log('[httpClient] 401 Unauthorized - attempting token refresh');
      
      const refreshed = await refreshTokenAndRetry(requestHeaders);
      if (refreshed) {
        // Retry request with new token
        let token = null;
        const keycloak = getKeycloakInstance();
        if (keycloak && keycloak.token) {
          token = keycloak.token;
        } else if (typeof window !== 'undefined') {
          token = localStorage.getItem('token');
        }
        if (token) requestHeaders['Authorization'] = `Bearer ${token}`;
        const retryResponse = await fetch(fullUrl, { ...config, headers: requestHeaders });
        return await parseResponse(retryResponse);
      } else {
        // Refresh failed - logout
        console.error('[httpClient] Token refresh failed - logging out');
        const keycloak = getKeycloakInstance();
        if (keycloak) {
          keycloak.logout({ redirectUri: window.location.origin });
        } else if (typeof window !== 'undefined') {
          localStorage.removeItem('token');
          localStorage.removeItem('refreshToken');
          localStorage.removeItem('user');
          localStorage.removeItem('isAuthenticated');
          window.location.href = window.location.origin + '/login';
        }
        throw new Error('Authentication failed - please log in again');
      }
    }

    return await parseResponse(response);
  } catch (error) {
    clearTimeout(timeoutId);
    // Network errors, aborted requests, etc.
    console.error('[httpClient] Request failed:', error);
    throw error;
  }
}

/**
 * Attempts to refresh the Keycloak token
 * @returns {Promise<boolean>} - True if refresh succeeded
 */
async function refreshTokenAndRetry(requestHeaders) {
  const keycloak = getKeycloakInstance();
  if (keycloak) {
    try {
      const refreshed = await keycloak.updateToken(5);
      if (refreshed && keycloak.token) {
        requestHeaders['Authorization'] = `Bearer ${keycloak.token}`;
        return true;
      }
      return !!keycloak.token;
    } catch (error) {
      console.error('[httpClient] Keycloak token refresh failed:', error);
      return false;
    }
  }
  // Backend token refresh
  if (typeof window !== 'undefined') {
    const refreshTokenVal = localStorage.getItem('refreshToken');
    if (refreshTokenVal) {
      try {
        const { refreshToken: doRefresh } = await import('./authApi');
        const data = await doRefresh(refreshTokenVal);
        localStorage.setItem('token', data.access_token);
        if (data.refresh_token) localStorage.setItem('refreshToken', data.refresh_token);
        requestHeaders['Authorization'] = `Bearer ${data.access_token}`;
        return true;
      } catch (err) {
        console.error('[httpClient] Backend token refresh failed:', err);
      }
    }
  }
  return false;
}

/**
 * Parses fetch response and extracts data
 * @param {Response} response - Fetch response object
 * @returns {Promise<HttpClientResponse>}
 */
async function parseResponse(response) {
  let data = null;

  // Parse response body if present
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    try {
      data = await response.json();
    } catch (error) {
      console.warn('[httpClient] Failed to parse JSON response:', error);
    }
  } else {
    try {
      data = await response.text();
    } catch (error) {
      console.warn('[httpClient] Failed to read response text:', error);
    }
  }

  // Handle error responses
  if (!response.ok) {
    const error = new Error(data?.message || `HTTP ${response.status}: ${response.statusText}`);
    error.status = response.status;
    error.response = response;
    error.data = data;
    throw error;
  }

  return {
    ok: response.ok,
    status: response.status,
    data,
    raw: response,
  };
}

/**
 * Convenience methods for common HTTP operations
 */

export const http = {
  /**
   * GET request
   * @param {string} url - API endpoint
   * @param {HttpClientOptions} options - Request options
   */
  get: (url, options = {}) => httpClient(url, { ...options, method: 'GET' }),

  /**
   * POST request
   * @param {string} url - API endpoint
   * @param {any} body - Request body
   * @param {HttpClientOptions} options - Request options
   */
  post: (url, body, options = {}) => httpClient(url, { ...options, method: 'POST', body }),

  /**
   * PUT request
   * @param {string} url - API endpoint
   * @param {any} body - Request body
   * @param {HttpClientOptions} options - Request options
   */
  put: (url, body, options = {}) => httpClient(url, { ...options, method: 'PUT', body }),

  /**
   * PATCH request
   * @param {string} url - API endpoint
   * @param {any} body - Request body
   * @param {HttpClientOptions} options - Request options
   */
  patch: (url, body, options = {}) => httpClient(url, { ...options, method: 'PATCH', body }),

  /**
   * DELETE request
   * @param {string} url - API endpoint
   * @param {HttpClientOptions} options - Request options
   */
  delete: (url, options = {}) => httpClient(url, { ...options, method: 'DELETE' }),
};

export default http;
