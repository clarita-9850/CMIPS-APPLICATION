/**
 * PermissionRoute Component
 *
 * Automatically determines required roles based on the current route path
 * using the ROUTE_PERMISSIONS mapping. If the route matches a domain prefix,
 * the user must hold at least one of the mapped roles.
 *
 * Routes that don't match any prefix (or map to roles=null) require only
 * authentication (handled by the parent RequireAuth wrapper).
 */

import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { getRequiredRoles } from '../lib/routePermissions';

const AccessDeniedPage = () => (
  <div style={{
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '60vh',
    flexDirection: 'column',
    gap: '1rem',
    padding: '2rem'
  }}>
    <div style={{
      backgroundColor: 'white',
      padding: '3rem',
      borderRadius: '8px',
      boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
      textAlign: 'center',
      maxWidth: '500px'
    }}>
      <h2 style={{ color: '#153554', marginBottom: '1rem' }}>Access Denied</h2>
      <p style={{ color: '#666', marginBottom: '1.5rem' }}>
        You do not have the required permissions to view this page.
        Please contact your administrator if you believe this is an error.
      </p>
      <button
        onClick={() => window.history.back()}
        style={{
          backgroundColor: '#153554',
          color: 'white',
          padding: '0.6rem 1.5rem',
          border: 'none',
          borderRadius: '4px',
          cursor: 'pointer',
          fontSize: '0.9rem',
          fontWeight: 500
        }}
      >
        Go Back
      </button>
    </div>
  </div>
);

export const PermissionRoute = ({ children }) => {
  const location = useLocation();
  const { hasAnyRole } = useAuth();

  const requiredRoles = getRequiredRoles(location.pathname);

  // No specific roles required for this path — authentication alone is sufficient
  if (!requiredRoles) {
    return <>{children}</>;
  }

  // User must hold at least one of the required roles
  if (hasAnyRole(requiredRoles)) {
    return <>{children}</>;
  }

  return <AccessDeniedPage />;
};

export default PermissionRoute;
