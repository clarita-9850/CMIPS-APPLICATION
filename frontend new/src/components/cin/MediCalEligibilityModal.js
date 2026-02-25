/**
 * MediCalEligibilityModal
 * Implements the "Medi-Cal Eligibility Information" pop-up (CI-121490).
 *
 * Opens after the EL/OM transaction succeeds.
 * Shows the OM response data and lets the user:
 *   Select  → triggers demographic check (EM-202, mismatch, or success)
 *   Cancel  → returns to CIN Search results list
 */

import React, { useState } from 'react';
import http from '../../api/httpClient';

const overlay = {
  position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.55)',
  zIndex: 2100, display: 'flex', alignItems: 'center', justifyContent: 'center',
};
const box = {
  backgroundColor: '#fff', borderRadius: '6px', padding: '1.5rem',
  width: '90%', maxWidth: '640px', boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
};
const sectionHeader = {
  fontSize: '0.875rem', fontWeight: 700, color: '#153554',
  backgroundColor: '#e8edf2', padding: '4px 8px', borderRadius: '3px',
  marginBottom: '0.75rem',
};
const grid = {
  display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem',
};
const fieldLabel = { fontSize: '0.75rem', color: '#666', marginBottom: '2px', fontWeight: 600 };
const fieldValue = {
  fontSize: '0.875rem', backgroundColor: '#f0f4f8', border: '1px solid #d0d3d6',
  padding: '4px 8px', borderRadius: '3px', minHeight: '28px',
};
const activeTag = {
  display: 'inline-block', backgroundColor: '#d4edda', color: '#155724',
  padding: '1px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 700,
};
const inactiveTag = {
  display: 'inline-block', backgroundColor: '#f8d7da', color: '#721c24',
  padding: '1px 8px', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 700,
};
const errBanner = {
  backgroundColor: '#f8d7da', border: '1px solid #f5c2c7', borderLeft: '4px solid #dc3545',
  borderRadius: '4px', padding: '0.6rem 1rem', marginBottom: '1rem',
  color: '#842029', fontSize: '0.875rem',
};
const actionBar = {
  display: 'flex', gap: '0.75rem', justifyContent: 'center', marginTop: '1.25rem',
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

const Field = ({ label, value }) => (
  <div>
    <div style={fieldLabel}>{label}</div>
    <div style={fieldValue}>{value || ''}</div>
  </div>
);

export const MediCalEligibilityModal = ({
  eligibilityData,   // OM response object from SCIService
  applicantData,     // { lastName, firstName, gender, dob } for demographic check
  applicationId,
  onSelectSuccess,   // fn({ cin, mediCalStatus, aidCode }) — CIN confirmed
  onMismatch,        // fn() — demographic mismatch detected
  onCancel,          // fn() — return to CIN search results
}) => {
  const [selecting, setSelecting] = useState(false);
  const [error, setError]         = useState('');

  const handleSelect = async () => {
    setSelecting(true);
    setError('');
    try {
      // Demographic comparison + CIN-in-use check via backend
      const res = await http.post(
        `/applications/${applicationId}/select-cin`,
        { ...eligibilityData, cin: eligibilityData.cin }
      );
      const { result, errorCode, message } = res.data;

      if (result === 'MISMATCH') {
        onMismatch();
        return;
      }
      if (result === 'CIN_IN_USE') {
        // EM-202 / EM-203
        setError(`${errorCode}: ${message}`);
        return;
      }
      if (result === 'SUCCESS') {
        onSelectSuccess({
          cin:         eligibilityData.cin,
          mediCalStatus: res.data.mediCalStatus,
          aidCode:     eligibilityData.aidCode,
        });
      }
    } catch (err) {
      setError(err?.response?.data?.error || 'An error occurred. Please try again.');
    } finally {
      setSelecting(false);
    }
  };

  const isActive = eligibilityData?.mediCalActive === true;

  return (
    <div style={overlay}>
      <div style={box}>
        <h2 style={{ color: '#153554', fontSize: '1.2rem', fontWeight: 700, marginBottom: '1rem',
                     borderBottom: '2px solid #153554', paddingBottom: '0.5rem' }}>
          Medi-Cal Eligibility Information
        </h2>

        {error && <div style={errBanner}>{error}</div>}

        {/* Demographics */}
        <p style={sectionHeader}>Demographic Information</p>
        <div style={grid}>
          <Field label="CIN"        value={eligibilityData?.cin} />
          <Field label="SSN"        value={eligibilityData?.ssn} />
          <Field label="Last Name"  value={eligibilityData?.lastName} />
          <Field label="First Name" value={eligibilityData?.firstName} />
          <Field label="Suffix"     value={eligibilityData?.suffix} />
          <Field label="Gender"     value={eligibilityData?.gender} />
          <Field label="Date of Birth" value={eligibilityData?.dob} />
          <Field label="County"     value={eligibilityData?.county} />
        </div>

        {/* Medi-Cal Eligibility */}
        <p style={sectionHeader}>Medi-Cal Eligibility (OM Response)</p>
        <div style={grid}>
          <div>
            <div style={fieldLabel}>Status</div>
            <div style={{ ...fieldValue, background: 'none', border: 'none', padding: '2px 0' }}>
              <span style={isActive ? activeTag : inactiveTag}>
                {isActive ? 'ACTIVE' : 'INACTIVE'}
              </span>
            </div>
          </div>
          <Field label="Aid Code"       value={eligibilityData?.aidCode} />
          <Field label="Effective Date" value={eligibilityData?.effectiveDate} />
          <Field label="End Date"       value={eligibilityData?.endDate} />
        </div>

        <div style={actionBar}>
          <button style={btnPrimary} onClick={handleSelect} disabled={selecting}>
            {selecting ? 'Selecting...' : 'Select'}
          </button>
          <button style={btnSecondary} onClick={onCancel}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
