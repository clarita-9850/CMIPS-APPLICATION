import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import './WorkQueues.css';

// DSD Section 25 — Case Termination
// Business rules: DSD 3.2
// EM#130: Cannot terminate if in-progress inter-county transfer
// NOA 1255 (IHSS Termination) auto-generated on save
// Termination Date cannot be in the future
// Reason codes: CC501–CC514 (CC514 = Medi-Cal non-compliance, triggers TR25 90-day reactivation block)

const TERMINATION_REASONS = [
  { code: 'CC501', label: 'CC501 - Recipient No Longer Meets Functional Need Criteria' },
  { code: 'CC502', label: 'CC502 - Recipient No Longer Medi-Cal Eligible' },
  { code: 'CC503', label: 'CC503 - Recipient Moved Out of County' },
  { code: 'CC504', label: 'CC504 - Recipient Moved Out of State' },
  { code: 'CC505', label: 'CC505 - Recipient Deceased' },
  { code: 'CC506', label: 'CC506 - Recipient Institutionalized' },
  { code: 'CC507', label: 'CC507 - Recipient Refused Services' },
  { code: 'CC508', label: 'CC508 - Recipient Entered Licensed Facility' },
  { code: 'CC509', label: 'CC509 - No Provider Available' },
  { code: 'CC510', label: 'CC510 - Recipient Disqualified for Fraud' },
  { code: 'CC511', label: 'CC511 - Recipient Failed to Cooperate' },
  { code: 'CC512', label: 'CC512 - Other Reason' },
  { code: 'CC513', label: 'CC513 - Health Care Certification Not Received' },
  { code: 'CC514', label: 'CC514 - Non-Compliance with Medi-Cal (90-day reactivation block)' },
];

export const TerminateCasePage = () => {
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

  const [form, setForm] = useState({
    terminationReason: '',
    authorizationEndDate: '',
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
          { label: 'Terminate Case' },
        ]);
      })
      .catch(() => navigate('/cases'))
      .finally(() => setLoading(false));
  }, [caseId, navigate, setBreadcrumbs]);

  const validate = () => {
    const e = {};
    if (!form.terminationReason) e.terminationReason = 'Termination reason is required.';
    if (!form.authorizationEndDate) e.authorizationEndDate = 'Authorization End Date is required.';
    else if (new Date(form.authorizationEndDate) > new Date()) e.authorizationEndDate = 'Authorization End Date cannot be in the future.';
    return e;
  };

  const handleSubmit = () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setSubmitting(true);
    setServerError('');
    casesApi.terminateCase(caseId, {
      reason: form.terminationReason,
      authorizationEndDate: form.authorizationEndDate,
      comments: form.comments,
      terminatedBy: username,
    })
      .then(() => {
        sessionStorage.setItem('caseInfoMessage', `Case terminated. NOA 1255 (Termination Notice) will be generated.`);
        navigate(`/cases/${caseId}`);
      })
      .catch(err => {
        setServerError(err?.response?.data?.message || err?.response?.data?.error || 'Termination failed.');
        setSubmitting(false);
      });
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  const c = caseData || {};
  const allowedStatuses = ['ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE', 'ON_LEAVE', 'ACTIVE'];
  if (!allowedStatuses.includes(c.status)) {
    return (
      <div className="wq-page">
        <div className="wq-panel">
          <div className="wq-panel-body">
            <p style={{ color: '#c53030' }}>Case status <strong>{c.status}</strong> does not allow termination. Only {allowedStatuses.join(', ')} cases can be terminated.</p>
            <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Back to Case</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Terminate Case</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>

      {/* Case Summary */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Case Summary</h4></div>
        <div className="wq-panel-body">
          <div className="wq-detail-grid">
            <div className="wq-detail-row"><span className="wq-detail-label">Case Number:</span><span className="wq-detail-value">{c.caseNumber || caseId}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Recipient:</span><span className="wq-detail-value">{c.recipientName || c.clientName || '\u2014'}</span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">Current Status:</span><span className="wq-detail-value"><span className={`wq-badge wq-badge-${(c.status||'').toLowerCase()}`}>{c.status}</span></span></div>
            <div className="wq-detail-row"><span className="wq-detail-label">County:</span><span className="wq-detail-value">{c.countyCode || '\u2014'}</span></div>
          </div>
        </div>
      </div>

      {/* Warning Banner */}
      <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>
        <strong style={{ color: '#c53030' }}>Warning:</strong> Terminating this case will end all authorized services effective the Authorization End Date. A Termination Notice (NOA 1255) will be generated and sent to the recipient. This action cannot be undone without a Rescind.
        {form.terminationReason === 'CC514' && (
          <div style={{ marginTop: '0.5rem', color: '#744210' }}>
            <strong>TR25:</strong> Reason CC514 (Medi-Cal non-compliance) applies a 90-day block on case reactivation.
          </div>
        )}
      </div>

      {serverError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030' }}>{serverError}</div>
      )}

      {/* Termination Form */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Termination Details</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', maxWidth: '700px' }}>
            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Termination Reason <span style={{ color: '#c53030' }}>*</span></label>
              <select className={`wq-input${errors.terminationReason ? ' wq-input-error' : ''}`}
                value={form.terminationReason}
                onChange={e => { setForm(p => ({ ...p, terminationReason: e.target.value })); setErrors(p => ({ ...p, terminationReason: '' })); }}
                style={{ width: '100%' }}>
                <option value="">Select reason...</option>
                {TERMINATION_REASONS.map(r => <option key={r.code} value={r.code}>{r.label}</option>)}
              </select>
              {errors.terminationReason && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.terminationReason}</p>}
            </div>

            <div>
              <label className="wq-label">Authorization End Date <span style={{ color: '#c53030' }}>*</span></label>
              <input type="date" className={`wq-input${errors.authorizationEndDate ? ' wq-input-error' : ''}`}
                value={form.authorizationEndDate}
                max={new Date().toISOString().slice(0, 10)}
                onChange={e => { setForm(p => ({ ...p, authorizationEndDate: e.target.value })); setErrors(p => ({ ...p, authorizationEndDate: '' })); }} />
              {errors.authorizationEndDate && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.authorizationEndDate}</p>}
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Comments / Notes</label>
              <textarea className="wq-input" rows={3} value={form.comments}
                onChange={e => setForm(p => ({ ...p, comments: e.target.value }))}
                placeholder="Document reason for termination..."
                style={{ width: '100%' }} />
            </div>
          </div>
        </div>
      </div>

      {/* NOA Info */}
      <div style={{ background: '#ebf8ff', border: '1px solid #bee3f8', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.875rem', color: '#2b6cb0' }}>
        <strong>NOA 1255</strong> (IHSS Termination Notice) will be automatically generated upon saving. The notice will be mailed to the recipient in their preferred language.
      </div>

      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <button className="wq-btn wq-btn-danger" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Processing...' : 'Confirm Termination'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>
    </div>
  );
};
