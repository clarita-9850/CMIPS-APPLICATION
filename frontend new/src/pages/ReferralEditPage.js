import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import * as referralsApi from '../api/referralsApi';
import './WorkQueues.css';

const CLOSE_REASONS = [
  'DUPLICATE', 'NO_LONGER_NEEDED', 'RECIPIENT_DECLINED', 'UNABLE_TO_CONTACT',
  'CONVERTED_TO_APPLICATION', 'MOVED_OUT_OF_COUNTY', 'OTHER',
];

export const ReferralEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [referral, setReferral] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Editable fields
  const [status, setStatus]               = useState('');
  const [priority, setPriority]           = useState('');
  const [followUpDate, setFollowUpDate]   = useState('');
  const [assignedWorkerId, setAssignedWorkerId] = useState('');
  const [assignedWorkerName, setAssignedWorkerName] = useState('');
  const [notes, setNotes]                 = useState('');

  // Close modal
  const [showCloseModal, setShowCloseModal]   = useState(false);
  const [closeReason, setCloseReason]         = useState('');
  const [closeDetails, setCloseDetails]       = useState('');
  const [closeError, setCloseError]           = useState('');

  // Reopen modal
  const [showReopenModal, setShowReopenModal] = useState(false);
  const [reopenReason, setReopenReason]       = useState('');

  useEffect(() => {
    referralsApi.getReferralById(id)
      .then(data => {
        setReferral(data);
        setStatus(data.status || '');
        setPriority(data.priority || '');
        setFollowUpDate(data.followUpDate || '');
        setAssignedWorkerId(data.assignedWorkerId || '');
        setAssignedWorkerName(data.assignedWorkerName || '');
        setNotes(data.notes || '');
      })
      .catch(() => setError('Referral not found.'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleStatusUpdate = () => {
    if (!status) return;
    setSaving(true); setError(''); setSuccess('');
    referralsApi.updateReferralStatus(id, status)
      .then(updated => { setReferral(updated); setSuccess('Status updated.'); })
      .catch(e => setError(e?.response?.data?.error || 'Failed to update status.'))
      .finally(() => setSaving(false));
  };

  const handlePriorityUpdate = () => {
    if (!priority) return;
    setSaving(true); setError(''); setSuccess('');
    referralsApi.updateReferralPriority(id, priority)
      .then(updated => { setReferral(updated); setSuccess('Priority updated.'); })
      .catch(e => setError(e?.response?.data?.error || 'Failed to update priority.'))
      .finally(() => setSaving(false));
  };

  const handleFollowUpUpdate = () => {
    if (!followUpDate) return;
    setSaving(true); setError(''); setSuccess('');
    import('../api/httpClient').then(({ default: http }) => {
      return http.patch(`/referrals/${id}/follow-up`, { followUpDate });
    })
      .then(res => { setReferral(res.data); setSuccess('Follow-up date updated.'); })
      .catch(e => setError(e?.response?.data?.error || 'Failed to update follow-up date.'))
      .finally(() => setSaving(false));
  };

  const handleAssignUpdate = () => {
    if (!assignedWorkerId) return;
    setSaving(true); setError(''); setSuccess('');
    referralsApi.assignReferral(id, { workerId: assignedWorkerId, workerName: assignedWorkerName })
      .then(updated => { setReferral(updated); setSuccess('Assigned worker updated.'); })
      .catch(e => setError(e?.response?.data?.error || 'Failed to assign worker.'))
      .finally(() => setSaving(false));
  };

  const handleClose = () => {
    if (!closeReason) { setCloseError('Please select a reason.'); return; }
    setSaving(true); setCloseError('');
    referralsApi.closeReferral(id, { reason: closeReason, reasonDetails: closeDetails })
      .then(updated => {
        setReferral(updated);
        setStatus(updated.status || '');
        setShowCloseModal(false);
        setSuccess('Referral closed successfully.');
      })
      .catch(e => setCloseError(e?.response?.data?.error || 'Failed to close referral.'))
      .finally(() => setSaving(false));
  };

  const handleReopen = () => {
    if (!reopenReason) { setError('Please provide a reason to reopen.'); return; }
    setSaving(true); setError('');
    referralsApi.reopenReferral(id, { reason: reopenReason })
      .then(updated => {
        setReferral(updated);
        setStatus(updated.status || '');
        setShowReopenModal(false);
        setSuccess('Referral reopened successfully.');
      })
      .catch(e => setError(e?.response?.data?.error || 'Failed to reopen referral.'))
      .finally(() => setSaving(false));
  };

  if (loading) return <div className="wq-page"><div className="wq-loading">Loading referral…</div></div>;
  if (!referral && error) return <div className="wq-page"><div className="wq-alert wq-alert-error">{error}</div></div>;

  const isClosed = referral?.status === 'CLOSED' || referral?.status === 'CONVERTED';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <div>
          <button className="wq-btn wq-btn-secondary" onClick={() => navigate('/referrals')}
            style={{ marginRight: '8px' }}>← Back</button>
          <h2 style={{ display: 'inline' }}>Edit Referral</h2>
          <span style={{
            marginLeft: '12px', fontSize: '0.85rem', color: '#666',
            fontFamily: 'monospace'
          }}>{id}</span>
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          {!isClosed && (
            <button className="wq-btn wq-btn-danger" onClick={() => setShowCloseModal(true)}>
              Close Referral
            </button>
          )}
          {isClosed && referral?.status === 'CLOSED' && (
            <button className="wq-btn wq-btn-secondary" onClick={() => setShowReopenModal(true)}>
              Reopen Referral
            </button>
          )}
          {referral?.recipientId && (
            <button className="wq-btn wq-btn-primary"
              onClick={() => navigate(`/recipients/${referral.recipientId}`)}>
              View Person
            </button>
          )}
        </div>
      </div>

      {error && <div className="wq-alert wq-alert-error">{error}</div>}
      {success && <div className="wq-alert wq-alert-success">{success}</div>}

      {/* Referral Summary */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Referral Summary (Read-Only)</h4></div>
        <div className="wq-panel-body">
          <div className="wq-detail-grid">
            <div className="wq-detail-item"><label>Recipient Name</label><span>{referral.potentialRecipientName || '—'}</span></div>
            <div className="wq-detail-item"><label>Phone</label><span>{referral.potentialRecipientPhone || '—'}</span></div>
            <div className="wq-detail-item"><label>County</label><span>{referral.countyName || referral.countyCode || '—'}</span></div>
            <div className="wq-detail-item"><label>Referral Date</label><span>{referral.referralDate || '—'}</span></div>
            <div className="wq-detail-item"><label>Source</label><span>{referral.source || '—'}</span></div>
            <div className="wq-detail-item"><label>Preferred Language</label><span>{referral.preferredLanguage || '—'}</span></div>
            <div className="wq-detail-item"><label>Address</label>
              <span>{[referral.streetAddress, referral.city, referral.state, referral.zipCode].filter(Boolean).join(', ') || '—'}</span>
            </div>
            <div className="wq-detail-item"><label>Contact</label>
              <span>{[referral.contactFirstName, referral.contactLastName].filter(Boolean).join(' ') || '—'}
                {referral.contactPhone ? ` — ${referral.contactPhone}` : ''}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Editable: Status */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Status</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
            <div className="wq-form-field" style={{ marginBottom: 0 }}>
              <label>Current Status</label>
              <select value={status} onChange={e => setStatus(e.target.value)} disabled={isClosed}>
                <option value="OPEN">Open</option>
                <option value="PENDING">Pending</option>
                <option value="IN_PROGRESS">In Progress</option>
              </select>
            </div>
            <button className="wq-btn wq-btn-primary" onClick={handleStatusUpdate}
              disabled={saving || isClosed}>Update Status</button>
          </div>
        </div>
      </div>

      {/* Editable: Priority */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Priority</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
            <div className="wq-form-field" style={{ marginBottom: 0 }}>
              <label>Priority Level</label>
              <select value={priority} onChange={e => setPriority(e.target.value)} disabled={isClosed}>
                <option value="URGENT">Urgent</option>
                <option value="HIGH">High</option>
                <option value="NORMAL">Normal</option>
                <option value="LOW">Low</option>
              </select>
            </div>
            <button className="wq-btn wq-btn-primary" onClick={handlePriorityUpdate}
              disabled={saving || isClosed}>Update Priority</button>
          </div>
        </div>
      </div>

      {/* Editable: Follow-Up Date */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Follow-Up Date</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end' }}>
            <div className="wq-form-field" style={{ marginBottom: 0 }}>
              <label>Follow-Up Date</label>
              <input type="date" value={followUpDate} onChange={e => setFollowUpDate(e.target.value)}
                disabled={isClosed} />
            </div>
            <button className="wq-btn wq-btn-primary" onClick={handleFollowUpUpdate}
              disabled={saving || isClosed || !followUpDate}>Set Follow-Up</button>
          </div>
        </div>
      </div>

      {/* Editable: Assign Worker */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Assigned Worker</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
            <div className="wq-form-field" style={{ marginBottom: 0 }}>
              <label>Worker ID</label>
              <input type="text" value={assignedWorkerId} onChange={e => setAssignedWorkerId(e.target.value)}
                placeholder="e.g. SW001" disabled={isClosed} />
            </div>
            <div className="wq-form-field" style={{ marginBottom: 0 }}>
              <label>Worker Name</label>
              <input type="text" value={assignedWorkerName} onChange={e => setAssignedWorkerName(e.target.value)}
                placeholder="Full name" disabled={isClosed} />
            </div>
            <button className="wq-btn wq-btn-primary" onClick={handleAssignUpdate}
              disabled={saving || isClosed || !assignedWorkerId}>Assign</button>
          </div>
        </div>
      </div>

      {/* Close Referral Modal */}
      {showCloseModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header"><h3>Close Referral</h3></div>
            <div className="wq-modal-body">
              {closeError && <div className="wq-alert wq-alert-error">{closeError}</div>}
              <div className="wq-form-field">
                <label>Reason <span className="wq-required">*</span></label>
                <select value={closeReason} onChange={e => setCloseReason(e.target.value)}>
                  <option value="">— Select reason —</option>
                  {CLOSE_REASONS.map(r => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
              <div className="wq-form-field">
                <label>Additional Details</label>
                <textarea value={closeDetails} onChange={e => setCloseDetails(e.target.value)}
                  rows={3} placeholder="Optional additional information" />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setShowCloseModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-danger" onClick={handleClose} disabled={saving}>
                {saving ? 'Closing…' : 'Close Referral'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Reopen Referral Modal */}
      {showReopenModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header"><h3>Reopen Referral</h3></div>
            <div className="wq-modal-body">
              <div className="wq-form-field">
                <label>Reason for Reopening <span className="wq-required">*</span></label>
                <textarea value={reopenReason} onChange={e => setReopenReason(e.target.value)}
                  rows={3} placeholder="Why is this referral being reopened?" />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setShowReopenModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleReopen} disabled={saving}>
                {saving ? 'Reopening…' : 'Reopen'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
