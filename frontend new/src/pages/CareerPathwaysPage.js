import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as api from '../api/careerPathwayApi';
import './WorkQueues.css';

/**
 * Career Pathways Program — DSD Section 27 CI-823458
 * (California Senate Bill No. 172)
 *
 * IHSS/WPCS providers submit claims via ESP; CDSS reviews and approves.
 * Two-step approval: Initial Review → Final CDSS Approval.
 *
 * Modes:
 *   - Provider search: view claims by provider + cumulative hours
 *   - Pending Review queue: initial reviewer work queue
 *   - Pending Approval queue: CDSS final approver work queue
 */
const CLAIM_TYPES = [
  { value: 'TRAINING_TIME', label: 'Training Time' },
  { value: 'TRAINING_INCENTIVE', label: 'Training Incentive ($500)' },
  { value: 'ONE_MONTH_INCENTIVE', label: 'One-Month Incentive ($500)' },
  { value: 'SIX_MONTH_INCENTIVE', label: 'Six-Month Incentive ($2,000)' }
];

const PATHWAY_CATEGORIES = [
  { value: 'ADULT_EDUCATION', label: 'Adult Education' },
  { value: 'GENERAL_HEALTH_SAFETY', label: 'General Health & Safety' },
  { value: 'COGNITIVE_IMPAIRMENTS_BEHAVIORAL_HEALTH', label: 'Cognitive Impairments / Behavioral Health' },
  { value: 'COMPLEX_PHYSICAL_CARE_NEEDS', label: 'Complex Physical Care Needs' },
  { value: 'TRANSITION_TO_HOME_COMMUNITY_LIVING', label: 'Transition to Home & Community Living' }
];

const REJECTION_REASONS = [
  'INSUFFICIENT_TRAINING_HOURS', 'INVALID_CAREER_PATHWAY',
  'DUPLICATE_CLAIM', 'PROVIDER_NOT_ELIGIBLE', 'RECIPIENT_NOT_ELIGIBLE', 'OTHER'
];

const STATUS_COLORS = {
  PENDING_REVIEW: '#feebc8',
  PENDING_APPROVAL: '#e9d8fd',
  APPROVED: '#c6f6d5',
  PENDING_PAYROLL: '#bee3f8',
  REJECTED: '#fed7d7',
  PAID: '#c6f6d5'
};

