import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as biApi from '../api/biApi';
import './WorkQueues.css';

export const BIReportsPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();

  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showGenerate, setShowGenerate] = useState(false);
  const [genForm, setGenForm] = useState({ reportType: '', parameters: '{}' });
  const [generating, setGenerating] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'BI Reports' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const loadJobs = async () => {
    setLoading(true);
    try {
      const data = await biApi.getAllJobs();
      const list = Array.isArray(data) ? data : (data?.jobs || data?.content || []);
      setJobs(list);
    } catch (err) {
      console.warn('[BIReports] Error:', err?.message);
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadJobs(); }, []);

  const handleGenerate = async () => {
    if (!genForm.reportType) { setError('Select a report type.'); return; }
    setError(''); setSuccess(''); setGenerating(true);
    try {
      let params = {};
      try { params = JSON.parse(genForm.parameters); } catch { /* ignore parse errors */ }
      await biApi.generateReport({ reportType: genForm.reportType, parameters: params });
      setSuccess('Report generation started.');
      setShowGenerate(false);
      setGenForm({ reportType: '', parameters: '{}' });
      await loadJobs();
    } catch (err) {
      setError('Generation failed: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    } finally {
      setGenerating(false);
    }
  };

  const handleCancel = async (jobId) => {
    try {
      await biApi.cancelJob(jobId);
      await loadJobs();
    } catch (err) {
      setError('Cancel failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const handleDownload = async (jobId) => {
    try {
      const blob = await biApi.downloadJob(jobId);
      const url = window.URL.createObjectURL(new Blob([blob]));
      const a = document.createElement('a');
      a.href = url;
      a.download = `report-${jobId}.csv`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Download failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const formatDateTime = (d) => d ? new Date(d).toLocaleString() : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>BI Reports</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="wq-btn wq-btn-primary" onClick={() => setShowGenerate(!showGenerate)}>
            {showGenerate ? 'Cancel' : 'Generate Report'}
          </button>
          <button className="wq-btn wq-btn-outline" onClick={loadJobs}>Refresh</button>
        </div>
      </div>

      {error && <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>}
      {success && <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>{success}</div>}

      {showGenerate && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Generate New Report</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field">
                <label>Report Type *</label>
                <select value={genForm.reportType} onChange={e => setGenForm(p => ({ ...p, reportType: e.target.value }))}>
                  <option value="">-- Select --</option>
                  <option value="CASELOAD_SUMMARY">Caseload Summary</option>
                  <option value="PROVIDER_ACTIVITY">Provider Activity</option>
                  <option value="PAYMENT_SUMMARY">Payment Summary</option>
                  <option value="TIMESHEET_REPORT">Timesheet Report</option>
                  <option value="WAIVER_STATUS">Waiver Status</option>
                  <option value="COUNTY_DEMOGRAPHICS">County Demographics</option>
                </select>
              </div>
              <div className="wq-form-field" style={{ gridColumn: '1 / -1' }}>
                <label>Parameters (JSON)</label>
                <textarea rows={3} value={genForm.parameters} onChange={e => setGenForm(p => ({ ...p, parameters: e.target.value }))}
                  style={{ width: '100%', fontFamily: 'monospace', fontSize: '0.85rem', padding: '0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleGenerate} disabled={generating}>
                {generating ? 'Generating...' : 'Generate'}
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Report Jobs ({jobs.length})</h4></div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? (
            <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
          ) : jobs.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No report jobs found.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr><th>Job ID</th><th>Report Type</th><th>Status</th><th>Created</th><th>Completed</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {jobs.map(j => (
                  <tr key={j.jobId || j.id}>
                    <td>{j.jobId || j.id}</td>
                    <td>{j.reportType || '\u2014'}</td>
                    <td><span className={`wq-badge wq-badge-${(j.status || '').toLowerCase()}`}>{j.status}</span></td>
                    <td>{formatDateTime(j.createdAt || j.startTime)}</td>
                    <td>{formatDateTime(j.completedAt || j.endTime)}</td>
                    <td>
                      {j.status === 'COMPLETED' && (
                        <button className="wq-btn wq-btn-primary" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', marginRight: '0.25rem' }}
                          onClick={() => handleDownload(j.jobId || j.id)}>Download</button>
                      )}
                      {(j.status === 'RUNNING' || j.status === 'PENDING') && (
                        <button className="wq-btn" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', color: '#c53030', border: '1px solid #c53030' }}
                          onClick={() => handleCancel(j.jobId || j.id)}>Cancel</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
