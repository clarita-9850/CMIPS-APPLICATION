/**
 * Example Usage of FID-Based Authorization
 * 
 * Demonstrates how to use the permission model in React components
 */

import React from 'react';
import { RequirePermission, usePermissions } from './RequirePermission';
import { FIDS } from './permissions';

/**
 * Example 1: Conditionally render UI elements based on FIDs
 */
export function CaseActionsExample() {
  return (
    <div className="case-actions">
      <h3>Case Actions</h3>

      {/* Show Edit button only if user has FID_CASE_EDIT */}
      <RequirePermission fid={FIDS.CASE_EDIT}>
        <button className="btn-edit">Edit Case</button>
      </RequirePermission>

      {/* Show Delete button only if user has FID_CASE_DELETE */}
      <RequirePermission fid={FIDS.CASE_DELETE}>
        <button className="btn-delete">Delete Case</button>
      </RequirePermission>

      {/* Show Assign button for SUPERVISOR role */}
      <RequirePermission role="SUPERVISOR">
        <button className="btn-assign">Assign Case</button>
      </RequirePermission>
    </div>
  );
}

/**
 * Example 2: Multiple FID checks with fallback
 */
export function EvidenceManagementExample() {
  return (
    <div className="evidence-panel">
      {/* User needs either view OR upload permission */}
      <RequirePermission 
        anyFids={[FIDS.EVIDENCE_VIEW, FIDS.EVIDENCE_UPLOAD]}
        fallback={<div>You don't have access to evidence management</div>}
      >
        <div className="evidence-list">
          <h3>Evidence Documents</h3>
          
          {/* Upload button requires specific permission */}
          <RequirePermission fid={FIDS.EVIDENCE_UPLOAD}>
            <button className="btn-upload">Upload Evidence</button>
          </RequirePermission>

          {/* Verify button requires both view and verify */}
          <RequirePermission allFids={[FIDS.EVIDENCE_VIEW, FIDS.EVIDENCE_VERIFY]}>
            <button className="btn-verify">Verify Evidence</button>
          </RequirePermission>
        </div>
      </RequirePermission>
    </div>
  );
}

/**
 * Example 3: Programmatic permission checks using hook
 */
export function PaymentFormExample() {
  const { canAccess, canAccessAny } = usePermissions();

  const handleCalculate = () => {
    if (canAccess(FIDS.PAYMENT_CALCULATE)) {
      console.log('Calculating payment...');
      // Perform calculation
    } else {
      alert('You do not have permission to calculate payments');
    }
  };

  const handleAuthorize = () => {
    if (canAccess(FIDS.PAYMENT_AUTHORIZE)) {
      console.log('Authorizing payment...');
      // Perform authorization
    } else {
      alert('You do not have permission to authorize payments');
    }
  };

  // Determine if form should be editable
  const canModifyPayment = canAccessAny([
    FIDS.PAYMENT_CALCULATE,
    FIDS.PAYMENT_AUTHORIZE,
  ]);

  return (
    <div className="payment-form">
      <h3>Payment Details</h3>
      
      <input 
        type="text" 
        placeholder="Amount"
        disabled={!canModifyPayment}
      />

      <RequirePermission fid={FIDS.PAYMENT_CALCULATE}>
        <button onClick={handleCalculate}>Calculate</button>
      </RequirePermission>

      <RequirePermission fid={FIDS.PAYMENT_AUTHORIZE}>
        <button onClick={handleAuthorize}>Authorize Payment</button>
      </RequirePermission>
    </div>
  );
}

/**
 * Example 4: Admin panel with role-based access
 */
export function AdminPanelExample() {
  const { hasRole, canAccess } = usePermissions();

  const isAdmin = hasRole('ADMIN');
  const canManageUsers = canAccess(FIDS.ADMIN_USER_MANAGE);

  return (
    <RequirePermission 
      role="ADMIN"
      fallback={<div>Admin access required</div>}
    >
      <div className="admin-panel">
        <h2>Administration</h2>

        <RequirePermission fid={FIDS.ADMIN_USER_MANAGE}>
          <section className="user-management">
            <h3>User Management</h3>
            <button>Add User</button>
            <button>Edit Roles</button>
          </section>
        </RequirePermission>

        <RequirePermission fid={FIDS.ADMIN_CONFIG_EDIT}>
          <section className="system-config">
            <h3>System Configuration</h3>
            <button>Edit Settings</button>
          </section>
        </RequirePermission>

        <RequirePermission fid={FIDS.ADMIN_AUDIT_VIEW}>
          <section className="audit-logs">
            <h3>Audit Logs</h3>
            <button>View Logs</button>
          </section>
        </RequirePermission>
      </div>
    </RequirePermission>
  );
}

/**
 * Example 5: Complex permission logic
 */
export function ApprovalWorkflowExample() {
  const { canAccess, canAccessAll } = usePermissions();

  const canSubmit = canAccess(FIDS.APPROVAL_SUBMIT);
  const canReview = canAccess(FIDS.APPROVAL_REVIEW);
  const canApprove = canAccess(FIDS.APPROVAL_APPROVE);
  const canOverride = canAccess(FIDS.APPROVAL_OVERRIDE);

  // Complex logic: can process if can review AND (approve OR override)
  const canProcessApproval = canAccessAll([FIDS.APPROVAL_REVIEW]) && 
                             (canApprove || canOverride);

  return (
    <div className="approval-workflow">
      <h3>Approval Workflow</h3>

      {/* Submit for approval */}
      <RequirePermission fid={FIDS.APPROVAL_SUBMIT}>
        <button className="btn-submit">Submit for Approval</button>
      </RequirePermission>

      {/* Review approval */}
      <RequirePermission fid={FIDS.APPROVAL_REVIEW}>
        <div className="review-section">
          <h4>Review</h4>
          <textarea placeholder="Review comments..." />

          {/* Approve/Reject buttons */}
          <RequirePermission fid={FIDS.APPROVAL_APPROVE}>
            <button className="btn-approve">Approve</button>
          </RequirePermission>

          <RequirePermission fid={FIDS.APPROVAL_REJECT}>
            <button className="btn-reject">Reject</button>
          </RequirePermission>
        </div>
      </RequirePermission>

      {/* Override button for admins */}
      <RequirePermission 
        fid={FIDS.APPROVAL_OVERRIDE}
        fallback={null}
      >
        <button className="btn-override">Override Approval</button>
      </RequirePermission>

      {/* Status indicator */}
      {canProcessApproval && (
        <div className="status-indicator">
          ✓ You can process this approval
        </div>
      )}
    </div>
  );
}
