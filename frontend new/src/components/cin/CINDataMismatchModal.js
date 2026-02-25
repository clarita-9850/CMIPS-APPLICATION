/**
 * CINDataMismatchModal
 * Implements the "CIN Data Does Not Match Applicant Data" screen (CI-67766).
 *
 * Scenario 5: Selected CIN's demographics don't match the applicant's data.
 * No data fields — only a warning message and a "Return to CIN Select" button.
 *
 * If the user determines their OWN record is wrong they must:
 * Cancel → Create Case → fix demographics on Person screen → restart flow.
 */

import React from 'react';

const overlay = {
  position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.55)',
  zIndex: 2200, display: 'flex', alignItems: 'center', justifyContent: 'center',
};
const box = {
  backgroundColor: '#fff', borderRadius: '6px', padding: '2rem',
  width: '90%', maxWidth: '500px', boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
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

export const CINDataMismatchModal = ({ onReturnToCINSelect }) => (
  <div style={overlay}>
    <div style={box}>
      <div style={icon}>⚠️</div>
      <h2 style={title}>CIN Data Does Not Match Applicant Data</h2>

      <p style={msg}>
        The demographic information associated with the selected CIN does not match
        the applicant's data on file. Please review the records and try again.
      </p>

      <p style={hint}>
        If the applicant's own demographic information is incorrect, cancel back to
        the Create Case screen, correct the demographics on the Person record, then
        restart the CIN clearance process.
      </p>

      <button style={btnPrimary} onClick={onReturnToCINSelect}>
        Return to CIN Select
      </button>
    </div>
  </div>
);