export const CareerPathwaysPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();

  const [viewMode, setViewMode] = useState('search'); // 'search' | 'pendingReview' | 'pendingApproval'
  const [providerIdInput, setProviderIdInput] = useState('');
  const [claims, setClaims] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [cumulativeHours, setCumulativeHours] = useState(null);

  const [createModal, setCreateModal] = useState(false);
  const [form, setForm] = useState({
    providerId: '', providerName: '', caseId: '',
    claimType: 'TRAINING_TIME', careerPathwayCategory: 'ADULT_EDUCATION',
    servicePeriodFrom: '', servicePeriodTo: '',
    trainingHoursClaimedMinutes: '', className: '', classNumber: '',
    trainingDateFrom: '', trainingDateTo: '', receivedDate: ''
  });
  const [saving, setSaving] = useState(false);

  const [rejectModal, setRejectModal] = useState(null);
  const [rejectForm, setRejectForm] = useState({ rejectionReason: 'OTHER', notes: '' });
  const [viewModal, setViewModal] = useState(null);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'My Workspace', path: '/workspace' },
      { label: 'Career Pathways Program', path: '/payments/career-pathways' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    if (viewMode === 'pendingReview') loadPendingReview();
    else if (viewMode === 'pendingApproval') loadPendingApproval();
    else setClaims([]);
  }, [viewMode]);

  const loadPendingReview = async () => {
    setLoading(true); setError('');
    try {
      const data = await api.getPendingReview();
      setClaims(Array.isArray(data) ? data : []);
    } catch {
      setError('Failed to load pending review queue.');
    } finally {
      setLoading(false);
    }
  };

  const loadPendingApproval = async () => {
    setLoading(true); setError('');
    try {
      const data = await api.getPendingApproval();
      setClaims(Array.isArray(data) ? data : []);
    } catch {
      setError('Failed to load pending approval queue.');
    } finally {
      setLoading(false);
    }
  };

  const handleProviderSearch = async (e) => {
    e.preventDefault();
    if (!providerIdInput.trim()) { setError('Provider ID is required.'); return; }
    setLoading(true); setError(''); setClaims([]); setCumulativeHours(null);
    try {
      const [claimsData, hoursData] = await Promise.all([
        api.getByProvider(providerIdInput.trim()),
        api.getCumulativeHours(providerIdInput.trim())
      ]);
      setClaims(Array.isArray(claimsData) ? claimsData : []);
      setCumulativeHours(hoursData);
    } catch {
      setError('Failed to load provider claims.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    setSaving(true); setError('');
    try {
      await api.create({
        ...form,
        trainingHoursClaimedMinutes: form.trainingHoursClaimedMinutes
          ? Number(form.trainingHoursClaimedMinutes) : null
      });
      setSuccess('Career pathway claim created.');
      setCreateModal(false);
      resetForm();
      if (providerIdInput) handleProviderSearch({ preventDefault: () => {} });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create claim.');
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async (action, id, extra) => {
    setError(''); setSuccess('');
    try {
      if (action === 'submitForApproval') await api.submitForApproval(id);
      else if (action === 'approve') await api.approve(id);
      else if (action === 'reject') {
        await api.reject(id, extra.rejectionReason, extra.notes);
        setRejectModal(null);
      } else if (action === 'reissue') await api.reissue(id);
      const msgs = {
        submitForApproval: 'submitted for approval',
        approve: 'approved', reject: 'rejected', reissue: 'reissued'
      };
      setSuccess(`Claim ${msgs[action]} successfully.`);
      if (viewMode === 'pendingReview') loadPendingReview();
      else if (viewMode === 'pendingApproval') loadPendingApproval();
      else if (providerIdInput) handleProviderSearch({ preventDefault: () => {} });
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${action}.`);
    }
  };

  const resetForm = () => setForm({
    providerId: '', providerName: '', caseId: '',
    claimType: 'TRAINING_TIME', careerPathwayCategory: 'ADULT_EDUCATION',
    servicePeriodFrom: '', servicePeriodTo: '',
    trainingHoursClaimedMinutes: '', className: '', classNumber: '',
    trainingDateFrom: '', trainingDateTo: '', receivedDate: ''
  });

  const fmtMins = (m) => {
    if (!m) return '—';
    const h = Math.floor(m / 60), min = m % 60;
    return `${h}:${String(min).padStart(2, '0')} hrs`;
  };

  const incentiveAmt = (type) => ({
    TRAINING_INCENTIVE: '$500', ONE_MONTH_INCENTIVE: '$500', SIX_MONTH_INCENTIVE: '$2,000'
  })[type] || '—';

  return (
    <div className="wq-page">
      <div className="wq-header">
        <div>
          <h1 className="wq-title">Career Pathways Program</h1>
          <p className="wq-subtitle">DSD Section 27 CI-823458 — California Senate Bill No. 172</p>
        </div>
      </div>

      {/* Mode Tabs */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 24 }}>
        {[
          { key: 'search', label: 'Provider Claims' },
          { key: 'pendingReview', label: 'Pending Initial Review' },
          { key: 'pendingApproval', label: 'Pending CDSS Approval' }
        ].map(m => (
          <button key={m.key}
            className={viewMode === m.key ? 'wq-btn wq-btn-primary' : 'wq-btn wq-btn-secondary'}
            onClick={() => { setViewMode(m.key); setError(''); setSuccess(''); }}>
            {m.label}
          </button>
        ))}
      </div>

      {error && <div className="wq-error-msg">{error}</div>}
      {success && <div className="wq-success-msg">{success}</div>}

      {/* Provider Search */}
      {viewMode === 'search' && (
        <div className="wq-card" style={{ marginBottom: 24 }}>
          <form onSubmit={handleProviderSearch} style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
            <div className="wq-form-group" style={{ flex: 1, marginBottom: 0 }}>
              <label className="wq-label">Provider ID *</label>
              <input className="wq-input" value={providerIdInput}
                onChange={e => setProviderIdInput(e.target.value)}
                placeholder="Enter provider ID to search claims" />
            </div>
            <button type="submit" className="wq-btn wq-btn-primary" disabled={loading}>
              {loading ? 'Searching…' : 'Search'}
            </button>
            <button type="button" className="wq-btn wq-btn-primary"
              onClick={() => setCreateModal(true)}>
              + New Claim
            </button>
          </form>

          {/* Cumulative Hours */}
          {cumulativeHours && (
            <div style={{ marginTop: 16, padding: 12, background: '#ebf8ff', border: '1px solid #90cdf4', borderRadius: 8 }}>
              <h4 style={{ marginBottom: 8, fontSize: 13, color: '#2c5282' }}>Cumulative Training Hours by Pathway</h4>
              <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                {PATHWAY_CATEGORIES.map(cat => {
                  const mins = cumulativeHours[cat.value] || 0;
                  return (
                    <div key={cat.value} style={{ fontSize: 12, color: '#2d3748' }}>
                      <strong>{cat.label}:</strong> {fmtMins(mins)}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}

      {/* Claims Table */}
      {(viewMode !== 'search' || claims.length > 0) && (
        <div className="wq-card">
          <div className="wq-card-header">
            <h2 className="wq-section-title">
              {viewMode === 'search' ? `Claims (${claims.length})` :
               viewMode === 'pendingReview' ? 'Initial Review Queue' : 'CDSS Approval Queue'}
            </h2>
          </div>
          {loading ? (
            <div className="wq-loading">Loading…</div>
          ) : claims.length === 0 ? (
            <div className="wq-empty">No claims found.</div>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Claim Type</th>
                  <th>Pathway</th>
                  <th>Provider</th>
                  <th>Service Period</th>
                  <th>Training Hours</th>
                  <th>Incentive</th>
                  <th>Status</th>
                  <th>Reissued</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {claims.map(c => (
                  <tr key={c.id}>
                    <td style={{ fontSize: 12 }}>
                      {CLAIM_TYPES.find(t => t.value === c.claimType)?.label || c.claimType}
                    </td>
                    <td style={{ fontSize: 11 }}>
                      {PATHWAY_CATEGORIES.find(p => p.value === c.careerPathwayCategory)?.label || c.careerPathwayCategory}
                    </td>
                    <td>{c.providerName || c.providerId || '—'}</td>
                    <td style={{ fontSize: 12 }}>
                      {c.servicePeriodFrom ? `${c.servicePeriodFrom} – ${c.servicePeriodTo || '…'}` : '—'}
                    </td>
                    <td>{fmtMins(c.trainingHoursClaimedMinutes)}</td>
                    <td>{incentiveAmt(c.claimType)}</td>
                    <td>
                      <span style={{
                        background: STATUS_COLORS[c.status] || '#e2e8f0',
                        padding: '2px 8px', borderRadius: 12, fontSize: 11, fontWeight: 600
                      }}>{c.status?.replace(/_/g, ' ')}</span>
                    </td>
                    <td style={{ textAlign: 'center' }}>{c.isReissued ? '✓' : ''}</td>
                    <td style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                      <button className="wq-btn wq-btn-secondary wq-btn-sm"
                        onClick={() => setViewModal(c)}>View</button>
                      {c.status === 'PENDING_REVIEW' && (
                        <button className="wq-btn wq-btn-primary wq-btn-sm"
                          onClick={() => handleAction('submitForApproval', c.id)}>
                          Submit for Approval
                        </button>
                      )}
                      {c.status === 'PENDING_APPROVAL' && (
                        <>
                          <button className="wq-btn wq-btn-primary wq-btn-sm"
                            onClick={() => handleAction('approve', c.id)}>Approve</button>
                          <button className="wq-btn wq-btn-danger wq-btn-sm"
                            onClick={() => { setRejectModal(c.id); setRejectForm({ rejectionReason: 'OTHER', notes: '' }); }}>
                            Reject
                          </button>
                        </>
                      )}
                      {(c.status === 'REJECTED' || c.status === 'PAID') && (
                        <button className="wq-btn wq-btn-secondary wq-btn-sm"
                          onClick={() => handleAction('reissue', c.id)}>Reissue</button>
                      )}
                    </td>
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
              <h3>New Career Pathway Claim</h3>
              <button className="wq-modal-close" onClick={() => { setCreateModal(false); resetForm(); }}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-grid">
                <div className="wq-form-group">
                  <label className="wq-label">Claim Type *</label>
                  <select className="wq-input" value={form.claimType}
                    onChange={e => setForm(f => ({ ...f, claimType: e.target.value }))}>
                    {CLAIM_TYPES.map(t => (
                      <option key={t.value} value={t.value}>{t.label}</option>
                    ))}
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Career Pathway Category *</label>
                  <select className="wq-input" value={form.careerPathwayCategory}
                    onChange={e => setForm(f => ({ ...f, careerPathwayCategory: e.target.value }))}>
                    {PATHWAY_CATEGORIES.map(p => (
                      <option key={p.value} value={p.value}>{p.label}</option>
                    ))}
                  </select>
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Provider ID *</label>
                  <input className="wq-input" value={form.providerId}
                    onChange={e => setForm(f => ({ ...f, providerId: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Provider Name</label>
                  <input className="wq-input" value={form.providerName}
                    onChange={e => setForm(f => ({ ...f, providerName: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Case ID</label>
                  <input className="wq-input" value={form.caseId}
                    onChange={e => setForm(f => ({ ...f, caseId: e.target.value }))} />
                </div>
                <div className="wq-form-group">
                  <label className="wq-label">Received Date</label>
                  <input type="date" className="wq-input" value={form.receivedDate}
                    onChange={e => setForm(f => ({ ...f, receivedDate: e.target.value }))} />
                </div>
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
                {form.claimType === 'TRAINING_TIME' && (
                  <>
                    <div className="wq-form-group">
                      <label className="wq-label">Training Hours Claimed (minutes)</label>
                      <input type="number" className="wq-input" value={form.trainingHoursClaimedMinutes}
                        onChange={e => setForm(f => ({ ...f, trainingHoursClaimedMinutes: e.target.value }))} />
                    </div>
                    <div className="wq-form-group">
                      <label className="wq-label">Class Name</label>
                      <input className="wq-input" value={form.className}
                        onChange={e => setForm(f => ({ ...f, className: e.target.value }))} />
                    </div>
                    <div className="wq-form-group">
                      <label className="wq-label">Class Number</label>
                      <input className="wq-input" value={form.classNumber}
                        onChange={e => setForm(f => ({ ...f, classNumber: e.target.value }))} />
                    </div>
                    <div className="wq-form-group">
                      <label className="wq-label">Training Date From</label>
                      <input type="date" className="wq-input" value={form.trainingDateFrom}
                        onChange={e => setForm(f => ({ ...f, trainingDateFrom: e.target.value }))} />
                    </div>
                    <div className="wq-form-group">
                      <label className="wq-label">Training Date To</label>
                      <input type="date" className="wq-input" value={form.trainingDateTo}
                        onChange={e => setForm(f => ({ ...f, trainingDateTo: e.target.value }))} />
                    </div>
                  </>
                )}
              </div>
              {form.claimType !== 'TRAINING_TIME' && (
                <div style={{ padding: '8px 12px', background: '#f0fff4', border: '1px solid #9ae6b4', borderRadius: 6, fontSize: 13, color: '#276749', marginTop: 8 }}>
                  Incentive amount: {incentiveAmt(form.claimType)}
                </div>
              )}
              <div style={{ padding: '8px 12px', background: '#fffbeb', border: '1px solid #f6e05e', borderRadius: 6, fontSize: 13, color: '#744210', marginTop: 8 }}>
                Claim submitted with status PENDING_REVIEW. Initial reviewer submits for CDSS final approval.
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => { setCreateModal(false); resetForm(); }}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Saving…' : 'Create Claim'}
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
              <h3>Reject Career Pathway Claim</h3>
              <button className="wq-modal-close" onClick={() => setRejectModal(null)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-group">
                <label className="wq-label">Rejection Reason *</label>
                <select className="wq-input" value={rejectForm.rejectionReason}
                  onChange={e => setRejectForm(f => ({ ...f, rejectionReason: e.target.value }))}>
                  {REJECTION_REASONS.map(r => (
                    <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>
                  ))}
                </select>
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={3} value={rejectForm.notes}
                  onChange={e => setRejectForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setRejectModal(null)}>Cancel</button>
              <button className="wq-btn wq-btn-danger"
                onClick={() => handleAction('reject', rejectModal, rejectForm)}>
                Reject Claim
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
              <h3>Career Pathway Claim Details</h3>
              <button className="wq-modal-close" onClick={() => setViewModal(null)}>✕</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-form-grid">
                {[
                  ['Claim Type', CLAIM_TYPES.find(t => t.value === viewModal.claimType)?.label],
                  ['Pathway', PATHWAY_CATEGORIES.find(p => p.value === viewModal.careerPathwayCategory)?.label],
                  ['Status', viewModal.status],
                  ['Provider', viewModal.providerName],
                  ['Incentive Amount', incentiveAmt(viewModal.claimType)],
                  ['Service Period', `${viewModal.servicePeriodFrom || '—'} – ${viewModal.servicePeriodTo || '—'}`],
                  ['Training Hours', fmtMins(viewModal.trainingHoursClaimedMinutes)],
                  ['Class Name', viewModal.className],
                  ['Class Number', viewModal.classNumber],
                  ['Training Dates', viewModal.trainingDateFrom ? `${viewModal.trainingDateFrom} – ${viewModal.trainingDateTo}` : '—'],
                  ['Received Date', viewModal.receivedDate],
                  ['Reviewer Comments', viewModal.reviewerComments],
                  ['Reissued', viewModal.isReissued ? 'Yes' : 'No'],
                  ['Original Claim ID', viewModal.originalClaimId],
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
