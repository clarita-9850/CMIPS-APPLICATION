/**
 * UserSearchModal
 * Implements DSD CI-67746 — User Search pop-up for Case Owner Assignment.
 *
 * Opens from Create Case / Case Home when Assigned Worker lookup icon is clicked.
 * Searches workers via GET /api/users/search and displays results in a 7-column table.
 * Selecting a worker populates the Assigned Worker field on the parent form.
 *
 * Search Criteria (9 fields per DSD page 64-65):
 *   Worker Number, Username, First Name, Last Name, District Office, Unit,
 *   ZIP Code, Position Name, Language
 *
 * Results Columns (7 per DSD page 65):
 *   First Name, Last Name, Worker Number, District Office, Language 1, Language 2, Case Count
 */

import React, { useState } from 'react';
import * as usersApi from '../api/usersApi';

const modalOverlay = {
  position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.45)',
  zIndex: 2000, display: 'flex', alignItems: 'center', justifyContent: 'center',
};
const modalBox = {
  backgroundColor: '#fff', borderRadius: '6px', padding: '1.5rem',
  width: '90%', maxWidth: '950px', maxHeight: '90vh', overflowY: 'auto',
  boxShadow: '0 8px 32px rgba(0,0,0,0.25)',
};
const gridStyle = {
  display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
  gap: '0.6rem 1rem', marginBottom: '0.75rem',
};
const fieldBox = { display: 'flex', flexDirection: 'column' };
const labelStyle = { fontSize: '0.75rem', color: '#333', marginBottom: '2px', fontWeight: 600 };
const inputStyle = {
  padding: '0.35rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '3px', fontSize: '0.85rem',
};
const selectStyle = { ...inputStyle };
const tableStyle = { width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' };
const thStyle = {
  backgroundColor: '#153554', color: '#fff', padding: '6px 10px',
  textAlign: 'left', fontWeight: 600, whiteSpace: 'nowrap',
};
const tdStyle = { padding: '6px 10px', borderBottom: '1px solid #e2e8f0' };
const selectLink = {
  background: 'none', border: 'none', color: '#153554', textDecoration: 'underline',
  cursor: 'pointer', padding: 0, fontSize: '0.85rem', fontWeight: 600,
};
const actionBar = {
  display: 'flex', gap: '0.75rem', justifyContent: 'center', marginTop: '1rem',
};
const errBanner = {
  backgroundColor: '#f8d7da', border: '1px solid #f5c2c7', borderLeft: '4px solid #dc3545',
  borderRadius: '4px', padding: '0.6rem 1rem', marginBottom: '0.75rem',
  color: '#842029', fontSize: '0.875rem',
};

const DISTRICT_OFFICES = [
  '', 'Sacramento', 'Los Angeles', 'San Francisco', 'San Diego', 'Fresno',
  'Alameda', 'Contra Costa', 'Orange', 'Riverside', 'San Bernardino',
  'Santa Clara', 'Kern', 'Stanislaus', 'Ventura',
];

const POSITIONS = ['', 'CASE_WORKER', 'SUPERVISOR', 'MANAGER'];

const LANGUAGES = ['', 'English', 'Spanish', 'Chinese', 'Vietnamese', 'Korean',
  'Tagalog', 'Armenian', 'Cantonese', 'Mandarin'];

/**
 * @param {Function} onSelect — called with the selected worker object
 * @param {Function} onCancel — closes the modal
 */
export const UserSearchModal = ({ onSelect, onCancel }) => {
  const [criteria, setCriteria] = useState({
    workerNumber: '', username: '', firstName: '', lastName: '',
    districtOffice: '', unit: '', zipCode: '', position: '', language: '',
  });

  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (field, value) => {
    setCriteria(prev => ({ ...prev, [field]: value }));
  };

  const handleSearch = async () => {
    // Must have at least one criterion
    const hasAnyCriteria = Object.values(criteria).some(v => v.trim());
    if (!hasAnyCriteria) {
      setError('Please enter at least one search criterion.');
      return;
    }
    setError('');
    setLoading(true);
    setSearched(true);
    try {
      const data = await usersApi.searchUsers(criteria);
      setResults(data || []);
    } catch (err) {
      console.error('[UserSearchModal] search failed:', err);
      setError(err?.response?.data?.error || err?.message || 'Search failed');
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setCriteria({
      workerNumber: '', username: '', firstName: '', lastName: '',
      districtOffice: '', unit: '', zipCode: '', position: '', language: '',
    });
    setResults([]);
    setSearched(false);
    setError('');
  };

  const handleSelect = (worker) => {
    onSelect(worker);
  };

  return (
    <div style={modalOverlay} onClick={onCancel}>
      <div style={modalBox} onClick={e => e.stopPropagation()}>
        <h3 style={{ marginTop: 0, marginBottom: '0.75rem', color: '#153554' }}>
          User Search — Assign Case Owner
        </h3>
        <p style={{ fontSize: '0.825rem', color: '#4a5568', margin: '0 0 1rem 0' }}>
          Search for a worker to assign as case owner. Results are limited to workers in your county.
        </p>

        {error && <div style={errBanner}>{error}</div>}

        {/* ── Search Criteria ── */}
        <div style={{ background: '#f7f9fb', border: '1px solid #e2e8f0', borderRadius: '4px', padding: '0.75rem', marginBottom: '0.75rem' }}>
          <div style={gridStyle}>
            <div style={fieldBox}>
              <label style={labelStyle}>Worker Number</label>
              <input style={inputStyle} type="text" value={criteria.workerNumber}
                onChange={e => handleChange('workerNumber', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()} />
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>Username</label>
              <input style={inputStyle} type="text" value={criteria.username}
                onChange={e => handleChange('username', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()} />
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>First Name</label>
              <input style={inputStyle} type="text" value={criteria.firstName}
                onChange={e => handleChange('firstName', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()} />
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>Last Name</label>
              <input style={inputStyle} type="text" value={criteria.lastName}
                onChange={e => handleChange('lastName', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()} />
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>District Office</label>
              <select style={selectStyle} value={criteria.districtOffice}
                onChange={e => handleChange('districtOffice', e.target.value)}>
                {DISTRICT_OFFICES.map(o => <option key={o} value={o}>{o || '(All)'}</option>)}
              </select>
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>Unit</label>
              <input style={inputStyle} type="text" value={criteria.unit}
                onChange={e => handleChange('unit', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()} />
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>ZIP Code</label>
              <input style={inputStyle} type="text" value={criteria.zipCode}
                onChange={e => handleChange('zipCode', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSearch()} />
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>Position Name</label>
              <select style={selectStyle} value={criteria.position}
                onChange={e => handleChange('position', e.target.value)}>
                {POSITIONS.map(p => <option key={p} value={p}>{p || '(All)'}</option>)}
              </select>
            </div>
            <div style={fieldBox}>
              <label style={labelStyle}>Language</label>
              <select style={selectStyle} value={criteria.language}
                onChange={e => handleChange('language', e.target.value)}>
                {LANGUAGES.map(l => <option key={l} value={l}>{l || '(All)'}</option>)}
              </select>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}
              style={{ padding: '0.35rem 1rem', fontSize: '0.85rem' }}>
              {loading ? 'Searching...' : 'Search'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleReset}
              style={{ padding: '0.35rem 1rem', fontSize: '0.85rem' }}>
              Reset
            </button>
          </div>
        </div>

        {/* ── Results ── */}
        {searched && (
          <div>
            <h4 style={{ margin: '0.5rem 0', fontSize: '0.9rem', color: '#153554' }}>
              Search Results {results.length > 0 ? `(${results.length})` : ''}
            </h4>
            {results.length === 0 ? (
              <div style={{ padding: '1.5rem', textAlign: 'center', color: '#4a5568', fontSize: '0.9rem',
                background: '#f7f9fb', border: '1px solid #e2e8f0', borderRadius: '4px' }}>
                No workers found matching the criteria.
              </div>
            ) : (
              <div style={{ overflowX: 'auto', border: '1px solid #e2e8f0', borderRadius: '4px' }}>
                <table style={tableStyle}>
                  <thead>
                    <tr>
                      <th style={thStyle}>First Name</th>
                      <th style={thStyle}>Last Name</th>
                      <th style={thStyle}>Worker Number</th>
                      <th style={thStyle}>District Office</th>
                      <th style={thStyle}>Language 1</th>
                      <th style={thStyle}>Language 2</th>
                      <th style={thStyle}>Case Count</th>
                      <th style={thStyle}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {results.map((w, i) => (
                      <tr key={w.workerNumber || i} style={{ backgroundColor: i % 2 === 0 ? '#fff' : '#f7f9fb' }}>
                        <td style={tdStyle}>{w.firstName}</td>
                        <td style={tdStyle}>{w.lastName}</td>
                        <td style={{ ...tdStyle, fontFamily: 'monospace' }}>{w.workerNumber}</td>
                        <td style={tdStyle}>{w.districtOffice}</td>
                        <td style={tdStyle}>{w.language || ''}</td>
                        <td style={tdStyle}>{w.language2 || ''}</td>
                        <td style={{ ...tdStyle, textAlign: 'center' }}>{w.caseCount || '—'}</td>
                        <td style={tdStyle}>
                          <button style={selectLink} onClick={() => handleSelect(w)}>Select</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* ── Cancel ── */}
        <div style={actionBar}>
          <button className="wq-btn wq-btn-outline" onClick={onCancel}
            style={{ padding: '0.4rem 1.5rem' }}>
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};
