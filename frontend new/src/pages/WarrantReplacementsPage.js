import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as warrantApi from '../api/warrantApi';
import './WorkQueues.css';

export const WarrantReplacementsPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [searchParams, setSearchParams] = useState({ warrantNumber: '', providerId: '', caseNumber: '', status: '' });
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'Warrant Replacements' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = async () => {
    if (!searchParams.warrantNumber && !searchParams.providerId && !searchParams.caseNumber) {
      setError('Enter at least one search criterion.');
      return;
    }
    setError('');
    setLoading(true);
    try {
      const data = await warrantApi.searchWarrants(searchParams);
      const list = Array.isArray(data) ? data : (data?.content || data?.items || []);
      setResults(list);
      if (list.length === 0) setError('No warrants found matching criteria.');
    } catch (err) {
      setError('Search failed: ' + (err?.message || 'Unknown error'));
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleReplace = async (warrantNumber) => {
    const reason = prompt('Enter replacement reason:');
    if (!reason) return;
    try {
      await warrantApi.requestReplacement(warrantNumber, { reason, requestedBy: 'current-user' });
      alert('Replacement requested successfully.');
      handleSearch();
    } catch (err) {
      alert('Replacement request failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
  const formatAmount = (a) => a != null ? `$${Number(a).toFixed(2)}` : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Warrant Replacements</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to Payments</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Warrants</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Warrant Number</label>
              <input type="text" value={searchParams.warrantNumber} onChange={e => setSearchParams(p => ({ ...p, warrantNumber: e.target.value }))} placeholder="Enter warrant number" />
            </div>
            <div className="wq-form-field">
              <label>Provider ID</label>
              <input type="text" value={searchParams.providerId} onChange={e => setSearchParams(p => ({ ...p, providerId: e.target.value }))} />
            </div>
            <div className="wq-form-field">
              <label>Case Number</label>
              <input type="text" value={searchParams.caseNumber} onChange={e => setSearchParams(p => ({ ...p, caseNumber: e.target.value }))} />
            </div>
            <div className="wq-form-field">
              <label>Status</label>
              <select value={searchParams.status} onChange={e => setSearchParams(p => ({ ...p, status: e.target.value }))}>
                <option value="">All</option>
                <option value="ISSUED">Issued</option>
                <option value="PAID">Paid</option>
                <option value="VOIDED">Voided</option>
                <option value="STALE">Stale</option>
              </select>
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
              {loading ? 'Searching...' : 'Search'}
            </button>
          </div>
        </div>
      </div>

      {results.length > 0 && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Results ({results.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Warrant #</th>
                  <th>Provider ID</th>
                  <th>Case #</th>
                  <th>County</th>
                  <th>Amount</th>
                  <th>Issue Date</th>
                  <th>Paid Date</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {results.map(w => (
                  <tr key={w.warrantNumber || w.id}>
                    <td>{w.warrantNumber}</td>
                    <td>{w.providerId}</td>
                    <td>{w.caseNumber}</td>
                    <td>{w.countyCode}</td>
                    <td>{formatAmount(w.amount)}</td>
                    <td>{formatDate(w.issueDate)}</td>
                    <td>{formatDate(w.paidDate)}</td>
                    <td><span className={`wq-badge wq-badge-${(w.status || '').toLowerCase()}`}>{w.status}</span></td>
                    <td>
                      {(w.status === 'ISSUED' || w.status === 'STALE') && (
                        <button className="wq-btn wq-btn-primary" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                          onClick={() => handleReplace(w.warrantNumber)}>
                          Request Replacement
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
