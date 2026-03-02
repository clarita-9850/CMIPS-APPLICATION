import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import * as recipientsApi from '../api/recipientsApi';
import './WorkQueues.css';

const PERSON_TYPE_STYLES = {
  OPEN_REFERRAL:   { background: '#bee3f8', color: '#2b6cb0' },
  CLOSED_REFERRAL: { background: '#e2e8f0', color: '#4a5568' },
  APPLICANT:       { background: '#feebc8', color: '#c05621' },
  RECIPIENT:       { background: '#c6f6d5', color: '#276749' },
};

const PersonTypeBadge = ({ type }) => {
  if (!type) return <span style={{ color: '#999' }}>—</span>;
  const style = PERSON_TYPE_STYLES[type] || { background: '#e2e8f0', color: '#4a5568' };
  const label = type.replace('_', ' ').replace(/\b\w/g, c => c.toUpperCase());
  return <span style={{ ...style, padding: '0.15rem 0.5rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600, whiteSpace: 'nowrap' }}>{label}</span>;
};

const PAGE_SIZE = 10;

/**
 * Person Search — Application Path (CI-67788)
 * "Select existing" → ApplicationsNewPage with pre-filled recipientId
 * "Create New Application" → ApplicationsNewPage blank
 */
export const PersonSearchApplicationPage = () => {
  const navigate = useNavigate();

  const [form, setForm] = useState({ lastName: '', firstName: '', dob: '', ssn: '', countyCode: '' });
  const [displaySsn, setDisplaySsn] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [page, setPage] = useState(0);

  const handleChange = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleSsnChange = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9);
    let masked = '';
    for (let i = 0; i < digits.length; i++) {
      masked += i < 5 ? 'X' : digits[i];
    }
    setDisplaySsn(masked);
    handleChange('ssn', digits);
  };

  const doSearch = async () => {
    if (!form.lastName.trim()) {
      alert('Last Name is required to search.');
      return;
    }
    setLoading(true);
    setSearched(true);
    setPage(0);
    try {
      const params = {};
      if (form.lastName)   params.lastName   = form.lastName;
      if (form.firstName)  params.firstName  = form.firstName;
      if (form.ssn)        params.ssn        = form.ssn;
      if (form.dob)        params.dob        = form.dob;
      if (form.countyCode) params.countyCode = form.countyCode;
      const data = await recipientsApi.searchRecipients(params);
      const arr = Array.isArray(data) ? data : (data?.content || []);
      setResults(arr);
    } catch {
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleClear = () => {
    setForm({ lastName: '', firstName: '', dob: '', ssn: '', countyCode: '' });
    setDisplaySsn('');
    setResults([]);
    setSearched(false);
  };

  const totalPages = Math.ceil(results.length / PAGE_SIZE);
  const paginated  = results.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Person Search — New Application</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Cancel</button>
      </div>

      <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '4px', padding: '0.6rem 1rem', marginBottom: '1rem', fontSize: '0.875rem', color: '#1e40af' }}>
        Search for an existing person before starting a new application. Select a match to pre-fill the application form, or click "Create New Application" to start fresh.
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Last Name *</label>
              <input type="text" value={form.lastName} onChange={e => handleChange('lastName', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && doSearch()} autoFocus />
            </div>
            <div className="wq-form-field">
              <label>First Name</label>
              <input type="text" value={form.firstName} onChange={e => handleChange('firstName', e.target.value)}
                onKeyDown={e => e.key === 'Enter' && doSearch()} />
            </div>
            <div className="wq-form-field">
              <label>Date of Birth</label>
              <input type="date" value={form.dob} onChange={e => handleChange('dob', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>SSN (masked)</label>
              <input type="text" value={displaySsn} onChange={e => handleSsnChange(e.target.value)}
                placeholder="XXX-XX-####" maxLength={9} style={{ fontFamily: 'monospace' }} />
            </div>
            <div className="wq-form-field">
              <label>County</label>
              <input type="text" value={form.countyCode} onChange={e => handleChange('countyCode', e.target.value)} />
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
          <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h4>Results ({results.length})</h4>
            <button className="wq-btn wq-btn-primary"
              onClick={() => navigate('/applications/new?source=new')}>
              Create New Application
            </button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {results.length === 0 ? (
              <div style={{ padding: '1.5rem', textAlign: 'center' }}>
                <p style={{ color: '#4a5568', marginBottom: '1rem' }}>No matching persons found. Click "Create New Application" to proceed.</p>
              </div>
            ) : (
              <>
                <div style={{ background: '#fffbeb', border: '1px solid #f6ad55', padding: '0.5rem 1rem', margin: '0.75rem', borderRadius: '4px', fontSize: '0.8rem', color: '#c05621' }}>
                  Review matches below. Select an existing person to pre-fill the application, or create a new one.
                </div>
                <table className="wq-table">
                  <thead>
                    <tr><th>Name</th><th>DOB</th><th>CIN</th><th>County</th><th>Person Type</th><th>Address</th><th>Action</th></tr>
                  </thead>
                  <tbody>
                    {paginated.map((r, i) => (
                      <tr key={r.id || i}>
                        <td><strong>{[r.lastName, r.firstName].filter(Boolean).join(', ')}</strong></td>
                        <td>{r.dateOfBirth ? new Date(r.dateOfBirth).toLocaleDateString() : '—'}</td>
                        <td>{r.cin || '—'}</td>
                        <td>{r.countyCode || '—'}</td>
                        <td><PersonTypeBadge type={r.personType} /></td>
                        <td style={{ fontSize: '0.8rem' }}>{[r.residenceStreetNumber, r.residenceStreetName, r.residenceCity].filter(Boolean).join(' ') || '—'}</td>
                        <td>
                          <button className="wq-btn wq-btn-outline"
                            onClick={() => navigate(`/applications/new?recipientId=${r.id}&source=existing`)}>
                            Select
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>

                {totalPages > 1 && (
                  <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', padding: '1rem' }}>
                    <button className="wq-btn wq-btn-outline" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>Previous</button>
                    <span style={{ padding: '0.4rem 0.75rem', fontSize: '0.875rem', color: '#4a5568' }}>Page {page + 1} of {totalPages}</span>
                    <button className="wq-btn wq-btn-outline" onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}>Next</button>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
