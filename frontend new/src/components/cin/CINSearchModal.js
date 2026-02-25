/**
 * CINSearchModal
 * Implements the "Statewide Client Index – CIN Search" pop-up (CI-67768).
 *
 * Flow:
 *  1. Opens with applicant demographics pre-populated (read-only)
 *  2. Calls GET /api/sci/search (OI transaction, BR 32/33)
 *  3. Shows results (EM-186 if matches found, "CIN does not exist" if none)
 *  4. User clicks "MEDS Eligibility" on a row → calls GET /api/sci/meds-eligibility (EL/OM)
 *  5. MediCalEligibilityModal opens (handled by parent via onShowEligibility)
 *  6. Cancel returns to Create Case
 */

import React, { useState } from 'react';
import http from '../../api/httpClient';

const modalOverlay = {
  position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.45)',
  zIndex: 2000, display: 'flex', alignItems: 'center', justifyContent: 'center',
};
const modalBox = {
  backgroundColor: '#fff', borderRadius: '6px', padding: '1.5rem',
  width: '90%', maxWidth: '900px', maxHeight: '90vh', overflowY: 'auto',
  boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
};
const fieldRow = { display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '0.5rem' };
const fieldBox = { display: 'flex', flexDirection: 'column', minWidth: '140px' };
const labelStyle = { fontSize: '0.75rem', color: '#666', marginBottom: '2px', fontWeight: 600 };
const valueStyle = {
  fontSize: '0.875rem', backgroundColor: '#f0f4f8', border: '1px solid #d0d3d6',
  padding: '4px 8px', borderRadius: '3px', minHeight: '28px',
};
const em186Banner = {
  backgroundColor: '#fff3cd', border: '1px solid #ffc107', borderLeft: '4px solid #856404',
  borderRadius: '4px', padding: '0.6rem 1rem', marginBottom: '1rem',
  color: '#856404', fontSize: '0.875rem',
};
const noMatchBanner = {
  backgroundColor: '#f8d7da', border: '1px solid #f5c2c7', borderLeft: '4px solid #842029',
  borderRadius: '4px', padding: '0.6rem 1rem', marginBottom: '1rem',
  color: '#842029', fontSize: '0.875rem',
};
const errBanner = {
  backgroundColor: '#f8d7da', border: '1px solid #f5c2c7', borderLeft: '4px solid #dc3545',
  borderRadius: '4px', padding: '0.6rem 1rem', marginBottom: '1rem',
  color: '#842029', fontSize: '0.875rem',
};
const tableStyle = { width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' };
const thStyle = {
  backgroundColor: '#153554', color: '#fff', padding: '6px 10px',
  textAlign: 'left', fontWeight: 600, whiteSpace: 'nowrap',
};
const tdStyle = { padding: '6px 10px', borderBottom: '1px solid #e2e8f0' };
const linkBtn = {
  background: 'none', border: 'none', color: '#153554', textDecoration: 'underline',
  cursor: 'pointer', padding: 0, fontSize: '0.85rem',
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

export const CINSearchModal = ({
  applicantData,     // { lastName, firstName, dob, gender, cin, ssn, mediCalPseudo }
  applicationId,     // optional — used for CIN availability check
  onShowEligibility, // fn(cin, eligibilityData) — opens MediCalEligibilityModal
  onCancel,          // fn() — closes modal without selecting
}) => {
  const [loading,  setLoading]  = useState(false);
  const [elLoading, setElLoading] = useState(null); // which CIN is loading EL
  const [searched, setSearched] = useState(false);
  const [searchResult, setSearchResult] = useState(null);
  const [error,    setError]    = useState('');

  const handleSearch = async () => {
    setLoading(true);
    setError('');
    setSearchResult(null);
    try {
      const params = new URLSearchParams();
      if (applicantData.lastName)     params.set('lastName',     applicantData.lastName);
      if (applicantData.firstName)    params.set('firstName',    applicantData.firstName);
      if (applicantData.dob)          params.set('dob',          applicantData.dob);
      if (applicantData.gender)       params.set('gender',       applicantData.gender);
      if (applicantData.cin)          params.set('cin',          applicantData.cin);
      if (applicantData.ssn && !applicantData.mediCalPseudo) params.set('ssn', applicantData.ssn);
      if (applicantData.mediCalPseudo) params.set('mediCalPseudo', 'true');

      const res = await http.get(`/sci/search?${params}`);
      setSearchResult(res.data);
      setSearched(true);
    } catch (err) {
      setError(err?.response?.data?.error || 'SCI search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // EL Transaction: fetch Medi-Cal eligibility for a selected CIN row
  const handleCheckEligibility = async (cin) => {
    setElLoading(cin);
    setError('');
    try {
      const res = await http.get(`/sci/meds-eligibility?cin=${encodeURIComponent(cin)}`);
      if (res.data.status === 'FAILED') {
        // Scenario 3: EL/OM transaction failed — show error on THIS screen
        setError(`SCI transaction was not successful: ${res.data.message || ''}`);
        return;
      }
      // Scenario success: open eligibility modal in parent
      onShowEligibility(cin, res.data);
    } catch (err) {
      setError(err?.response?.data?.error || 'EL transaction failed. Please try again.');
    } finally {
      setElLoading(null);
    }
  };

  const results = searchResult?.results || [];
  const status  = searchResult?.status;
  const criteria = searchResult?.sentCriteria || {};

  return (
    <div style={modalOverlay} onClick={e => { if (e.target === e.currentTarget) onCancel(); }}>
      <div style={modalBox}>
        <h2 style={{ color: '#153554', fontSize: '1.25rem', fontWeight: 700, marginBottom: '1rem',
                     borderBottom: '2px solid #153554', paddingBottom: '0.5rem' }}>
          Statewide Client Index – CIN Search
        </h2>

        {/* Read-only search criteria (system-populated per BR 32/33) */}
        <section style={{ marginBottom: '1rem' }}>
          <h3 style={{ fontSize: '0.9rem', color: '#153554', fontWeight: 700, marginBottom: '0.5rem',
                       backgroundColor: '#e8edf2', padding: '4px 8px', borderRadius: '3px' }}>
            Search Criteria (System-Populated)
          </h3>
          <div style={fieldRow}>
            <div style={fieldBox}>
              <span style={labelStyle}>Last Name</span>
              <span style={valueStyle}>{applicantData.lastName || ''}</span>
            </div>
            <div style={fieldBox}>
              <span style={labelStyle}>First Name</span>
              <span style={valueStyle}>{applicantData.firstName || ''}</span>
            </div>
            <div style={fieldBox}>
              <span style={labelStyle}>Gender</span>
              <span style={valueStyle}>{applicantData.gender || ''}</span>
            </div>
            <div style={fieldBox}>
              <span style={labelStyle}>Date of Birth</span>
              <span style={valueStyle}>{applicantData.dob || ''}</span>
            </div>
            {applicantData.cin && (
              <div style={fieldBox}>
                <span style={labelStyle}>CIN</span>
                <span style={valueStyle}>{applicantData.cin}</span>
              </div>
            )}
            {applicantData.ssn && !applicantData.mediCalPseudo && (
              <div style={fieldBox}>
                <span style={labelStyle}>SSN</span>
                <span style={valueStyle}>{applicantData.ssn}</span>
              </div>
            )}
          </div>
          {searched && Object.keys(criteria).length > 0 && (
            <p style={{ fontSize: '0.75rem', color: '#666', marginTop: '0.25rem' }}>
              Criteria sent to SCI: {Object.entries(criteria).map(([k, v]) => `${k}=${v}`).join(', ')}
            </p>
          )}
        </section>

        {/* Error banner */}
        {error && <div style={errBanner}>{error}</div>}

        {/* Search action */}
        {!searched && (
          <div style={actionBar}>
            <button style={btnPrimary} onClick={handleSearch} disabled={loading}>
              {loading ? 'Searching SCI...' : 'Search SCI'}
            </button>
            <button style={btnSecondary} onClick={onCancel}>Cancel</button>
          </div>
        )}

        {/* Results */}
        {searched && status === 'NO_MATCH' && (
          <div style={noMatchBanner}>
            CIN does not exist for the applicant
          </div>
        )}

        {searched && status === 'MATCHES_FOUND' && (
          <>
            <div style={em186Banner}>
              {searchResult.message}
            </div>
            <section>
              <h3 style={{ fontSize: '0.9rem', color: '#153554', fontWeight: 700, marginBottom: '0.5rem',
                           backgroundColor: '#e8edf2', padding: '4px 8px', borderRadius: '3px' }}>
                Search Results
              </h3>
              <div style={{ overflowX: 'auto' }}>
                <table style={tableStyle}>
                  <thead>
                    <tr>
                      <th style={thStyle}>CIN</th>
                      <th style={thStyle}>SSN</th>
                      <th style={thStyle}>First Name</th>
                      <th style={thStyle}>Last Name</th>
                      <th style={thStyle}>Suffix</th>
                      <th style={thStyle}>Gender</th>
                      <th style={thStyle}>DOB</th>
                      <th style={thStyle}>MEDS Eligibility</th>
                    </tr>
                  </thead>
                  <tbody>
                    {results.map((row) => (
                      <tr key={row.cin} style={{ backgroundColor: '#fff' }}>
                        <td style={tdStyle}>{row.cin}</td>
                        <td style={tdStyle}>{row.ssn}</td>
                        <td style={tdStyle}>{row.firstName}</td>
                        <td style={tdStyle}>{row.lastName}</td>
                        <td style={tdStyle}>{row.suffix}</td>
                        <td style={tdStyle}>{row.gender}</td>
                        <td style={tdStyle}>{row.dob}</td>
                        <td style={tdStyle}>
                          <button
                            style={linkBtn}
                            disabled={elLoading === row.cin}
                            onClick={() => handleCheckEligibility(row.cin)}
                          >
                            {elLoading === row.cin ? 'Loading...' : 'MEDS Eligibility'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </section>
          </>
        )}

        {/* Bottom action bar */}
        <div style={actionBar}>
          {searched && (
            <button style={btnSecondary} onClick={() => { setSearched(false); setSearchResult(null); setError(''); }}>
              Search Again
            </button>
          )}
          <button style={btnSecondary} onClick={onCancel}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
