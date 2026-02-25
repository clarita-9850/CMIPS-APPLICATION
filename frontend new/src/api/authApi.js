/**
 * Authentication API - Backend proxy to Keycloak
 * Calls CMIPS backend /api/auth/login and /api/auth/refresh
 */

const RAW_BASE = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8081';
// Strip trailing /api so we always build /api/auth/... ourselves
const API_BASE = RAW_BASE.replace(/\/api\/?$/, '');

/**
 * Login with username and password
 * @param {string} username
 * @param {string} password
 * @returns {Promise<{ access_token: string, refresh_token?: string }>}
 */
export async function login(username, password) {
  const res = await fetch(`${API_BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error || data.message || `Login failed: ${res.status}`);
  }
  if (!data.access_token) {
    throw new Error('No access token received');
  }
  return data;
}

/**
 * Refresh token
 * @param {string} refreshToken
 * @returns {Promise<{ access_token: string, refresh_token?: string }>}
 */
export async function refreshToken(refreshToken) {
  const res = await fetch(`${API_BASE}/api/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refresh_token: refreshToken }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) {
    throw new Error(data.error || 'Token refresh failed');
  }
  return data;
}

/**
 * Logout — invalidate Keycloak session via backend proxy
 * Backend handles client credentials for Keycloak session termination.
 * @param {string} refreshTokenValue - The refresh token to revoke (pass explicitly since localStorage may already be cleared)
 */
export async function keycloakLogout(refreshTokenValue) {
  try {
    const token = refreshTokenValue || localStorage.getItem('refreshToken');
    if (!token) return;

    await fetch(`${API_BASE}/api/auth/logout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refresh_token: token }),
    }).catch(() => {}); // Best-effort — don't block on failure
  } catch (e) {
    console.warn('Logout session cleanup failed (non-critical):', e.message);
  }
}
