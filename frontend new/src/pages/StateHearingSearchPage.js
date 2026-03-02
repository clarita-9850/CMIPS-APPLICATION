import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import { useAuth } from '../auth/AuthContext';
import http from '../api/httpClient';
import './WorkQueues.css';

/**
 * State Hearing Search — DSD Section 20 CI-67779
 *
 * Search Criteria (per DSD Screen Design p.130):
 *   1. State Hearing Status (dropdown, required, default: "Scheduled")
 *   2. County (dropdown, required, default: user's county)
 *   3. Hearing Request From Date (date, required)
 *   4. Hearing Request To Date (date, optional — auto: +6 months from From Date)
 *
 * Search Results (3 columns per DSD p.131):
 *   - Case Number → clickable → Case Home
 *   - Recipient Name
 *   - State Hearing Status
 *
 * DSD Rules:
 *   - Search limited to 6-month period from "From Date"
 *   - If no To Date → auto-calculate 6 months from From Date
 *   - Limited to user's county (statewide users can select county)
 */

const COUNTIES = [
  'Alameda','Alpine','Amador','Butte','Calaveras','Colusa','Contra Costa','Del Norte',
  'El Dorado','Fresno','Glenn','Humboldt','Imperial','Inyo','Kern','Kings','Lake','Lassen',
  'Los Angeles','Madera','Marin','Mariposa','Mendocino','Merced','Modoc','Mono','Monterey',
  'Napa','Nevada','Orange','Placer','Plumas','Riverside','Sacramento','San Benito',
  'San Bernardino','San Diego','San Francisco','San Joaquin','San Luis Obispo','San Mateo',
  'Santa Barbara','Santa Clara','Santa Cruz','Shasta','Sierra','Siskiyou','Solano','Sonoma',
  'Stanislaus','Sutter','Tehama','Trinity','Tulare','Tuolumne','Ventura','Yolo','Yuba'
];

// DSD Code Table: Search State Hearing Status (CI-68051)
const STATE_HEARING_STATUSES = [
  { code: 'SSHS002', label: 'Scheduled' },
  { code: 'SSHS001', label: 'Requested' },
  { code: 'SSHS003', label: 'Resolved' },
  { code: 'SSHS004', label: 'Requested And Scheduled' },
];

// Status badge colors per status
const STATUS_BADGE = {
  REQUESTED:  { bg: '#feebc8', color: '#c05621' },
  SCHEDULED:  { bg: '#bee3f8', color: '#2b6cb0' },
  RESOLVED:   { bg: '#c6f6d5', color: '#276749' },
};

