import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as payrollApi from '../api/payrollApi';
import './WorkQueues.css';

const TABS = [
  { key: 'payRates', label: 'Pay Rates' },
  { key: 'advancePay', label: 'Advance Pay' },
  { key: 'deductions', label: 'Deductions' },
  { key: 'tax', label: 'Tax' },
  { key: 'batches', label: 'Payroll Batches' },
  { key: 'earnings', label: 'Earnings' }
];

const RATE_TYPES = ['REGULAR', 'OVERTIME', 'WAIT_TIME', 'TRAVEL'];
const BATCH_TYPES = ['REGULAR', 'SUPPLEMENTAL', 'CORRECTION', 'ADVANCE'];
const DEDUCTION_TYPES = [
  'FEDERAL_TAX', 'STATE_TAX', 'FICA', 'MEDICARE', 'GARNISHMENT',
  'HEALTH_INSURANCE', 'DENTAL', 'VISION', 'UNION_DUES', 'OTHER'
];
const DEDUCTION_FREQUENCIES = ['PER_PAY_PERIOD', 'MONTHLY', 'QUARTERLY', 'ANNUAL', 'ONE_TIME'];

const ADVANCE_STATUS_COLORS = {
  PENDING:   { bg: '#fefcbf', color: '#744210' },
  ISSUED:    { bg: '#bee3f8', color: '#2a4365' },
  RECOVERED: { bg: '#c6f6d5', color: '#22543d' },
  CANCELLED: { bg: '#e2e8f0', color: '#4a5568' }
};

const BATCH_STATUS_COLORS = {
  PENDING:    { bg: '#fefcbf', color: '#744210' },
  PROCESSING: { bg: '#bee3f8', color: '#2a4365' },
  COMPLETED:  { bg: '#c6f6d5', color: '#22543d' },
  FAILED:     { bg: '#fed7d7', color: '#9b2c2c' },
  CANCELLED:  { bg: '#e2e8f0', color: '#4a5568' }
};

const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
const formatDateTime = (d) => d ? new Date(d).toLocaleString() : '\u2014';
const formatMoney = (a) => a != null ? `$${Number(a).toFixed(2)}` : '\u2014';

const StatusBadge = ({ status, colorMap }) => {
  const s = colorMap[status] || { bg: '#e2e8f0', color: '#4a5568' };
  return (
    <span style={{
      display: 'inline-block', padding: '2px 8px', borderRadius: '4px',
      fontSize: '0.75rem', fontWeight: 600, backgroundColor: s.bg, color: s.color
    }}>
      {status}
    </span>
  );
};

const ErrorBanner = ({ message }) => message ? (
  <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
    {message}
  </div>
) : null;

const SuccessBanner = ({ message }) => message ? (
  <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>
    {message}
  </div>
) : null;

// ======================== Pay Rates Tab ========================

