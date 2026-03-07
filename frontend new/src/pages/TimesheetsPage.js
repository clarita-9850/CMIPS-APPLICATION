import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as tsApi from '../api/timesheetApi';
import './WorkQueues.css';

const STATUSES = [
  '', 'PENDING_ISSUANCE', 'ISSUED', 'RECEIVED', 'VALIDATING', 'EXCEPTION',
  'HOLD_EARLY_SUBMISSION', 'HOLD_LATE_SUBMISSION', 'HOLD_EXCESSIVE_HOURS',
  'HOLD_RANDOM_SAMPLING', 'HOLD_FLAGGED_REVIEW', 'HOLD_BVI_REVIEW', 'HOLD_USER_REVIEW',
  'APPROVED_FOR_PAYROLL', 'SENT_TO_PAYROLL', 'PROCESSED', 'REJECTED', 'VOID', 'CANCELLED'
];

const statusColor = (s) => {
  if (!s) return '#888';
  if (s.startsWith('HOLD_')) return '#b45309';
  if (s === 'EXCEPTION' || s === 'REJECTED') return '#dc2626';
  if (s === 'APPROVED_FOR_PAYROLL' || s === 'PROCESSED') return '#16a34a';
  if (s === 'SENT_TO_PAYROLL') return '#2563eb';
  if (s === 'VOID' || s === 'CANCELLED') return '#6b7280';
  return '#0369a1';
};

