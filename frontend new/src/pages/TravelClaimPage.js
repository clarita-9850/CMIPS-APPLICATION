import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as tsApi from '../api/timesheetApi';
import './WorkQueues.css';

const CLAIM_STATUSES = [
  '', 'PENDING_ISSUANCE', 'ISSUED', 'RECEIVED', 'HOLD_TIMESHEET_NOT_PROCESSED',
  'VALIDATING', 'EXCEPTION', 'APPROVED_FOR_PAYROLL', 'SENT_TO_PAYROLL', 'PROCESSED', 'REJECTED', 'VOID'
];

const statusColor = (s) => {
  if (!s) return '#888';
  if (s.startsWith('HOLD_')) return '#b45309';
  if (s === 'EXCEPTION' || s === 'REJECTED') return '#dc2626';
  if (s === 'APPROVED_FOR_PAYROLL' || s === 'PROCESSED') return '#16a34a';
  if (s === 'VOID') return '#6b7280';
  return '#0369a1';
};

const buildDailyGrid = (start, end) => {
  if (!start || !end) return [];
  const s = new Date(start + 'T00:00:00');
  const e = new Date(end + 'T00:00:00');
  const days = [];
  for (let d = new Date(s); d <= e; d.setDate(d.getDate() + 1)) {
    const iso = d.toISOString().split('T')[0];
    const dow = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][d.getDay()];
    days.push({ date: iso, dow, hours: '', hasPaidServiceHours: true, hasActiveTravelRecord: true });
  }
  return days;
};

