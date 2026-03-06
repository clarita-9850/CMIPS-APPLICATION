import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import http from '../api/httpClient';
import './WorkQueues.css';

export const CasesPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialQuery = searchParams.get('search') || '';

  const [cases, setCases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const [caseNumber, setCaseNumber] = useState(initialQuery);
  const [cin, setCin] = useState('');
  const [status, setStatus] = useState('');
  const [countyCode, setCountyCode] = useState('');

  const doSearch = () => {
    setLoading(true);
    setSearched(true);
    const params = {};
    if (caseNumber) params.caseNumber = caseNumber;
    if (cin) params.cin = cin;
    if (status) params.status = status;
    if (countyCode) params.countyCode = countyCode;
    const qs = new URLSearchParams(params).toString();
    const url = qs ? `/cases/search?${qs}` : '/cases';
    http.get(url)
      .then(res => {
        const d = res?.data;
        setCases(Array.isArray(d) ? d : (d?.content || d?.items || []));
      })
      .catch(() => setCases([]))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    if (initialQuery) doSearch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleClear = () => {
    setCaseNumber(''); setCin(''); setStatus(''); setCountyCode('');
    setCases([]); setSearched(false);
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Case Search</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => navigate('/cases/new')}>New Case</button>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Case Number</label>
              <input type="text" value={caseNumber} onChange={e => setCaseNumber(e.target.value)}
                placeholder="Enter case number" onKeyDown={e => e.key === 'Enter' && doSearch()} />
            </div>
            <div className="wq-form-field">
              <label>CIN</label>
              <input type="text" value={cin} onChange={e => setCin(e.target.value)} placeholder="Client Index Number" />
            </div>
            <div className="wq-form-field">
              <label>Status</label>
              <select value={status} onChange={e => setStatus(e.target.value)}>
                <option value="">All</option>
                <option value="PENDING">Pending</option>
                <option value="ACTIVE">Active</option>
                <option value="APPROVED">Approved</option>
                <option value="DENIED">Denied</option>
                <option value="TERMINATED">Terminated</option>
                <option value="ON_LEAVE">On Leave</option>
                <option value="WITHDRAWN">Withdrawn</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>County</label>
              <input type="text" value={countyCode} onChange={e => setCountyCode(e.target.value)} placeholder="County code" />
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={doSearch} disabled={loading}>
              {loading ? 'Searching...' : 'Search'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleClear}>Clear</button>
          </div>
        </div>
      </div>

      {searched && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Search Results ({cases.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {cases.length === 0 ? (
              <p className="wq-empty">No cases found matching your criteria.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Case Number</th>
                    <th>Recipient Name</th>
                    <th>County</th>
                    <th>Status</th>
                    <th>Case Owner</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {cases.map((c, i) => (
                    <tr key={c.id || i} className="wq-clickable-row" onClick={() => navigate(`/cases/${c.id}`)}>
                      <td><button className="action-link">{c.caseNumber || c.id}</button></td>
                      <td>{c.recipientName || c.clientName || '\u2014'}</td>
                      <td>{c.countyCode || c.county || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(c.status || '').toLowerCase()}`}>{c.status || '\u2014'}</span></td>
                      <td>{c.caseOwnerId || c.assignedTo || '\u2014'}</td>
                      <td>{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '\u2014'}</td>
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
