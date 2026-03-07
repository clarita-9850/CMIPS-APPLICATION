import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as evvApi from '../api/evvApi';
import './WorkQueues.css';

const STATUS_COLORS = {
  PENDING_REVIEW: { bg: '#fef3c7', color: '#92400e' },
  APPROVED: { bg: '#d1fae5', color: '#065f46' },
  DENIED: { bg: '#fee2e2', color: '#991b1b' },
  EXPIRED: { bg: '#e5e7eb', color: '#374151' },
  CANCELLED: { bg: '#f3f4f6', color: '#6b7280' }
};

const EXCEPTION_REASONS = [
  { value: 'SYSTEM_MALFUNCTION', label: 'System Malfunction' },
  { value: 'PHONE_ISSUE', label: 'Phone Issue' },
  { value: 'NO_CELL_SIGNAL', label: 'No Cell Signal' },
  { value: 'POWER_OUTAGE', label: 'Power Outage' },
  { value: 'RECIPIENT_PHONE_ISSUE', label: 'Recipient Phone Issue' },
  { value: 'FORGOT_TO_CLOCK', label: 'Forgot to Clock In/Out' },
  { value: 'EMERGENCY', label: 'Emergency' },
  { value: 'OTHER', label: 'Other' }
];

export const EvvExceptionPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [tab, setTab] = useState('queue'); // queue | submit
  const [exceptions, setExceptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [countyFilter, setCountyFilter] = useState('');
  const [actionModal, setActionModal] = useState(null);
  const [actionInput, setActionInput] = useState('');
  const [processing, setProcessing] = useState(false);

  // Submit form state
  const [submitForm, setSubmitForm] = useState({
    providerId: '', recipientId: '', caseId: '', timesheetId: '',
    serviceDate: '', hoursClaimed: '', evvHoursRecorded: '',
    exceptionReason: 'SYSTEM_MALFUNCTION', reasonDescription: '', countyCode: ''
  });
  const [submitError, setSubmitError] = useState('');
  const [submitSuccess, setSubmitSuccess] = useState('');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'EVV Exceptions' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const loadExceptions = async () => {
    setLoading(true);
    try {
      const data = await evvApi.listPendingExceptions(countyFilter || undefined);
      setExceptions(data);
    } catch (err) { console.error('Failed to load EVV exceptions:', err); }
    finally { setLoading(false); }
  };

  useEffect(() => { if (tab === 'queue') loadExceptions(); }, [tab, countyFilter]);

  const handleApprove = async (exc) => {
    setProcessing(true);
    try {
      await evvApi.approveException(exc.id, 'cmipsadmin', actionInput);
      setActionModal(null); setActionInput(''); loadExceptions();
    } catch (err) { alert('Failed: ' + (err?.response?.data?.error || err.message)); }
    finally { setProcessing(false); }
  };

  const handleDeny = async (exc) => {
    if (!actionInput.trim()) { alert('Denial reason required.'); return; }
    setProcessing(true);
    try {
      await evvApi.denyException(exc.id, 'cmipsadmin', actionInput);
      setActionModal(null); setActionInput(''); loadExceptions();
    } catch (err) { alert('Failed: ' + (err?.response?.data?.error || err.message)); }
    finally { setProcessing(false); }
  };

  const handleSubmit = async () => {
    if (!submitForm.providerId || !submitForm.recipientId || !submitForm.caseId || !submitForm.serviceDate) {
      setSubmitError('Provider ID, Recipient ID, Case ID, and Service Date are required.'); return;
    }
    setSubmitError(''); setSubmitSuccess(''); setProcessing(true);
    try {
      const exc = await evvApi.submitException(submitForm);
      setSubmitSuccess(`EVV Exception ${exc.exceptionNumber} submitted for review.`);
      setSubmitForm({ ...submitForm, serviceDate: '', hoursClaimed: '', evvHoursRecorded: '', reasonDescription: '' });
    } catch (err) {
      setSubmitError('Failed: ' + (err?.response?.data?.error || err.message));
    } finally { setProcessing(false); }
  };

  const setF = (k, v) => setSubmitForm(prev => ({ ...prev, [k]: v }));

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>EVV Exception Management</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back</button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '0', marginBottom: '1rem', borderBottom: '2px solid #e5e7eb' }}>
        {['queue', 'submit'].map(t => (
          <button key={t} onClick={() => setTab(t)}
            style={{ padding: '0.6rem 1.2rem', border: 'none', borderBottom: tab === t ? '2px solid #2563eb' : '2px solid transparent',
              background: 'transparent', fontWeight: tab === t ? 600 : 400, color: tab === t ? '#2563eb' : '#6b7280',
              cursor: 'pointer', fontSize: '0.85rem', marginBottom: '-2px' }}>
            {t === 'queue' ? 'Review Queue' : 'Submit Exception'}
          </button>
        ))}
      </div>

      {/* Review Queue */}
      {tab === 'queue' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Pending EVV Exceptions ({exceptions.length})</h4>
            <input type="text" placeholder="County Code" value={countyFilter}
              onChange={e => setCountyFilter(e.target.value)}
              style={{ width: '100px', padding: '0.3rem 0.5rem', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '0.8rem' }} />
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div> : (
              <table className="wq-table" style={{ fontSize: '0.8rem' }}>
                <thead>
                  <tr>
                    <th>Exception #</th>
                    <th>Provider</th>
                    <th>Recipient</th>
                    <th>Service Date</th>
                    <th>Claimed</th>
                    <th>EVV Recorded</th>
                    <th>Discrepancy</th>
                    <th>Reason</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {exceptions.length === 0 ? (
                    <tr><td colSpan={10} style={{ textAlign: 'center', padding: '2rem', color: '#9ca3af' }}>No pending EVV exceptions</td></tr>
                  ) : exceptions.map(exc => {
                    const sc = STATUS_COLORS[exc.status] || {};
                    return (
                      <tr key={exc.id}>
                        <td style={{ fontWeight: 600 }}>{exc.exceptionNumber}</td>
                        <td>{exc.providerId}</td>
                        <td>{exc.recipientId}</td>
                        <td>{exc.serviceDate}</td>
                        <td>{exc.hoursClaimed?.toFixed(1)}</td>
                        <td>{exc.evvHoursRecorded?.toFixed(1) || '—'}</td>
                        <td style={{ color: exc.hoursDiscrepancy > 0 ? '#dc2626' : '#16a34a' }}>
                          {exc.hoursDiscrepancy != null ? (exc.hoursDiscrepancy > 0 ? '+' : '') + exc.hoursDiscrepancy.toFixed(1) : '—'}
                        </td>
                        <td>{(exc.exceptionReason || '').replace(/_/g, ' ')}</td>
                        <td><span style={{ padding: '2px 8px', borderRadius: '12px', fontSize: '0.7rem', fontWeight: 600, background: sc.bg, color: sc.color }}>{(exc.status || '').replace(/_/g, ' ')}</span></td>
                        <td>
                          {exc.status === 'PENDING_REVIEW' && (
                            <div style={{ display: 'flex', gap: '0.25rem' }}>
                              <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem', background: '#16a34a' }}
                                onClick={() => { setActionModal({ type: 'approve', exc }); setActionInput(''); }}>Approve</button>
                              <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem', background: '#dc2626' }}
                                onClick={() => { setActionModal({ type: 'deny', exc }); setActionInput(''); }}>Deny</button>
                            </div>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Submit Exception */}
      {tab === 'submit' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Submit EVV Exception Request</h4></div>
          <div className="wq-panel-body">
            {submitError && <div style={{ padding: '0.5rem', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '4px', color: '#dc2626', marginBottom: '0.75rem', fontSize: '0.85rem' }}>{submitError}</div>}
            {submitSuccess && <div style={{ padding: '0.5rem', background: '#f0fdf4', border: '1px solid #86efac', borderRadius: '4px', color: '#16a34a', marginBottom: '0.75rem', fontSize: '0.85rem' }}>{submitSuccess}</div>}
            <div className="wq-search-grid">
              <div className="wq-form-field">
                <label>Provider ID *</label>
                <input type="number" value={submitForm.providerId} onChange={e => setF('providerId', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>Recipient ID *</label>
                <input type="number" value={submitForm.recipientId} onChange={e => setF('recipientId', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>Case ID *</label>
                <input type="number" value={submitForm.caseId} onChange={e => setF('caseId', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>Timesheet ID</label>
                <input type="number" value={submitForm.timesheetId} onChange={e => setF('timesheetId', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>Service Date *</label>
                <input type="date" value={submitForm.serviceDate} onChange={e => setF('serviceDate', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>Hours Claimed</label>
                <input type="number" step="0.5" value={submitForm.hoursClaimed} onChange={e => setF('hoursClaimed', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>EVV Hours Recorded</label>
                <input type="number" step="0.5" value={submitForm.evvHoursRecorded} onChange={e => setF('evvHoursRecorded', e.target.value)} />
              </div>
              <div className="wq-form-field">
                <label>Exception Reason</label>
                <select value={submitForm.exceptionReason} onChange={e => setF('exceptionReason', e.target.value)}>
                  {EXCEPTION_REASONS.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
                </select>
              </div>
              <div className="wq-form-field">
                <label>County Code</label>
                <input type="text" value={submitForm.countyCode} onChange={e => setF('countyCode', e.target.value)} placeholder="e.g. 19" />
              </div>
            </div>
            <div className="wq-form-field" style={{ marginTop: '0.75rem' }}>
              <label>Reason Description</label>
              <textarea rows={3} value={submitForm.reasonDescription} onChange={e => setF('reasonDescription', e.target.value)}
                style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '0.85rem' }}
                placeholder="Describe why EVV data is missing or incorrect..." />
            </div>
            <div style={{ marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleSubmit} disabled={processing}>
                {processing ? 'Submitting...' : 'Submit Exception'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Action Modal */}
      {actionModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: '8px', padding: '1.5rem', width: '420px', boxShadow: '0 8px 30px rgba(0,0,0,0.2)' }}>
            <h4 style={{ marginBottom: '1rem' }}>
              {actionModal.type === 'approve' ? 'Approve EVV Exception' : 'Deny EVV Exception'}
            </h4>
            <p style={{ fontSize: '0.85rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Exception: {actionModal.exc.exceptionNumber}
            </p>
            <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              {actionModal.type === 'approve' ? 'Review Notes (optional)' : 'Denial Reason *'}
            </label>
            <textarea rows={3} value={actionInput} onChange={e => setActionInput(e.target.value)}
              placeholder={actionModal.type === 'approve' ? 'Optional notes...' : 'Enter denial reason...'}
              style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '4px', marginTop: '0.25rem', marginBottom: '1rem', fontSize: '0.85rem' }} />
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => setActionModal(null)} disabled={processing}>Cancel</button>
              <button className="wq-btn wq-btn-primary"
                style={{ background: actionModal.type === 'approve' ? '#16a34a' : '#dc2626' }}
                onClick={() => actionModal.type === 'approve' ? handleApprove(actionModal.exc) : handleDeny(actionModal.exc)}
                disabled={processing}>
                {processing ? 'Processing...' : actionModal.type === 'approve' ? 'Approve' : 'Deny'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
