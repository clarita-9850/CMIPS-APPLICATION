/**
 * RequirePermission Component
 * 
 * Conditionally renders children based on Curam Functional Identifier (FID) permissions.
 * Hides UI elements if the user doesn't have the required permission.
 * 
 * Usage Examples:
 * 
 * 1. Single FID requirement:
 *    <RequirePermission fid={FIDS.CASE_EDIT}>
 *      <button>Edit Case</button>
 *    </RequirePermission>
 * 
 * 2. Multiple FIDs (any - OR logic):
 *    <RequirePermission anyFids={[FIDS.CASE_EDIT, FIDS.CASE_DELETE]}>
 *      <button>Modify Case</button>
 *    </RequirePermission>
 * 
 * 3. Multiple FIDs (all - AND logic):
 *    <RequirePermission allFids={[FIDS.CASE_VIEW, FIDS.EVIDENCE_VIEW]}>
 *      <CaseWithEvidencePanel />
 *    </RequirePermission>
 * 
 * 4. Show fallback when permission denied:
 *    <RequirePermission fid={FIDS.ADMIN_USER_MANAGE} fallback={<div>Access Denied</div>}>
 *      <AdminPanel />
 *    </RequirePermission>
 * 
 * 5. Check role instead of FID:
 *    <RequirePermission role="CMWORKER">
 *      <WorkerDashboard />
 *    </RequirePermission>
 */

import React from 'react';
import { useAuth } from './AuthContext';
import { hasFid, hasAnyFid, hasAllFids, hasRole } from './permissions';

/**
 * @typedef {Object} RequirePermissionProps
 * @property {string} [fid] - Single FID required to render children
 * @property {string[]} [anyFids] - Array of FIDs - user needs ANY one (OR logic)
 * @property {string[]} [allFids] - Array of FIDs - user needs ALL (AND logic)
 * @property {string} [role] - Role required to render children (alternative to FID)
 * @property {React.ReactNode} [fallback] - Component to render when permission denied
 * @property {React.ReactNode} children - Content to render when permission granted
 */

/**
 * RequirePermission Component
 * 
 * Conditionally renders children based on user permissions.
 * 
 * Permission Check Priority:
 * 1. If 'fid' prop provided → checks single FID
 * 2. If 'anyFids' provided → checks if user has ANY of the FIDs
 * 3. If 'allFids' provided → checks if user has ALL of the FIDs
 * 4. If 'role' provided → checks if user has the role
 * 5. No props → always renders (no restriction)
 * 
 * @param {RequirePermissionProps} props
 * @returns {React.ReactElement|null}
 */
export const RequirePermission = ({
  fid,
  anyFids,
  allFids,
  role,
  fallback = null,
  children,
}) => {
  const { user, authenticated } = useAuth();

  // Not authenticated → deny access
  if (!authenticated || !user) {
    return fallback;
  }

  // Get token claims from user object
  // Assuming user object contains token claims (adjust based on your AuthContext)
  const tokenClaims = user;

  // Check single FID
  if (fid) {
    const hasPermission = hasFid(fid, tokenClaims);
    return hasPermission ? children : fallback;
  }

  // Check ANY FID (OR logic)
  if (anyFids && Array.isArray(anyFids) && anyFids.length > 0) {
    const hasPermission = hasAnyFid(anyFids, tokenClaims);
    return hasPermission ? children : fallback;
  }

  // Check ALL FIDs (AND logic)
  if (allFids && Array.isArray(allFids) && allFids.length > 0) {
    const hasPermission = hasAllFids(allFids, tokenClaims);
    return hasPermission ? children : fallback;
  }

  // Check role
  if (role) {
    const hasPermission = hasRole(role, tokenClaims);
    return hasPermission ? children : fallback;
  }

  // No restrictions specified → render children
  return children;
};

/**
 * Hook for programmatic permission checks
 * 
 * Use this in component logic when you need to check permissions
 * without conditionally rendering components.
 * 
 * @returns {Object} Permission check functions
 * 
 * @example
 * function MyComponent() {
 *   const { canEdit, canDelete } = usePermissions();
 * 
 *   const handleEdit = () => {
 *     if (canEdit(FIDS.CASE_EDIT)) {
 *       // Perform edit
 *     } else {
 *       alert('No permission');
 *     }
 *   };
 * }
 */
export const usePermissions = () => {
  const { user, authenticated } = useAuth();

  const tokenClaims = authenticated && user ? user : null;

  return {
    /**
     * Check if user has a specific FID
     */
    canAccess: (fid) => hasFid(fid, tokenClaims),

    /**
     * Check if user has any of the FIDs
     */
    canAccessAny: (fids) => hasAnyFid(fids, tokenClaims),

    /**
     * Check if user has all of the FIDs
     */
    canAccessAll: (fids) => hasAllFids(fids, tokenClaims),

    /**
     * Check if user has a role
     */
    hasRole: (role) => hasRole(role, tokenClaims),

    /**
     * Shorthand aliases
     */
    canView: (fid) => hasFid(fid, tokenClaims),
    canEdit: (fid) => hasFid(fid, tokenClaims),
    canDelete: (fid) => hasFid(fid, tokenClaims),
    canCreate: (fid) => hasFid(fid, tokenClaims),
  };
};

/**
 * Higher-Order Component for permission-based component wrapping
 * 
 * Use this to wrap entire components with permission checks.
 * 
 * @param {React.Component} Component - Component to wrap
 * @param {Object} permissionConfig - Permission configuration
 * @returns {React.Component} Wrapped component with permission check
 * 
 * @example
 * const ProtectedAdminPanel = withPermission(AdminPanel, {
 *   fid: FIDS.ADMIN_USER_MANAGE,
 *   fallback: <div>Access Denied</div>
 * });
 */
export const withPermission = (Component, permissionConfig) => {
  return function PermissionWrappedComponent(props) {
    return (
      <RequirePermission {...permissionConfig}>
        <Component {...props} />
      </RequirePermission>
    );
  };
};

export default RequirePermission;