export const TravelClaimPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [activeView, setActiveView] = useState('search');
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [filters, setFilters] = useState({ caseId: '', providerId: '', status: '', fromDate: '', toDate: '' });
  const [form, setForm] = useState({
    caseId: '', recipientId: '', providerId: '', programType: 'IHSS',
    payPeriodStart: '', payPeriodEnd: '', timesheetId: '',
    providerEligibleForTravel: true, countyCode: '', createdBy: ''
  });
  const [dailyEntries, setDailyEntries] = useState([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'Travel Claims' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    setDailyEntries(buildDailyGrid(form.payPeriodStart, form.payPeriodEnd));
  }, [form.payPeriodStart, form.payPeriodEnd]);

  const setFi = (k, v) => setFilters(prev => ({ ...prev, [k]: v }));
  const setFo = (k, v) => setForm(prev => ({ ...prev, [k]: v }));
  const updateDay = (idx, field, val) => {
    setDailyEntries(prev => prev.map((d, i) => i === idx ? { ...d, [field]: val } : d));
  };

  const handleSearch = async () => {
    setLoading(true); setError('');
    try {
      const params = {};
      Object.entries(filters).forEach(([k, v]) => { if (v) params[k] = v; });
      const data = await tsApi.searchTravelClaims(params);
      setClaims(Array.isArray(data) ? data : []);
    } catch (err) { setError('Search failed: ' + (err?.message || 'Error')); }
    finally { setLoading(false); }
  };

  const loadDetail = async (id) => {
    try {
      const d = await tsApi.getTravelClaimById(id);
      setDetail(d);
      setActiveView('detail');
    } catch (err) { setError('Failed to load claim: ' + (err?.message || 'Error')); }
  };

  const handleCreate = async (andValidate = false) => {
    if (!form.caseId || !form.recipientId || !form.providerId || !form.payPeriodStart || !form.payPeriodEnd) {
      setError('Case ID, Recipient ID, Provider ID, and Pay Period are required.');
      return;
    }
    setError(''); setSuccess(''); setSubmitting(true);
    try {
      const payload = {
        ...form,
        caseId: parseInt(form.caseId),
        recipientId: parseInt(form.recipientId),
        providerId: parseInt(form.providerId),
        timesheetId: form.timesheetId ? parseInt(form.timesheetId) : null,
        dailyEntries: dailyEntries.filter(d => d.hours).map(d => ({
          date: d.date,
          hours: parseFloat(d.hours),
          hasPaidServiceHours: d.hasPaidServiceHours,
          hasActiveTravelRecord: d.hasActiveTravelRecord
        }))
      };
      const tc = await tsApi.createTravelClaim(payload);
      if (andValidate && tc?.id) {
        await tsApi.validateTravelClaim(tc.id);
        setSuccess(`Travel claim ${tc.travelClaimNumber || tc.id} created and validated.`);
      } else {
        setSuccess(`Travel claim ${tc.travelClaimNumber || tc.id} created.`);
      }
      setTimeout(() => { loadDetail(tc.id); }, 1000);
    } catch (err) {
      setError('Failed: ' + (err?.response?.data?.error || err?.message || 'Error'));
    } finally { setSubmitting(false); }
  };

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
  const fmtHrs = (h) => h != null ? Number(h).toFixed(1) : '\u2014';
  const totalClaimed = dailyEntries.reduce((s, d) => s + (parseFloat(d.hours) || 0), 0);

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>IHSS Travel Claims (SOC 2275)</h2>
      </div>

      {error && <div style={{ padding: '0.75rem', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '6px', color: '#dc2626', marginBottom: '1rem' }}>{error}</div>}
      {success && <div style={{ padding: '0.75rem', background: '#f0fdf4', border: '1px solid #86efac', borderRadius: '6px', color: '#16a34a', marginBottom: '1rem' }}>{success}</div>}

      {/* View Tabs */}
      <div style={{ display: 'flex', gap: 0, borderBottom: '2px solid #e2e8f0', marginBottom: '1rem' }}>
        {['search', 'create', 'detail'].filter(v => v !== 'detail' || detail).map(v => (
          <button key={v} onClick={() => setActiveView(v)}
            style={{
              padding: '0.6rem 1.2rem', border: 'none', cursor: 'pointer', fontWeight: 500, fontSize: '0.85rem',
              borderBottom: activeView === v ? '3px solid #153554' : '3px solid transparent',
              background: activeView === v ? '#f0f7ff' : 'transparent',
              color: activeView === v ? '#153554' : '#666'
            }}>
            {v === 'search' ? 'Search Claims' : v === 'create' ? 'New Travel Claim' : 'Claim Detail'}
          </button>
        ))}
      </div>

      {/* SEARCH VIEW */}
      {activeView === 'search' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Search Filters</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Case ID</label>
                  <input type="text" value={filters.caseId} onChange={e => setFi('caseId', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>Provider ID</label>
                  <input type="text" value={filters.providerId} onChange={e => setFi('providerId', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>Status</label>
                  <select value={filters.status} onChange={e => setFi('status', e.target.value)}>
                    {CLAIM_STATUSES.map(s => <option key={s} value={s}>{s || 'All'}</option>)}
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>From Date</label>
                  <input type="date" value={filters.fromDate} onChange={e => setFi('fromDate', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>To Date</label>
                  <input type="date" value={filters.toDate} onChange={e => setFi('toDate', e.target.value)} />
                </div>
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <button className="wq-btn wq-btn-primary" onClick={handleSearch}>Search</button>
                <button className="wq-btn wq-btn-outline" style={{ marginLeft: '0.5rem' }} onClick={() => { setFilters({ caseId: '', providerId: '', status: '', fromDate: '', toDate: '' }); setClaims([]); }}>Clear</button>
              </div>
            </div>
          </div>

          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Results ({claims.length})</h4></div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {loading ? <p style={{ padding: '1rem', color: '#888' }}>Loading...</p> :
               claims.length === 0 ? <p style={{ padding: '1rem', color: '#888' }}>No claims found.</p> : (
                <table className="wq-table">
                  <thead>
                    <tr>
                      <th>Claim #</th>
                      <th>Case</th>
                      <th>Provider</th>
                      <th>Program</th>
                      <th>Pay Period</th>
                      <th>Hrs Claimed</th>
                      <th>Hrs Approved</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {claims.map(c => (
                      <tr key={c.id} className="wq-clickable-row" onClick={() => loadDetail(c.id)}>
                        <td style={{ fontWeight: 600 }}>{c.travelClaimNumber || c.id}</td>
                        <td>{c.caseId}</td>
                        <td>{c.providerId}</td>
                        <td>{c.programType}</td>
                        <td>{fmtDate(c.payPeriodStart)} - {fmtDate(c.payPeriodEnd)}</td>
                        <td>{fmtHrs(c.totalTravelHoursClaimed)}</td>
                        <td>{fmtHrs(c.totalTravelHoursApproved)}</td>
                        <td><span style={{ padding: '2px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 600, color: '#fff', background: statusColor(c.status) }}>{c.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </>
      )}

      {/* CREATE VIEW */}
      {activeView === 'create' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>New Travel Claim</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field"><label>Case ID *</label><input type="number" value={form.caseId} onChange={e => setFo('caseId', e.target.value)} /></div>
                <div className="wq-form-field"><label>Recipient ID *</label><input type="number" value={form.recipientId} onChange={e => setFo('recipientId', e.target.value)} /></div>
                <div className="wq-form-field"><label>Provider ID *</label><input type="number" value={form.providerId} onChange={e => setFo('providerId', e.target.value)} /></div>
                <div className="wq-form-field">
                  <label>Program</label>
                  <select value={form.programType} onChange={e => setFo('programType', e.target.value)}>
                    <option value="IHSS">IHSS</option><option value="WPCS">WPCS</option>
                  </select>
                </div>
                <div className="wq-form-field"><label>Linked Timesheet ID</label><input type="number" value={form.timesheetId} onChange={e => setFo('timesheetId', e.target.value)} placeholder="Optional" /></div>
                <div className="wq-form-field"><label>County Code</label><input type="text" value={form.countyCode} onChange={e => setFo('countyCode', e.target.value)} /></div>
                <div className="wq-form-field"><label>Pay Period Start *</label><input type="date" value={form.payPeriodStart} onChange={e => setFo('payPeriodStart', e.target.value)} /></div>
                <div className="wq-form-field"><label>Pay Period End *</label><input type="date" value={form.payPeriodEnd} onChange={e => setFo('payPeriodEnd', e.target.value)} /></div>
              </div>
              <div style={{ marginTop: '0.5rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <input type="checkbox" checked={form.providerEligibleForTravel} onChange={e => setFo('providerEligibleForTravel', e.target.checked)} />
                  Provider Eligible for Travel
                </label>
              </div>
            </div>
          </div>

          {dailyEntries.length > 0 && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Daily Travel Hours</h4>
                <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Total: {totalClaimed.toFixed(1)} hrs (7hr/wk cap, 14hr/wk max)</span>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                <table className="wq-table" style={{ fontSize: '0.8rem' }}>
                  <thead>
                    <tr><th>Day</th><th>Date</th><th>DOW</th><th>Travel Hours</th><th>Paid Service Same Day</th><th>Active Travel Record</th></tr>
                  </thead>
                  <tbody>
                    {dailyEntries.map((d, i) => (
                      <tr key={i} style={{ background: d.dow === 'Sat' || d.dow === 'Sun' ? '#f8fafc' : 'transparent' }}>
                        <td>{i + 1}</td>
                        <td>{d.date}</td>
                        <td>{d.dow}</td>
                        <td><input type="number" min="0" max="7" step="0.25" value={d.hours} onChange={e => updateDay(i, 'hours', e.target.value)} style={{ width: '70px', padding: '0.25rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '4px' }} /></td>
                        <td><input type="checkbox" checked={d.hasPaidServiceHours} onChange={e => updateDay(i, 'hasPaidServiceHours', e.target.checked)} /></td>
                        <td><input type="checkbox" checked={d.hasActiveTravelRecord} onChange={e => updateDay(i, 'hasActiveTravelRecord', e.target.checked)} /></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={() => handleCreate(false)} disabled={submitting}>{submitting ? 'Saving...' : 'Save Claim'}</button>
            <button className="wq-btn wq-btn-primary" style={{ background: '#16a34a' }} onClick={() => handleCreate(true)} disabled={submitting}>Save & Validate</button>
            <button className="wq-btn wq-btn-outline" onClick={() => setActiveView('search')} disabled={submitting}>Cancel</button>
          </div>
        </>
      )}

      {/* DETAIL VIEW */}
      {activeView === 'detail' && detail && (
        <>
          {(() => {
            const tc = detail.travelClaim || detail;
            const entries = detail.timeEntries || [];
            const exceptions = detail.exceptions || [];
            return (
              <>
                <div className="wq-panel">
                  <div className="wq-panel-header">
                    <h4>Travel Claim: {tc.travelClaimNumber || tc.id}</h4>
                    <span style={{ padding: '2px 10px', borderRadius: '4px', fontSize: '0.8rem', fontWeight: 600, color: '#fff', background: statusColor(tc.status) }}>{tc.status}</span>
                  </div>
                  <div className="wq-panel-body">
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '0.75rem' }}>
                      <Fld label="Case ID" val={tc.caseId} />
                      <Fld label="Recipient ID" val={tc.recipientId} />
                      <Fld label="Provider ID" val={tc.providerId} />
                      <Fld label="Program" val={tc.programType} />
                      <Fld label="Pay Period" val={`${fmtDate(tc.payPeriodStart)} - ${fmtDate(tc.payPeriodEnd)}`} />
                      <Fld label="Linked Timesheet" val={tc.timesheetId || '\u2014'} />
                      <Fld label="Hrs Claimed" val={fmtHrs(tc.totalTravelHoursClaimed)} />
                      <Fld label="Hrs Approved" val={fmtHrs(tc.totalTravelHoursApproved)} />
                      <Fld label="Hrs Cutback" val={fmtHrs(tc.travelHoursCutback)} />
                      <Fld label="Weekly Cap" val={`${tc.weeklyTravelHoursCap || 7} hrs`} />
                      <Fld label="Provider Eligible" val={tc.providerEligibleForTravel ? 'Yes' : 'No'} />
                      <Fld label="Date Received" val={fmtDate(tc.dateReceived)} />
                    </div>
                  </div>
                </div>

                {entries.length > 0 && (
                  <div className="wq-panel">
                    <div className="wq-panel-header"><h4>Daily Travel Entries</h4></div>
                    <div className="wq-panel-body" style={{ padding: 0 }}>
                      <table className="wq-table" style={{ fontSize: '0.8rem' }}>
                        <thead><tr><th>Date</th><th>Hrs Claimed</th><th>Hrs Approved</th><th>Cutback</th><th>Cutback Reason</th><th>Paid Svc</th><th>Travel Rec</th></tr></thead>
                        <tbody>
                          {entries.map((e, i) => (
                            <tr key={i}>
                              <td>{fmtDate(e.entryDate)}</td>
                              <td>{fmtHrs(e.travelHoursClaimed)}</td>
                              <td style={{ color: '#16a34a' }}>{fmtHrs(e.travelHoursApproved)}</td>
                              <td style={{ color: e.travelHoursCutback > 0 ? '#dc2626' : '#888' }}>{fmtHrs(e.travelHoursCutback)}</td>
                              <td style={{ fontSize: '0.7rem' }}>{e.cutbackReason || '\u2014'}</td>
                              <td>{e.hasPaidServiceHours ? 'Yes' : <span style={{ color: '#dc2626' }}>No</span>}</td>
                              <td>{e.hasActiveTravelRecord ? 'Yes' : <span style={{ color: '#dc2626' }}>No</span>}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}

                {exceptions.length > 0 && (
                  <div className="wq-panel">
                    <div className="wq-panel-header"><h4>Validation Exceptions ({exceptions.length})</h4></div>
                    <div className="wq-panel-body">
                      {exceptions.map((ex, i) => (
                        <div key={i} style={{ padding: '0.4rem 0.75rem', margin: '0.25rem 0', borderRadius: '4px', background: '#fef2f2', border: '1px solid #fca5a522', display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
                          <span style={{ fontWeight: 700, fontSize: '0.75rem', color: '#dc2626' }}>Rule {ex.ruleNumber}</span>
                          <span style={{ fontSize: '0.75rem', color: '#4a5568', padding: '1px 6px', background: '#f1f5f9', borderRadius: '3px' }}>{ex.errorCode}</span>
                          <span style={{ fontSize: '0.8rem' }}>{ex.message}</span>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <button className="wq-btn wq-btn-outline" onClick={() => { setDetail(null); setActiveView('search'); }}>Back to Search</button>
              </>
            );
          })()}
        </>
      )}
    </div>
  );
};

const Fld = ({ label, val }) => (
  <div>
    <div style={{ fontSize: '0.7rem', color: '#888', textTransform: 'uppercase' }}>{label}</div>
    <div style={{ fontSize: '0.9rem', fontWeight: 500 }}>{val || '\u2014'}</div>
  </div>
);
