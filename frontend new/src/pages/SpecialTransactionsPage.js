import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as api from '../api/specialTransactionsApi';
import './WorkQueues.css';

/**
 * Special Transactions — DSD Section 27 CI-67322
 *
 * One-time payments and deductions (27+ pay types, 8 deduction types).
 * County/CDSS require Payroll Approver secondary approval.
 * Vendor travel claims and System transactions auto-approve.
 *
 * Modes:
 *   - /cases/:caseId/special-transactions  — case context
 *   - /payments/special-transactions       — pending approval work queue
 */
const PAY_TYPES = [
  'REGULAR_IHSS', 'OVERTIME_IHSS', 'DOUBLE_TIME_IHSS', 'HOLIDAY_IHSS',
  'IHSS_SICK_LEAVE', 'TRAINING', 'TRAINING_INCENTIVE', 'ORIENTATION',
  'CMSP_REGULAR', 'CMSP_OVERTIME', 'WPCS_REGULAR', 'WPCS_OVERTIME',
  'WPCS_DOUBLE_TIME', 'WPCS_SICK_LEAVE', 'ADVANCE_PAY', 'IHSS_RESTORATION',
  'CMSP_RESTORATION', 'WPCS_RESTORATION', 'PROVIDER_WAGE_ADJUSTMENT',
  'COUNTY_FUNDED_IHSS', 'EVV_ADJUSTMENT', 'PARAMEDICAL', 'BACK_PAY',
  'SUPPLEMENTAL_SECURITY', 'VENDOR_TRAVEL', 'COUNTY_CONTRACTOR', 'HOMEMAKER',
  'DEDUCTION_OVERPAYMENT', 'DEDUCTION_GARNISHMENT', 'DEDUCTION_TAX_LEVY',
  'DEDUCTION_HEALTH_INSURANCE', 'DEDUCTION_DENTAL', 'DEDUCTION_VISION',
  'DEDUCTION_OTHER', 'DEDUCTION_UNION_DUES'
];

const STATUS_COLORS = {
  PENDING: '#feebc8',
  PENDING_APPROVAL: '#e9d8fd',
  APPROVED: '#c6f6d5',
  REJECTED: '#fed7d7',
  CANCELLED: '#e2e8f0',
  PROCESSED: '#bee3f8'
};

