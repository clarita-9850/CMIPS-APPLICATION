import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import './WorkQueues.css';

// DSD Section 3.6 — Reactivate Case
// EM-58:  Person record Death Outcome = Death Confirmed → Do not allow
// EM-98:  TR25 — 90-day reactivation block after CC514 termination
// EM-100: Person record has Duplicate SSN or Suspect SSN → Do not allow
// EM-112: IHSS Referral Date changed to date >14 calendar days prior → Do not allow
// EM-113: IHSS Referral Date changed to future date → Do not allow
// EM-117: CIN/SCI search has not been performed → Do not allow
// When reactivated: case status → PENDING, all evidence must be entered as new case

const RESIDENCY_OPTIONS = [
  { code: 'Y', label: 'Yes — Meets California Residency Requirement' },
  { code: 'N', label: 'No — Does Not Meet Residency Requirement' },
  { code: 'P', label: 'Pending Verification' },
];

const REFERRAL_SOURCES = [
  { code: 'RS01', label: 'RS01 - Self-Referral' },
  { code: 'RS02', label: 'RS02 - Family Member' },
  { code: 'RS03', label: 'RS03 - Friend / Neighbor' },
  { code: 'RS04', label: 'RS04 - Hospital / Medical Facility' },
  { code: 'RS05', label: 'RS05 - Physician / Medical Provider' },
  { code: 'RS06', label: 'RS06 - Public Health Nurse' },
  { code: 'RS07', label: 'RS07 - Social Worker' },
  { code: 'RS08', label: 'RS08 - Community Organization' },
  { code: 'RS09', label: 'RS09 - County Adult Protective Services' },
  { code: 'RS10', label: 'RS10 - Ombudsman' },
  { code: 'RS11', label: 'RS11 - Legal Aid' },
  { code: 'RS12', label: 'RS12 - Mental Health Services' },
  { code: 'RS13', label: 'RS13 - Regional Center' },
  { code: 'RS14', label: 'RS14 - Developmental Disability Program' },
  { code: 'RS15', label: 'RS15 - Veterans Administration' },
  { code: 'RS16', label: 'RS16 - Medi-Cal Eligibility Worker' },
  { code: 'RS17', label: 'RS17 - Mail In' },
  { code: 'RS18', label: 'RS18 - Phone-In' },
  { code: 'RS19', label: 'RS19 - Internet / Online' },
  { code: 'RS20', label: 'RS20 - Walk-In' },
  { code: 'RS21', label: 'RS21 - CPS / Child Protective Services' },
  { code: 'RS22', label: 'RS22 - Parole / Probation' },
  { code: 'RS23', label: 'RS23 - Skilled Nursing Facility' },
  { code: 'RS24', label: 'RS24 - Intermediate Care Facility' },
  { code: 'RS25', label: 'RS25 - Residential Care Facility' },
  { code: 'RS26', label: 'RS26 - Home Health Agency' },
  { code: 'RS27', label: 'RS27 - Senior Center' },
  { code: 'RS28', label: 'RS28 - Senior Information and Assistance' },
  { code: 'RS29', label: 'RS29 - Area Agency on Aging' },
  { code: 'RS30', label: 'RS30 - State Hearing Decision' },
  { code: 'RS31', label: 'RS31 - Other State Agency' },
  { code: 'RS32', label: 'RS32 - Other County Agency' },
  { code: 'RS33', label: 'RS33 - DHCS' },
  { code: 'RS34', label: 'RS34 - Prior CMIPS Record' },
  { code: 'RS35', label: 'RS35 - School District' },
  { code: 'RS36', label: 'RS36 - Conversion' },
  { code: 'RS37', label: 'RS37 - Reinstatement' },
  { code: 'RS38', label: 'RS38 - Transition Assistance Program' },
  { code: 'RS39', label: 'RS39 - Other' },
  { code: 'RS40', label: 'RS40 - Unknown' },
];

