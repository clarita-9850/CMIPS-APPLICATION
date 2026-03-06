import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as api from '../api/overpaymentApi';
import './WorkQueues.css';

/**
 * Overpayment Recovery — DSD Section 27 CI-67319
 *
 * Setup from recipient case perspective.
 * 14 overpayment types, recovery methods: Payroll Deduction or Personal Payment.
 * Status lifecycle: PENDING → PENDING_PAYROLL / ACTIVE → STOPPED/CANCELLED/CLOSED
 */
// DSD Section 27 — 14 Overpayment Recovery Types
const OVERPAYMENT_TYPES = [
  { value: 'ADVANCE_PAY_RECIPIENT_PAYROLL_DEDUCTIONS', label: 'Advance Pay – Recipient Payroll Deductions' },
  { value: 'ADVANCE_PAY_OTHER', label: 'Advance Pay – Other' },
  { value: 'EXCESS_COMPENSATION_HOURS', label: 'Excess Compensation – Hours' },
  { value: 'EXCESS_COMPENSATION_RATE', label: 'Excess Compensation – Rate' },
  { value: 'RESTAURANT_MEALS', label: 'Restaurant Meals' },
  { value: 'SHARE_OF_COST', label: 'Share of Cost' },
  { value: 'SPECIAL_TRANSACTION_DOLLARS_RECIPIENT_OR_PROVIDER', label: 'Special Transaction (Dollars) – Recipient/Provider' },
  { value: 'SPECIAL_TRANSACTION_HOURS_RECIPIENT_OR_PROVIDER', label: 'Special Transaction (Hours) – Recipient/Provider' },
  { value: 'SPECIAL_TRANSACTION_DOLLARS_PROVIDER', label: 'Special Transaction (Dollars) – Provider' },
  { value: 'SPECIAL_TRANSACTION_HOURS_PROVIDER_AP', label: 'Special Transaction (Hours) – Provider AP' },
  { value: 'CONVERTED_OVERPAYMENT', label: 'Converted Overpayment' },
  { value: 'LEGACY_SPECIAL_TRANSACTION_RECIPIENT_OR_PROVIDER', label: 'Legacy Special Transaction – Recipient/Provider' },
  { value: 'LEGACY_SPECIAL_TRANSACTION_PROVIDER', label: 'Legacy Special Transaction – Provider' },
  { value: 'EXCESS_COMPENSATION_TRAVEL', label: 'Excess Compensation – Travel' }
];

const STATUS_COLORS = {
  PENDING: '#feebc8',
  PENDING_PAYROLL: '#e9d8fd',
  ACTIVE: '#c6f6d5',
  STOPPED: '#fed7d7',
  CANCELLED: '#e2e8f0',
  CLOSED: '#bee3f8'
};