export const SpecialTransactionsPage = () => {
  const { caseId } = useParams();
  const { setBreadcrumbs } = useBreadcrumbs();
  const isWorkQueue = !caseId;

  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [createModal, setCreateModal] = useState(false);
  const [form, setForm] = useState({
    transactionSource: 'COUNTY', payType: 'REGULAR_IHSS',
    transactionDirection: 'PAYMENT', payeeType: 'PROVIDER',
    payeeId: '', payeeName: '', amountType: 'DOLLARS',
    amountDollars: '', amountMinutes: '', servicePeriodFrom: '',
    servicePeriodTo: '', program: 'IHSS', fundingSource: '',
    notes: '', isTravelClaim: false, travelClaimFormNumber: ''
  });
  const [saving, setSaving] = useState(false);

  const [rejectModal, setRejectModal] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [viewModal, setViewModal] = useState(null);

  useEffect(() => {
    const crumbs = [{ label: 'My Workspace', path: '/workspace' }];
    if (caseId) {
      crumbs.push({ label: `Case ${caseId}`, path: `/cases/${caseId}` });
      crumbs.push({ label: 'Special Transactions' });
    } else {
      crumbs.push({ label: 'Special Transactions — Pending Approval' });
    }
    setBreadcrumbs(crumbs);
    return () => setBreadcrumbs([]);
  }, [caseId, setBreadcrumbs]);

  useEffect(() => { load(); }, [caseId]);

  const load = async () => {
    setLoading(true); setError('');
    try {
      const data = isWorkQueue
        ? await api.getPendingApproval()
        : await api.getByCase(caseId);
      setTransactions(Array.isArray(data) ? data : []);
    } catch {
      setError('Failed to load transactions.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    setSaving(true); setError('');
    try {
      await api.create(caseId, form);
      setSuccess('Special transaction created.');
      setCreateModal(false);
      resetForm();
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create transaction.');
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async (action, id, extra) => {
    setError(''); setSuccess('');
    try {
      if (action === 'submit') await api.submit(id);
      else if (action === 'approve') await api.approve(id);
      else if (action === 'reject') { await api.reject(id, extra); setRejectModal(null); }
      else if (action === 'cancel') await api.cancel(id);
      setSuccess(`Transaction ${action}ted successfully.`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action} transaction.`);
    }
  };

  const resetForm = () => setForm({
    transactionSource: 'COUNTY', payType: 'REGULAR_IHSS',
    transactionDirection: 'PAYMENT', payeeType: 'PROVIDER',
    payeeId: '', payeeName: '', amountType: 'DOLLARS',
    amountDollars: '', amountMinutes: '', servicePeriodFrom: '',
    servicePeriodTo: '', program: 'IHSS', fundingSource: '',
    notes: '', isTravelClaim: false, travelClaimFormNumber: ''
  });

  const fmt = (v) => v || '—';
  const fmtAmt = (t) => {
    if (t.amountType === 'DOLLARS' && t.amountDollars != null) return `$${Number(t.amountDollars).toFixed(2)}`;
    if (t.amountType === 'HOURS' && t.amountMinutes != null) {
      const h = Math.floor(t.amountMinutes / 60), m = t.amountMinutes % 60;
      return `${h}:${String(m).padStart(2, '0')} hrs`;
    }
    return '—';
  };

  return (
    <div className="wq-page">
      <div className="wq-header">
        <div>
          <h1 className="wq-title">
            {isWorkQueue ? 'Special Transactions — Pending Approval' : 'Special Transactions'}
          </h1>
          <p className="wq-subtitle">DSD Section 27 CI-67322 — One-time payments and deductions</p>
        </div>
        {!isWorkQueue && (
          <button className="wq-btn wq-btn-primary" onClick={() => setCreateModal(true)}>
            + New Special Transaction
          </button>
        )}
      </div>

      {error && <div className="wq-error-msg">{error}</div>}
      {success && <div className="wq-success-msg">{success}</div>}

      <div className="wq-card">
        {loading ? (
          <div className="wq-loading">Loading…</div>
        ) : transactions.length === 0 ? (
          <div className="wq-empty">No special transactions found.</div>
        ) : (
          <table className="wq-table">
            <thead>
              <tr>
                <th>Pay Type</th>
                <th>Direction</th>
                <th>Payee</th>
                <th>Amount</th>
                <th>Service Period</th>
                <th>Source</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map(t => (
                <tr key={t.id}>
                  <td>{fmt(t.payType)}</td>
                  <td>{fmt(t.transactionDirection)}</td>
                  <td>{fmt(t.payeeName)}</td>
                  <td>{fmtAmt(t)}</td>
                  <td>
                    {t.servicePeriodFrom ? `${t.servicePeriodFrom} – ${t.servicePeriodTo || '…'}` : '—'}
                  </td>
                  <td>{fmt(t.transactionSource)}</td>
                  <td>
                    <span style={{
                      background: STATUS_COLORS[t.status] || '#e2e8f0',
                      padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600
                    }}>{t.status}</span>
                  </td>
                  <td style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                    <button className="wq-btn wq-btn-secondary wq-btn-sm"
                      onClick={() => setViewModal(t)}>View</button>
                    {t.status === 'PENDING' && (
                      <button className="wq-btn wq-btn-primary wq-btn-sm"
                        onClick={() => handleAction('submit', t.id)}>Submit</button>
                    )}
                    {t.status === 'PENDING_APPROVAL' && (
                      <>
                        <button className="wq-btn wq-btn-primary wq-btn-sm"
                          onClick={() => handleAction('approve', t.id)}>Approve</button>
                        <button className="wq-btn wq-btn-danger wq-btn-sm"
                          onClick={() => { setRejectModal(t.id); setRejectReason(''); }}>Reject</button>
                      </>
                    )}
                    {(t.status === 'PENDING' || t.status === 'PENDING_APPROVAL') && (
                      <button className="wq-btn wq-btn-secondary wq-btn-sm"
                        onClick={() => handleAction('cancel', t.id)}>Cancel</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Create Modal */}
      {createModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal wq-modal-lg">
            <div className="wq-modal-header">
              <h3>New Special Transaction</h3>
              <button className="wq-modal-close" onClick={() => { setCreateModal(false); resetForm(); }}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-grid">
                <div className="wq-form-group">
                  <label className="wq-label">Transaction Source *</label>
                  <select className="wq-input" value={form.transactionSource}
                    onChange={e => setForm(f => ({ ...f, transactionSource: e.target.value }))}>
                    <option value="COUNTY">County</option>
                    <option value="CDSS">CDSS</option>
                    <option value="VENDOR">Vendor (auto-approve)</option>
                    <option value="SYSTEM">System (auto-approve)</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Direction *</label>
                  <select className="wq-input" value={form.transactionDirection}
                    onChange={e => setForm(f => ({ ...f, transactionDirection: e.target.value }))}>
                    <option value="PAYMENT">Payment</option>
                    <option value="DEDUCTION">Deduction</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Pay Type *</label>
                  <select className="wq-input" value={form.payType}
                    onChange={e => setForm(f => ({ ...f, payType: e.target.value }))}>
                    {PAY_TYPES.map(p => <option key={p} value={p}>{p.replace(/_/g, ' ')}</option>)}
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Payee Type *</label>
                  <select className="wq-input" value={form.payeeType}
                    onChange={e => setForm(f => ({ ...f, payeeType: e.target.value }))}>
                    <option value="RECIPIENT">Recipient</option>
                    <option value="PROVIDER">Provider</option>
                    <option value="AP_PROVIDER">AP Provider</option>
                    <option value="WPCS_PROVIDER">WPCS Provider</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Payee ID</label>
                  <input className="wq-input" value={form.payeeId}
                    onChange={e => setForm(f => ({ ...f, payeeId: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Payee Name</label>
                  <input className="wq-input" value={form.payeeName}
                    onChange={e => setForm(f => ({ ...f, payeeName: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Amount Type *</label>
                  <select className="wq-input" value={form.amountType}
                    onChange={e => setForm(f => ({ ...f, amountType: e.target.value }))}>
                    <option value="DOLLARS">Dollars</option>
                    <option value="HOURS">Hours</option>
                  </select>
                </div>
                {form.amountType === 'DOLLARS' ? (
                  <div className="wq-form-group">
                    <label className="wq-label">Amount ($)</label>
                    <input type="number" step="0.01" className="wq-input" value={form.amountDollars}
                      onChange={e => setForm(f => ({ ...f, amountDollars: e.target.value }))} />
                  </div>
                ) : (
                  <div className="wq-form-group">
                    <label className="wq-label">Hours (minutes)</label>
                    <input type="number" className="wq-input" placeholder="Total minutes"
                      value={form.amountMinutes}
                      onChange={e => setForm(f => ({ ...f, amountMinutes: e.target.value }))} />
                  </div>
                )}
                <div className="wq-form-group">
                  <label className="wq-label">Service Period From</label>
                  <input type="date" className="wq-input" value={form.servicePeriodFrom}
                    onChange={e => setForm(f => ({ ...f, servicePeriodFrom: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Service Period To</label>
                  <input type="date" className="wq-input" value={form.servicePeriodTo}
                    onChange={e => setForm(f => ({ ...f, servicePeriodTo: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Program *</label>
                  <select className="wq-input" value={form.program}
                    onChange={e => setForm(f => ({ ...f, program: e.target.value }))}>
                    <option value="IHSS">IHSS</option>
                    <option value="WPCS">WPCS</option>
                    <option value="CMSP">CMSP</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Funding Source</label>
                  <input className="wq-input" value={form.fundingSource}
                    onChange={e => setForm(f => ({ ...f, fundingSource: e.target.value }))} />
                </div>
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={3} value={form.notes}
                  onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
              {form.transactionSource === 'VENDOR' && (
                <div style={{ display: 'flex', gap: 16, alignItems: 'center' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                    <input type="checkbox" checked={form.isTravelClaim}
                      onChange={e => setForm(f => ({ ...f, isTravelClaim: e.target.checked }))} />
                    Travel Claim
                  </label>
                  {form.isTravelClaim && (
                    <div className="wq-form-group" style={{ flex: 1, marginBottom: 0 }}>
                      <label className="wq-label">Travel Claim Form #</label>
                      <input className="wq-input" value={form.travelClaimFormNumber}
                        onChange={e => setForm(f => ({ ...f, travelClaimFormNumber: e.target.value }))} />
                    </div>
                  )}
                </div>
              )}
              {(form.transactionSource === 'COUNTY' || form.transactionSource === 'CDSS') && (
                <div style={{ marginTop: 12, padding: '8px 12px', background: '#fffbeb', border: '1px solid #f6e05e', borderRadius: 6, fontSize: 13, color: '#744210' }}>
                  County/CDSS transactions require secondary Payroll Approver approval before processing.
                </div>
              )}
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => { setCreateModal(false); resetForm(); }}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Saving…' : 'Create Transaction'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reject Modal */}
      {rejectModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header">
              <h3>Reject Transaction</h3>
              <button className="wq-modal-close" onClick={() => setRejectModal(null)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-group">
                <label className="wq-label">Rejection Reason *</label>
                <textarea className="wq-input" rows={3} value={rejectReason}
                  onChange={e => setRejectReason(e.target.value)}
                  placeholder="Enter reason for rejection…" />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setRejectModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-danger" disabled={!rejectReason.trim()}
                onClick={() => handleAction('reject', rejectModal, rejectReason)}>
                Reject
              </button>
            </div>
          </div>
        </div>
      )}

      {/* View Modal */}
      {viewModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal wq-modal-lg">
            <div className="wq-modal-header">
              <h3>Special Transaction Details</h3>
              <button className="wq-modal-close" onClick={() => setViewModal(null)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-grid">
                {[
                  ['Pay Type', viewModal.payType],
                  ['Direction', viewModal.transactionDirection],
                  ['Source', viewModal.transactionSource],
                  ['Status', viewModal.status],
                  ['Payee Type', viewModal.payeeType],
                  ['Payee Name', viewModal.payeeName],
                  ['Amount', fmtAmt(viewModal)],
                  ['Program', viewModal.program],
                  ['Service Period', `${viewModal.servicePeriodFrom || '—'} – ${viewModal.servicePeriodTo || '—'}`],
                  ['Notes', viewModal.notes],
                  ['Submitted By', viewModal.submittedBy],
                  ['Approved By', viewModal.approvedBy],
                ].map(([label, val]) => (
                  <div key={label}>
                    <span className="wq-label">{label}</span>
                    <div>{val || '—'}</div>
                  </div>
                ))}
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setViewModal(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
