import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as internalOpsApi from '../api/internalOpsApi';
import './WorkQueues.css';

/**
 * Internal Operations — DSD Section 32
 *
 * Two tabs:
 *   1. Garnishments — court-ordered garnishment management per provider
 *   2. Direct Deposit — provider direct deposit account management
 */

const GARNISHMENT_TYPES = [
  { value: 'CHILD_SUPPORT', label: 'Child Support' },
  { value: 'TAX_LEVY', label: 'Tax Levy' },
  { value: 'CREDITOR_GARNISHMENT', label: 'Creditor Garnishment' },
  { value: 'BANKRUPTCY', label: 'Bankruptcy' },
  { value: 'STUDENT_LOAN', label: 'Student Loan' },
  { value: 'FEDERAL_LEVY', label: 'Federal Levy' }
];

const GARNISHMENT_STATUS_COLORS = {
  ACTIVE: { bg: '#c6f6d5', text: '#276749' },
  SUSPENDED: { bg: '#fefcbf', text: '#975a16' },
  SATISFIED: { bg: '#bee3f8', text: '#2b6cb0' },
  TERMINATED: { bg: '#e2e8f0', text: '#4a5568' }
};

const ACCOUNT_TYPES = [
  { value: 'CHECKING', label: 'Checking' },
  { value: 'SAVINGS', label: 'Savings' }
];

const DEPOSIT_TYPES = [
  { value: 'FULL', label: 'Full' },
  { value: 'PARTIAL', label: 'Partial' }
];

const PRENOTE_STATUS_COLORS = {
  PENDING: { bg: '#fefcbf', text: '#975a16' },
  PENDING_VERIFICATION: { bg: '#fefcbf', text: '#975a16' },
  VERIFIED: { bg: '#c6f6d5', text: '#276749' },
  FAILED: { bg: '#fed7d7', text: '#9b2c2c' }
};

const DD_STATUS_COLORS = {
  ACTIVE: { bg: '#c6f6d5', text: '#276749' },
  INACTIVE: { bg: '#e2e8f0', text: '#4a5568' },
  PENDING_VERIFICATION: { bg: '#fefcbf', text: '#975a16' }
};

const emptyGarnishmentForm = {
  garnishmentType: 'CHILD_SUPPORT',
  courtOrderNumber: '',
  issuingAuthority: '',
  garnishmentAmount: '',
  garnishmentPercentage: '',
  maxPerPayPeriod: '',
  priority: '',
  startDate: '',
  endDate: '',
  payeeInfo: '',
  notes: ''
};

const emptyDDForm = {
  bankName: '',
  accountType: 'CHECKING',
  routingNumber: '',
  accountNumber: '',
  depositType: 'FULL',
  depositAmount: ''
};

const maskAccount = (acct) => {
  if (!acct || acct.length < 5) return acct || '';
  return '****' + acct.slice(-4);
};

const fmtDate = (d) => {
  if (!d) return '';
  try { return new Date(d).toLocaleDateString(); } catch { return d; }
};

