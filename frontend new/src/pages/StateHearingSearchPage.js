import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const StateHearingSearchPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [caseNumber, setCaseNumber] = useState('');
  const [recipientName, setRecipientName] = useState('');
  const [countyCode, setCountyCode] = useState('');
  const [hearingDate, setHearingDate] = useState('');
  const [status, setStatus] = useState('');
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Cases', path: '/cases' }, { label: 'State Hearing Search' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = () => {
    setLoading(true);
    setSearched(true);
    const params = {};
    if (caseNumber) params.caseNumber = caseNumber;
    if (recipientName) params.recipientName = recipientName;
    if (countyCode) params.countyCode = countyCode;
    if (hearingDate) params.hearingDate = hearingDate;
    if (status) params.status = status;
    const qs = new URLSearchParams(params).toString();
    http.get(`/cases/search?${qs}`)
      .then(res => {
        const d = res?.data;
        setResults(Array.isArray(d) ? d : (d?.content || []));
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  };

  const handleReset = () => {
    setCaseNumber('');
    setRecipientName('');
    setCountyCode('');
    setHearingDate('');
    setStatus('');
    setResults([]);
    setSearched(false);
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>State Hearing Search</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Back to Cases</button>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Case Number</label>
              <input type="text" value={caseNumber} onChange={e => setCaseNumber(e.target.value)} placeholder="Enter case number" />
            </div>
            <div className="wq-form-field">
              <label>Recipient Name</label>
              <input type="text" value={recipientName} onChange={e => setRecipientName(e.target.value)} placeholder="Last name, First name" />
            </div>
            <div className="wq-form-field">
              <label>County Code</label>
              <input type="text" value={countyCode} onChange={e => setCountyCode(e.target.value)} placeholder="e.g. 19" />
            </div>
            <div className="wq-form-field">
              <label>Hearing Date</label>
              <input type="date" value={hearingDate} onChange={e => setHearingDate(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Status</label>
              <select value={status} onChange={e => setStatus(e.target.value)}>
                <option value="">All Statuses</option>
                <option value="PENDING">Pending</option>
                <option value="SCHEDULED">Scheduled</option>
                <option value="COMPLETED">Completed</option>
                <option value="WITHDRAWN">Withdrawn</option>
                <option value="DISMISSED">Dismissed</option>
              </select>
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
              {loading ? 'Searching...' : 'Search'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleReset}>Reset</button>
          </div>
        </div>
      </div>

      {searched && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Results ({results.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? (
              <p style={{ padding: '1rem', color: '#888' }}>Searching...</p>
            ) : results.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No state hearing records found.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Case Number</th>
                    <th>Recipient</th>
                    <th>County</th>
                    <th>Status</th>
                    <th>Hearing Date</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((r, i) => (
                    <tr key={i} className="wq-clickable-row" onClick={() => navigate(`/cases/${r.caseId || r.id}`)}>
                      <td>{r.caseNumber || '\u2014'}</td>
                      <td>{r.recipientName || r.clientName || '\u2014'}</td>
                      <td>{r.countyCode || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(r.caseStatus || r.status || '').toLowerCase()}`}>{r.caseStatus || r.status || '\u2014'}</span></td>
                      <td>{r.hearingDate || r.nextReviewDate || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