const PayRatesTab = () => {
  const [countyCode, setCountyCode] = useState('');
  const [rates, setRates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({ countyCode: '', rateType: 'REGULAR', hourlyRate: '', effectiveDate: '', description: '' });
  const [saving, setSaving] = useState(false);

  const loadRates = async () => {
    if (!countyCode.trim()) return;
    setLoading(true); setError(''); setSuccess('');
    try {
      const res = await payrollApi.getPayRates(countyCode.trim());
      setRates(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to load pay rates: ' + (err?.response?.data?.message || err?.message));
      setRates([]);
    } finally { setLoading(false); }
  };

  const handleAdd = async () => {
    setSaving(true); setError('');
    try {
      await payrollApi.createPayRate({ ...form, countyCode: countyCode.trim() }).then(r => r.data);
      setSuccess('Pay rate created.');
      setShowAdd(false);
      setForm({ countyCode: '', rateType: 'REGULAR', hourlyRate: '', effectiveDate: '', description: '' });
      loadRates();
    } catch (err) {
      setError('Failed to create rate: ' + (err?.response?.data?.message || err?.message));
    } finally { setSaving(false); }
  };

  const handleInactivate = async (id) => {
    if (!window.confirm('Inactivate this pay rate?')) return;
    setError('');
    try {
      await payrollApi.inactivatePayRate(id).then(r => r.data);
      setSuccess('Pay rate inactivated.');
      loadRates();
    } catch (err) {
      setError('Failed to inactivate: ' + (err?.response?.data?.message || err?.message));
    }
  };

  return (
    <div>
      <ErrorBanner message={error} />
      <SuccessBanner message={success} />

      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '1rem' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">County Code</label>
          <input className="wq-input" value={countyCode} onChange={e => setCountyCode(e.target.value)}
            placeholder="e.g. 19" onKeyDown={e => e.key === 'Enter' && loadRates()} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={loadRates}>Load Rates</button>
        <button className="wq-btn wq-btn-primary" onClick={() => setShowAdd(true)}>Add Rate</button>
      </div>

      {showAdd && (
        <div className="wq-modal-overlay" onClick={() => setShowAdd(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()}>
            <h3 className="wq-modal-title">Add Pay Rate</h3>
            <div className="wq-form-group">
              <label className="wq-detail-label">County Code</label>
              <input className="wq-input" value={countyCode} disabled />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Rate Type</label>
              <select className="wq-select" value={form.rateType} onChange={e => setForm(p => ({ ...p, rateType: e.target.value }))}>
                {RATE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Hourly Rate ($)</label>
              <input className="wq-input" type="number" step="0.01" value={form.hourlyRate}
                onChange={e => setForm(p => ({ ...p, hourlyRate: e.target.value }))} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Effective Date</label>
              <input className="wq-input" type="date" value={form.effectiveDate}
                onChange={e => setForm(p => ({ ...p, effectiveDate: e.target.value }))} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Description</label>
              <input className="wq-input" value={form.description}
                onChange={e => setForm(p => ({ ...p, description: e.target.value }))} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleAdd} disabled={saving}>
                {saving ? 'Saving...' : 'Save'}
              </button>
              <button className="wq-btn wq-btn-sm" onClick={() => setShowAdd(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : rates.length === 0 ? (
        <p style={{ color: '#888' }}>No pay rates found. Enter a county code and click Load Rates.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Rate Type</th><th>Hourly Rate</th><th>Effective Date</th>
              <th>End Date</th><th>Status</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {rates.map(r => (
              <tr key={r.id}>
                <td>{r.rateType}</td>
                <td>{formatMoney(r.hourlyRate)}</td>
                <td>{formatDate(r.effectiveDate)}</td>
                <td>{formatDate(r.endDate)}</td>
                <td>{r.status || 'ACTIVE'}</td>
                <td>
                  {(r.status === 'ACTIVE' || !r.status) && (
                    <button className="wq-btn wq-btn-sm" onClick={() => handleInactivate(r.id)}>Inactivate</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// ======================== Advance Pay Tab ========================

const AdvancePayTab = () => {
  const [providerId, setProviderId] = useState('');
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const search = async () => {
    if (!providerId.trim()) return;
    setLoading(true); setError(''); setSuccess('');
    try {
      const res = await payrollApi.getAdvancePayByProvider(providerId.trim());
      setRecords(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to search: ' + (err?.response?.data?.message || err?.message));
      setRecords([]);
    } finally { setLoading(false); }
  };

  const handleAction = async (id, action) => {
    setError(''); setSuccess('');
    try {
      if (action === 'issue') await payrollApi.issueAdvancePay(id).then(r => r.data);
      else if (action === 'cancel') await payrollApi.cancelAdvancePay(id).then(r => r.data);
      else if (action === 'recover') await payrollApi.recoverAdvancePay(id).then(r => r.data);
      setSuccess(`Advance pay ${action}d successfully.`);
      search();
    } catch (err) {
      setError(`Failed to ${action}: ` + (err?.response?.data?.message || err?.message));
    }
  };

  return (
    <div>
      <ErrorBanner message={error} />
      <SuccessBanner message={success} />

      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '1rem' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">Provider ID</label>
          <input className="wq-input" value={providerId} onChange={e => setProviderId(e.target.value)}
            placeholder="Enter Provider ID" onKeyDown={e => e.key === 'Enter' && search()} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={search}>Search</button>
      </div>

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : records.length === 0 ? (
        <p style={{ color: '#888' }}>No advance pay records found. Enter a Provider ID and click Search.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Case ID</th><th>Amount</th><th>Status</th><th>Payment Date</th>
              <th>Warrant #</th><th>Created Date</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {records.map(r => (
              <tr key={r.id}>
                <td>{r.caseId}</td>
                <td>{formatMoney(r.amount)}</td>
                <td><StatusBadge status={r.status} colorMap={ADVANCE_STATUS_COLORS} /></td>
                <td>{formatDate(r.paymentDate)}</td>
                <td>{r.warrantNumber || '\u2014'}</td>
                <td>{formatDate(r.createdAt || r.createdDate)}</td>
                <td style={{ display: 'flex', gap: '0.25rem' }}>
                  {r.status === 'PENDING' && (
                    <>
                      <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleAction(r.id, 'issue')}>Issue</button>
                      <button className="wq-btn wq-btn-sm" onClick={() => handleAction(r.id, 'cancel')}>Cancel</button>
                    </>
                  )}
                  {r.status === 'ISSUED' && (
                    <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleAction(r.id, 'recover')}>Recover</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// ======================== Deductions Tab ========================

const DeductionsTab = () => {
  const [providerId, setProviderId] = useState('');
  const [deductions, setDeductions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({
    deductionType: 'FEDERAL_TAX', amount: '', percentage: '', priority: '1',
    frequency: 'PER_PAY_PERIOD', description: ''
  });
  const [saving, setSaving] = useState(false);

  const load = async () => {
    if (!providerId.trim()) return;
    setLoading(true); setError(''); setSuccess('');
    try {
      const res = await payrollApi.getDeductions(providerId.trim());
      setDeductions(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to load deductions: ' + (err?.response?.data?.message || err?.message));
      setDeductions([]);
    } finally { setLoading(false); }
  };

  const handleAdd = async () => {
    setSaving(true); setError('');
    try {
      await payrollApi.createDeduction(providerId.trim(), form).then(r => r.data);
      setSuccess('Deduction created.');
      setShowAdd(false);
      setForm({ deductionType: 'FEDERAL_TAX', amount: '', percentage: '', priority: '1', frequency: 'PER_PAY_PERIOD', description: '' });
      load();
    } catch (err) {
      setError('Failed to create deduction: ' + (err?.response?.data?.message || err?.message));
    } finally { setSaving(false); }
  };

  const handleSuspend = async (id) => {
    setError(''); setSuccess('');
    try {
      await payrollApi.suspendDeduction(id).then(r => r.data);
      setSuccess('Deduction suspended.');
      load();
    } catch (err) {
      setError('Failed to suspend: ' + (err?.response?.data?.message || err?.message));
    }
  };

  const handleReactivate = async (id) => {
    setError(''); setSuccess('');
    try {
      await payrollApi.reactivateDeduction(id).then(r => r.data);
      setSuccess('Deduction reactivated.');
      load();
    } catch (err) {
      setError('Failed to reactivate: ' + (err?.response?.data?.message || err?.message));
    }
  };

  return (
    <div>
      <ErrorBanner message={error} />
      <SuccessBanner message={success} />

      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '1rem' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">Provider ID</label>
          <input className="wq-input" value={providerId} onChange={e => setProviderId(e.target.value)}
            placeholder="Enter Provider ID" onKeyDown={e => e.key === 'Enter' && load()} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={load}>Load</button>
        <button className="wq-btn wq-btn-primary" onClick={() => setShowAdd(true)} disabled={!providerId.trim()}>Add Deduction</button>
      </div>

      {showAdd && (
        <div className="wq-modal-overlay" onClick={() => setShowAdd(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()}>
            <h3 className="wq-modal-title">Add Deduction</h3>
            <div className="wq-form-group">
              <label className="wq-detail-label">Deduction Type</label>
              <select className="wq-select" value={form.deductionType}
                onChange={e => setForm(p => ({ ...p, deductionType: e.target.value }))}>
                {DEDUCTION_TYPES.map(t => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Amount ($)</label>
              <input className="wq-input" type="number" step="0.01" value={form.amount}
                onChange={e => setForm(p => ({ ...p, amount: e.target.value }))} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Percentage (%)</label>
              <input className="wq-input" type="number" step="0.01" value={form.percentage}
                onChange={e => setForm(p => ({ ...p, percentage: e.target.value }))} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Priority</label>
              <input className="wq-input" type="number" value={form.priority}
                onChange={e => setForm(p => ({ ...p, priority: e.target.value }))} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Frequency</label>
              <select className="wq-select" value={form.frequency}
                onChange={e => setForm(p => ({ ...p, frequency: e.target.value }))}>
                {DEDUCTION_FREQUENCIES.map(f => <option key={f} value={f}>{f.replace(/_/g, ' ')}</option>)}
              </select>
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Description</label>
              <input className="wq-input" value={form.description}
                onChange={e => setForm(p => ({ ...p, description: e.target.value }))} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleAdd} disabled={saving}>
                {saving ? 'Saving...' : 'Save'}
              </button>
              <button className="wq-btn wq-btn-sm" onClick={() => setShowAdd(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : deductions.length === 0 ? (
        <p style={{ color: '#888' }}>No deductions found. Enter a Provider ID and click Load.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Deduction Type</th><th>Amount</th><th>%</th><th>Priority</th>
              <th>Frequency</th><th>Status</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {deductions.map(d => (
              <tr key={d.id}>
                <td>{(d.deductionType || '').replace(/_/g, ' ')}</td>
                <td>{formatMoney(d.amount)}</td>
                <td>{d.percentage != null ? `${d.percentage}%` : '\u2014'}</td>
                <td>{d.priority ?? '\u2014'}</td>
                <td>{(d.frequency || '').replace(/_/g, ' ')}</td>
                <td>{d.status || 'ACTIVE'}</td>
                <td style={{ display: 'flex', gap: '0.25rem' }}>
                  {(d.status === 'ACTIVE' || !d.status) && (
                    <button className="wq-btn wq-btn-sm" onClick={() => handleSuspend(d.id)}>Suspend</button>
                  )}
                  {d.status === 'SUSPENDED' && (
                    <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleReactivate(d.id)}>Reactivate</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// ======================== Tax Tab ========================

const TaxTab = () => {
  const [providerId, setProviderId] = useState('');
  const [year, setYear] = useState(new Date().getFullYear().toString());
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    if (!providerId.trim()) return;
    setLoading(true); setError(''); setSuccess('');
    try {
      const res = year
        ? await payrollApi.getTaxByYear(providerId.trim(), year)
        : await payrollApi.getTaxRecords(providerId.trim());
      setRecords(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to load tax records: ' + (err?.response?.data?.message || err?.message));
      setRecords([]);
    } finally { setLoading(false); }
  };

  const handleGenerateW2 = async (id) => {
    setError(''); setSuccess('');
    try {
      await payrollApi.generateW2(id).then(r => r.data);
      setSuccess('W2 generated successfully.');
      load();
    } catch (err) {
      setError('Failed to generate W2: ' + (err?.response?.data?.message || err?.message));
    }
  };

  return (
    <div>
      <ErrorBanner message={error} />
      <SuccessBanner message={success} />

      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '1rem' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">Provider ID</label>
          <input className="wq-input" value={providerId} onChange={e => setProviderId(e.target.value)}
            placeholder="Enter Provider ID" onKeyDown={e => e.key === 'Enter' && load()} />
        </div>
        <div className="wq-form-group">
          <label className="wq-detail-label">Year</label>
          <input className="wq-input" type="number" value={year} onChange={e => setYear(e.target.value)}
            placeholder="e.g. 2026" style={{ width: '100px' }} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={load}>Load</button>
      </div>

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : records.length === 0 ? (
        <p style={{ color: '#888' }}>No tax records found. Enter a Provider ID and click Load.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Year</th><th>Quarter</th><th>Filing Status</th><th>Allowances</th>
              <th>Federal YTD</th><th>State YTD</th><th>W2 Generated</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {records.map(r => (
              <tr key={r.id}>
                <td>{r.taxYear || r.year}</td>
                <td>{r.quarter ? `Q${r.quarter}` : '\u2014'}</td>
                <td>{r.filingStatus || '\u2014'}</td>
                <td>{r.allowances ?? '\u2014'}</td>
                <td>{formatMoney(r.federalWithholdingYtd || r.federalYtd)}</td>
                <td>{formatMoney(r.stateWithholdingYtd || r.stateYtd)}</td>
                <td>{r.w2Generated ? 'Yes' : 'No'}</td>
                <td>
                  {!r.w2Generated && (
                    <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleGenerateW2(r.id)}>Generate W2</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// ======================== Payroll Batches Tab ========================

const PayrollBatchesTab = () => {
  const [batches, setBatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ batchType: 'REGULAR', payPeriodStart: '', payPeriodEnd: '' });
  const [saving, setSaving] = useState(false);

  const loadBatches = async () => {
    setLoading(true); setError('');
    try {
      const res = await payrollApi.getRecentBatches();
      setBatches(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to load batches: ' + (err?.response?.data?.message || err?.message));
    } finally { setLoading(false); }
  };

  useEffect(() => { loadBatches(); }, []);

  const handleCreate = async () => {
    setSaving(true); setError('');
    try {
      await payrollApi.createPayrollBatch(form).then(r => r.data);
      setSuccess('Payroll batch created.');
      setShowCreate(false);
      setForm({ batchType: 'REGULAR', payPeriodStart: '', payPeriodEnd: '' });
      loadBatches();
    } catch (err) {
      setError('Failed to create batch: ' + (err?.response?.data?.message || err?.message));
    } finally { setSaving(false); }
  };

  const handleAction = async (id, action) => {
    setError(''); setSuccess('');
    try {
      if (action === 'process') await payrollApi.processPayrollBatch(id).then(r => r.data);
      else if (action === 'complete') await payrollApi.completePayrollBatch(id).then(r => r.data);
      else if (action === 'fail') await payrollApi.failPayrollBatch(id, { reason: 'Manual fail' }).then(r => r.data);
      else if (action === 'cancel') await payrollApi.cancelPayrollBatch(id).then(r => r.data);
      setSuccess(`Batch ${action} successful.`);
      loadBatches();
    } catch (err) {
      setError(`Failed to ${action} batch: ` + (err?.response?.data?.message || err?.message));
    }
  };

  return (
    <div>
      <ErrorBanner message={error} />
      <SuccessBanner message={success} />

      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={() => setShowCreate(true)}>Create Batch</button>
        <button className="wq-btn wq-btn-sm" onClick={loadBatches}>Refresh</button>
      </div>

      {showCreate && (
        <div className="wq-modal-overlay" onClick={() => setShowCreate(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()}>
            <h3 className="wq-modal-title">Create Payroll Batch</h3>
            <div className="wq-form-group">
              <label className="wq-detail-label">Batch Type</label>
              <select className="wq-select" value={form.batchType}
                onChange={e => setForm(p => ({ ...p, batchType: e.target.value }))}>
                {BATCH_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Pay Period Start</label>
              <input className="wq-input" type="date" value={form.payPeriodStart}
                onChange={e => setForm(p => ({ ...p, payPeriodStart: e.target.value }))} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Pay Period End</label>
              <input className="wq-input" type="date" value={form.payPeriodEnd}
                onChange={e => setForm(p => ({ ...p, payPeriodEnd: e.target.value }))} />
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Creating...' : 'Create'}
              </button>
              <button className="wq-btn wq-btn-sm" onClick={() => setShowCreate(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : batches.length === 0 ? (
        <p style={{ color: '#888' }}>No payroll batches found.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Batch #</th><th>Batch Type</th><th>Status</th><th>Total Timesheets</th>
              <th>Total Gross Pay</th><th>Created Date</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {batches.map(b => (
              <tr key={b.id}>
                <td>{b.batchNumber || b.id}</td>
                <td>{b.batchType}</td>
                <td><StatusBadge status={b.status} colorMap={BATCH_STATUS_COLORS} /></td>
                <td>{b.totalTimesheets ?? '\u2014'}</td>
                <td>{formatMoney(b.totalGrossPay)}</td>
                <td>{formatDateTime(b.createdAt || b.createdDate)}</td>
                <td style={{ display: 'flex', gap: '0.25rem', flexWrap: 'wrap' }}>
                  {b.status === 'PENDING' && (
                    <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleAction(b.id, 'process')}>Process</button>
                  )}
                  {b.status === 'PROCESSING' && (
                    <>
                      <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleAction(b.id, 'complete')}>Complete</button>
                      <button className="wq-btn wq-btn-sm" onClick={() => handleAction(b.id, 'fail')}>Fail</button>
                    </>
                  )}
                  {(b.status === 'COMPLETED' || b.status === 'PROCESSING') && (
                    <button className="wq-btn wq-btn-sm" onClick={() => handleAction(b.id, 'cancel')}>Cancel</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// ======================== Earnings Tab ========================

const EarningsTab = () => {
  const [providerId, setProviderId] = useState('');
  const [statements, setStatements] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const load = async () => {
    if (!providerId.trim()) return;
    setLoading(true); setError(''); setSuccess('');
    try {
      const res = await payrollApi.getEarningsStatements(providerId.trim());
      setStatements(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to load earnings: ' + (err?.response?.data?.message || err?.message));
      setStatements([]);
    } finally { setLoading(false); }
  };

  const handleMarkMailed = async (id) => {
    setError(''); setSuccess('');
    try {
      await payrollApi.markStatementMailed(id).then(r => r.data);
      setSuccess('Statement marked as mailed.');
      load();
    } catch (err) {
      setError('Failed to mark mailed: ' + (err?.response?.data?.message || err?.message));
    }
  };

  return (
    <div>
      <ErrorBanner message={error} />
      <SuccessBanner message={success} />

      <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '1rem' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">Provider ID</label>
          <input className="wq-input" value={providerId} onChange={e => setProviderId(e.target.value)}
            placeholder="Enter Provider ID" onKeyDown={e => e.key === 'Enter' && load()} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={load}>Load</button>
      </div>

      {loading ? (
        <p style={{ color: '#888' }}>Loading...</p>
      ) : statements.length === 0 ? (
        <p style={{ color: '#888' }}>No earnings statements found. Enter a Provider ID and click Load.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Pay Period</th><th>Gross Pay</th><th>Net Pay</th><th>Regular Hours</th>
              <th>OT Hours</th><th>Mailed</th><th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {statements.map(s => (
              <tr key={s.id}>
                <td>{formatDate(s.payPeriodStart)} - {formatDate(s.payPeriodEnd)}</td>
                <td>{formatMoney(s.grossPay)}</td>
                <td>{formatMoney(s.netPay)}</td>
                <td>{s.regularHours ?? '\u2014'}</td>
                <td>{s.overtimeHours ?? '\u2014'}</td>
                <td>{s.mailed ? 'Yes' : 'No'}</td>
                <td>
                  {!s.mailed && (
                    <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleMarkMailed(s.id)}>Mark Mailed</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// ======================== Main Page ========================

export const PayrollAdminPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();
  const [activeTab, setActiveTab] = useState('payRates');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'Payroll Administration' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const renderTab = () => {
    switch (activeTab) {
      case 'payRates':   return <PayRatesTab />;
      case 'advancePay': return <AdvancePayTab />;
      case 'deductions': return <DeductionsTab />;
      case 'tax':        return <TaxTab />;
      case 'batches':    return <PayrollBatchesTab />;
      case 'earnings':   return <EarningsTab />;
      default:           return null;
    }
  };

  return (
    <div className="wq-page">
      <h2 className="wq-page-title">Payroll Administration</h2>

      <div className="wq-tabs" style={{ marginBottom: '1.5rem' }}>
        {TABS.map(t => (
          <button
            key={t.key}
            className={`wq-tab ${activeTab === t.key ? 'wq-tab-active' : ''}`}
            onClick={() => setActiveTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </div>

      {renderTab()}
    </div>
  );
};
