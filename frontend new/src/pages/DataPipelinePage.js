import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as pipelineApi from '../api/pipelineApi';
import './WorkQueues.css';

export const DataPipelinePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [activeTab, setActiveTab] = useState('status');
  const [status, setStatus] = useState(null);
  const [roles, setRoles] = useState([]);
  const [counties, setCounties] = useState([]);
  const [reportTypes, setReportTypes] = useState([]);
  const [fields, setFields] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Report generation form
  const [reportForm, setReportForm] = useState({ county: '', reportType: '', userRole: '' });
  const [generating, setGenerating] = useState(false);

  // Masking rules
  const [selectedRole, setSelectedRole] = useState('');
  const [maskingRules, setMaskingRules] = useState(null);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Administration', path: '/admin' }, { label: 'Data Pipeline' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const [s, r, c, rt, f] = await Promise.all([
          pipelineApi.getStatus().catch(() => null),
          pipelineApi.getUserRoles().catch(() => []),
          pipelineApi.getCounties().catch(() => []),
          pipelineApi.getReportTypes().catch(() => []),
          pipelineApi.getAvailableFields().catch(() => [])
        ]);
        setStatus(s);
        setRoles(Array.isArray(r) ? r : []);
        setCounties(Array.isArray(c) ? c : []);
        setReportTypes(Array.isArray(rt) ? rt : []);
        setFields(Array.isArray(f) ? f : []);
      } catch (err) {
        console.warn('[Pipeline] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleGenerateReport = async () => {
    setError(''); setSuccess(''); setGenerating(true);
    try {
      await pipelineApi.generateReport(reportForm);
      setSuccess('Report generation triggered.');
    } catch (err) {
      setError('Failed: ' + (err?.message || 'Unknown error'));
    } finally {
      setGenerating(false);
    }
  };

  const handleLoadMaskingRules = async () => {
    if (!selectedRole) return;
    try {
      const rules = await pipelineApi.getMaskingRules(selectedRole);
      setMaskingRules(rules);
    } catch (err) {
      setError('Failed to load masking rules: ' + (err?.message || 'Unknown error'));
    }
  };

  if (loading) return <div className="wq-page"><p>Loading pipeline status...</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Data Pipeline</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/admin')}>Back to Admin</button>
      </div>

      {error && <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>}
      {success && <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>{success}</div>}

      {/* Status Summary */}
      {status && (
        <div className="workspace-stats-row">
          <div className="workspace-stat-card"><div className="stat-number">{roles.length}</div><div className="stat-label">User Roles</div></div>
          <div className="workspace-stat-card"><div className="stat-number">{counties.length}</div><div className="stat-label">Counties</div></div>
          <div className="workspace-stat-card"><div className="stat-number">{reportTypes.length}</div><div className="stat-label">Report Types</div></div>
          <div className="workspace-stat-card"><div className="stat-number">{fields.length}</div><div className="stat-label">Available Fields</div></div>
        </div>
      )}

      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'status' ? 'active' : ''}`} onClick={() => setActiveTab('status')}>Pipeline Status</button>
        <button className={`wq-tab ${activeTab === 'reports' ? 'active' : ''}`} onClick={() => setActiveTab('reports')}>Generate Report</button>
        <button className={`wq-tab ${activeTab === 'masking' ? 'active' : ''}`} onClick={() => setActiveTab('masking')}>Field Masking</button>
        <button className={`wq-tab ${activeTab === 'fields' ? 'active' : ''}`} onClick={() => setActiveTab('fields')}>Available Fields</button>
      </div>

      {activeTab === 'status' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Pipeline Status</h4></div>
          <div className="wq-panel-body">
            <pre style={{ fontSize: '0.85rem', overflow: 'auto', background: '#f7fafc', padding: '1rem', borderRadius: '4px', maxHeight: '400px' }}>
              {JSON.stringify(status, null, 2)}
            </pre>
          </div>
        </div>
      )}

      {activeTab === 'reports' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Generate Data Extract Report</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field">
                <label>County</label>
                <select value={reportForm.county} onChange={e => setReportForm(p => ({ ...p, county: e.target.value }))}>
                  <option value="">All Counties</option>
                  {counties.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>
              <div className="wq-form-field">
                <label>Report Type</label>
                <select value={reportForm.reportType} onChange={e => setReportForm(p => ({ ...p, reportType: e.target.value }))}>
                  <option value="">-- Select --</option>
                  {reportTypes.map(rt => <option key={rt} value={rt}>{rt}</option>)}
                </select>
              </div>
              <div className="wq-form-field">
                <label>User Role</label>
                <select value={reportForm.userRole} onChange={e => setReportForm(p => ({ ...p, userRole: e.target.value }))}>
                  <option value="">-- Select --</option>
                  {roles.map(r => <option key={r} value={r}>{r}</option>)}
                </select>
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleGenerateReport} disabled={generating}>
                {generating ? 'Generating...' : 'Generate Report'}
              </button>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'masking' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Field Masking Rules by Role</h4></div>
          <div className="wq-panel-body">
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
              <select value={selectedRole} onChange={e => setSelectedRole(e.target.value)}
                style={{ flex: 1, padding: '0.4rem', border: '1px solid #cbd5e0', borderRadius: '4px' }}>
                <option value="">-- Select Role --</option>
                {roles.map(r => <option key={r} value={r}>{r}</option>)}
              </select>
              <button className="wq-btn wq-btn-primary" onClick={handleLoadMaskingRules} disabled={!selectedRole}>Load Rules</button>
            </div>
            {maskingRules && (
              <pre style={{ fontSize: '0.85rem', overflow: 'auto', background: '#f7fafc', padding: '1rem', borderRadius: '4px', maxHeight: '400px' }}>
                {JSON.stringify(maskingRules, null, 2)}
              </pre>
            )}
          </div>
        </div>
      )}

      {activeTab === 'fields' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Available Fields ({fields.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {fields.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No fields configured.</p>
            ) : (
              <table className="wq-table">
                <thead><tr><th>Field Name</th><th>Type</th><th>Description</th></tr></thead>
                <tbody>
                  {fields.map((f, i) => (
                    <tr key={i}>
                      <td>{typeof f === 'string' ? f : (f.name || f.fieldName)}</td>
                      <td>{f.type || f.dataType || '\u2014'}</td>
                      <td>{f.description || '\u2014'}</td>
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