export const StateHearingSearchPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const { user, roles } = useAuth();

  // DSD defaults: status = "Scheduled" (SSHS002), county = user's county
  const userCounty = user?.county || 'Sacramento';
  const hasStatewideAccess = roles?.some(r =>
    r.includes('ADMIN') || r.includes('STATE') || r.includes('SUPERVISOR')
  );

  const [stateHearingStatus, setStateHearingStatus] = useState('SSHS002');
  const [county, setCounty] = useState(userCounty);
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    setBreadcrumbs([
      { label: 'My Workspace', path: '/workspace' },
      { label: 'State Hearing Search' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const validate = () => {
    const e = {};
    if (!stateHearingStatus) e.stateHearingStatus = 'State Hearing Status is required';
    if (!county) e.county = 'County is required';
    if (!fromDate) e.fromDate = 'Hearing Request From Date is required';
    if (fromDate && toDate && toDate < fromDate) {
      e.toDate = 'To Date cannot be before From Date';
    }
    // DSD: 6-month max period
    if (fromDate && toDate) {
      const from = new Date(fromDate);
      const maxTo = new Date(from);
      maxTo.setMonth(maxTo.getMonth() + 6);
      if (new Date(toDate) > maxTo) {
        e.toDate = 'Search period cannot exceed 6 months';
      }
    }
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSearch = () => {
    if (!validate()) return;

    setLoading(true);
    setSearched(true);

    const params = new URLSearchParams({
      stateHearingStatus,
      countyCode: county,
      fromDate,
    });
    if (toDate) params.set('toDate', toDate);

    http.get(`/state-hearings/search?${params.toString()}`)
      .then(res => {
        const d = res?.data;
        setResults(Array.isArray(d) ? d : []);
      })
      .catch(err => {
        const msg = err?.response?.data?.error || 'Search failed';
        setErrors({ api: msg });
        setResults([]);
      })
      .finally(() => setLoading(false));
  };

  const handleReset = () => {
    setStateHearingStatus('SSHS002');
    setCounty(userCounty);
    setFromDate('');
    setToDate('');
    setResults([]);
    setSearched(false);
    setErrors({});
  };

  const errStyle = { color: '#e53e3e', fontSize: '0.8rem', marginLeft: 8 };
  const reqMark = <span style={{ color: '#e53e3e' }}>*</span>;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>State Hearing Search</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/workspace')}>
          Back to Workspace
        </button>
      </div>

      {/* Search Criteria Panel — DSD CI-67779 */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          {errors.api && (
            <div style={{ background: '#fed7d7', color: '#c53030', padding: '0.75rem 1rem',
              borderRadius: 4, marginBottom: '1rem', fontSize: '0.9rem' }}>
              {errors.api}
            </div>
          )}

          <div className="wq-search-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>

            {/* Row 1: State Hearing Status + County */}
            <div className="wq-form-field">
              <label>State Hearing Status {reqMark}
                {errors.stateHearingStatus && <span style={errStyle}>{errors.stateHearingStatus}</span>}
              </label>
              <select value={stateHearingStatus} onChange={e => setStateHearingStatus(e.target.value)}>
                {STATE_HEARING_STATUSES.map(s => (
                  <option key={s.code} value={s.code}>{s.label}</option>
                ))}
              </select>
            </div>

            <div className="wq-form-field">
              <label>County {reqMark}
                {errors.county && <span style={errStyle}>{errors.county}</span>}
              </label>
              {hasStatewideAccess ? (
                <select value={county} onChange={e => setCounty(e.target.value)}>
                  <option value="">Select County...</option>
                  {COUNTIES.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              ) : (
                <input type="text" value={county} readOnly
                  style={{ backgroundColor: '#f7fafc', cursor: 'not-allowed' }}
                  title="County is set from your user profile"
                />
              )}
            </div>

            {/* Row 2: From Date + To Date */}
            <div className="wq-form-field">
              <label>Hearing Request From Date {reqMark}
                {errors.fromDate && <span style={errStyle}>{errors.fromDate}</span>}
              </label>
              <input type="date" value={fromDate} onChange={e => setFromDate(e.target.value)} />
            </div>

            <div className="wq-form-field">
              <label>Hearing Request To Date
                {errors.toDate && <span style={errStyle}>{errors.toDate}</span>}
              </label>
              <input type="date" value={toDate} onChange={e => setToDate(e.target.value)}
                placeholder="Auto: 6 months from From Date"
              />
              {!toDate && fromDate && (
                <span style={{ fontSize: '0.8rem', color: '#718096', marginTop: 4 }}>
                  If blank, defaults to {new Date(new Date(fromDate).setMonth(new Date(fromDate).getMonth() + 6))
                    .toLocaleDateString('en-US', { month: '2-digit', day: '2-digit', year: 'numeric' })}
                </span>
              )}
            </div>
          </div>

          {/* Actions: Search + Reset */}
          <div className="wq-search-actions" style={{ marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
              {loading ? 'Searching...' : 'Search'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleReset} style={{ marginLeft: 8 }}>
              Reset
            </button>
          </div>
        </div>
      </div>

      {/* Search Results — DSD: 3 columns (Case Number, Recipient Name, State Hearing Status) */}
      {searched && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>State Hearing Search Results ({results.length})</h4>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? (
              <p style={{ padding: '1rem', color: '#888' }}>Searching...</p>
            ) : results.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No state hearing records found matching the search criteria.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Case Number</th>
                    <th>Recipient Name</th>
                    <th>State Hearing Status</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((r, i) => {
                    const statusBadge = STATUS_BADGE[r.stateHearingStatus] || STATUS_BADGE.REQUESTED;
                    return (
                      <tr key={r.id || i}>
                        {/* Case Number → clickable → Case Home */}
                        <td>
                          <button
                            onClick={() => navigate(`/cases/${r.caseId}`)}
                            style={{
                              background: 'none', border: 'none', color: '#2b6cb0',
                              textDecoration: 'underline', cursor: 'pointer', padding: 0,
                              fontSize: 'inherit', fontFamily: 'inherit'
                            }}
                          >
                            {r.caseNumber || '\u2014'}
                          </button>
                        </td>
                        {/* Recipient Name */}
                        <td>
                          <button
                            onClick={() => r.recipientId && navigate(`/recipients/${r.recipientId}`)}
                            style={{
                              background: 'none', border: 'none', color: '#2b6cb0',
                              textDecoration: 'underline', cursor: r.recipientId ? 'pointer' : 'default',
                              padding: 0, fontSize: 'inherit', fontFamily: 'inherit'
                            }}
                          >
                            {r.recipientName || '\u2014'}
                          </button>
                        </td>
                        {/* State Hearing Status */}
                        <td>
                          <span style={{
                            display: 'inline-block',
                            padding: '2px 10px',
                            borderRadius: 12,
                            fontWeight: 600,
                            fontSize: '0.85rem',
                            backgroundColor: statusBadge.bg,
                            color: statusBadge.color,
                          }}>
                            {r.stateHearingStatusDisplay || r.stateHearingStatus || '\u2014'}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