export const OverpaymentRecoveryPage = () => {
  const { caseId } = useParams();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [overpayments, setOverpayments] = useState([]);
  const [showAll, setShowAll] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [createModal, setCreateModal] = useState(false);
  const [form, setForm] = useState({
    overpaymentType: 'ADVANCE_PAY_RECIPIENT_PAYROLL_DEDUCTIONS',
    program: 'IHSS',
    overpaidPayeeType: 'RECIPIENT', overpaidPayeeId: '', overpaidPayeeName: '',
    recoveryPayeeType: 'RECIPIENT', recoveryPayeeId: '', recoveryPayeeName: '',
    amountType: 'DOLLARS',
    servicePeriodFrom: '', servicePeriodTo: '',
    totalNetOverpayment: '', overpaidHoursMinutes: '',
    recoveryMethod: 'PAYROLL_DEDUCTION',
    installmentType: 'AUTOMATIC', installmentAmount: '',
    reason: '', comments: ''
  });
  const [saving, setSaving] = useState(false);

  // Collections
  const [selectedOp, setSelectedOp] = useState(null);
  const [collections, setCollections] = useState([]);
  const [collectionsLoading, setCollectionsLoading] = useState(false);
  const [addCollectionModal, setAddCollectionModal] = useState(false);
  const [collectionForm, setCollectionForm] = useState({
    collectionDate: '', amount: '', modeOfPayment: 'PERSONAL_PAYMENT',
    receiptNumber: '', notes: ''
  });
  const [collectionSaving, setCollectionSaving] = useState(false);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'My Workspace', path: '/workspace' },
      { label: `Case ${caseId}`, path: `/cases/${caseId}` },
      { label: 'Overpayment Recovery' }
    ]);
    return () => setBreadcrumbs([]);
  }, [caseId, setBreadcrumbs]);

  useEffect(() => { load(); }, [caseId, showAll]);

  const load = async () => {
    setLoading(true); setError('');
    try {
      const data = showAll ? await api.getAll(caseId) : await api.getActive(caseId);
      setOverpayments(Array.isArray(data) ? data : []);
    } catch {
      setError('Failed to load overpayments.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    setSaving(true); setError('');
    try {
      await api.create(caseId, {
        ...form,
        totalNetOverpayment: form.totalNetOverpayment ? Number(form.totalNetOverpayment) : null,
        overpaidHoursMinutes: form.overpaidHoursMinutes ? Number(form.overpaidHoursMinutes) : null,
        installmentAmount: form.installmentAmount ? Number(form.installmentAmount) : null
      });
      setSuccess('Overpayment recovery created.');
      setCreateModal(false);
      resetForm();
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create overpayment.');
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async (action, id) => {
    setError(''); setSuccess('');
    try {
      if (action === 'submit') await api.submit(id);
      else if (action === 'cancel') await api.cancel(id);
      else if (action === 'stop') await api.stop(id);
      const msg = { submit: 'submitted', cancel: 'cancelled', stop: 'stopped' };
      setSuccess(`Overpayment recovery ${msg[action]} successfully.`);
      load();
      if (selectedOp?.id === id) setSelectedOp(null);
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action}.`);
    }
  };

  const handleViewCollections = async (op) => {
    setSelectedOp(op);
    setCollectionsLoading(true);
    try {
      const data = await api.getCollections(op.id);
      setCollections(Array.isArray(data) ? data : []);
    } catch {
      setCollections([]);
    } finally {
      setCollectionsLoading(false);
    }
  };

  const handleAddCollection = async () => {
    setCollectionSaving(true);
    try {
      await api.addCollection(selectedOp.id, {
        ...collectionForm,
        amount: Number(collectionForm.amount)
      });
      setSuccess('Personal payment recorded.');
      setAddCollectionModal(false);
      setCollectionForm({ collectionDate: '', amount: '', modeOfPayment: 'PERSONAL_PAYMENT', receiptNumber: '', notes: '' });
      handleViewCollections(selectedOp);
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to record payment.');
    } finally {
      setCollectionSaving(false);
    }
  };

  const resetForm = () => setForm({
    overpaymentType: 'ADVANCE_PAY_RECIPIENT_PAYROLL_DEDUCTIONS', program: 'IHSS',
    overpaidPayeeType: 'RECIPIENT', overpaidPayeeId: '', overpaidPayeeName: '',
    recoveryPayeeType: 'RECIPIENT', recoveryPayeeId: '', recoveryPayeeName: '',
    amountType: 'DOLLARS', servicePeriodFrom: '', servicePeriodTo: '',
    totalNetOverpayment: '', overpaidHoursMinutes: '',
    recoveryMethod: 'PAYROLL_DEDUCTION', installmentType: 'AUTOMATIC',
    installmentAmount: '', reason: '', comments: ''
  });

  const fmtAmt = (v) => v != null ? `$${Number(v).toFixed(2)}` : '—';

  return (
    <div className="wq-page">
      <div className="wq-header">
        <div>
          <h1 className="wq-title">Overpayment Recovery</h1>
          <p className="wq-subtitle">DSD Section 27 CI-67319 — Case {caseId}</p>
        </div>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className={`wq-btn ${showAll ? 'wq-btn-primary' : 'wq-btn-secondary'}`}
            onClick={() => setShowAll(!showAll)}>
            {showAll ? 'Showing All' : 'Show All (incl. Closed)'}
          </button>
          <button className="wq-btn wq-btn-primary" onClick={() => setCreateModal(true)}>
            + New Overpayment
          </button>
        </div>
      </div>

      {error && <div className="wq-error-msg">{error}</div>}
      {success && <div className="wq-success-msg">{success}</div>}

      <div className="wq-card" style={{ marginBottom: 24 }}>
        {loading ? (
          <div className="wq-loading">Loading…</div>
        ) : overpayments.length === 0 ? (
          <div className="wq-empty">No overpayment records found.</div>
        ) : (
          <table className="wq-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Program</th>
                <th>Service Period</th>
                <th>Total OP Amount</th>
                <th>Balance</th>
                <th>Recovery Method</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {overpayments.map(op => (
                <tr key={op.id}>
                  <td style={{ fontSize: 12 }}>{OVERPAYMENT_TYPES.find(t => t.value === op.overpaymentType)?.label || op.overpaymentType}</td>
                  <td>{op.program || '—'}</td>
                  <td style={{ fontSize: 12 }}>
                    {op.servicePeriodFrom ? `${op.servicePeriodFrom} – ${op.servicePeriodTo || '…'}` : '—'}
                  </td>
                  <td>{fmtAmt(op.totalNetOverpayment)}</td>
                  <td>{fmtAmt(op.balance)}</td>
                  <td style={{ fontSize: 12 }}>{op.recoveryMethod?.replace(/_/g, ' ')}</td>
                  <td>
                    <span style={{
                      background: STATUS_COLORS[op.status] || '#e2e8f0',
                      padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600
                    }}>{op.status}</span>
                  </td>
                  <td style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                    <button className="wq-btn wq-btn-secondary wq-btn-sm"
                      onClick={() => handleViewCollections(op)}>
                      Collections
                    </button>
                    {op.status === 'PENDING' && (
                      <>
                        <button className="wq-btn wq-btn-primary wq-btn-sm"
                          onClick={() => handleAction('submit', op.id)}>Submit</button>
                        <button className="wq-btn wq-btn-secondary wq-btn-sm"
                          onClick={() => handleAction('cancel', op.id)}>Cancel</button>
                      </>
                    )}
                    {op.status === 'ACTIVE' && op.balance > 0 && (
                      <button className="wq-btn wq-btn-danger wq-btn-sm"
                        onClick={() => handleAction('stop', op.id)}>Stop</button>
                    )}
                    {op.status === 'PENDING_PAYROLL' && (
                      <button className="wq-btn wq-btn-secondary wq-btn-sm"
                        onClick={() => handleAction('cancel', op.id)}>Cancel</button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Collections Panel */}
      {selectedOp && (
        <div className="wq-card">
          <div className="wq-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 className="wq-section-title">
              Collections — Overpayment #{selectedOp.id}
              <span style={{ fontSize: 13, fontWeight: 400, color: '#718096', marginLeft: 12 }}>
                Balance: {fmtAmt(selectedOp.balance)}
              </span>
            </h2>
            <div style={{ display: 'flex', gap: 8 }}>
              {selectedOp.status === 'ACTIVE' && (
                <button className="wq-btn wq-btn-primary wq-btn-sm"
                  onClick={() => setAddCollectionModal(true)}>
                  + Add Personal Payment
                </button>
              )}
              <button className="wq-btn wq-btn-secondary wq-btn-sm"
                onClick={() => setSelectedOp(null)}>Close</button>
            </div>
          </div>

          {collectionsLoading ? (
            <div className="wq-loading">Loading collections…</div>
          ) : collections.length === 0 ? (
            <div className="wq-empty">No collections recorded yet.</div>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Amount</th>
                  <th>Payment Mode</th>
                  <th>Receipt #</th>
                  <th>Recorded By</th>
                </tr>
              </thead>
              <tbody>
                {collections.map(c => (
                  <tr key={c.id}>
                    <td>{c.collectionDate || '—'}</td>
                    <td>{fmtAmt(c.amount)}</td>
                    <td>{c.modeOfPayment?.replace(/_/g, ' ')}</td>
                    <td>{c.receiptNumber || '—'}</td>
                    <td>{c.createdBy || '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* Create Modal */}
      {createModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal wq-modal-lg">
            <div className="wq-modal-header">
              <h3>New Overpayment Recovery</h3>
              <button className="wq-modal-close" onClick={() => { setCreateModal(false); resetForm(); }}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-grid">
                <div className="wq-form-group">
                  <label className="wq-label">Overpayment Type *</label>
                  <select className="wq-input" value={form.overpaymentType}
                    onChange={e => setForm(f => ({ ...f, overpaymentType: e.target.value }))}>
                    {OVERPAYMENT_TYPES.map(t => (
                      <option key={t.value} value={t.value}>{t.label}</option>
                    ))}
                  </select>
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
                  <label className="wq-label">Overpaid Payee Type</label>
                  <select className="wq-input" value={form.overpaidPayeeType}
                    onChange={e => setForm(f => ({ ...f, overpaidPayeeType: e.target.value }))}>
                    <option value="RECIPIENT">Recipient</option>
                    <option value="PROVIDER">Provider</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Overpaid Payee Name</label>
                  <input className="wq-input" value={form.overpaidPayeeName}
                    onChange={e => setForm(f => ({ ...f, overpaidPayeeName: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Service Period From *</label>
                  <input type="date" className="wq-input" value={form.servicePeriodFrom}
                    onChange={e => setForm(f => ({ ...f, servicePeriodFrom: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Service Period To *</label>
                  <input type="date" className="wq-input" value={form.servicePeriodTo}
                    onChange={e => setForm(f => ({ ...f, servicePeriodTo: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Amount Type</label>
                  <select className="wq-input" value={form.amountType}
                    onChange={e => setForm(f => ({ ...f, amountType: e.target.value }))}>
                    <option value="DOLLARS">Dollars</option>
                    <option value="HOURS">Hours</option>
                  </select>
                </div>
                {form.amountType === 'DOLLARS' ? (
                  <div className="wq-form-group">
                    <label className="wq-label">Total Net Overpayment ($) *</label>
                    <input type="number" step="0.01" className="wq-input" value={form.totalNetOverpayment}
                      onChange={e => setForm(f => ({ ...f, totalNetOverpayment: e.target.value }))} />
                  </div>
                ) : (
                  <div className="wq-form-group">
                    <label className="wq-label">Overpaid Hours (minutes) *</label>
                    <input type="number" className="wq-input" value={form.overpaidHoursMinutes}
                      onChange={e => setForm(f => ({ ...f, overpaidHoursMinutes: e.target.value }))} />
                  </div>
                )}
                <div className="wq-form-group">
                  <label className="wq-label">Recovery Method *</label>
                  <select className="wq-input" value={form.recoveryMethod}
                    onChange={e => setForm(f => ({ ...f, recoveryMethod: e.target.value }))}>
                    <option value="PAYROLL_DEDUCTION">Payroll Deduction</option>
                    <option value="PERSONAL_PAYMENT">Personal Payment</option>
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Installment Type</label>
                  <select className="wq-input" value={form.installmentType}
                    onChange={e => setForm(f => ({ ...f, installmentType: e.target.value }))}>
                    <option value="AUTOMATIC">Automatic</option>
                    <option value="NEGOTIATED">Negotiated</option>
                    <option value="OTHER">Other</option>
                  </select>
                </div>
                {form.installmentType === 'NEGOTIATED' && (
                  <div className="wq-form-group">
                    <label className="wq-label">Installment Amount ($)</label>
                    <input type="number" step="0.01" className="wq-input" value={form.installmentAmount}
                      onChange={e => setForm(f => ({ ...f, installmentAmount: e.target.value }))} />
                  </div>
                )}
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Reason *</label>
                <textarea className="wq-input" rows={2} value={form.reason}
                  onChange={e => setForm(f => ({ ...f, reason: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Comments</label>
                <textarea className="wq-input" rows={2} value={form.comments}
                  onChange={e => setForm(f => ({ ...f, comments: e.target.value }))} />
              </div>
              <div style={{ padding: '8px 12px', background: '#ebf8ff', border: '1px solid #90cdf4', borderRadius: 6, fontSize: 13, color: '#2c5282', marginTop: 8 }}>
                Service period max 12 months. PAYROLL_DEDUCTION → status PENDING_PAYROLL. PERSONAL_PAYMENT → status ACTIVE.
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => { setCreateModal(false); resetForm(); }}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Saving…' : 'Create Overpayment'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Collection Modal */}
      {addCollectionModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header">
              <h3>Record Personal Payment</h3>
              <button className="wq-modal-close" onClick={() => setAddCollectionModal(false)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-group">
                <label className="wq-label">Payment Date *</label>
                <input type="date" className="wq-input" value={collectionForm.collectionDate}
                  onChange={e => setCollectionForm(f => ({ ...f, collectionDate: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Amount ($) *</label>
                <input type="number" step="0.01" className="wq-input" value={collectionForm.amount}
                  onChange={e => setCollectionForm(f => ({ ...f, amount: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Payment Mode</label>
                <select className="wq-input" value={collectionForm.modeOfPayment}
                  onChange={e => setCollectionForm(f => ({ ...f, modeOfPayment: e.target.value }))}>
                  <option value="PERSONAL_PAYMENT">Personal Payment</option>
                  <option value="PAYROLL_DEDUCTION">Payroll Deduction</option>
                </select>
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Receipt Number</label>
                <input className="wq-input" value={collectionForm.receiptNumber}
                  onChange={e => setCollectionForm(f => ({ ...f, receiptNumber: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={2} value={collectionForm.notes}
                  onChange={e => setCollectionForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setAddCollectionModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleAddCollection} disabled={collectionSaving}>
                {collectionSaving ? 'Saving…' : 'Record Payment'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
