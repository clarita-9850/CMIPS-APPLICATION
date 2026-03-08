import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as reportsApi from '../api/reportsApi';
import './WorkQueues.css';

// ==================== Constants ====================

const REPORT_CATEGORIES = [
  'FINANCIAL', 'CASE_MANAGEMENT', 'PROVIDER', 'STATISTICAL',
  'COMPLIANCE', 'OPERATIONAL', 'FEDERAL'
];

const FRAUD_TYPES = [
  'PROVIDER_FRAUD', 'RECIPIENT_FRAUD', 'TIMESHEET_FRAUD', 'IDENTITY_FRAUD', 'OTHER'
];

const FRAUD_STATUSES = [
  'OPEN', 'UNDER_INVESTIGATION', 'SUBSTANTIATED', 'REFERRED_TO_DA', 'CLOSED', 'DISMISSED'
];

const MAILING_STATUSES = {
  DRAFT: { bg: '#e2e8f0', fg: '#4a5568' },
  SCHEDULED: { bg: '#bee3f8', fg: '#2b6cb0' },
  EXECUTING: { bg: '#fefcbf', fg: '#975a16' },
  COMPLETED: { bg: '#c6f6d5', fg: '#276749' },
  CANCELLED: { bg: '#fed7d7', fg: '#9b2c2c' }
};

const DEATH_MATCH_STATUSES = {
  PENDING: { bg: '#fefcbf', fg: '#975a16' },
  VERIFIED: { bg: '#fed7d7', fg: '#9b2c2c' },
  FALSE_MATCH: { bg: '#c6f6d5', fg: '#276749' },
  ACTION_RECORDED: { bg: '#bee3f8', fg: '#2b6cb0' }
};

const DEATH_MATCH_SOURCES = ['CDPH', 'SCO', 'SSA', 'MEDS'];

const RECIPIENT_TYPES = ['PROVIDER', 'RECIPIENT', 'ALL'];
const LANGUAGES = ['ENGLISH', 'SPANISH', 'CHINESE', 'ARMENIAN'];

const OUTPUT_FORMATS = ['PDF', 'CSV', 'EXCEL', 'XML'];
const FREQUENCIES = ['ON_DEMAND', 'DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUALLY'];

// ==================== Helper Components ====================

const StatusBadge = ({ status, colorMap }) => {
  const colors = colorMap[status] || { bg: '#e2e8f0', fg: '#4a5568' };
  return (
    <span style={{
      display: 'inline-block', padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem',
      fontWeight: 600, backgroundColor: colors.bg, color: colors.fg
    }}>
      {(status || '').replace(/_/g, ' ')}
    </span>
  );
};

