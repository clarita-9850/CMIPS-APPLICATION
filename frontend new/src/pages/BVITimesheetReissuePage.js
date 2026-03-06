import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const BVITimesheetReissuePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [form, setForm] = useState({ providerId: '', caseNumber: '', payPeriodStart: '', payPeriodEnd: '', reason: '' });
  const [submitting, setSubmitting] = useState(false);
  const [results, setResults] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'BVI Timesheet Reissue' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = async () => {
    if (!form.providerId && !form.caseNumber) { setError('Enter Provider ID or Case Number.'); return; }
    setError(''); setSuccess('');
    setSubmitting(true);
    try {
      const params = new URLSearchParams();
      if (form.providerId) params.append('providerId', form.providerId);
      if (form.caseNumber) params.append('caseNumber', form.caseNumber);
      if (form.payPeriodStart) params.append('startDate', form.payPeriodStart);
      if (form.payPeriodEnd) params.append('endDate', form.payPeriodEnd);
      const res = await http.get(`/timesheets?${params.toString()}`);
      const list = Array.isArray(res.data) ? res.data : (res.data?.content || []);
      setResults(list);
      if (list.length === 0) setError('No timesheets found for reissue.');
    } catch (err) {
      setError('Search failed: ' + (err?.message || 'Unknown error'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleReissue = async (tsId) => {
    if (!form.reason) { setError('Enter a reason for reissue.'); return; }
    setError(''); setSuccess('');
    try {
      await http.post(`/timesheets/${tsId}/reissue`, { reason: form.reason });
      setSuccess(`Timesheet #${tsId} reissue requested.`);
    } catch (err) {
      setError('Reissue failed: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>BVI Timesheet Reissue</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to Payments</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}
      {success && (
        <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>
          {success}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search for Timesheet to Reissue</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Provider ID</label>
              <input type="text" value={form.providerId} onChange={e => setForm(p => ({ ...p, providerId: e.target.value }))} />
            </div>
            <div className="wq-form-field">
              <label>Case Number</label>
              <input type="text" value={form.caseNumber} onChange={e => setForm(p => ({ ...p, caseNumber: e.target.value }))} />
            </div>
            <div className="wq-form-field">
              <label>Pay Period Start</label>
              <input type="date" value={form.payPeriodStart} onChange={e => setForm(p => ({ ...p, payPeriodStart: e.target.value }))} />
            </div>
            <div className="wq-form-field">
              <label>Pay Period End</label>
              <input type="date" value={form.payPeriodEnd} onChange={e => setForm(p => ({ ...p, payPeriodEnd: e.target.value }))} />
            </div>
            <div className="wq-form-field" style={{ gridColumn: '1 / -1' }}>
              <label>Reason for Reissue *</label>
              <textarea rows={2} value={form.reason} onChange={e => setForm(p => ({ ...p, reason: e.target.value }))}
                placeholder="Enter reason for BVI timesheet reissue..."
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={submitting}>
              {submitting ? 'Searching...' : 'Search'}
            </button>
          </div>
        </div>
      </div>

      {results.length > 0 && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Matching Timesheets ({results.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            <table className="wq-table">
              <thead>
                <tr><th>ID</th><th>Employee</th><th>Pay Period</th><th>Hours</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {results.map(t => (
                  <tr key={t.id}>
                    <td>{t.id}</td>
                    <td>{t.employeeName || t.userId}</td>
                    <td>{formatDate(t.payPeriodStart)} - {formatDate(t.payPeriodEnd)}</td>
                    <td>{t.totalHours ?? '\u2014'}</td>
                    <td><span className={`wq-badge wq-badge-${(t.status || '').toLowerCase()}`}>{t.status}</span></td>
                    <td>
                      <button className="wq-btn wq-btn-primary" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                        onClick={() => handleReissue(t.id)}>
                        Reissue
                      </button>
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
