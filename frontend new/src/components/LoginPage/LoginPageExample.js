import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LoginPage from './LoginPage';
import { login as backendLogin } from '../../api/authApi';

/**
 * Parse JWT payload
 */
function parseJwt(token) {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return {};
  }
}

/**
 * LoginPage with backend authentication
 */
const LoginPageExample = ({ onLoginSuccess }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (credentials) => {
    setIsLoading(true);
    setErrorMessage('');
    try {
      const data = await backendLogin(credentials.username, credentials.password);
      const decoded = parseJwt(data.access_token);
      const user = {
        username: decoded.preferred_username || credentials.username,
        name: decoded.name || decoded.preferred_username || credentials.username,
        sub: decoded.sub,
      };
      const roles = decoded.realm_access?.roles || [];
      localStorage.setItem('token', data.access_token);
      if (data.refresh_token) localStorage.setItem('refreshToken', data.refresh_token);
      localStorage.setItem('user', JSON.stringify({ ...user, roles }));
      localStorage.setItem('isAuthenticated', 'true');
      window.dispatchEvent(new CustomEvent('cmips-auth-login', { detail: { user: { ...user, roles } } }));
      if (onLoginSuccess) onLoginSuccess();
      navigate('/workspace');
    } catch (err) {
      setErrorMessage(err?.message || 'Login failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <LoginPage
      onLogin={handleLogin}
      isLoading={isLoading}
      errorMessage={errorMessage}
    />
  );
};

export default LoginPageExample;
