import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import './WorkQueues.css';

// DSD Section 3.1 — Withdraw Case
// EM-87: Withdrawal Date cannot be before the Application Date
// EM-93: Withdrawal Date must not be future (must be current or prior date)
// EM-94: Withdrawal Date must be on or after the Application Date
// W0001 generates NOA; W0002 = conversion status (no NOA)

const WITHDRAWAL_REASONS = [
  { code: 'W0001', label: 'W0001 - Withdrawal requested by Recipient' },
  { code: 'W0002', label: 'W0002 - Withdrawal Status at Conversion' },
];

export const WithdrawalCasePage = () => {
  const [searchParams] = useSearchParams();
  const caseId = searchParams.get('caseId');
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'system';
  const { setBreadcrumbs } = useBreadcrumbs();

  const [caseData, setCaseData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState('');

  const today = new Date().toISOString().slice(0, 10);

  const [form, setForm] = useState({
    withdrawalReason: '',
    withdrawalDate: today,
    comments: '',
  });

  useEffect(() => {
    if (!caseId) { navigate('/cases'); return; }
    casesApi.getCaseById(caseId)
      .then(d => {
        setCaseData(d);
        setBreadcrumbs([
          { label: 'Cases', path: '/cases' },
          { label: `Case ${d.caseNumber || caseId}`, path: `/cases/${caseId}` },
          { label: 'Withdraw Application' },
        ]);
      })
      .catch(() => navigate('/cases'))
      .finally(() => setLoading(false));
  }, [caseId, navigate, setBreadcrumbs]);

  const validate = () => {
    const e = {};
    if (!form.withdrawalReason) e.withdrawalReason = 'Withdrawal reason is required.';
    if (!form.withdrawalDate) {
      e.withdrawalDate = 'Withdrawal Date is required.';
    } else {
      const wd = new Date(form.withdrawalDate);
      const now = new Date();
      now.setHours(23, 59, 59, 999);
      // EM-93: cannot be future
      if (wd > now) e.withdrawalDate = 'EM-93: Withdrawal Date must not be a future date.';
      // EM-87/94: cannot be before application date
      else if (caseData?.applicationDate) {
        const appDate = new Date(caseData.applicationDate);
        if (wd < appDate) {
          const fmt = appDate.toLocaleDateString('en-US');
          e.withdrawalDate = `EM-87: Withdrawal Date cannot be before the Application Date (${fmt}).`;
        }
      }
    }
    return e;
  };

  const handleSubmit = () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setSubmitting(true);
    setServerError('');
    casesApi.withdrawCase(caseId, {
      reason: form.withdrawalReason,
      withdrawalDate: form.withdrawalDate,
      comments: form.comments,
      withdrawnBy: username,
    })
      .then(() => {
        const noaMsg = form.withdrawalReason === 'W0001'
          ? ' NOA will be generated and mailed to recipient.' : '';
        sessionStorage.setItem('caseInfoMessage', `Application withdrawn.${noaMsg}`);
        navigate(`/cases/${caseId}`);
      })
      .catch(err => {
        setServerError(err?.response?.data?.message || err?.response?.data?.error || 'Withdrawal failed.');
        setSubmitting(false);
      });
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  const c = caseData || {};
  if (c.status !== 'PENDING') {
    return (
      <div className="wq-page">
        <div className="wq-panel">
          <div className="wq-panel-body">
            <p style={{ color: '#c53030' }}>Case status <strong>{c.status}</strong> does not allow withdrawal. Only PENDING applications can be withdrawn.</p>
            <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Back to Case</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Withdraw Application</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>

      {/* Case Summary */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Case Summary</h4></div>
        <div className="wq-panel-body">
          <div className="wq-detail-grid">
            <div className="wq-detail-row"><span className="wq-detail-label">Case Number:</span><span className="wq-detail-value">{c.caseNumber || caseId}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Recipient:</span><span className="wq-detail-value">{c.recipientName || c.clientName || '\u2014'}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Current Status:</span><span className="wq-detail-value"><span className={`wq-badge wq-badge-${(c.status || '').toLowerCase()}`}>{c.status}</span></span></div>
            {c.applicationDate && (
              <div className="wq-detail-row"><span className="wq-detail-label">Application Date:</span><span className="wq-detail-value">{new Date(c.applicationDate).toLocaleDateString('en-US')}</span></div>
            )}
          </div>
        </div>
      </div>

      {/* Warning */}
      <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>
        <strong style={{ color: '#c53030' }}>Warning:</strong> Withdrawing this application will set the case status to APPLICATION_WITHDRAWN. This action can be reversed with a Reactivate.
        {form.withdrawalReason === 'W0001' && (
          <div style={{ marginTop: '0.5rem', color: '#744210' }}>A Withdrawal Notice (NOA) will be generated and mailed to the recipient.</div>
        )}
      </div>

      {serverError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030' }}>{serverError}</div>
      )}

      {/* Form */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Withdrawal Details</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', maxWidth: '700px' }}>
            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Withdrawal Reason <span style={{ color: '#c53030' }}>*</span></label>
              <select className={`wq-input${errors.withdrawalReason ? ' wq-input-error' : ''}`}
                value={form.withdrawalReason}
                onChange={e => { setForm(p => ({ ...p, withdrawalReason: e.target.value })); setErrors(p => ({ ...p, withdrawalReason: '' })); }}
                style={{ width: '100%' }}>
                <option value="">Select reason...</option>
                {WITHDRAWAL_REASONS.map(r => <option key={r.code} value={r.code}>{r.label}</option>)}
              </select>
              {errors.withdrawalReason && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.withdrawalReason}</p>}
            </div>

            <div>
              <label className="wq-label">Withdrawal Date <span style={{ color: '#c53030' }}>*</span></label>
              <input type="date" className={`wq-input${errors.withdrawalDate ? ' wq-input-error' : ''}`}
                value={form.withdrawalDate}
                max={today}
                min={c.applicationDate ? c.applicationDate.slice(0, 10) : undefined}
                onChange={e => { setForm(p => ({ ...p, withdrawalDate: e.target.value })); setErrors(p => ({ ...p, withdrawalDate: '' })); }} />
              {errors.withdrawalDate && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.withdrawalDate}</p>}
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Comments / Notes</label>
              <textarea className="wq-input" rows={3} value={form.comments}
                onChange={e => setForm(p => ({ ...p, comments: e.target.value }))}
                placeholder="Document reason for withdrawal..."
                style={{ width: '100%' }} />
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <button className="wq-btn wq-btn-danger" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Processing...' : 'Confirm Withdrawal'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>
    </div>
  );
};
