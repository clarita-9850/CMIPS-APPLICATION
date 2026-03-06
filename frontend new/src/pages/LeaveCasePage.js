import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import './WorkQueues.css';

// DSD Section 3.2 — Leave Case
// EM-41: If L0006 AND Auth Start Date before Resource Suspension End Date → error
// EM-43: If L0006 AND Resource Suspension End Date is blank → error
// EM-52: If L0006 AND Funding Source not IHSS-R → error
// EM-88: Resource Suspension End Date before Leave Auth End Date → error
// EM-90: Future auth with Term/Leave status, Leave Auth End Date after prior Auth End Date → error
// EM-96: Auth End Date more than one month in the future → error
// EM-127: Leave Auth End Date before latest of Application Date or Initial Assessment Auth Start Date → error

const LEAVE_REASONS = [
  { code: 'L0001', label: 'L0001 - Temporarily in Hospital' },
  { code: 'L0002', label: 'L0002 - Temporarily in SNF' },
  { code: 'L0003', label: 'L0003 - Temporarily in ICF' },
  { code: 'L0004', label: 'L0004 - Temporarily in CCF' },
  { code: 'L0005', label: 'L0005 - Temporarily out of State (over 6 months)' },
  { code: 'L0006', label: 'L0006 - Undervalue Disposal of Resources' },
  { code: 'L0007', label: 'L0007 - Leave Status at Conversion' },
  { code: 'L0008', label: 'L0008 - Other Facility' },
];