const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '--';
const fmtDateTime = (d) => d ? new Date(d).toLocaleString() : '--';
const fmtCurrency = (v) => v != null ? `$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '--';

// ==================== Main Component ====================

export const ReportsAdminPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();
  const [activeTab, setActiveTab] = useState('reports');
  const TABS = [
    { key: 'reports', label: 'Reports' },
    { key: 'fraud', label: 'Fraud Cases' },
    { key: 'mailings', label: 'Targeted Mailings' },
    { key: 'deathMatch', label: 'Death Match' }
  ];

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Admin', path: '/admin' },
      { label: 'Reports & Program Management' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  return (
    <div className="wq-page">
      <h2 className="wq-page-title">Reports & Program Management</h2>

      <div className="wq-tabs" style={{ marginBottom: '1rem' }}>
        {TABS.map(t => (
          <button key={t.key}
            className={`wq-tab ${activeTab === t.key ? 'wq-tab-active' : ''}`}
            onClick={() => setActiveTab(t.key)}>
            {t.label}
          </button>
        ))}
      </div>

      {activeTab === 'reports' && <ReportsTab />}
      {activeTab === 'fraud' && <FraudCasesTab />}
      {activeTab === 'mailings' && <TargetedMailingsTab />}
      {activeTab === 'deathMatch' && <DeathMatchTab />}
    </div>
  );
};

// ==================== Reports Tab ====================

const ReportsTab = () => {
  const [subTab, setSubTab] = useState('catalog');
  const [definitions, setDefinitions] = useState([]);
  const [executions, setExecutions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [runningOnly, setRunningOnly] = useState(false);

  // Add Report modal
  const [showAddModal, setShowAddModal] = useState(false);
  const [addForm, setAddForm] = useState({
    reportCode: '', reportName: '', category: 'FINANCIAL', frequency: 'ON_DEMAND',
    outputFormat: 'PDF', description: ''
  });
  const [saving, setSaving] = useState(false);

  // Execute modal
  const [execModal, setExecModal] = useState(null);
  const [execParams, setExecParams] = useState('{}');
  const [executing, setExecuting] = useState(false);

  useEffect(() => {
    if (subTab === 'catalog') loadDefinitions();
    else loadExecutions();
  }, [subTab, categoryFilter, runningOnly]);

  const loadDefinitions = async () => {
    setLoading(true); setError('');
    try {
      const res = categoryFilter
        ? await reportsApi.getReportsByCategory(categoryFilter)
        : await reportsApi.getReportDefinitions();
      setDefinitions(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      console.warn('[ReportsAdmin] Load definitions error:', err?.message);
      setError('Failed to load report definitions.');
    } finally {
      setLoading(false);
    }
  };

  const loadExecutions = async () => {
    setLoading(true); setError('');
    try {
      const res = runningOnly
        ? await reportsApi.getRunningExecutions()
        : await reportsApi.getRecentExecutions();
      setExecutions(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      console.warn('[ReportsAdmin] Load executions error:', err?.message);
      setError('Failed to load executions.');
    } finally {
      setLoading(false);
    }
  };

  const handleAddReport = async () => {
    if (!addForm.reportCode.trim() || !addForm.reportName.trim()) return;
    setSaving(true); setError('');
    try {
      const res = await reportsApi.createReportDefinition(addForm);
      setDefinitions(prev => [...prev, res.data]);
      setShowAddModal(false);
      setAddForm({ reportCode: '', reportName: '', category: 'FINANCIAL', frequency: 'ON_DEMAND', outputFormat: 'PDF', description: '' });
      setSuccess('Report definition created.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create report definition.');
    } finally {
      setSaving(false);
    }
  };

  const handleExecute = async () => {
    if (!execModal) return;
    setExecuting(true); setError('');
    try {
      let params = {};
      try { params = JSON.parse(execParams); } catch { setError('Invalid JSON parameters.'); setExecuting(false); return; }
      await reportsApi.executeReport(execModal.reportCode, params);
      setExecModal(null);
      setExecParams('{}');
      setSuccess('Report execution started.');
      setTimeout(() => setSuccess(''), 3000);
      if (subTab === 'executions') loadExecutions();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to execute report.');
    } finally {
      setExecuting(false);
    }
  };

  const handleInactivate = async (id) => {
    if (!window.confirm('Inactivate this report definition?')) return;
    try {
      await reportsApi.inactivateReportDefinition(id);
      setDefinitions(prev => prev.map(d => d.id === id ? { ...d, status: 'INACTIVE' } : d));
      setSuccess('Report inactivated.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to inactivate report.');
    }
  };

  return (
    <>
      {error && <div style={{ color: '#c53030', backgroundColor: '#fff5f5', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ color: '#276749', backgroundColor: '#f0fff4', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{success}</div>}

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <button className={`wq-btn ${subTab === 'catalog' ? 'wq-btn-primary' : ''}`} onClick={() => setSubTab('catalog')}>Catalog</button>
        <button className={`wq-btn ${subTab === 'executions' ? 'wq-btn-primary' : ''}`} onClick={() => setSubTab('executions')}>Executions</button>
      </div>

      {subTab === 'catalog' && (
        <>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap' }}>
            <div className="wq-form-group" style={{ margin: 0 }}>
              <select className="wq-select" value={categoryFilter} onChange={e => setCategoryFilter(e.target.value)}>
                <option value="">All Categories</option>
                {REPORT_CATEGORIES.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAddModal(true)}>Add Report</button>
          </div>

          {loading ? <p>Loading...</p> : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Report Code</th><th>Name</th><th>Category</th><th>Frequency</th><th>Output Format</th><th>Status</th><th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {definitions.length === 0 ? (
                  <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>No report definitions found.</td></tr>
                ) : definitions.map(d => (
                  <tr key={d.id || d.reportCode}>
                    <td><strong>{d.reportCode}</strong></td>
                    <td>{d.reportName}</td>
                    <td>{(d.category || '').replace(/_/g, ' ')}</td>
                    <td>{(d.frequency || '').replace(/_/g, ' ')}</td>
                    <td>{d.outputFormat}</td>
                    <td>
                      <span style={{
                        padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600,
                        backgroundColor: d.status === 'ACTIVE' ? '#c6f6d5' : '#e2e8f0',
                        color: d.status === 'ACTIVE' ? '#276749' : '#4a5568'
                      }}>
                        {d.status || 'ACTIVE'}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.25rem' }}>
                        <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => { setExecModal(d); setExecParams('{}'); }}
                          disabled={d.status === 'INACTIVE'}>Execute</button>
                        <button className="wq-btn wq-btn-sm" onClick={() => handleInactivate(d.id)}
                          disabled={d.status === 'INACTIVE'}>Inactivate</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {subTab === 'executions' && (
        <>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '1rem' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.25rem', cursor: 'pointer' }}>
              <input type="checkbox" checked={runningOnly} onChange={e => setRunningOnly(e.target.checked)} />
              Running only
            </label>
            <button className="wq-btn wq-btn-sm" onClick={loadExecutions}>Refresh</button>
          </div>

          {loading ? <p>Loading...</p> : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Report Code</th><th>Status</th><th>Started</th><th>Duration</th><th>Rows</th><th>Output</th>
                </tr>
              </thead>
              <tbody>
                {executions.length === 0 ? (
                  <tr><td colSpan={6} style={{ textAlign: 'center', padding: '2rem' }}>No executions found.</td></tr>
                ) : executions.map(e => (
                  <tr key={e.id}>
                    <td><strong>{e.reportCode}</strong></td>
                    <td>
                      <span style={{
                        padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600,
                        backgroundColor: e.status === 'COMPLETED' ? '#c6f6d5' : e.status === 'RUNNING' ? '#fefcbf' : e.status === 'FAILED' ? '#fed7d7' : '#e2e8f0',
                        color: e.status === 'COMPLETED' ? '#276749' : e.status === 'RUNNING' ? '#975a16' : e.status === 'FAILED' ? '#9b2c2c' : '#4a5568'
                      }}>
                        {e.status}
                      </span>
                    </td>
                    <td>{fmtDateTime(e.startedAt)}</td>
                    <td>{e.durationMs != null ? `${(e.durationMs / 1000).toFixed(1)}s` : '--'}</td>
                    <td>{e.rowCount != null ? e.rowCount.toLocaleString() : '--'}</td>
                    <td>{e.outputPath ? <a href={e.outputPath} target="_blank" rel="noreferrer">Download</a> : '--'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}

      {/* Add Report Modal */}
      {showAddModal && (
        <div className="wq-modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <h3 className="wq-modal-title">Add Report Definition</h3>
            <div className="wq-form-group">
              <label>Report Code *</label>
              <input className="wq-input" value={addForm.reportCode}
                onChange={e => setAddForm(p => ({ ...p, reportCode: e.target.value.toUpperCase() }))} placeholder="e.g. RPT_FIN_001" />
            </div>
            <div className="wq-form-group">
              <label>Report Name *</label>
              <input className="wq-input" value={addForm.reportName}
                onChange={e => setAddForm(p => ({ ...p, reportName: e.target.value }))} placeholder="Report display name" />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="wq-form-group">
                <label>Category</label>
                <select className="wq-select" value={addForm.category}
                  onChange={e => setAddForm(p => ({ ...p, category: e.target.value }))}>
                  {REPORT_CATEGORIES.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
              <div className="wq-form-group">
                <label>Frequency</label>
                <select className="wq-select" value={addForm.frequency}
                  onChange={e => setAddForm(p => ({ ...p, frequency: e.target.value }))}>
                  {FREQUENCIES.map(f => <option key={f} value={f}>{f.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
            </div>
            <div className="wq-form-group">
              <label>Output Format</label>
              <select className="wq-select" value={addForm.outputFormat}
                onChange={e => setAddForm(p => ({ ...p, outputFormat: e.target.value }))}>
                {OUTPUT_FORMATS.map(f => <option key={f} value={f}>{f}</option>)}
              </select>
            </div>
            <div className="wq-form-group">
              <label>Description</label>
              <textarea className="wq-input" rows={2} value={addForm.description}
                onChange={e => setAddForm(p => ({ ...p, description: e.target.value }))} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setShowAddModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleAddReport} disabled={saving}>
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Execute Modal */}
      {execModal && (
        <div className="wq-modal-overlay" onClick={() => setExecModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <h3 className="wq-modal-title">Execute Report: {execModal.reportCode}</h3>
            <p style={{ margin: '0 0 0.75rem', color: '#718096' }}>{execModal.reportName}</p>
            <div className="wq-form-group">
              <label>Parameters (JSON)</label>
              <textarea className="wq-input" rows={5} value={execParams}
                onChange={e => setExecParams(e.target.value)}
                style={{ fontFamily: 'monospace', fontSize: '0.85rem' }} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setExecModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleExecute} disabled={executing}>
                {executing ? 'Executing...' : 'Execute'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

// ==================== Fraud Cases Tab ====================

const FraudCasesTab = () => {
  const [fraudCases, setFraudCases] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Filters
  const [filterType, setFilterType] = useState('status');
  const [filterCaseId, setFilterCaseId] = useState('');
  const [filterProviderId, setFilterProviderId] = useState('');
  const [filterStatus, setFilterStatus] = useState('OPEN');

  // New Fraud Case modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    caseId: '', providerId: '', fraudType: 'PROVIDER_FRAUD', amountInvolved: '', allegationSummary: ''
  });
  const [saving, setSaving] = useState(false);

  // Substantiate modal
  const [substantiateModal, setSubstantiateModal] = useState(null);
  const [findings, setFindings] = useState('');

  // Refer to DA modal
  const [referModal, setReferModal] = useState(null);
  const [referNotes, setReferNotes] = useState('');

  useEffect(() => { loadFraud(); }, []);

  const loadFraud = async () => {
    setLoading(true); setError('');
    try {
      let res;
      if (filterType === 'caseId' && filterCaseId.trim()) {
        res = await reportsApi.getFraudCasesByCaseId(filterCaseId.trim());
      } else if (filterType === 'providerId' && filterProviderId.trim()) {
        res = await reportsApi.getFraudCasesByProvider(filterProviderId.trim());
      } else {
        res = await reportsApi.getFraudCasesByStatus(filterStatus);
      }
      setFraudCases(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      console.warn('[ReportsAdmin] Load fraud error:', err?.message);
      setError('Failed to load fraud cases.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    loadFraud();
  };

  const handleCreate = async () => {
    if (!createForm.caseId && !createForm.providerId) { setError('Case ID or Provider ID is required.'); return; }
    setSaving(true); setError('');
    try {
      const payload = {
        ...createForm,
        caseId: createForm.caseId ? Number(createForm.caseId) : null,
        providerId: createForm.providerId ? Number(createForm.providerId) : null,
        amountInvolved: createForm.amountInvolved ? Number(createForm.amountInvolved) : null
      };
      const res = await reportsApi.createFraudCase(payload);
      setFraudCases(prev => [res.data, ...prev]);
      setShowCreateModal(false);
      setCreateForm({ caseId: '', providerId: '', fraudType: 'PROVIDER_FRAUD', amountInvolved: '', allegationSummary: '' });
      setSuccess('Fraud case created.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create fraud case.');
    } finally {
      setSaving(false);
    }
  };

  const handleSubstantiate = async () => {
    if (!substantiateModal || !findings.trim()) return;
    try {
      const res = await reportsApi.substantiateFraudCase(substantiateModal.id, { findings: findings.trim() });
      setFraudCases(prev => prev.map(f => f.id === substantiateModal.id ? res.data : f));
      setSubstantiateModal(null); setFindings('');
      setSuccess('Fraud case substantiated.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to substantiate.');
    }
  };

  const handleReferToDA = async () => {
    if (!referModal) return;
    try {
      const res = await reportsApi.referFraudToDA(referModal.id, { notes: referNotes.trim() });
      setFraudCases(prev => prev.map(f => f.id === referModal.id ? res.data : f));
      setReferModal(null); setReferNotes('');
      setSuccess('Fraud case referred to DA.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to refer to DA.');
    }
  };

  const handleClose = async (fc) => {
    if (!window.confirm(`Close fraud case #${fc.id}?`)) return;
    try {
      const res = await reportsApi.closeFraudCase(fc.id);
      setFraudCases(prev => prev.map(f => f.id === fc.id ? res.data : f));
      setSuccess('Fraud case closed.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to close fraud case.');
    }
  };

  const fraudStatusColors = {
    OPEN: { bg: '#fefcbf', fg: '#975a16' },
    UNDER_INVESTIGATION: { bg: '#bee3f8', fg: '#2b6cb0' },
    SUBSTANTIATED: { bg: '#fed7d7', fg: '#9b2c2c' },
    REFERRED_TO_DA: { bg: '#e9d8fd', fg: '#6b46c1' },
    CLOSED: { bg: '#e2e8f0', fg: '#4a5568' },
    DISMISSED: { bg: '#e2e8f0', fg: '#4a5568' }
  };

  return (
    <>
      {error && <div style={{ color: '#c53030', backgroundColor: '#fff5f5', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ color: '#276749', backgroundColor: '#f0fff4', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{success}</div>}

      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '1rem', flexWrap: 'wrap' }}>
        <div className="wq-form-group" style={{ margin: 0 }}>
          <label>Filter By</label>
          <select className="wq-select" value={filterType} onChange={e => setFilterType(e.target.value)}>
            <option value="status">Status</option>
            <option value="caseId">Case ID</option>
            <option value="providerId">Provider ID</option>
          </select>
        </div>
        {filterType === 'status' && (
          <div className="wq-form-group" style={{ margin: 0 }}>
            <label>Status</label>
            <select className="wq-select" value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
              {FRAUD_STATUSES.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
            </select>
          </div>
        )}
        {filterType === 'caseId' && (
          <div className="wq-form-group" style={{ margin: 0 }}>
            <label>Case ID</label>
            <input className="wq-input" value={filterCaseId} onChange={e => setFilterCaseId(e.target.value)} placeholder="Enter Case ID" />
          </div>
        )}
        {filterType === 'providerId' && (
          <div className="wq-form-group" style={{ margin: 0 }}>
            <label>Provider ID</label>
            <input className="wq-input" value={filterProviderId} onChange={e => setFilterProviderId(e.target.value)} placeholder="Enter Provider ID" />
          </div>
        )}
        <button className="wq-btn wq-btn-primary" type="submit">Search</button>
        <button className="wq-btn wq-btn-primary" type="button" onClick={() => setShowCreateModal(true)}>New Fraud Case</button>
      </form>

      {loading ? <p>Loading...</p> : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Case ID</th><th>Provider ID</th><th>Fraud Type</th><th>Investigation Status</th>
              <th>Amount Involved</th><th>Created Date</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {fraudCases.length === 0 ? (
              <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>No fraud cases found.</td></tr>
            ) : fraudCases.map(fc => (
              <tr key={fc.id}>
                <td>{fc.caseId || '--'}</td>
                <td>{fc.providerId || '--'}</td>
                <td>{(fc.fraudType || '').replace(/_/g, ' ')}</td>
                <td><StatusBadge status={fc.investigationStatus || fc.status} colorMap={fraudStatusColors} /></td>
                <td>{fmtCurrency(fc.amountInvolved)}</td>
                <td>{fmtDate(fc.createdDate || fc.createdAt)}</td>
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                    {['OPEN', 'UNDER_INVESTIGATION'].includes(fc.investigationStatus || fc.status) && (
                      <button className="wq-btn wq-btn-sm wq-btn-primary"
                        onClick={() => { setSubstantiateModal(fc); setFindings(''); }}>Substantiate</button>
                    )}
                    {(fc.investigationStatus || fc.status) === 'SUBSTANTIATED' && (
                      <button className="wq-btn wq-btn-sm wq-btn-primary"
                        onClick={() => { setReferModal(fc); setReferNotes(''); }}>Refer to DA</button>
                    )}
                    {!['CLOSED', 'DISMISSED'].includes(fc.investigationStatus || fc.status) && (
                      <button className="wq-btn wq-btn-sm" onClick={() => handleClose(fc)}>Close</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* New Fraud Case Modal */}
      {showCreateModal && (
        <div className="wq-modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <h3 className="wq-modal-title">New Fraud Case</h3>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="wq-form-group">
                <label>Case ID</label>
                <input className="wq-input" value={createForm.caseId}
                  onChange={e => setCreateForm(p => ({ ...p, caseId: e.target.value }))} placeholder="Case ID" />
              </div>
              <div className="wq-form-group">
                <label>Provider ID</label>
                <input className="wq-input" value={createForm.providerId}
                  onChange={e => setCreateForm(p => ({ ...p, providerId: e.target.value }))} placeholder="Provider ID" />
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="wq-form-group">
                <label>Fraud Type *</label>
                <select className="wq-select" value={createForm.fraudType}
                  onChange={e => setCreateForm(p => ({ ...p, fraudType: e.target.value }))}>
                  {FRAUD_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
              <div className="wq-form-group">
                <label>Amount Involved</label>
                <input className="wq-input" type="number" step="0.01" value={createForm.amountInvolved}
                  onChange={e => setCreateForm(p => ({ ...p, amountInvolved: e.target.value }))} placeholder="0.00" />
              </div>
            </div>
            <div className="wq-form-group">
              <label>Allegation Summary *</label>
              <textarea className="wq-input" rows={4} value={createForm.allegationSummary}
                onChange={e => setCreateForm(p => ({ ...p, allegationSummary: e.target.value }))}
                placeholder="Describe the fraud allegation..." />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setShowCreateModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Substantiate Modal */}
      {substantiateModal && (
        <div className="wq-modal-overlay" onClick={() => setSubstantiateModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <h3 className="wq-modal-title">Substantiate Fraud Case #{substantiateModal.id}</h3>
            <div className="wq-form-group">
              <label>Findings *</label>
              <textarea className="wq-input" rows={4} value={findings}
                onChange={e => setFindings(e.target.value)}
                placeholder="Enter investigation findings..." />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setSubstantiateModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleSubstantiate} disabled={!findings.trim()}>Substantiate</button>
            </div>
          </div>
        </div>
      )}

      {/* Refer to DA Modal */}
      {referModal && (
        <div className="wq-modal-overlay" onClick={() => setReferModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <h3 className="wq-modal-title">Refer to DA - Fraud Case #{referModal.id}</h3>
            <div className="wq-form-group">
              <label>Referral Notes</label>
              <textarea className="wq-input" rows={3} value={referNotes}
                onChange={e => setReferNotes(e.target.value)}
                placeholder="Optional notes for DA referral..." />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setReferModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleReferToDA}>Refer to DA</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

// ==================== Targeted Mailings Tab ====================

const TargetedMailingsTab = () => {
  const [mailings, setMailings] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // New Mailing modal
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    mailingName: '', recipientType: 'RECIPIENT', language: 'ENGLISH',
    countyCode: '', templateName: '', targetCriteria: '{}'
  });
  const [saving, setSaving] = useState(false);

  // Schedule modal
  const [scheduleModal, setScheduleModal] = useState(null);
  const [scheduledDate, setScheduledDate] = useState('');

  useEffect(() => { loadMailings(); }, []);

  const loadMailings = async () => {
    setLoading(true); setError('');
    try {
      const res = await reportsApi.getTargetedMailings();
      setMailings(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      console.warn('[ReportsAdmin] Load mailings error:', err?.message);
      setError('Failed to load targeted mailings.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    if (!createForm.mailingName.trim()) { setError('Mailing name is required.'); return; }
    setSaving(true); setError('');
    try {
      let criteria = {};
      try { criteria = JSON.parse(createForm.targetCriteria); } catch { setError('Invalid JSON in target criteria.'); setSaving(false); return; }
      const payload = { ...createForm, targetCriteria: criteria };
      const res = await reportsApi.createTargetedMailing(payload);
      setMailings(prev => [res.data, ...prev]);
      setShowCreateModal(false);
      setCreateForm({ mailingName: '', recipientType: 'RECIPIENT', language: 'ENGLISH', countyCode: '', templateName: '', targetCriteria: '{}' });
      setSuccess('Targeted mailing created.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create mailing.');
    } finally {
      setSaving(false);
    }
  };

  const handleSchedule = async () => {
    if (!scheduleModal || !scheduledDate) return;
    try {
      const res = await reportsApi.scheduleMailing(scheduleModal.id, { scheduledDate });
      setMailings(prev => prev.map(m => m.id === scheduleModal.id ? res.data : m));
      setScheduleModal(null); setScheduledDate('');
      setSuccess('Mailing scheduled.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to schedule mailing.');
    }
  };

  const handleExecute = async (mailing) => {
    if (!window.confirm(`Execute mailing "${mailing.mailingName}" now?`)) return;
    try {
      const res = await reportsApi.executeMailing(mailing.id);
      setMailings(prev => prev.map(m => m.id === mailing.id ? res.data : m));
      setSuccess('Mailing execution started.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to execute mailing.');
    }
  };

  const handleCancel = async (mailing) => {
    if (!window.confirm(`Cancel mailing "${mailing.mailingName}"?`)) return;
    try {
      const res = await reportsApi.cancelMailing(mailing.id);
      setMailings(prev => prev.map(m => m.id === mailing.id ? res.data : m));
      setSuccess('Mailing cancelled.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to cancel mailing.');
    }
  };

  return (
    <>
      {error && <div style={{ color: '#c53030', backgroundColor: '#fff5f5', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ color: '#276749', backgroundColor: '#f0fff4', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{success}</div>}

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={() => setShowCreateModal(true)}>New Mailing</button>
        <button className="wq-btn" onClick={loadMailings}>Refresh</button>
      </div>

      {loading ? <p>Loading...</p> : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Mailing Name</th><th>Recipient Type</th><th>Language</th><th>Status</th>
              <th>Scheduled Date</th><th>Created Date</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {mailings.length === 0 ? (
              <tr><td colSpan={7} style={{ textAlign: 'center', padding: '2rem' }}>No targeted mailings found.</td></tr>
            ) : mailings.map(m => (
              <tr key={m.id}>
                <td><strong>{m.mailingName}</strong></td>
                <td>{(m.recipientType || '').replace(/_/g, ' ')}</td>
                <td>{m.language}</td>
                <td><StatusBadge status={m.status} colorMap={MAILING_STATUSES} /></td>
                <td>{fmtDate(m.scheduledDate)}</td>
                <td>{fmtDate(m.createdDate || m.createdAt)}</td>
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                    {m.status === 'DRAFT' && (
                      <button className="wq-btn wq-btn-sm wq-btn-primary"
                        onClick={() => { setScheduleModal(m); setScheduledDate(''); }}>Schedule</button>
                    )}
                    {['DRAFT', 'SCHEDULED'].includes(m.status) && (
                      <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleExecute(m)}>Execute</button>
                    )}
                    {['DRAFT', 'SCHEDULED'].includes(m.status) && (
                      <button className="wq-btn wq-btn-sm" onClick={() => handleCancel(m)}>Cancel</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* New Mailing Modal */}
      {showCreateModal && (
        <div className="wq-modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <h3 className="wq-modal-title">New Targeted Mailing</h3>
            <div className="wq-form-group">
              <label>Mailing Name *</label>
              <input className="wq-input" value={createForm.mailingName}
                onChange={e => setCreateForm(p => ({ ...p, mailingName: e.target.value }))}
                placeholder="Mailing campaign name" />
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="wq-form-group">
                <label>Recipient Type</label>
                <select className="wq-select" value={createForm.recipientType}
                  onChange={e => setCreateForm(p => ({ ...p, recipientType: e.target.value }))}>
                  {RECIPIENT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
              </div>
              <div className="wq-form-group">
                <label>Language</label>
                <select className="wq-select" value={createForm.language}
                  onChange={e => setCreateForm(p => ({ ...p, language: e.target.value }))}>
                  {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
                </select>
              </div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="wq-form-group">
                <label>County Code</label>
                <input className="wq-input" value={createForm.countyCode}
                  onChange={e => setCreateForm(p => ({ ...p, countyCode: e.target.value }))}
                  placeholder="e.g. 19" />
              </div>
              <div className="wq-form-group">
                <label>Template Name</label>
                <input className="wq-input" value={createForm.templateName}
                  onChange={e => setCreateForm(p => ({ ...p, templateName: e.target.value }))}
                  placeholder="Template identifier" />
              </div>
            </div>
            <div className="wq-form-group">
              <label>Target Criteria (JSON)</label>
              <textarea className="wq-input" rows={4} value={createForm.targetCriteria}
                onChange={e => setCreateForm(p => ({ ...p, targetCriteria: e.target.value }))}
                style={{ fontFamily: 'monospace', fontSize: '0.85rem' }} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setShowCreateModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Schedule Modal */}
      {scheduleModal && (
        <div className="wq-modal-overlay" onClick={() => setScheduleModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '400px' }}>
            <h3 className="wq-modal-title">Schedule Mailing</h3>
            <p style={{ margin: '0 0 0.75rem', color: '#718096' }}>{scheduleModal.mailingName}</p>
            <div className="wq-form-group">
              <label>Scheduled Date *</label>
              <input className="wq-input" type="date" value={scheduledDate}
                onChange={e => setScheduledDate(e.target.value)} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setScheduleModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleSchedule} disabled={!scheduledDate}>Schedule</button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

// ==================== Death Match Tab ====================

const DeathMatchTab = () => {
  const [matches, setMatches] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Verify modal
  const [verifyModal, setVerifyModal] = useState(null);

  // False Match modal
  const [falseMatchModal, setFalseMatchModal] = useState(null);
  const [falseMatchReason, setFalseMatchReason] = useState('');

  // Record Action modal
  const [actionModal, setActionModal] = useState(null);
  const [actionTaken, setActionTaken] = useState('');
  const [actionNotes, setActionNotes] = useState('');

  useEffect(() => { loadMatches(); }, []);

  const loadMatches = async () => {
    setLoading(true); setError('');
    try {
      const res = await reportsApi.getPendingDeathMatches();
      setMatches(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      console.warn('[ReportsAdmin] Load death matches error:', err?.message);
      setError('Failed to load death match records.');
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async () => {
    if (!verifyModal) return;
    try {
      const res = await reportsApi.verifyDeathMatch(verifyModal.id, { verifiedDate: new Date().toISOString() });
      setMatches(prev => prev.map(m => m.id === verifyModal.id ? res.data : m));
      setVerifyModal(null);
      setSuccess('Death match verified.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to verify death match.');
    }
  };

  const handleFalseMatch = async () => {
    if (!falseMatchModal || !falseMatchReason.trim()) return;
    try {
      const res = await reportsApi.markFalseMatch(falseMatchModal.id, { reason: falseMatchReason.trim() });
      setMatches(prev => prev.map(m => m.id === falseMatchModal.id ? res.data : m));
      setFalseMatchModal(null); setFalseMatchReason('');
      setSuccess('Marked as false match.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to mark as false match.');
    }
  };

  const handleRecordAction = async () => {
    if (!actionModal || !actionTaken.trim()) return;
    try {
      const res = await reportsApi.recordDeathMatchAction(actionModal.id, {
        actionTaken: actionTaken.trim(),
        notes: actionNotes.trim()
      });
      setMatches(prev => prev.map(m => m.id === actionModal.id ? res.data : m));
      setActionModal(null); setActionTaken(''); setActionNotes('');
      setSuccess('Action recorded.');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to record action.');
    }
  };

  return (
    <>
      {error && <div style={{ color: '#c53030', backgroundColor: '#fff5f5', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ color: '#276749', backgroundColor: '#f0fff4', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>{success}</div>}

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <button className="wq-btn" onClick={loadMatches}>Refresh</button>
        <span style={{ alignSelf: 'center', color: '#718096', fontSize: '0.875rem' }}>
          Showing pending death matches from CDPH, SCO, SSA, and MEDS
        </span>
      </div>

      {loading ? <p>Loading...</p> : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Person ID</th><th>Person Type</th><th>First Name</th><th>Last Name</th>
              <th>DOB</th><th>Source</th><th>Match Date</th><th>Verification Status</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {matches.length === 0 ? (
              <tr><td colSpan={9} style={{ textAlign: 'center', padding: '2rem' }}>No pending death matches found.</td></tr>
            ) : matches.map(m => (
              <tr key={m.id}>
                <td><strong>{m.personId}</strong></td>
                <td>{(m.personType || '').replace(/_/g, ' ')}</td>
                <td>{m.firstName}</td>
                <td>{m.lastName}</td>
                <td>{fmtDate(m.dob || m.dateOfBirth)}</td>
                <td>
                  <span style={{
                    padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600,
                    backgroundColor: '#edf2f7', color: '#2d3748'
                  }}>
                    {m.source}
                  </span>
                </td>
                <td>{fmtDate(m.matchDate)}</td>
                <td><StatusBadge status={m.verificationStatus || m.status} colorMap={DEATH_MATCH_STATUSES} /></td>
                <td>
                  <div style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                    {(m.verificationStatus || m.status) === 'PENDING' && (
                      <>
                        <button className="wq-btn wq-btn-sm wq-btn-primary"
                          onClick={() => setVerifyModal(m)}>Verify</button>
                        <button className="wq-btn wq-btn-sm"
                          onClick={() => { setFalseMatchModal(m); setFalseMatchReason(''); }}>False Match</button>
                      </>
                    )}
                    {(m.verificationStatus || m.status) === 'VERIFIED' && (
                      <button className="wq-btn wq-btn-sm wq-btn-primary"
                        onClick={() => { setActionModal(m); setActionTaken(''); setActionNotes(''); }}>Record Action</button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {/* Verify Modal */}
      {verifyModal && (
        <div className="wq-modal-overlay" onClick={() => setVerifyModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <h3 className="wq-modal-title">Verify Death Match</h3>
            <p style={{ margin: '0 0 0.5rem' }}>
              <strong>{verifyModal.firstName} {verifyModal.lastName}</strong> (Person ID: {verifyModal.personId})
            </p>
            <p style={{ margin: '0 0 0.5rem', color: '#718096' }}>
              Source: {verifyModal.source} | Match Date: {fmtDate(verifyModal.matchDate)}
            </p>
            <p style={{ margin: '0.75rem 0', color: '#c53030', fontWeight: 500 }}>
              Confirming this match will mark the death as verified and record today's date as the verification date.
            </p>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setVerifyModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleVerify}>Confirm Verify</button>
            </div>
          </div>
        </div>
      )}

      {/* False Match Modal */}
      {falseMatchModal && (
        <div className="wq-modal-overlay" onClick={() => setFalseMatchModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <h3 className="wq-modal-title">Mark as False Match</h3>
            <p style={{ margin: '0 0 0.75rem' }}>
              <strong>{falseMatchModal.firstName} {falseMatchModal.lastName}</strong> (Person ID: {falseMatchModal.personId})
            </p>
            <div className="wq-form-group">
              <label>Reason *</label>
              <textarea className="wq-input" rows={3} value={falseMatchReason}
                onChange={e => setFalseMatchReason(e.target.value)}
                placeholder="Explain why this is a false match..." />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setFalseMatchModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleFalseMatch} disabled={!falseMatchReason.trim()}>
                Mark False Match
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Record Action Modal */}
      {actionModal && (
        <div className="wq-modal-overlay" onClick={() => setActionModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <h3 className="wq-modal-title">Record Action - Death Match</h3>
            <p style={{ margin: '0 0 0.75rem' }}>
              <strong>{actionModal.firstName} {actionModal.lastName}</strong> (Person ID: {actionModal.personId})
            </p>
            <div className="wq-form-group">
              <label>Action Taken *</label>
              <input className="wq-input" value={actionTaken}
                onChange={e => setActionTaken(e.target.value)}
                placeholder="e.g. Case terminated, benefits stopped" />
            </div>
            <div className="wq-form-group">
              <label>Notes</label>
              <textarea className="wq-input" rows={3} value={actionNotes}
                onChange={e => setActionNotes(e.target.value)}
                placeholder="Additional details..." />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn" onClick={() => setActionModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleRecordAction} disabled={!actionTaken.trim()}>
                Record Action
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
