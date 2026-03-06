import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as timesheetApi from '../api/timesheetApi';
import './WorkQueues.css';

export const TimesheetsPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [timesheets, setTimesheets] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [page, setPage] = useState(0);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'Timesheets' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const params = { page, size: 20 };
        if (statusFilter) params.status = statusFilter;
        const data = await timesheetApi.getTimesheets(params);
        const list = Array.isArray(data) ? data : (data?.content || data?.items || []);
        setTimesheets(list);
      } catch (err) {
        console.warn('[Timesheets] Error:', err?.message);
        setTimesheets([]);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [statusFilter, page]);

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Timesheets</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => navigate('/payments/timesheets/new')}>New Timesheet</button>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Filters</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Status</label>
              <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); }}>
                <option value="">All</option>
                <option value="DRAFT">Draft</option>
                <option value="SUBMITTED">Submitted</option>
                <option value="APPROVED">Approved</option>
                <option value="REJECTED">Rejected</option>
                <option value="PROCESSED">Processed</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Results ({timesheets.length})</h4>
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? (
            <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
          ) : timesheets.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No timesheets found.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Employee</th>
                  <th>Department</th>
                  <th>Pay Period</th>
                  <th>Total Hours</th>
                  <th>Status</th>
                  <th>Submitted</th>
                </tr>
              </thead>
              <tbody>
                {timesheets.map(t => (
                  <tr key={t.id} className="wq-clickable-row" onClick={() => navigate(`/payments/timesheets/${t.id}`)}>
                    <td>{t.id}</td>
                    <td>{t.employeeName || t.userId}</td>
                    <td>{t.department || '\u2014'}</td>
                    <td>{formatDate(t.payPeriodStart)} - {formatDate(t.payPeriodEnd)}</td>
                    <td>{t.totalHours ?? '\u2014'}</td>
                    <td><span className={`wq-badge wq-badge-${(t.status || '').toLowerCase()}`}>{t.status}</span></td>
                    <td>{formatDate(t.submittedAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {timesheets.length >= 20 && (
        <div style={{ display: 'flex', justifyContent: 'center', gap: '0.5rem', marginTop: '1rem' }}>
          <button className="wq-btn wq-btn-outline" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Previous</button>
          <span style={{ padding: '0.5rem', color: '#666' }}>Page {page + 1}</span>
          <button className="wq-btn wq-btn-outline" onClick={() => setPage(p => p + 1)}>Next</button>
        </div>
      )}
    </div>
  );
};