export const LeaveCasePage = () => {
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

  const today = new Date();
  const maxDate = new Date(today);
  maxDate.setMonth(maxDate.getMonth() + 1);
  const maxDateStr = maxDate.toISOString().slice(0, 10);

  const [form, setForm] = useState({
    leaveReason: '',
    authorizationEndDate: '',
    resourceSuspensionEndDate: '',
    comments: '',
  });

  const isL0006 = form.leaveReason === 'L0006';

  useEffect(() => {
    if (!caseId) { navigate('/cases'); return; }
    casesApi.getCaseById(caseId)
      .then(d => {
        setCaseData(d);
        setBreadcrumbs([
          { label: 'Cases', path: '/cases' },
          { label: `Case ${d.caseNumber || caseId}`, path: `/cases/${caseId}` },
          { label: 'Place on Leave' },
        ]);
      })
      .catch(() => navigate('/cases'))
      .finally(() => setLoading(false));
  }, [caseId, navigate, setBreadcrumbs]);

  const validate = () => {
    const e = {};
    if (!form.leaveReason) e.leaveReason = 'Leave reason is required.';
    if (!form.authorizationEndDate) {
      e.authorizationEndDate = 'Authorization End Date is required.';
    } else {
      const authEnd = new Date(form.authorizationEndDate);
      // EM-96: Auth End Date cannot be more than one month in the future
      if (authEnd > maxDate) {
        e.authorizationEndDate = 'EM-96: Authorization End Date cannot be more than one month in the future.';
      }
      // EM-127: Must be on or after application date
      if (caseData?.applicationDate) {
        const appDate = new Date(caseData.applicationDate);
        if (authEnd < appDate) {
          e.authorizationEndDate = 'EM-127: Authorization End Date must be on or after the Application Date.';
        }
      }
    }

    if (isL0006) {
      // EM-43: Resource Suspension End Date required
      if (!form.resourceSuspensionEndDate) {
        e.resourceSuspensionEndDate = 'EM-43: Resource Suspension End Date is required for this reason.';
      } else if (form.authorizationEndDate) {
        const resEnd = new Date(form.resourceSuspensionEndDate);
        const authEnd = new Date(form.authorizationEndDate);
        // EM-88: Resource Suspension End Date cannot be before Auth End Date
        if (resEnd < authEnd) {
          e.resourceSuspensionEndDate = 'EM-88: Resource Suspension End Date cannot be before the Authorization End Date.';
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
    const payload = {
      reason: form.leaveReason,
      authorizationEndDate: form.authorizationEndDate,
      comments: form.comments,
      placedOnLeaveBy: username,
    };
    if (isL0006 && form.resourceSuspensionEndDate) {
      payload.resourceSuspensionEndDate = form.resourceSuspensionEndDate;
    }
    casesApi.placeOnLeave(caseId, payload)
      .then(() => {
        const noaMsg = form.leaveReason !== 'L0007' ? ' A Leave Notice (NOA) will be generated.' : '';
        sessionStorage.setItem('caseInfoMessage', `Case placed on leave.${noaMsg}`);
        navigate(`/cases/${caseId}`);
      })
      .catch(err => {
        setServerError(err?.response?.data?.message || err?.response?.data?.error || 'Leave action failed.');
        setSubmitting(false);
      });
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  const c = caseData || {};
  const allowedStatuses = ['ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE'];
  if (!allowedStatuses.includes(c.status)) {
    return (
      <div className="wq-page">
        <div className="wq-panel">
          <div className="wq-panel-body">
            <p style={{ color: '#c53030' }}>Case status <strong>{c.status}</strong> does not allow Leave action. Only {allowedStatuses.join(', ')} cases can be placed on Leave.</p>
            <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Back to Case</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Place Case on Leave</h2>
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
            <div className="wq-detail-row"><span className="wq-detail-label">County:</span><span className="wq-detail-value">{c.countyCode || '\u2014'}</span></div>
          </div>
        </div>
      </div>

      {/* Info Banner */}
      <div style={{ background: '#fffbeb', border: '1px solid #f6e05e', borderLeft: '4px solid #d69e2e', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>
        <strong style={{ color: '#744210' }}>Leave of Absence:</strong> Case services will be suspended from the Authorization End Date. The recipient may return to services upon resolution of the leave condition.
        {isL0006 && (
          <div style={{ marginTop: '0.5rem', color: '#744210' }}>
            <strong>L0006 — Undervalue Disposal of Resources:</strong> Resource Suspension End Date is required. Funding Source must be IHSS-R. (EM-52)
          </div>
        )}
      </div>

      {serverError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030' }}>{serverError}</div>
      )}

      {/* Form */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Leave Details</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', maxWidth: '700px' }}>
            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Leave Reason <span style={{ color: '#c53030' }}>*</span></label>
              <select className={`wq-input${errors.leaveReason ? ' wq-input-error' : ''}`}
                value={form.leaveReason}
                onChange={e => { setForm(p => ({ ...p, leaveReason: e.target.value, resourceSuspensionEndDate: '' })); setErrors(p => ({ ...p, leaveReason: '', resourceSuspensionEndDate: '' })); }}
                style={{ width: '100%' }}>
                <option value="">Select reason...</option>
                {LEAVE_REASONS.map(r => <option key={r.code} value={r.code}>{r.label}</option>)}
              </select>
              {errors.leaveReason && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.leaveReason}</p>}
            </div>

            <div>
              <label className="wq-label">Authorization End Date <span style={{ color: '#c53030' }}>*</span></label>
              <input type="date" className={`wq-input${errors.authorizationEndDate ? ' wq-input-error' : ''}`}
                value={form.authorizationEndDate}
                max={maxDateStr}
                onChange={e => { setForm(p => ({ ...p, authorizationEndDate: e.target.value })); setErrors(p => ({ ...p, authorizationEndDate: '' })); }} />
              {errors.authorizationEndDate && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.authorizationEndDate}</p>}
              <p style={{ fontSize: '0.75rem', color: '#718096', margin: '2px 0 0' }}>Cannot be more than 1 month in the future (EM-96)</p>
            </div>

            {isL0006 && (
              <div>
                <label className="wq-label">Resource Suspension End Date <span style={{ color: '#c53030' }}>*</span></label>
                <input type="date" className={`wq-input${errors.resourceSuspensionEndDate ? ' wq-input-error' : ''}`}
                  value={form.resourceSuspensionEndDate}
                  min={form.authorizationEndDate || undefined}
                  onChange={e => { setForm(p => ({ ...p, resourceSuspensionEndDate: e.target.value })); setErrors(p => ({ ...p, resourceSuspensionEndDate: '' })); }} />
                {errors.resourceSuspensionEndDate && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.resourceSuspensionEndDate}</p>}
                <p style={{ fontSize: '0.75rem', color: '#718096', margin: '2px 0 0' }}>Required for L0006, must be on or after Authorization End Date (EM-88)</p>
              </div>
            )}

            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Comments / Notes</label>
              <textarea className="wq-input" rows={3} value={form.comments}
                onChange={e => setForm(p => ({ ...p, comments: e.target.value }))}
                placeholder="Document reason for leave..."
                style={{ width: '100%' }} />
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Processing...' : 'Confirm Leave'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>
    </div>
  );
};