export const ReactivateCasePage = () => {
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
  // EM-112: Max 14 days prior
  const minReferralDate = new Date();
  minReferralDate.setDate(minReferralDate.getDate() - 14);
  const minReferralDateStr = minReferralDate.toISOString().slice(0, 10);

  const [form, setForm] = useState({
    ihssReferralDate: today,
    meetsResidencyRequirement: '',
    referralSource: '',
    interpreterAvailable: false,
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
          { label: 'Reactivate Case' },
        ]);
      })
      .catch(() => navigate('/cases'))
      .finally(() => setLoading(false));
  }, [caseId, navigate, setBreadcrumbs]);

  const validate = () => {
    const e = {};
    if (!form.ihssReferralDate) {
      e.ihssReferralDate = 'IHSS Referral Date is required.';
    } else {
      const refDate = new Date(form.ihssReferralDate);
      const todayDate = new Date();
      todayDate.setHours(23, 59, 59, 999);
      // EM-113: Cannot be a future date
      if (refDate > todayDate) {
        e.ihssReferralDate = 'EM-113: IHSS Referral Date cannot be a future date.';
      }
      // EM-112: Cannot be more than 14 days prior
      else if (refDate < minReferralDate) {
        e.ihssReferralDate = 'EM-112: IHSS Referral Date may not be more than 14 calendar days prior to the current date.';
      }
    }
    if (!form.meetsResidencyRequirement) e.meetsResidencyRequirement = 'Residency requirement status is required.';
    if (!form.referralSource) e.referralSource = 'Referral Source is required.';
    return e;
  };

  const handleSubmit = () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setSubmitting(true);
    setServerError('');
    casesApi.reactivateCase(caseId, {
      ihssReferralDate: form.ihssReferralDate,
      meetsResidencyRequirement: form.meetsResidencyRequirement,
      referralSource: form.referralSource,
      interpreterAvailable: form.interpreterAvailable,
      comments: form.comments,
      reactivatedBy: username,
    })
      .then(() => {
        sessionStorage.setItem('caseInfoMessage', 'Case reactivated. Status set to PENDING. All evidence must be entered as a new case.');
        navigate(`/cases/${caseId}`);
      })
      .catch(err => {
        setServerError(err?.response?.data?.message || err?.response?.data?.error || 'Reactivation failed.');
        setSubmitting(false);
      });
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  const c = caseData || {};
  const allowedStatuses = ['TERMINATED', 'DENIED', 'APPLICATION_WITHDRAWN'];
  if (!allowedStatuses.includes(c.status)) {
    return (
      <div className="wq-page">
        <div className="wq-panel">
          <div className="wq-panel-body">
            <p style={{ color: '#c53030' }}>Case status <strong>{c.status}</strong> does not allow Reactivation. Only {allowedStatuses.join(', ')} cases can be reactivated.</p>
            <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Back to Case</button>
          </div>
        </div>
      </div>
    );
  }

  // TR25: Show block warning if terminated with CC514 less than 90 days ago
  const isTr25Block = c.tr25BlockEndDate && new Date(c.tr25BlockEndDate) > new Date();

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Reactivate Case</h2>
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

      {/* TR25 block warning */}
      {isTr25Block && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem' }}>
          <strong style={{ color: '#c53030' }}>TR25 — Reactivation Blocked:</strong> This case was terminated with reason CC514 (Medi-Cal Non-Compliance). A 90-day reactivation block is in effect until <strong>{new Date(c.tr25BlockEndDate).toLocaleDateString('en-US')}</strong>. (EM-98)
        </div>
      )}

      {/* Info Banner */}
      <div style={{ background: '#f0fff4', border: '1px solid #9ae6b4', borderLeft: '4px solid #38a169', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', fontSize: '0.875rem' }}>
        <strong style={{ color: '#276749' }}>Reactivation:</strong> Case status will be set to <strong>PENDING</strong>. All program evidence (assessment, eligibility, authorization) must be entered as a new case after reactivation.
        <ul style={{ margin: '0.25rem 0 0 1rem', color: '#276749' }}>
          <li>EM-58: Deceased recipients (Death Confirmed) cannot be reactivated.</li>
          <li>EM-100: Cases with Duplicate/Suspect SSN cannot be reactivated.</li>
          <li>EM-117: CIN/SCI search must be performed before reactivation.</li>
        </ul>
      </div>

      {serverError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030' }}>{serverError}</div>
      )}

      {/* Form */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Reactivation Details</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', maxWidth: '700px' }}>

            <div>
              <label className="wq-label">IHSS Referral Date <span style={{ color: '#c53030' }}>*</span></label>
              <input type="date" className={`wq-input${errors.ihssReferralDate ? ' wq-input-error' : ''}`}
                value={form.ihssReferralDate}
                min={minReferralDateStr}
                max={today}
                onChange={e => { setForm(p => ({ ...p, ihssReferralDate: e.target.value })); setErrors(p => ({ ...p, ihssReferralDate: '' })); }} />
              {errors.ihssReferralDate && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.ihssReferralDate}</p>}
              <p style={{ fontSize: '0.75rem', color: '#718096', margin: '2px 0 0' }}>Cannot be more than 14 days prior (EM-112) or future (EM-113)</p>
            </div>

            <div>
              <label className="wq-label">Meets Residency Requirement <span style={{ color: '#c53030' }}>*</span></label>
              <select className={`wq-input${errors.meetsResidencyRequirement ? ' wq-input-error' : ''}`}
                value={form.meetsResidencyRequirement}
                onChange={e => { setForm(p => ({ ...p, meetsResidencyRequirement: e.target.value })); setErrors(p => ({ ...p, meetsResidencyRequirement: '' })); }}
                style={{ width: '100%' }}>
                <option value="">Select...</option>
                {RESIDENCY_OPTIONS.map(r => <option key={r.code} value={r.code}>{r.label}</option>)}
              </select>
              {errors.meetsResidencyRequirement && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.meetsResidencyRequirement}</p>}
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Referral Source <span style={{ color: '#c53030' }}>*</span></label>
              <select className={`wq-input${errors.referralSource ? ' wq-input-error' : ''}`}
                value={form.referralSource}
                onChange={e => { setForm(p => ({ ...p, referralSource: e.target.value })); setErrors(p => ({ ...p, referralSource: '' })); }}
                style={{ width: '100%' }}>
                <option value="">Select referral source...</option>
                {REFERRAL_SOURCES.map(r => <option key={r.code} value={r.code}>{r.label}</option>)}
              </select>
              {errors.referralSource && <p style={{ color: '#c53030', fontSize: '0.8rem', margin: '2px 0 0' }}>{errors.referralSource}</p>}
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                <input type="checkbox"
                  checked={form.interpreterAvailable}
                  onChange={e => setForm(p => ({ ...p, interpreterAvailable: e.target.checked }))} />
                <span className="wq-label" style={{ margin: 0 }}>Interpreter Available in Home</span>
              </label>
            </div>

            <div style={{ gridColumn: '1 / -1' }}>
              <label className="wq-label">Comments / Notes</label>
              <textarea className="wq-input" rows={3} value={form.comments}
                onChange={e => setForm(p => ({ ...p, comments: e.target.value }))}
                placeholder="Document reason for reactivation..."
                style={{ width: '100%' }} />
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '0.75rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={handleSubmit} disabled={submitting || isTr25Block}>
          {submitting ? 'Processing...' : 'Confirm Reactivation'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/cases/${caseId}`)}>Cancel</button>
      </div>
    </div>
  );
};
