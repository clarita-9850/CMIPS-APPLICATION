import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as api from '../api/paymentCorrectionsApi';
import './WorkQueues.css';

/**
 * Payment Corrections — DSD Section 27 CI-67321
 *
 * Corrects over-reported hours, prior underpayments, timesheet exceptions,
 * and WPCS recipient-on-leave situations.
 * All corrections require secondary Payroll Approver approval.
 */
const CORRECTION_TYPES = [
  { value: 'OVER_REPORTED_HOURS', label: 'Over-Reported Hours' },
  { value: 'PRIOR_UNDERPAYMENT', label: 'Prior Underpayment' },
  { value: 'TIMESHEET_EXCEPTION', label: 'Timesheet Exception' },
  { value: 'WPCS_RECIPIENT_ON_LEAVE', label: 'WPCS Recipient On Leave' }
];

const DAYS = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'];

const STATUS_COLORS = {
  PENDING: '#feebc8',
  PENDING_APPROVAL: '#e9d8fd',
  APPROVED: '#c6f6d5',
  REJECTED: '#fed7d7',
  CANCELLED: '#e2e8f0',
  PROCESSED: '#bee3f8'
};

export const PaymentCorrectionsPage = () => {
  const { caseId } = useParams();
  const { setBreadcrumbs } = useBreadcrumbs();
  const isWorkQueue = !caseId;

  const [corrections, setCorrections] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [createModal, setCreateModal] = useState(false);
  const [form, setForm] = useState({
    providerId: '', providerName: '',
    correctionType: 'OVER_REPORTED_HOURS',
    payPeriodStart: '', payPeriodEnd: '',
    hoursCorrectedMinutes: '',
    dailyTimeEntries: Object.fromEntries(DAYS.map(d => [d, ''])),
    program: 'IHSS', notes: ''
  });
  const [saving, setSaving] = useState(false);

  const [rejectModal, setRejectModal] = useState(null);
  const [rejectReason, setRejectReason] = useState('');
  const [viewModal, setViewModal] = useState(null);

  useEffect(() => {
    const crumbs = [{ label: 'My Workspace', path: '/workspace' }];
    if (caseId) {
      crumbs.push({ label: `Case ${caseId}`, path: `/cases/${caseId}` });
      crumbs.push({ label: 'Payment Corrections' });
    } else {
      crumbs.push({ label: 'Payment Corrections — Pending Approval' });
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
      setCorrections(Array.isArray(data) ? data : []);
    } catch {
      setError('Failed to load payment corrections.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    setSaving(true); setError('');
    try {
      const payload = {
        ...form,
        hoursCorrectedMinutes: form.hoursCorrectedMinutes ? Number(form.hoursCorrectedMinutes) : null,
        dailyTimeEntries: JSON.stringify(form.dailyTimeEntries)
      };
      await api.create(caseId, payload);
      setSuccess('Payment correction created.');
      setCreateModal(false);
      resetForm();
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create correction.');
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
      setSuccess(`Correction ${action}ted successfully.`);
      load();
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action}.`);
    }
  };

  const resetForm = () => setForm({
    providerId: '', providerName: '',
    correctionType: 'OVER_REPORTED_HOURS',
    payPeriodStart: '', payPeriodEnd: '',
    hoursCorrectedMinutes: '',
    dailyTimeEntries: Object.fromEntries(DAYS.map(d => [d, ''])),
    program: 'IHSS', notes: ''
  });

  const fmtMins = (m) => {
    if (m == null) return '—';
    const h = Math.floor(m / 60), min = m % 60;
    return `${h}:${String(min).padStart(2, '0')}`;
  };

  return (
    <div className="wq-page">
      <div className="wq-header">
        <div>
          <h1 className="wq-title">
            {isWorkQueue ? 'Payment Corrections — Pending Approval' : 'Payment Corrections'}
          </h1>
          <p className="wq-subtitle">DSD Section 27 CI-67321 — Requires Payroll Approver secondary approval</p>
        </div>
        {!isWorkQueue && (
          <button className="wq-btn wq-btn-primary" onClick={() => setCreateModal(true)}>
            + New Payment Correction
          </button>
        )}
      </div>

      {error && <div className="wq-error-msg">{error}</div>}
      {success && <div className="wq-success-msg">{success}</div>}

      <div className="wq-card">
        {loading ? (
          <div className="wq-loading">Loading…</div>
        ) : corrections.length === 0 ? (
          <div className="wq-empty">No payment corrections found.</div>
        ) : (
          <table className="wq-table">
            <thead>
              <tr>
                <th>Correction Type</th>
                <th>Provider</th>
                <th>Pay Period</th>
                <th>Hours Corrected</th>
                <th>Program</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {corrections.map(c => (
                <tr key={c.id}>
                  <td>{CORRECTION_TYPES.find(t => t.value === c.correctionType)?.label || c.correctionType}</td>
                  <td>{c.providerName || '—'}</td>
                  <td>{c.payPeriodStart ? `${c.payPeriodStart} – ${c.payPeriodEnd || '…'}` : '—'}</td>
                  <td>{fmtMins(c.hoursCorrectedMinutes)}</td>
                  <td>{c.program || '—'}</td>
                  <td>
                    <span style={{
                      background: STATUS_COLORS[c.status] || '#e2e8f0',
                      padding: '2px 8px', borderRadius: 12, fontSize: 12, fontWeight: 600
                    }}>{c.status}</span>
                  </td>
                  <td style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                    <button className="wq-btn wq-btn-secondary wq-btn-sm"
                      onClick={() => setViewModal(c)}>View</button>
                    {c.status === 'PENDING' && (
                      <button className="wq-btn wq-btn-primary wq-btn-sm"
                        onClick={() => handleAction('submit', c.id)}>Submit</button>
                    )}
                    {c.status === 'PENDING_APPROVAL' && (
                      <>
                        <button className="wq-btn wq-btn-primary wq-btn-sm"
                          onClick={() => handleAction('approve', c.id)}>Approve</button>
                        <button className="wq-btn wq-btn-danger wq-btn-sm"
                          onClick={() => { setRejectModal(c.id); setRejectReason(''); }}>Reject</button>
                      </>
                    )}
                    {(c.status === 'PENDING' || c.status === 'PENDING_APPROVAL') && (
                      <button className="wq-btn wq-btn-secondary wq-btn-sm"
                        onClick={() => handleAction('cancel', c.id)}>Cancel</button>
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
              <h3>New Payment Correction</h3>
              <button className="wq-modal-close" onClick={() => { setCreateModal(false); resetForm(); }}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div style={{ marginBottom: 12, padding: '8px 12px', background: '#fffbeb', border: '1px solid #f6e05e', borderRadius: 6, fontSize: 13, color: '#744210' }}>
                All payment corrections require secondary Payroll Approver approval before processing.
              </div>
              <div className="wq-form-grid">
                <div className="wq-form-group">
                  <label className="wq-label">Correction Type *</label>
                  <select className="wq-input" value={form.correctionType}
                    onChange={e => setForm(f => ({ ...f, correctionType: e.target.value }))}>
                    {CORRECTION_TYPES.map(t => (
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
                  <label className="wq-label">Provider ID</label>
                  <input className="wq-input" value={form.providerId}
                    onChange={e => setForm(f => ({ ...f, providerId: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Provider Name</label>
                  <input className="wq-input" value={form.providerName}
                    onChange={e => setForm(f => ({ ...f, providerName: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Pay Period Start *</label>
                  <input type="date" className="wq-input" value={form.payPeriodStart}
                    onChange={e => setForm(f => ({ ...f, payPeriodStart: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Pay Period End *</label>
                  <input type="date" className="wq-input" value={form.payPeriodEnd}
                    onChange={e => setForm(f => ({ ...f, payPeriodEnd: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Hours Corrected (minutes total)</label>
                  <input type="number" className="wq-input" value={form.hoursCorrectedMinutes}
                    onChange={e => setForm(f => ({ ...f, hoursCorrectedMinutes: e.target.value }))}
                    placeholder="e.g. 120 = 2:00 hrs" />
                </div>
              </div>

              {/* Daily Time Entry Grid */}
              {form.correctionType === 'OVER_REPORTED_HOURS' && (
                <div style={{ marginTop: 16 }}>
                  <h4 style={{ marginBottom: 8, fontSize: 14, color: '#2d3748' }}>Daily Time Entries (minutes per day)</h4>
                  <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    {DAYS.map(day => (
                      <div key={day} style={{ minWidth: 70 }}>
                        <label className="wq-label">{day}</label>
                        <input type="number" className="wq-input" min="0" max="1440"
                          value={form.dailyTimeEntries[day]}
                          onChange={e => setForm(f => ({
                            ...f,
                            dailyTimeEntries: { ...f.dailyTimeEntries, [day]: e.target.value }
                          }))} />
                      </div>
                    ))}
                  </div>
                </div>
              )}

              <div className="wq-form-group" style={{ marginTop: 16 }}>
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={3} value={form.notes}
                  onChange={e => setForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => { setCreateModal(false); resetForm(); }}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Saving…' : 'Create Correction'}
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
              <h3>Reject Payment Correction</h3>
              <button className="wq-modal-close" onClick={() => setRejectModal(null)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-group">
                <label className="wq-label">Rejection Reason *</label>
                <textarea className="wq-input" rows={3} value={rejectReason}
                  onChange={e => setRejectReason(e.target.value)} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setRejectModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-danger" disabled={!rejectReason.trim()}
                onClick={() => handleAction('reject', rejectModal, rejectReason)}>Reject</button>
            </div>
          </div>
        </div>
      )}

      {/* View Modal */}
      {viewModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal wq-modal-lg">
            <div className="wq-modal-header">
              <h3>Payment Correction Details</h3>
              <button className="wq-modal-close" onClick={() => setViewModal(null)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-grid">
                {[
                  ['Type', CORRECTION_TYPES.find(t => t.value === viewModal.correctionType)?.label || viewModal.correctionType],
                  ['Status', viewModal.status],
                  ['Provider', viewModal.providerName],
                  ['Program', viewModal.program],
                  ['Pay Period', `${viewModal.payPeriodStart || '—'} – ${viewModal.payPeriodEnd || '—'}`],
                  ['Hours Corrected', fmtMins(viewModal.hoursCorrectedMinutes)],
                  ['Approved By', viewModal.approvedBy],
                  ['Notes', viewModal.notes],
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
