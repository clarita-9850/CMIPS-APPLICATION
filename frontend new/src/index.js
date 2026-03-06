import React from 'react';
import ReactDOM from 'react-dom/client';
import "@cagovweb/state-template/dist/css/cagov.core.css";
import "@cagovweb/state-template/dist/css/colortheme-sacramento.css";
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { initKeycloak } from './auth/keycloak';
import { AuthProvider } from './auth/AuthContext';

const root = ReactDOM.createRoot(document.getElementById('root'));

/**
 * Loading Screen Component
 */
const LoadingScreen = () => (
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
      Initializing Authentication...
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
 * Error Screen Component
 */
const ErrorScreen = ({ error }) => (
  <div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    height: '100vh',
    flexDirection: 'column',
    gap: '1rem',
    backgroundColor: '#f8f9fa',
    padding: '2rem'
  }}>
    <div style={{
      backgroundColor: '#dc3545',
      color: 'white',
      padding: '1rem 2rem',
      borderRadius: '8px',
      fontSize: '1.5rem',
      fontWeight: 600
    }}>
      Authentication Error
    </div>
    <div style={{
      backgroundColor: 'white',
      padding: '2rem',
      borderRadius: '8px',
      maxWidth: '600px',
      boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
    }}>
      <h3 style={{ color: '#153554', marginBottom: '1rem' }}>Failed to Initialize Keycloak</h3>
      <p style={{ color: '#666', marginBottom: '1rem' }}>
        {error?.message || 'An error occurred during authentication initialization.'}
      </p>
      <details style={{ marginTop: '1rem' }}>
        <summary style={{ cursor: 'pointer', color: '#153554', fontWeight: 500 }}>
          Technical Details
        </summary>
        <pre style={{
          backgroundColor: '#f8f9fa',
          padding: '1rem',
          borderRadius: '4px',
          overflow: 'auto',
          fontSize: '0.85rem',
          marginTop: '0.5rem'
        }}>
          {error?.stack || JSON.stringify(error, null, 2)}
        </pre>
      </details>
      <div style={{ marginTop: '1.5rem' }}>
        <button
          onClick={() => window.location.reload()}
          style={{
            backgroundColor: '#153554',
            color: 'white',
            padding: '0.75rem 1.5rem',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer',
            fontSize: '1rem',
            fontWeight: 500
          }}
        >
          Retry
        </button>
      </div>
    </div>
  </div>
);

/**
 * Initialize application with Keycloak
 */
const initializeApp = async () => {
  // Show loading screen
  root.render(
    <React.StrictMode>
      <LoadingScreen />
    </React.StrictMode>
  );

  try {
    // Check if Keycloak is configured
    const noAuthMode = process.env.REACT_APP_DISABLE_AUTH === 'true';
    const isKeycloakConfigured = !noAuthMode && !!(
      process.env.REACT_APP_KEYCLOAK_URL &&
      process.env.REACT_APP_KEYCLOAK_REALM &&
      process.env.REACT_APP_KEYCLOAK_CLIENT_ID
    );

    let keycloakInstance = null;

    if (isKeycloakConfigured) {
      console.log('Keycloak configured, initializing...');
      try {
        keycloakInstance = await initKeycloak();
        console.log('Keycloak initialized successfully');
      } catch (err) {
        console.warn('Keycloak init failed, falling back to no-auth mode:', err?.message);
        keycloakInstance = null;
      }
    }
    if (!keycloakInstance) {
      console.warn(
        'Running without Keycloak. Set REACT_APP_DISABLE_AUTH=false and Keycloak env vars to enable authentication.'
      );
    }

    // Use no-auth mode when Keycloak not available (unconfigured or init failed)
    const effectiveNoAuthMode = !keycloakInstance;

    // Render app with AuthProvider
    root.render(
      <React.StrictMode>
        <AuthProvider keycloakInstance={keycloakInstance} noAuthMode={effectiveNoAuthMode}>
          <App />
        </AuthProvider>
      </React.StrictMode>
    );

  } catch (error) {
    console.error('Failed to initialize application:', error);
    
    // Show error screen
    root.render(
      <React.StrictMode>
        <ErrorScreen error={error} />
      </React.StrictMode>
    );
  }
};

// Initialize the application
initializeApp();

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