export const TimesheetsPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [timesheets, setTimesheets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [dashboard, setDashboard] = useState(null);
  const [filters, setFilters] = useState({
    caseId: '', recipientId: '', providerId: '', status: '',
    programType: '', fromDate: '', toDate: '', countyCode: ''
  });
  const [activeTab, setActiveTab] = useState('search');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'IHSS Timesheets' }]);
    loadDashboard();
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const loadDashboard = async () => {
    try { setDashboard(await tsApi.getDashboard()); } catch {}
  };

  const handleSearch = async () => {
    setLoading(true);
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => { if (v) params[k] = v; });
      const data = await tsApi.searchTimesheets(params);
      setTimesheets(Array.isArray(data) ? data : []);
    } catch (err) {
      console.warn('[Timesheets] Search error:', err?.message);
      setTimesheets([]);
    } finally { setLoading(false); }
  };

  const handleStatusClick = async (status) => {
    setFilters(prev => ({ ...prev, status }));
    setActiveTab('search');
    setLoading(true);
    try {
      const data = await tsApi.getTimesheetsByStatus(status);
      setTimesheets(Array.isArray(data) ? data : []);
    } catch { setTimesheets([]); }
    finally { setLoading(false); }
  };

  const handleBatchRelease = async () => {
    try {
      const r = await tsApi.batchReleaseHeld();
      alert(`Released ${r.released} held timesheets`);
      loadDashboard();
    } catch (err) { alert('Batch release failed: ' + (err?.message || 'Error')); }
  };

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
  const fmtHrs = (h) => h != null ? Number(h).toFixed(1) : '\u2014';
  const setF = (k, v) => setFilters(prev => ({ ...prev, [k]: v }));

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>IHSS Timesheets</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="wq-btn wq-btn-primary" onClick={() => navigate('/payments/timesheets/new')}>Manual Entry</button>
          <button className="wq-btn wq-btn-outline" onClick={handleBatchRelease}>Batch Release Held</button>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0', borderBottom: '2px solid #e2e8f0', marginBottom: '1rem' }}>
        {['dashboard', 'search', 'sampling', 'flagged'].map(tab => (
          <button key={tab} onClick={() => setActiveTab(tab)}
            style={{
              padding: '0.6rem 1.2rem', border: 'none', cursor: 'pointer', fontWeight: 500, fontSize: '0.85rem',
              borderBottom: activeTab === tab ? '3px solid #153554' : '3px solid transparent',
              background: activeTab === tab ? '#f0f7ff' : 'transparent',
              color: activeTab === tab ? '#153554' : '#666'
            }}>
            {tab === 'dashboard' ? 'Dashboard' : tab === 'search' ? 'Search' :
             tab === 'sampling' ? 'Random Sampling' : 'Flagged Review'}
          </button>
        ))}
      </div>

      {/* DASHBOARD TAB */}
      {activeTab === 'dashboard' && dashboard && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Timesheet Dashboard</h4></div>
          <div className="wq-panel-body">
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.75rem' }}>
              {Object.entries(dashboard).map(([k, v]) => (
                <div key={k} onClick={() => { const st = k === 'pendingValidation' ? 'RECEIVED' : k === 'inException' ? 'EXCEPTION' : k === 'heldEarly' ? 'HOLD_EARLY_SUBMISSION' : k === 'heldLate' ? 'HOLD_LATE_SUBMISSION' : k === 'heldExcessive' ? 'HOLD_EXCESSIVE_HOURS' : k === 'heldSampling' ? 'HOLD_RANDOM_SAMPLING' : k === 'heldFlagged' ? 'HOLD_FLAGGED_REVIEW' : k === 'heldBvi' ? 'HOLD_BVI_REVIEW' : k === 'approvedForPayroll' ? 'APPROVED_FOR_PAYROLL' : k === 'sentToPayroll' ? 'SENT_TO_PAYROLL' : 'PROCESSED'; handleStatusClick(st); }}
                  style={{ padding: '1rem', background: '#f8fafc', borderRadius: '8px', border: '1px solid #e2e8f0', cursor: 'pointer', textAlign: 'center' }}>
                  <div style={{ fontSize: '1.8rem', fontWeight: 700, color: '#153554' }}>{v}</div>
                  <div style={{ fontSize: '0.75rem', color: '#666', marginTop: '0.25rem' }}>
                    {k.replace(/([A-Z])/g, ' $1').replace(/^./, s => s.toUpperCase())}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* SEARCH TAB */}
      {activeTab === 'search' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Search Filters</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Case ID</label>
                  <input type="text" value={filters.caseId} onChange={e => setF('caseId', e.target.value)} placeholder="Case ID" />
                </div>
                <div className="wq-form-field">
                  <label>Recipient ID</label>
                  <input type="text" value={filters.recipientId} onChange={e => setF('recipientId', e.target.value)} placeholder="Recipient ID" />
                </div>
                <div className="wq-form-field">
                  <label>Provider ID</label>
                  <input type="text" value={filters.providerId} onChange={e => setF('providerId', e.target.value)} placeholder="Provider ID" />
                </div>
                <div className="wq-form-field">
                  <label>Status</label>
                  <select value={filters.status} onChange={e => setF('status', e.target.value)}>
                    {STATUSES.map(s => <option key={s} value={s}>{s || 'All'}</option>)}
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Program</label>
                  <select value={filters.programType} onChange={e => setF('programType', e.target.value)}>
                    <option value="">All</option>
                    <option value="IHSS">IHSS</option>
                    <option value="WPCS">WPCS</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>From Date</label>
                  <input type="date" value={filters.fromDate} onChange={e => setF('fromDate', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>To Date</label>
                  <input type="date" value={filters.toDate} onChange={e => setF('toDate', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>County Code</label>
                  <input type="text" value={filters.countyCode} onChange={e => setF('countyCode', e.target.value)} placeholder="e.g. 19" />
                </div>
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <button className="wq-btn wq-btn-primary" onClick={handleSearch}>Search</button>
                <button className="wq-btn wq-btn-outline" style={{ marginLeft: '0.5rem' }}
                  onClick={() => { setFilters({ caseId: '', recipientId: '', providerId: '', status: '', programType: '', fromDate: '', toDate: '', countyCode: '' }); setTimesheets([]); }}>
                  Clear
                </button>
              </div>
            </div>
          </div>

          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Results ({timesheets.length})</h4></div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {loading ? <p style={{ padding: '1rem', color: '#888' }}>Loading...</p> :
               timesheets.length === 0 ? <p style={{ padding: '1rem', color: '#888' }}>No timesheets found. Use filters above to search.</p> : (
                <table className="wq-table">
                  <thead>
                    <tr>
                      <th>TS #</th>
                      <th>Case ID</th>
                      <th>Provider</th>
                      <th>Program</th>
                      <th>Pay Period</th>
                      <th>Hours Claimed</th>
                      <th>Hours Approved</th>
                      <th>Source</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {timesheets.map(t => (
                      <tr key={t.id} className="wq-clickable-row" onClick={() => navigate(`/payments/timesheets/${t.id}`)}>
                        <td style={{ fontWeight: 600 }}>{t.timesheetNumber || t.id}</td>
                        <td>{t.caseId}</td>
                        <td>{t.providerId}</td>
                        <td>{t.programType}</td>
                        <td>{fmtDate(t.payPeriodStart)} - {fmtDate(t.payPeriodEnd)}</td>
                        <td>{fmtHrs(t.totalHoursClaimed)}</td>
                        <td>{fmtHrs(t.totalHoursApproved)}</td>
                        <td>{t.sourceType}</td>
                        <td><span style={{ padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, color: '#fff', background: statusColor(t.status) }}>{t.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </>
      )}

      {/* SAMPLING TAB */}
      {activeTab === 'sampling' && <SamplingTab navigate={navigate} />}

      {/* FLAGGED TAB */}
      {activeTab === 'flagged' && <FlaggedTab navigate={navigate} />}
    </div>
  );
};

// ── Random Sampling Sub-tab ──
const SamplingTab = ({ navigate }) => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try { setItems(await tsApi.getSamplingPending()); } catch {}
      finally { setLoading(false); }
    })();
  }, []);

  const handleVerify = async (id, action) => {
    const reason = action === 'REJECT' ? prompt('Rejection reason:') : null;
    if (action === 'REJECT' && !reason) return;
    try {
      await tsApi.verifySampling(id, { action, verifiedBy: 'CURRENT_USER', reason });
      setItems(prev => prev.filter(i => i.id !== id));
    } catch (err) { alert('Error: ' + (err?.message || 'Failed')); }
  };

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';

  return (
    <div className="wq-panel">
      <div className="wq-panel-header"><h4>Random Sampling Queue ({items.length})</h4></div>
      <div className="wq-panel-body" style={{ padding: 0 }}>
        {loading ? <p style={{ padding: '1rem', color: '#888' }}>Loading...</p> :
         items.length === 0 ? <p style={{ padding: '1rem', color: '#888' }}>No timesheets pending sampling verification.</p> : (
          <table className="wq-table">
            <thead><tr><th>TS #</th><th>Case</th><th>Provider</th><th>Pay Period</th><th>Hours</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map(t => (
                <tr key={t.id}>
                  <td style={{ fontWeight: 600, cursor: 'pointer', color: '#2563eb' }} onClick={() => navigate(`/payments/timesheets/${t.id}`)}>{t.timesheetNumber || t.id}</td>
                  <td>{t.caseId}</td>
                  <td>{t.providerId}</td>
                  <td>{fmtDate(t.payPeriodStart)} - {fmtDate(t.payPeriodEnd)}</td>
                  <td>{t.totalHoursClaimed}</td>
                  <td>
                    <button className="wq-btn wq-btn-primary" style={{ marginRight: '0.25rem', fontSize: '0.75rem', padding: '0.25rem 0.5rem' }} onClick={() => handleVerify(t.id, 'APPROVE')}>Approve</button>
                    <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', color: '#dc2626', borderColor: '#dc2626' }} onClick={() => handleVerify(t.id, 'REJECT')}>Reject</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

// ── Flagged Review Sub-tab ──
const FlaggedTab = ({ navigate }) => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      try { setItems(await tsApi.getFlaggedPending()); } catch {}
      finally { setLoading(false); }
    })();
  }, []);

  const handleReview = async (id, action) => {
    const reason = action === 'REJECT' ? prompt('Rejection reason:') : null;
    if (action === 'REJECT' && !reason) return;
    try {
      await tsApi.completeFlaggedReview(id, { action, reviewedBy: 'CURRENT_USER', reason });
      setItems(prev => prev.filter(i => i.id !== id));
    } catch (err) { alert('Error: ' + (err?.message || 'Failed')); }
  };

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';

  return (
    <div className="wq-panel">
      <div className="wq-panel-header"><h4>Flagged Review Queue ({items.length})</h4></div>
      <div className="wq-panel-body" style={{ padding: 0 }}>
        {loading ? <p style={{ padding: '1rem', color: '#888' }}>Loading...</p> :
         items.length === 0 ? <p style={{ padding: '1rem', color: '#888' }}>No timesheets pending flagged review.</p> : (
          <table className="wq-table">
            <thead><tr><th>TS #</th><th>Case</th><th>Provider</th><th>Pay Period</th><th>Hours</th><th>Actions</th></tr></thead>
            <tbody>
              {items.map(t => (
                <tr key={t.id}>
                  <td style={{ fontWeight: 600, cursor: 'pointer', color: '#2563eb' }} onClick={() => navigate(`/payments/timesheets/${t.id}`)}>{t.timesheetNumber || t.id}</td>
                  <td>{t.caseId}</td>
                  <td>{t.providerId}</td>
                  <td>{fmtDate(t.payPeriodStart)} - {fmtDate(t.payPeriodEnd)}</td>
                  <td>{t.totalHoursClaimed}</td>
                  <td>
                    <button className="wq-btn wq-btn-primary" style={{ marginRight: '0.25rem', fontSize: '0.75rem', padding: '0.25rem 0.5rem' }} onClick={() => handleReview(t.id, 'APPROVE')}>Approve</button>
                    <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem', color: '#dc2626', borderColor: '#dc2626' }} onClick={() => handleReview(t.id, 'REJECT')}>Reject</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};
