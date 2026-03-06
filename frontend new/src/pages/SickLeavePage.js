import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const SickLeavePage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [activeView, setActiveView] = useState('search');
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [searchParams, setSearchParams] = useState({ employeeId: '', startDate: '', endDate: '', status: '' });
  const [form, setForm] = useState({
    employeeId: '', employeeName: '', leaveStartDate: '', leaveEndDate: '', hoursRequested: '', reason: ''
  });

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'Sick Leave' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = async () => {
    setError(''); setLoading(true);
    try {
      const params = new URLSearchParams();
      if (searchParams.employeeId) params.append('employeeId', searchParams.employeeId);
      if (searchParams.startDate) params.append('startDate', searchParams.startDate);
      if (searchParams.endDate) params.append('endDate', searchParams.endDate);
      if (searchParams.status) params.append('status', searchParams.status);
      const res = await http.get(`/payments/sick-leave?${params.toString()}`);
      const list = Array.isArray(res.data) ? res.data : (res.data?.content || []);
      setClaims(list);
    } catch (err) {
      setError('Search failed: ' + (err?.message || 'Unknown error'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitClaim = async () => {
    if (!form.employeeId || !form.leaveStartDate || !form.hoursRequested || !form.reason) {
      setError('Fill in all required fields.'); return;
    }
    setError(''); setSuccess('');
    try {
      await http.post('/payments/sick-leave', { ...form, submittedBy: username });
      setSuccess('Sick leave claim submitted successfully.');
      setForm({ employeeId: '', employeeName: '', leaveStartDate: '', leaveEndDate: '', hoursRequested: '', reason: '' });
    } catch (err) {
      setError('Submit failed: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Sick Leave Claims</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to Payments</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>
      )}
      {success && (
        <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>{success}</div>
      )}

      <div className="wq-tabs">
        <button className={`wq-tab ${activeView === 'search' ? 'active' : ''}`} onClick={() => setActiveView('search')}>Search Claims</button>
        <button className={`wq-tab ${activeView === 'new' ? 'active' : ''}`} onClick={() => setActiveView('new')}>New Claim</button>
      </div>

      {activeView === 'search' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Search Sick Leave Claims</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field"><label>Employee ID</label><input type="text" value={searchParams.employeeId} onChange={e => setSearchParams(p => ({ ...p, employeeId: e.target.value }))} /></div>
                <div className="wq-form-field"><label>Start Date</label><input type="date" value={searchParams.startDate} onChange={e => setSearchParams(p => ({ ...p, startDate: e.target.value }))} /></div>
                <div className="wq-form-field"><label>End Date</label><input type="date" value={searchParams.endDate} onChange={e => setSearchParams(p => ({ ...p, endDate: e.target.value }))} /></div>
                <div className="wq-form-field">
                  <label>Status</label>
                  <select value={searchParams.status} onChange={e => setSearchParams(p => ({ ...p, status: e.target.value }))}>
                    <option value="">All</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                  </select>
                </div>
              </div>
              <div className="wq-search-actions">
                <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>{loading ? 'Searching...' : 'Search'}</button>
              </div>
            </div>
          </div>

          {claims.length > 0 && (
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Results ({claims.length})</h4></div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                <table className="wq-table">
                  <thead><tr><th>ID</th><th>Employee</th><th>Leave Dates</th><th>Hours</th><th>Reason</th><th>Status</th></tr></thead>
                  <tbody>
                    {claims.map(c => (
                      <tr key={c.id}>
                        <td>{c.id}</td>
                        <td>{c.employeeName || c.employeeId}</td>
                        <td>{formatDate(c.leaveStartDate)} - {formatDate(c.leaveEndDate)}</td>
                        <td>{c.hoursRequested ?? '\u2014'}</td>
                        <td style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis' }}>{c.reason}</td>
                        <td><span className={`wq-badge wq-badge-${(c.status || '').toLowerCase()}`}>{c.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}

      {activeView === 'new' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Submit New Sick Leave Claim</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field"><label>Employee ID *</label><input type="text" value={form.employeeId} onChange={e => setForm(p => ({ ...p, employeeId: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Employee Name</label><input type="text" value={form.employeeName} onChange={e => setForm(p => ({ ...p, employeeName: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Leave Start Date *</label><input type="date" value={form.leaveStartDate} onChange={e => setForm(p => ({ ...p, leaveStartDate: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Leave End Date</label><input type="date" value={form.leaveEndDate} onChange={e => setForm(p => ({ ...p, leaveEndDate: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Hours Requested *</label><input type="number" step="0.5" min="0" value={form.hoursRequested} onChange={e => setForm(p => ({ ...p, hoursRequested: e.target.value }))} /></div>
              <div className="wq-form-field" style={{ gridColumn: '1 / -1' }}>
                <label>Reason *</label>
                <textarea rows={2} value={form.reason} onChange={e => setForm(p => ({ ...p, reason: e.target.value }))}
                  placeholder="Enter reason for sick leave..." style={{ width: '100%', padding: '0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleSubmitClaim}>Submit Claim</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
