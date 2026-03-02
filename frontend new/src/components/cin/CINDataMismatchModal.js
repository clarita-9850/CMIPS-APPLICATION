/**
 * CINDataMismatchModal
 * Implements the "CIN Data Does Not Match Applicant Data" screen (CI-67766).
 *
 * Scenario D: Selected CIN's demographics don't match the applicant's data.
 *
 * DSD Options:
 *   1. Return to CIN Select — try a different CIN
 *   2. Proceed without CIN — create case without CIN (triggers S1 referral to SAWS)
 *   3. Cancel Create Case — abort and return to fix demographics
 */

import React from 'react';

const overlay = {
  position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.55)',
  zIndex: 2200, display: 'flex', alignItems: 'center', justifyContent: 'center',
};
const box = {
  backgroundColor: '#fff', borderRadius: '6px', padding: '2rem',
  width: '90%', maxWidth: '520px', boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
  textAlign: 'center',
};
const icon = { fontSize: '2.5rem', marginBottom: '0.75rem', color: '#856404' };
const title = { color: '#153554', fontSize: '1.2rem', fontWeight: 700, marginBottom: '1rem' };
const msg = {
  color: '#555', fontSize: '0.9rem', lineHeight: 1.6,
  backgroundColor: '#fff3cd', border: '1px solid #ffc107',
  borderRadius: '4px', padding: '1rem', marginBottom: '1.25rem',
};
const hint = { color: '#666', fontSize: '0.8rem', lineHeight: 1.5, marginBottom: '1.25rem' };
const btnPrimary = {
  backgroundColor: '#153554', color: '#fff', border: 'none', borderRadius: '4px',
  padding: '0.5rem 1.5rem', cursor: 'pointer', fontWeight: 600, fontSize: '0.875rem',
};
const btnSecondary = {
  backgroundColor: '#fff', color: '#153554', border: '1px solid #153554',
  borderRadius: '4px', padding: '0.5rem 1.5rem', cursor: 'pointer',
  fontWeight: 600, fontSize: '0.875rem',
};
const btnDanger = {
  backgroundColor: '#fff', color: '#c53030', border: '1px solid #c53030',
  borderRadius: '4px', padding: '0.5rem 1.5rem', cursor: 'pointer',
  fontWeight: 600, fontSize: '0.875rem',
};

export const CINDataMismatchModal = ({ onReturnToCINSelect, onProceedWithoutCIN, onCancelCreateCase }) => (
  <div style={overlay}>
    <div style={box}>
      <div style={icon}>⚠️</div>
      <h2 style={title}>CIN Data Does Not Match Applicant Data</h2>

      <p style={msg}>
        The demographic information associated with the selected CIN does not match
        the applicant's data on file. Please review the records and try again.
      </p>

      <p style={hint}>
        You can return to CIN select to try a different CIN, proceed without a CIN
        (a Medi-Cal Eligibility Referral will be sent to SAWS), or cancel to correct
        demographics on the Person record first.
      </p>

      <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center', flexWrap: 'wrap' }}>
        <button style={btnPrimary} onClick={onReturnToCINSelect}>
          Return to CIN Select
        </button>
        {onProceedWithoutCIN && (
          <button style={btnSecondary} onClick={onProceedWithoutCIN}>
            Proceed without CIN
          </button>
        )}
        {onCancelCreateCase && (
          <button style={btnDanger} onClick={onCancelCreateCase}>
            Cancel
          </button>
        )}
      </div>
    </div>
  </div>
);
