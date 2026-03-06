import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import './WorkQueues.css';

// DSD Section 3.3 — Rescind Case
// EM-45: Only the Case Owner may rescind a case
// EM-92: Rescind not allowed when CIN does not have active Medi-Cal eligibility
// EM-99: Person record has Duplicate SSN or Suspect SSN → Do not allow
// R0001: State Hearing Filed before Termination effective (generates NOA)
// R0002: Recipient rescinds request for termination (no NOA)
// R0003: Administrative Error (generates NOA)
// R0004: State Hearing Decision (no NOA)
// R0005: Medi-Cal Non-Compliance Resolved — automated TR25 rescind only (generates NOA)

const RESCIND_REASONS = [
  { code: 'R0001', label: 'R0001 - State Hearing Filed Before Termination Effective', generatesNoa: true },
  { code: 'R0002', label: 'R0002 - Recipient Rescinds Request for Termination', generatesNoa: false },
  { code: 'R0003', label: 'R0003 - Administrative Error', generatesNoa: true },
  { code: 'R0004', label: 'R0004 - State Hearing Decision', generatesNoa: false },
  { code: 'R0005', label: 'R0005 - Medi-Cal Non-Compliance Resolved (TR25 Automated)', generatesNoa: true },
];

export const RescindCasePage = () => {
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
    rescindReason: '',
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
          { label: 'Rescind Case' },
        ]);
      })
      .catch(() => navigate('/cases'))
      .finally(() => setLoading(false));
  }, [caseId, navigate, setBreadcrumbs]);

  const validate = () => {
    const e = {};
    if (!form.rescindReason) e.rescindReason = 'Rescind reason is required.';
    return e;
  };

  const handleSubmit = () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setSubmitting(true);
    setServerError('');
    casesApi.rescindCase(caseId, {
      reason: form.rescindReason,
      rescindDate: today,
      comments: form.comments,
      rescindedBy: username,
    })
      .then(() => {
        const selected = RESCIND_REASONS.find(r => r.code === form.rescindReason);
        const noaMsg = selected?.generatesNoa ? ' A Rescind Notice (NOA) will be generated.' : '';
        sessionStorage.setItem('caseInfoMessage', `Case rescinded.${noaMsg}`);
        navigate(`/cases/${caseId}`);
      })
      .catch(err => {
        setServerError(err?.response?.data?.message || err?.response?.data?.error || 'Rescind failed.');
        setSubmitting(false);
      });
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  const c = caseData || {};
  const allowedStatuses = ['TERMINATED', 'DENIED'];
  if (!allowedStatuses.includes(c.status)) {
    return (
      <div className="wq-page">
        <div className="wq-panel">
          <div className="wq-panel-body">
            <p style={{ color: '#c53030' }}>Case status <strong>{c.status}</strong> does not allow Rescind. Only {allowedStatuses.join(', ')} cases can be rescinded.</p>
            <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Back to Case</button>
          </div>
        </div>
      </div>
    );
  }

  const selectedReason = RESCIND_REASONS.find(r => r.code === form.rescindReason);
  const isR0005 = form.rescindReason === 'R0005';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Rescind Case</h2>
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
            <div className="wq-detail-row"><span className="wq-detail-label">Rescind Date:</span><span className="wq-detail-value">{new Date(today).toLocaleDateString('en-US')} (auto-set to today)</span></div>
          </div>
          {/* Medi-Cal eligibility display if available */}
          {c.mediCalStatus && (
            <div className="wq-detail-grid" style={{ marginTop: '0.5rem', paddingTop: '0.5rem', borderTop: '1px solid #e2e8f0' }}>
              <div className="wq-detail-row"><span className="wq-detail-label">Medi-Cal Status:</span><span className="wq-detail-value">{c.mediCalStatus}</span></div>
              {c.lastMediCalEligibilityMonth && (
                <div className="wq-detail-row"><span className="wq-detail-label">Last Medi-Cal Eligibility Month:</span><span className="wq-detail-value">{c.lastMediCalEligibilityMonth}</span></div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Business rules notice */}
      <div style={{ background: '#ebf8ff', border: '1px solid #bee3f8', borderLeft: '4px solid #3182ce', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.875rem' }}>
        <strong style={{ color: '#2b6cb0' }}>Rescind Rules:</strong>
        <ul style={{ margin: '0.25rem 0 0 1rem', color: '#2b6cb0' }}>
          <li>EM-45: Only the Case Owner may rescind a case.</li>
          <li>EM-92: Active Medi-Cal eligibility is required for rescind.</li>
          <li>EM-99: Cases with Duplicate/Suspect SSN cannot be rescinded.</li>
        </ul>
        {isR0005 && (
          <div style={{ marginTop: '0.5rem', color: '#744210' }}>
            <strong>R0005 — TR25 Automated Rescind:</strong> This reason is reserved for automated Medi-Cal Non-Compliance Resolved workflows. Manual use is permitted only for TR25 correction.
          </div>
        )}
      </div>

      {serverError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030' }}>{serverError}</div>
      )}

      {/* Form */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Rescind Details</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', maxWidth: '700px' }}>
            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Rescind Reason <span style={{ color: '#c53030' }}>*</span></label>
              <select className={`wq-input${errors.rescindReason ? ' wq-input-error' : ''}`}
                value={form.rescindReason}
                onChange={e => { setForm(p => ({ ...p, rescindReason: e.target.value })); setErrors(p => ({ ...p, rescindReason: '' })); }}
                style={{ width: '100%' }}>
                <option value="">Select reason...</option>
                {RESCIND_REASONS.map(r => <option key={r.code} value={r.code}>{r.label}</option>)}
              </select>
              {errors.rescindReason && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.rescindReason}</p>}
              {selectedReason && (
                <p style={{ fontSize: '0.8rem', color: selectedReason.generatesNoa ? '#276749' : '#718096', margin: '4px 0 0' }}>
                  {selectedReason.generatesNoa ? '✓ NOA will be generated' : 'No NOA generated for this reason'}
                </p>
              )}
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Comments / Notes</label>
              <textarea className="wq-input" rows={3} value={form.comments}
                onChange={e => setForm(p => ({ ...p, comments: e.target.value }))}
                placeholder="Document reason for rescind..."
                style={{ width: '100%' }} />
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={handleSubmit} disabled={submitting}>
          {submitting ? 'Processing...' : 'Confirm Rescind'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>
    </div>
  );
};
