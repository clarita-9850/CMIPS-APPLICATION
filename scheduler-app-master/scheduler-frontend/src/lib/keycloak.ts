import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: process.env.NEXT_PUBLIC_KEYCLOAK_URL || 'http://localhost:8085',
  realm: process.env.NEXT_PUBLIC_KEYCLOAK_REALM || 'cmips',
  clientId: process.env.NEXT_PUBLIC_KEYCLOAK_CLIENT_ID || 'batch-scheduler-app',
};

let keycloakInstance: Keycloak | null = null;

export const getKeycloak = (): Keycloak => {
  if (!keycloakInstance) {
    keycloakInstance = new Keycloak(keycloakConfig);
  }
  return keycloakInstance;
};

export const initKeycloak = async (): Promise<boolean> => {
  const keycloak = getKeycloak();

  try {
    const authenticated = await keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false,
      pkceMethod: 'S256',
    });

    if (authenticated) {
      // Store token in localStorage for API requests
      localStorage.setItem('token', keycloak.token || '');

      // Set up token refresh
      setInterval(async () => {
        try {
          const refreshed = await keycloak.updateToken(30);
          if (refreshed) {
            localStorage.setItem('token', keycloak.token || '');
          }
        } catch (error) {
          console.error('Failed to refresh token', error);
          keycloak.login();
        }
      }, 10000); // Check every 10 seconds
    }

    return authenticated;
  } catch (error) {
    console.error('Keycloak init failed', error);
    return false;
  }
};

export const logout = (): void => {
  const keycloak = getKeycloak();
  localStorage.removeItem('token');
  keycloak.logout({ redirectUri: window.location.origin });
};

export const getToken = (): string | null => {
  return localStorage.getItem('token');
};

export const getUserInfo = () => {
  const keycloak = getKeycloak();
  return {
    username: keycloak.tokenParsed?.preferred_username,
    email: keycloak.tokenParsed?.email,
    name: keycloak.tokenParsed?.name,
    roles: keycloak.tokenParsed?.realm_access?.roles || [],
  };
};
