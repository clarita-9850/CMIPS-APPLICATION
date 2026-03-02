/**
 * CreateCaseWithoutCINModal
 * Implements the "Create Case without CIN" screen (CI-67785) — Error Message 185.
 *
 * Scenario 7: User clicks Save on Create Case with no CIN, but CIN clearance
 * WAS performed (no match found or no active Medi-Cal).
 *
 * EM-185 informational message:
 *   "CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS."
 * User can Continue (triggers S1 via BR 9) or Cancel (saves person as Open-Referral).
 */

import React, { useState } from 'react';

const overlay = {
  position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.55)',
  zIndex: 2200, display: 'flex', alignItems: 'center', justifyContent: 'center',
};
const box = {
  backgroundColor: '#fff', borderRadius: '6px', padding: '2rem',
  width: '90%', maxWidth: '520px', boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
};
const title = {
  color: '#153554', fontSize: '1.2rem', fontWeight: 700, marginBottom: '1rem',
  borderBottom: '2px solid #153554', paddingBottom: '0.5rem',
};
const em185banner = {
  backgroundColor: '#d1ecf1', border: '1px solid #bee5eb', borderLeft: '4px solid #0c5460',
  borderRadius: '4px', padding: '0.75rem 1rem', marginBottom: '1.25rem',
  color: '#0c5460', fontSize: '0.9rem',
};
const detail = {
  color: '#555', fontSize: '0.85rem', lineHeight: 1.6,
  backgroundColor: '#f8f9fa', border: '1px solid #dee2e6',
  borderRadius: '4px', padding: '0.75rem 1rem', marginBottom: '1.25rem',
};
const actionBar = {
  display: 'flex', gap: '0.75rem', justifyContent: 'center',
};
const btnPrimary = {
  backgroundColor: '#153554', color: '#fff', border: 'none', borderRadius: '4px',
  padding: '0.5rem 1.5rem', cursor: 'pointer', fontWeight: 600, fontSize: '0.875rem',
};
const btnSecondary = {
  backgroundColor: '#fff', color: '#153554', border: '1px solid #153554',
  borderRadius: '4px', padding: '0.5rem 1.5rem', cursor: 'pointer',
  fontWeight: 600, fontSize: '0.875rem',
};

export const CreateCaseWithoutCINModal = ({ onContinue, onCancel, saving }) => (
  <div style={overlay}>
    <div style={box}>
      <h2 style={title}>Create Case without CIN</h2>

      <div style={em185banner}>
        <strong>EM-185:</strong> CIN not selected, Medi-Cal Eligibility Referral will be sent to SAWS.
      </div>

      <p style={detail}>
        No Client Index Number was found or selected for this applicant.
        If you click <strong>Continue</strong>, CMIPS will create the case and send an
        S1 transaction (IHSS Referral for Medi-Cal Eligibility Determination) to the
        county SAWS system per Business Rule 9.
        <br /><br />
        If you click <strong>Cancel</strong>, the person will be saved as
        <em> Open-Referral</em> and you will be returned to the Create Application screen.
      </p>

      <div style={actionBar}>
        <button style={btnPrimary} onClick={onContinue} disabled={saving}>
          {saving ? 'Processing...' : 'Continue'}
        </button>
        <button style={btnSecondary} onClick={onCancel} disabled={saving}>
          Cancel
        </button>
      </div>
    </div>
  </div>
);
