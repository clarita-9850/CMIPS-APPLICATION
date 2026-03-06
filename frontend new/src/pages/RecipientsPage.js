import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import http from '../api/httpClient';
import './WorkQueues.css';

export const RecipientsPage = () => {
  const navigate = useNavigate();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const [lastName, setLastName] = useState('');
  const [firstName, setFirstName] = useState('');
  const [ssn, setSsn] = useState('');
  const [cin, setCin] = useState('');
  const [dob, setDob] = useState('');
  const [countyCode, setCountyCode] = useState('');
  const [personType, setPersonType] = useState('');

  const doSearch = () => {
    setLoading(true);
    setSearched(true);
    const params = {};
    if (lastName) params.lastName = lastName;
    if (firstName) params.firstName = firstName;
    if (ssn) params.ssn = ssn;
    if (cin) params.cin = cin;
    if (countyCode) params.countyCode = countyCode;
    if (personType) params.personType = personType;
    const qs = new URLSearchParams(params).toString();
    http.get(`/recipients/search?${qs}`)
      .then(res => {
        const d = res?.data;
        setResults(Array.isArray(d) ? d : (d?.content || d?.items || []));
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  };

  const handleClear = () => {
    setLastName(''); setFirstName(''); setSsn(''); setCin('');
    setDob(''); setCountyCode(''); setPersonType('');
    setResults([]); setSearched(false);
  };

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Person Search</h2>
        <div>
          <button className="wq-btn wq-btn-primary" style={{ marginRight: '0.5rem' }}
            onClick={() => navigate('/recipients/new')}>New Referral</button>
          <button className="wq-btn wq-btn-outline"
            onClick={() => navigate('/applications/new')}>New Registration</button>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Last Name</label>
              <input type="text" value={lastName} onChange={e => setLastName(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && doSearch()} />
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
              <label>CIN</label>
              <input type="text" value={cin} onChange={e => setCin(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Date of Birth</label>
              <input type="date" value={dob} onChange={e => setDob(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>County</label>
              <input type="text" value={countyCode} onChange={e => setCountyCode(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Person Type</label>
              <select value={personType} onChange={e => setPersonType(e.target.value)}>
                <option value="">All</option>
                <option value="RECIPIENT">Recipient</option>
                <option value="PROVIDER">Provider</option>
                <option value="OTHER">Other</option>
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
              <p className="wq-empty">No persons found matching your criteria.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Name</th><th>CIN</th><th>SSN</th><th>Date of Birth</th><th>County</th><th>Person Type</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {results.map((r, i) => (
                    <tr key={r.id || i} className="wq-clickable-row" onClick={() => navigate(`/recipients/${r.id}`)}>
                      <td><button className="action-link">{[r.lastName, r.firstName].filter(Boolean).join(', ') || r.name || r.id}</button></td>
                      <td>{r.cin || '\u2014'}</td>
                      <td>{maskSsn(r.ssn)}</td>
                      <td>{r.dateOfBirth ? new Date(r.dateOfBirth).toLocaleDateString() : '\u2014'}</td>
                      <td>{r.countyCode || '\u2014'}</td>
                      <td>{r.personType || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(r.status || '').toLowerCase()}`}>{r.status || '\u2014'}</span></td>
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
