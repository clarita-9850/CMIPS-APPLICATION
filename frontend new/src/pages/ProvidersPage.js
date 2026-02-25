import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import http from '../api/httpClient';
import './WorkQueues.css';

export const ProvidersPage = () => {
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const [providerNumber, setProviderNumber] = useState('');
  const [lastName, setLastName] = useState('');
  const [firstName, setFirstName] = useState('');
  const [ssn, setSsn] = useState('');
  const [countyCode, setCountyCode] = useState('');
  const [status, setStatus] = useState('');

  const doSearch = () => {
    setLoading(true);
    setSearched(true);
    const params = {};
    if (providerNumber) params.providerNumber = providerNumber;
    if (lastName) params.lastName = lastName;
    if (firstName) params.firstName = firstName;
    if (ssn) params.ssn = ssn;
    if (countyCode) params.countyCode = countyCode;
    const qs = new URLSearchParams(params).toString();
    const url = qs ? `/providers/search?${qs}` : '/providers';
    http.get(url)
      .then(res => {
        const d = res?.data;
        setResults(Array.isArray(d) ? d : (d?.content || d?.items || []));
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  };

  const handleClear = () => {
    setProviderNumber(''); setLastName(''); setFirstName('');
    setSsn(''); setCountyCode(''); setStatus('');
    setResults([]); setSearched(false);
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Provider Search</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => navigate('/providers/register')}>Register Provider</button>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Provider Number</label>
              <input type="text" value={providerNumber} onChange={e => setProviderNumber(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && doSearch()} />
            </div>
            <div className="wq-form-field">
              <label>Last Name</label>
              <input type="text" value={lastName} onChange={e => setLastName(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>First Name</label>
              <input type="text" value={firstName} onChange={e => setFirstName(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>SSN</label>
              <input type="text" value={ssn} onChange={e => setSsn(e.target.value)} placeholder="###-##-####" />
            </div>
            <div className="wq-form-field">
              <label>County</label>
              <input type="text" value={countyCode} onChange={e => setCountyCode(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Status</label>
              <select value={status} onChange={e => setStatus(e.target.value)}>
                <option value="">All</option>
                <option value="PENDING">Pending</option>
                <option value="ACTIVE">Active</option>
                <option value="INELIGIBLE">Ineligible</option>
                <option value="TERMINATED">Terminated</option>
              </select>
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
          <div className="wq-panel-header"><h4>Results ({results.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {results.length === 0 ? (
              <p className="wq-empty">No providers found matching your criteria.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Provider Number</th><th>Name</th><th>County</th><th>Status</th><th>Phone</th><th>Enrolled</th></tr>
                </thead>
                <tbody>
                  {results.map((p, i) => (
                    <tr key={p.id || i} className="wq-clickable-row" onClick={() => navigate(`/providers/${p.id}`)}>
                      <td><button className="action-link">{p.providerNumber || p.id}</button></td>
                      <td>{[p.lastName, p.firstName].filter(Boolean).join(', ') || p.name || '\u2014'}</td>
                      <td>{p.countyCode || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(p.status || '').toLowerCase()}`}>{p.status || '\u2014'}</span></td>
                      <td>{p.phone || p.phoneNumber || '\u2014'}</td>
                      <td>{p.enrollmentDate ? new Date(p.enrollmentDate).toLocaleDateString() : '\u2014'}</td>
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