const fmtCurrency = (v) => {
  if (v == null || v === '') return '';
  return '$' + Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

export const InternalOpsPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();
  const [activeTab, setActiveTab] = useState('garnishments');

  // ── Garnishments state ──
  const [garnProviderId, setGarnProviderId] = useState('');
  const [garnishments, setGarnishments] = useState([]);
  const [garnLoading, setGarnLoading] = useState(false);
  const [garnError, setGarnError] = useState('');
  const [garnSuccess, setGarnSuccess] = useState('');
  const [garnCreateModal, setGarnCreateModal] = useState(false);
  const [garnEditModal, setGarnEditModal] = useState(null);
  const [garnForm, setGarnForm] = useState({ ...emptyGarnishmentForm });
  const [garnSaving, setGarnSaving] = useState(false);
  const [suspendModal, setSuspendModal] = useState(null);
  const [suspendReason, setSuspendReason] = useState('');

  // ── Direct Deposit state ──
  const [ddProviderId, setDdProviderId] = useState('');
  const [deposits, setDeposits] = useState([]);
  const [ddLoading, setDdLoading] = useState(false);
  const [ddError, setDdError] = useState('');
  const [ddSuccess, setDdSuccess] = useState('');
  const [ddCreateModal, setDdCreateModal] = useState(false);
  const [ddForm, setDdForm] = useState({ ...emptyDDForm });
  const [ddSaving, setDdSaving] = useState(false);
  const [inactivateModal, setInactivateModal] = useState(null);
  const [inactivateReason, setInactivateReason] = useState('');
  const [pendingPrenotes, setPendingPrenotes] = useState([]);
  const [showPending, setShowPending] = useState(false);
  const [pendingLoading, setPendingLoading] = useState(false);

  // ── Breadcrumbs ──
  useEffect(() => {
    setBreadcrumbs([
      { label: 'My Workspace', path: '/workspace' },
      { label: 'Internal Operations' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  // ════════════════════════════════════════════════
  //  GARNISHMENTS
  // ════════════════════════════════════════════════

  const loadGarnishments = async () => {
    if (!garnProviderId.trim()) { setGarnError('Enter a Provider ID.'); return; }
    setGarnLoading(true); setGarnError(''); setGarnSuccess('');
    try {
      const data = await internalOpsApi.getProviderGarnishments(garnProviderId.trim()).then(r => r.data);
      setGarnishments(Array.isArray(data) ? data : []);
    } catch {
      setGarnError('Failed to load garnishments.');
      setGarnishments([]);
    } finally {
      setGarnLoading(false);
    }
  };

  const openGarnCreate = () => {
    setGarnForm({ ...emptyGarnishmentForm });
    setGarnCreateModal(true);
    setGarnError('');
  };

  const openGarnEdit = (g) => {
    setGarnForm({
      garnishmentType: g.garnishmentType || 'CHILD_SUPPORT',
      courtOrderNumber: g.courtOrderNumber || '',
      issuingAuthority: g.issuingAuthority || '',
      garnishmentAmount: g.garnishmentAmount ?? '',
      garnishmentPercentage: g.garnishmentPercentage ?? '',
      maxPerPayPeriod: g.maxPerPayPeriod ?? '',
      priority: g.priority ?? '',
      startDate: g.startDate ? g.startDate.substring(0, 10) : '',
      endDate: g.endDate ? g.endDate.substring(0, 10) : '',
      payeeInfo: g.payeeInfo || '',
      notes: g.notes || ''
    });
    setGarnEditModal(g);
    setGarnError('');
  };

  const validateGarnForm = () => {
    if (!garnForm.courtOrderNumber.trim()) return 'Court Order Number is required.';
    if (!garnForm.issuingAuthority.trim()) return 'Issuing Authority is required.';
    if (!garnForm.garnishmentAmount && !garnForm.garnishmentPercentage) return 'Either Amount or Percentage is required.';
    if (!garnForm.startDate) return 'Start Date is required.';
    if (!garnForm.priority) return 'Priority is required.';
    return null;
  };

  const handleGarnCreate = async () => {
    const err = validateGarnForm();
    if (err) { setGarnError(err); return; }
    setGarnSaving(true); setGarnError('');
    try {
      await internalOpsApi.createGarnishment(garnProviderId.trim(), garnForm).then(r => r.data);
      setGarnSuccess('Garnishment created successfully.');
      setGarnCreateModal(false);
      await loadGarnishments();
    } catch (e) {
      setGarnError(e.response?.data?.message || 'Failed to create garnishment.');
    } finally {
      setGarnSaving(false);
    }
  };

  const handleGarnUpdate = async () => {
    const err = validateGarnForm();
    if (err) { setGarnError(err); return; }
    setGarnSaving(true); setGarnError('');
    try {
      await internalOpsApi.updateGarnishment(garnEditModal.id, garnForm).then(r => r.data);
      setGarnSuccess('Garnishment updated successfully.');
      setGarnEditModal(null);
      await loadGarnishments();
    } catch (e) {
      setGarnError(e.response?.data?.message || 'Failed to update garnishment.');
    } finally {
      setGarnSaving(false);
    }
  };

  const handleSuspend = async () => {
    if (!suspendReason.trim()) { setGarnError('Suspension reason is required.'); return; }
    setGarnSaving(true); setGarnError('');
    try {
      await internalOpsApi.suspendGarnishment(suspendModal.id, { reason: suspendReason.trim() }).then(r => r.data);
      setGarnSuccess('Garnishment suspended.');
      setSuspendModal(null);
      setSuspendReason('');
      await loadGarnishments();
    } catch (e) {
      setGarnError(e.response?.data?.message || 'Failed to suspend garnishment.');
    } finally {
      setGarnSaving(false);
    }
  };

  const handleSatisfy = async (g) => {
    if (!window.confirm(`Mark garnishment #${g.courtOrderNumber} as satisfied?`)) return;
    setGarnError(''); setGarnSuccess('');
    try {
      await internalOpsApi.satisfyGarnishment(g.id).then(r => r.data);
      setGarnSuccess('Garnishment marked as satisfied.');
      await loadGarnishments();
    } catch (e) {
      setGarnError(e.response?.data?.message || 'Failed to satisfy garnishment.');
    }
  };

  const handleTerminate = async (g) => {
    if (!window.confirm(`Terminate garnishment #${g.courtOrderNumber}? This cannot be undone.`)) return;
    setGarnError(''); setGarnSuccess('');
    try {
      await internalOpsApi.terminateGarnishment(g.id).then(r => r.data);
      setGarnSuccess('Garnishment terminated.');
      await loadGarnishments();
    } catch (e) {
      setGarnError(e.response?.data?.message || 'Failed to terminate garnishment.');
    }
  };

  // ════════════════════════════════════════════════
  //  DIRECT DEPOSIT
  // ════════════════════════════════════════════════

  const loadDeposits = async () => {
    if (!ddProviderId.trim()) { setDdError('Enter a Provider ID.'); return; }
    setDdLoading(true); setDdError(''); setDdSuccess(''); setShowPending(false);
    try {
      const data = await internalOpsApi.getProviderDirectDeposits(ddProviderId.trim()).then(r => r.data);
      setDeposits(Array.isArray(data) ? data : []);
    } catch {
      setDdError('Failed to load direct deposit accounts.');
      setDeposits([]);
    } finally {
      setDdLoading(false);
    }
  };

  const loadPendingPrenotes = async () => {
    setPendingLoading(true); setDdError(''); setDdSuccess('');
    try {
      const data = await internalOpsApi.getPendingPrenotes().then(r => r.data);
      setPendingPrenotes(Array.isArray(data) ? data : []);
      setShowPending(true);
    } catch {
      setDdError('Failed to load pending prenotes.');
    } finally {
      setPendingLoading(false);
    }
  };

  const openDDCreate = () => {
    setDdForm({ ...emptyDDForm });
    setDdCreateModal(true);
    setDdError('');
  };

  const validateDDForm = () => {
    if (!ddForm.bankName.trim()) return 'Bank Name is required.';
    if (!ddForm.routingNumber.trim()) return 'Routing Number is required.';
    if (!/^\d{9}$/.test(ddForm.routingNumber.trim())) return 'Routing Number must be exactly 9 digits.';
    if (!ddForm.accountNumber.trim()) return 'Account Number is required.';
    if (ddForm.depositType === 'PARTIAL' && (!ddForm.depositAmount || Number(ddForm.depositAmount) <= 0)) {
      return 'Deposit Amount is required for partial deposits.';
    }
    return null;
  };

  const handleDDCreate = async () => {
    const err = validateDDForm();
    if (err) { setDdError(err); return; }
    setDdSaving(true); setDdError('');
    const payload = { ...ddForm };
    if (payload.depositType === 'FULL') delete payload.depositAmount;
    try {
      await internalOpsApi.createDirectDeposit(ddProviderId.trim(), payload).then(r => r.data);
      setDdSuccess('Direct deposit account created. Prenote verification initiated.');
      setDdCreateModal(false);
      await loadDeposits();
    } catch (e) {
      setDdError(e.response?.data?.message || 'Failed to create direct deposit account.');
    } finally {
      setDdSaving(false);
    }
  };

  const handleVerifyPrenote = async (dd) => {
    if (!window.confirm(`Verify prenote for ${dd.bankName} account ending ${maskAccount(dd.accountNumber)}?`)) return;
    setDdError(''); setDdSuccess('');
    try {
      await internalOpsApi.verifyPrenote(dd.id).then(r => r.data);
      setDdSuccess('Prenote verified successfully.');
      if (showPending) await loadPendingPrenotes();
      else await loadDeposits();
    } catch (e) {
      setDdError(e.response?.data?.message || 'Failed to verify prenote.');
    }
  };

  const handleInactivate = async () => {
    if (!inactivateReason.trim()) { setDdError('Inactivation reason is required.'); return; }
    setDdSaving(true); setDdError('');
    try {
      await internalOpsApi.inactivateDirectDeposit(inactivateModal.id, { reason: inactivateReason.trim() }).then(r => r.data);
      setDdSuccess('Direct deposit account inactivated.');
      setInactivateModal(null);
      setInactivateReason('');
      await loadDeposits();
    } catch (e) {
      setDdError(e.response?.data?.message || 'Failed to inactivate account.');
    } finally {
      setDdSaving(false);
    }
  };

  // ════════════════════════════════════════════════
  //  RENDER HELPERS
  // ════════════════════════════════════════════════

  const renderStatusBadge = (status, colorMap) => {
    const c = colorMap[status] || { bg: '#e2e8f0', text: '#4a5568' };
    return (
      <span className="wq-status-badge" style={{ backgroundColor: c.bg, color: c.text }}>
        {(status || '').replace(/_/g, ' ')}
      </span>
    );
  };

  const renderGarnishmentFormFields = () => (
    <>
      <div className="wq-form-group">
        <label className="wq-detail-label">Garnishment Type *</label>
        <select className="wq-select" value={garnForm.garnishmentType}
          onChange={e => setGarnForm({ ...garnForm, garnishmentType: e.target.value })}>
          {GARNISHMENT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
        </select>
      </div>
      <div className="wq-form-group">
        <label className="wq-detail-label">Court Order Number *</label>
        <input className="wq-input" value={garnForm.courtOrderNumber}
          onChange={e => setGarnForm({ ...garnForm, courtOrderNumber: e.target.value })} />
      </div>
      <div className="wq-form-group">
        <label className="wq-detail-label">Issuing Authority *</label>
        <input className="wq-input" value={garnForm.issuingAuthority}
          onChange={e => setGarnForm({ ...garnForm, issuingAuthority: e.target.value })} />
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '12px' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">Amount ($)</label>
          <input className="wq-input" type="number" step="0.01" min="0" value={garnForm.garnishmentAmount}
            onChange={e => setGarnForm({ ...garnForm, garnishmentAmount: e.target.value })} />
        </div>
        <div className="wq-form-group">
          <label className="wq-detail-label">Percentage (%)</label>
          <input className="wq-input" type="number" step="0.01" min="0" max="100" value={garnForm.garnishmentPercentage}
            onChange={e => setGarnForm({ ...garnForm, garnishmentPercentage: e.target.value })} />
        </div>
        <div className="wq-form-group">
          <label className="wq-detail-label">Max Per Pay Period ($)</label>
          <input className="wq-input" type="number" step="0.01" min="0" value={garnForm.maxPerPayPeriod}
            onChange={e => setGarnForm({ ...garnForm, maxPerPayPeriod: e.target.value })} />
        </div>
      </div>
      <div className="wq-form-group">
        <label className="wq-detail-label">Priority *</label>
        <input className="wq-input" type="number" min="1" value={garnForm.priority}
          onChange={e => setGarnForm({ ...garnForm, priority: e.target.value })}
          placeholder="1 = highest priority" />
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
        <div className="wq-form-group">
          <label className="wq-detail-label">Start Date *</label>
          <input className="wq-input" type="date" value={garnForm.startDate}
            onChange={e => setGarnForm({ ...garnForm, startDate: e.target.value })} />
        </div>
        <div className="wq-form-group">
          <label className="wq-detail-label">End Date</label>
          <input className="wq-input" type="date" value={garnForm.endDate}
            onChange={e => setGarnForm({ ...garnForm, endDate: e.target.value })} />
        </div>
      </div>
      <div className="wq-form-group">
        <label className="wq-detail-label">Payee Info</label>
        <input className="wq-input" value={garnForm.payeeInfo}
          onChange={e => setGarnForm({ ...garnForm, payeeInfo: e.target.value })}
          placeholder="Name and address of garnishment payee" />
      </div>
      <div className="wq-form-group">
        <label className="wq-detail-label">Notes</label>
        <textarea className="wq-input" rows="3" value={garnForm.notes}
          onChange={e => setGarnForm({ ...garnForm, notes: e.target.value })} />
      </div>
    </>
  );

  // ════════════════════════════════════════════════
  //  GARNISHMENTS TAB
  // ════════════════════════════════════════════════

  const renderGarnishmentsTab = () => (
    <div>
      {garnError && <div style={{ color: '#c53030', background: '#fed7d7', padding: '8px 12px', borderRadius: '4px', marginBottom: '12px' }}>{garnError}</div>}
      {garnSuccess && <div style={{ color: '#276749', background: '#c6f6d5', padding: '8px 12px', borderRadius: '4px', marginBottom: '12px' }}>{garnSuccess}</div>}

      <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end', marginBottom: '16px' }}>
        <div className="wq-form-group" style={{ marginBottom: 0, flex: '0 0 240px' }}>
          <label className="wq-detail-label">Provider ID</label>
          <input className="wq-input" value={garnProviderId} placeholder="Enter Provider ID"
            onChange={e => setGarnProviderId(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadGarnishments()} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={loadGarnishments} disabled={garnLoading}>
          {garnLoading ? 'Loading...' : 'Load Garnishments'}
        </button>
        {garnProviderId.trim() && (
          <button className="wq-btn wq-btn-primary" onClick={openGarnCreate}>Add Garnishment</button>
        )}
      </div>

      {garnLoading ? (
        <p>Loading garnishments...</p>
      ) : garnishments.length === 0 ? (
        <p style={{ color: '#718096' }}>{garnProviderId.trim() ? 'No garnishments found for this provider.' : 'Enter a Provider ID to load garnishments.'}</p>
      ) : (
        <div style={{ overflowX: 'auto' }}>
          <table className="wq-table">
            <thead>
              <tr>
                <th>Type</th>
                <th>Court Order #</th>
                <th>Amount</th>
                <th>%</th>
                <th>Max/Pay Period</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Total Withheld</th>
                <th>Start Date</th>
                <th>End Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {garnishments.map(g => (
                <tr key={g.id}>
                  <td>{(GARNISHMENT_TYPES.find(t => t.value === g.garnishmentType) || {}).label || g.garnishmentType}</td>
                  <td>{g.courtOrderNumber}</td>
                  <td>{fmtCurrency(g.garnishmentAmount)}</td>
                  <td>{g.garnishmentPercentage != null ? `${g.garnishmentPercentage}%` : ''}</td>
                  <td>{fmtCurrency(g.maxPerPayPeriod)}</td>
                  <td>{g.priority}</td>
                  <td>{renderStatusBadge(g.status, GARNISHMENT_STATUS_COLORS)}</td>
                  <td>{fmtCurrency(g.totalWithheld)}</td>
                  <td>{fmtDate(g.startDate)}</td>
                  <td>{fmtDate(g.endDate)}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                      {g.status === 'ACTIVE' && (
                        <>
                          <button className="wq-btn wq-btn-sm" onClick={() => openGarnEdit(g)}>Edit</button>
                          <button className="wq-btn wq-btn-sm" onClick={() => { setSuspendModal(g); setSuspendReason(''); setGarnError(''); }}>Suspend</button>
                          <button className="wq-btn wq-btn-sm" onClick={() => handleSatisfy(g)}>Satisfy</button>
                          <button className="wq-btn wq-btn-sm" onClick={() => handleTerminate(g)}>Terminate</button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create Garnishment Modal */}
      {garnCreateModal && (
        <div className="wq-modal-overlay" onClick={() => setGarnCreateModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '600px' }}>
            <h3 className="wq-modal-title">Add Garnishment — Provider {garnProviderId}</h3>
            {garnError && <div style={{ color: '#c53030', marginBottom: '12px' }}>{garnError}</div>}
            {renderGarnishmentFormFields()}
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
              <button className="wq-btn" onClick={() => setGarnCreateModal(false)} disabled={garnSaving}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleGarnCreate} disabled={garnSaving}>
                {garnSaving ? 'Saving...' : 'Create Garnishment'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Edit Garnishment Modal */}
      {garnEditModal && (
        <div className="wq-modal-overlay" onClick={() => setGarnEditModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '600px' }}>
            <h3 className="wq-modal-title">Edit Garnishment — {garnEditModal.courtOrderNumber}</h3>
            {garnError && <div style={{ color: '#c53030', marginBottom: '12px' }}>{garnError}</div>}
            {renderGarnishmentFormFields()}
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
              <button className="wq-btn" onClick={() => setGarnEditModal(null)} disabled={garnSaving}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleGarnUpdate} disabled={garnSaving}>
                {garnSaving ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Suspend Garnishment Modal */}
      {suspendModal && (
        <div className="wq-modal-overlay" onClick={() => setSuspendModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '480px' }}>
            <h3 className="wq-modal-title">Suspend Garnishment — {suspendModal.courtOrderNumber}</h3>
            {garnError && <div style={{ color: '#c53030', marginBottom: '12px' }}>{garnError}</div>}
            <div className="wq-form-group">
              <label className="wq-detail-label">Suspension Reason *</label>
              <textarea className="wq-input" rows="3" value={suspendReason}
                onChange={e => setSuspendReason(e.target.value)}
                placeholder="Enter reason for suspending this garnishment" />
            </div>
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
              <button className="wq-btn" onClick={() => setSuspendModal(null)} disabled={garnSaving}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleSuspend} disabled={garnSaving}>
                {garnSaving ? 'Suspending...' : 'Suspend'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  // ════════════════════════════════════════════════
  //  DIRECT DEPOSIT TAB
  // ════════════════════════════════════════════════

  const renderDirectDepositTab = () => (
    <div>
      {ddError && <div style={{ color: '#c53030', background: '#fed7d7', padding: '8px 12px', borderRadius: '4px', marginBottom: '12px' }}>{ddError}</div>}
      {ddSuccess && <div style={{ color: '#276749', background: '#c6f6d5', padding: '8px 12px', borderRadius: '4px', marginBottom: '12px' }}>{ddSuccess}</div>}

      <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end', marginBottom: '16px' }}>
        <div className="wq-form-group" style={{ marginBottom: 0, flex: '0 0 240px' }}>
          <label className="wq-detail-label">Provider ID</label>
          <input className="wq-input" value={ddProviderId} placeholder="Enter Provider ID"
            onChange={e => setDdProviderId(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && loadDeposits()} />
        </div>
        <button className="wq-btn wq-btn-primary" onClick={loadDeposits} disabled={ddLoading}>
          {ddLoading ? 'Loading...' : 'Load Accounts'}
        </button>
        {ddProviderId.trim() && (
          <button className="wq-btn wq-btn-primary" onClick={openDDCreate}>Add Account</button>
        )}
        <button className="wq-btn" onClick={loadPendingPrenotes} disabled={pendingLoading}
          style={{ marginLeft: 'auto' }}>
          {pendingLoading ? 'Loading...' : 'Pending Prenotes'}
        </button>
      </div>

      {/* Pending Prenotes Section */}
      {showPending && (
        <div style={{ marginBottom: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <h4 style={{ margin: 0 }}>Pending Prenote Verifications</h4>
            <button className="wq-btn wq-btn-sm" onClick={() => setShowPending(false)}>Close</button>
          </div>
          {pendingPrenotes.length === 0 ? (
            <p style={{ color: '#718096' }}>No pending prenote verifications.</p>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Provider ID</th>
                    <th>Bank Name</th>
                    <th>Account Type</th>
                    <th>Routing #</th>
                    <th>Account #</th>
                    <th>Prenote Status</th>
                    <th>Created</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingPrenotes.map(dd => (
                    <tr key={dd.id}>
                      <td>{dd.providerId}</td>
                      <td>{dd.bankName}</td>
                      <td>{dd.accountType}</td>
                      <td>{dd.routingNumber}</td>
                      <td>{maskAccount(dd.accountNumber)}</td>
                      <td>{renderStatusBadge(dd.prenoteStatus, PRENOTE_STATUS_COLORS)}</td>
                      <td>{fmtDate(dd.createdDate)}</td>
                      <td>
                        <button className="wq-btn wq-btn-sm wq-btn-primary" onClick={() => handleVerifyPrenote(dd)}>
                          Verify Prenote
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Provider Accounts Table */}
      {ddLoading ? (
        <p>Loading accounts...</p>
      ) : deposits.length === 0 && !showPending ? (
        <p style={{ color: '#718096' }}>{ddProviderId.trim() ? 'No direct deposit accounts found for this provider.' : 'Enter a Provider ID to load accounts.'}</p>
      ) : deposits.length > 0 && (
        <div style={{ overflowX: 'auto' }}>
          <table className="wq-table">
            <thead>
              <tr>
                <th>Bank Name</th>
                <th>Account Type</th>
                <th>Routing #</th>
                <th>Account #</th>
                <th>Deposit Type</th>
                <th>Deposit Amount</th>
                <th>Prenote Status</th>
                <th>Status</th>
                <th>Created</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {deposits.map(dd => (
                <tr key={dd.id}>
                  <td>{dd.bankName}</td>
                  <td>{dd.accountType}</td>
                  <td>{dd.routingNumber}</td>
                  <td>{maskAccount(dd.accountNumber)}</td>
                  <td>{dd.depositType}</td>
                  <td>{dd.depositType === 'PARTIAL' ? fmtCurrency(dd.depositAmount) : ''}</td>
                  <td>{renderStatusBadge(dd.prenoteStatus, PRENOTE_STATUS_COLORS)}</td>
                  <td>{renderStatusBadge(dd.status, DD_STATUS_COLORS)}</td>
                  <td>{fmtDate(dd.createdDate)}</td>
                  <td>
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                      {(dd.prenoteStatus === 'PENDING' || dd.prenoteStatus === 'PENDING_VERIFICATION') && (
                        <button className="wq-btn wq-btn-sm" onClick={() => handleVerifyPrenote(dd)}>Verify Prenote</button>
                      )}
                      {dd.status === 'ACTIVE' && (
                        <button className="wq-btn wq-btn-sm" onClick={() => { setInactivateModal(dd); setInactivateReason(''); setDdError(''); }}>
                          Inactivate
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Create Direct Deposit Modal */}
      {ddCreateModal && (
        <div className="wq-modal-overlay" onClick={() => setDdCreateModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '520px' }}>
            <h3 className="wq-modal-title">Add Direct Deposit — Provider {ddProviderId}</h3>
            {ddError && <div style={{ color: '#c53030', marginBottom: '12px' }}>{ddError}</div>}
            <div className="wq-form-group">
              <label className="wq-detail-label">Bank Name *</label>
              <input className="wq-input" value={ddForm.bankName}
                onChange={e => setDdForm({ ...ddForm, bankName: e.target.value })} />
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Account Type *</label>
              <select className="wq-select" value={ddForm.accountType}
                onChange={e => setDdForm({ ...ddForm, accountType: e.target.value })}>
                {ACCOUNT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div className="wq-form-group">
                <label className="wq-detail-label">Routing Number *</label>
                <input className="wq-input" value={ddForm.routingNumber} maxLength={9}
                  onChange={e => setDdForm({ ...ddForm, routingNumber: e.target.value.replace(/\D/g, '') })}
                  placeholder="9 digits" />
              </div>
              <div className="wq-form-group">
                <label className="wq-detail-label">Account Number *</label>
                <input className="wq-input" value={ddForm.accountNumber}
                  onChange={e => setDdForm({ ...ddForm, accountNumber: e.target.value.replace(/\D/g, '') })} />
              </div>
            </div>
            <div className="wq-form-group">
              <label className="wq-detail-label">Deposit Type *</label>
              <select className="wq-select" value={ddForm.depositType}
                onChange={e => setDdForm({ ...ddForm, depositType: e.target.value })}>
                {DEPOSIT_TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>
            {ddForm.depositType === 'PARTIAL' && (
              <div className="wq-form-group">
                <label className="wq-detail-label">Deposit Amount ($) *</label>
                <input className="wq-input" type="number" step="0.01" min="0" value={ddForm.depositAmount}
                  onChange={e => setDdForm({ ...ddForm, depositAmount: e.target.value })} />
              </div>
            )}
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
              <button className="wq-btn" onClick={() => setDdCreateModal(false)} disabled={ddSaving}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleDDCreate} disabled={ddSaving}>
                {ddSaving ? 'Saving...' : 'Create Account'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Inactivate Direct Deposit Modal */}
      {inactivateModal && (
        <div className="wq-modal-overlay" onClick={() => setInactivateModal(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '480px' }}>
            <h3 className="wq-modal-title">Inactivate Direct Deposit — {inactivateModal.bankName}</h3>
            {ddError && <div style={{ color: '#c53030', marginBottom: '12px' }}>{ddError}</div>}
            <p style={{ marginBottom: '12px' }}>
              <span className="wq-detail-label">Account:</span>{' '}
              <span className="wq-detail-value">{inactivateModal.accountType} ending {maskAccount(inactivateModal.accountNumber)}</span>
            </p>
            <div className="wq-form-group">
              <label className="wq-detail-label">Reason for Inactivation *</label>
              <textarea className="wq-input" rows="3" value={inactivateReason}
                onChange={e => setInactivateReason(e.target.value)}
                placeholder="Enter reason for inactivating this account" />
            </div>
            <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end', marginTop: '16px' }}>
              <button className="wq-btn" onClick={() => setInactivateModal(null)} disabled={ddSaving}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleInactivate} disabled={ddSaving}>
                {ddSaving ? 'Inactivating...' : 'Inactivate'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  // ════════════════════════════════════════════════
  //  MAIN RENDER
  // ════════════════════════════════════════════════

  return (
    <div className="wq-page">
      <h2 className="wq-page-title">Internal Operations</h2>

      <div className="wq-tabs" style={{ marginBottom: '20px' }}>
        <button
          className={`wq-tab ${activeTab === 'garnishments' ? 'active' : ''}`}
          onClick={() => setActiveTab('garnishments')}
        >
          Garnishments
        </button>
        <button
          className={`wq-tab ${activeTab === 'directDeposit' ? 'active' : ''}`}
          onClick={() => setActiveTab('directDeposit')}
        >
          Direct Deposit
        </button>
      </div>

      {activeTab === 'garnishments' && renderGarnishmentsTab()}
      {activeTab === 'directDeposit' && renderDirectDepositTab()}
    </div>
  );
};
