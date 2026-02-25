import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as waiversApi from '../api/waiversApi';
import './WorkQueues.css';

export const WaiverDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [waiver, setWaiver] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Waivers', path: '/waivers' }, { label: `Waiver ${id}` }]);
    return () => setBreadcrumbs([]);
  }, [id, setBreadcrumbs]);

  const load = useCallback(async () => {
    try {
      const data = await waiversApi.getWaiver(id);
      setWaiver(data);
    } catch (err) {
      console.warn('[WaiverDetail] Error:', err?.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  const doAction = async (fn, label) => {
    setError(''); setSuccess('');
    try {
      await fn();
      setSuccess(`${label} completed successfully.`);
      await load();
    } catch (err) {
      setError(`${label} failed: ${err?.response?.data?.message || err?.message || 'Unknown error'}`);
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
  const formatDateTime = (d) => d ? new Date(d).toLocaleString() : '\u2014';

  if (loading) return <div className="wq-page"><p>Loading waiver...</p></div>;
  if (!waiver) return <div className="wq-page"><p>Waiver not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/waivers')}>Back</button></div>;

  const w = waiver;
  const status = w.status || '';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Waiver #{w.id || w.waiverId}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/waivers')}>Back to Waivers</button>
      </div>

      {error && <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>}
      {success && <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>{success}</div>}

      {/* Actions */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Workflow Actions</h4></div>
        <div className="wq-manage-bar">
          {(status === 'INITIATED' || status === 'PENDING_DISCLOSURE') && (
            <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.disclose(id, { disclosedBy: username }), 'Disclose')}>
              <span className="action-icon">&#128196;</span> Disclose
            </button>
          )}
          {status === 'PENDING_RECIPIENT_DECISION' && (
            <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.recipientDecision(id, { decision: 'ACCEPT', decidedBy: username }), 'Accept')}>
              <span className="action-icon">&#10003;</span> Recipient Accept
            </button>
          )}
          {status === 'PENDING_SIGNATURE' && (
            <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.signSoc2298(id, { signedBy: username }), 'Sign SOC 2298')}>
              <span className="action-icon">&#9997;</span> Sign SOC 2298
            </button>
          )}
          {(status === 'SIGNED' || status === 'PENDING_REVIEW') && (
            <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.submitForReview(id), 'Submit for Review')}>
              <span className="action-icon">&#9654;</span> Submit for Review
            </button>
          )}
          {status === 'PENDING_COUNTY_REVIEW' && (
            <>
              <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.countyDecision(id, { decision: 'APPROVE', decidedBy: username }), 'County Approve')}>
                <span className="action-icon">&#10003;</span> County Approve
              </button>
              <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.countyDecision(id, { decision: 'DENY', decidedBy: username }), 'County Deny')}>
                <span className="action-icon">&#10005;</span> County Deny
              </button>
            </>
          )}
          {status === 'PENDING_SUPERVISOR_REVIEW' && (
            <>
              <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.supervisorDecision(id, { decision: 'APPROVE', decidedBy: username }), 'Supervisor Approve')}>
                <span className="action-icon">&#10003;</span> Supervisor Approve
              </button>
              <button className="wq-manage-action" onClick={() => doAction(() => waiversApi.supervisorDecision(id, { decision: 'DENY', decidedBy: username }), 'Supervisor Deny')}>
                <span className="action-icon">&#10005;</span> Supervisor Deny
              </button>
            </>
          )}
          {(status === 'ACTIVE' || status === 'APPROVED') && (
            <button className="wq-manage-action" onClick={() => {
              const reason = prompt('Reason for revocation:');
              if (reason) doAction(() => waiversApi.revokeWaiver(id, { reason, revokedBy: username }), 'Revoke');
            }}>
              <span className="action-icon">&#128683;</span> Revoke
            </button>
          )}
        </div>
      </div>

      <div className="wq-task-columns">
        {/* Waiver Details */}
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Waiver Details</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Waiver ID:</span><span className="wq-detail-value">{w.id || w.waiverId}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Status:</span><span className="wq-detail-value"><span className={`wq-badge wq-badge-${status.toLowerCase().replace(/_/g, '-')}`}>{status}</span></span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">County:</span><span className="wq-detail-value">{w.countyCode || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Effective Date:</span><span className="wq-detail-value">{formatDate(w.effectiveDate)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Expiration Date:</span><span className="wq-detail-value">{formatDate(w.expirationDate)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Created:</span><span className="wq-detail-value">{formatDateTime(w.createdAt)}</span></div>
            </div>
          </div>
        </div>

        {/* Parties */}
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Parties</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Recipient:</span><span className="wq-detail-value">{w.recipientName || w.recipientId || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Provider:</span><span className="wq-detail-value">{w.providerName || w.providerId || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Reviewer:</span><span className="wq-detail-value">{w.reviewerName || w.reviewerId || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Supervisor:</span><span className="wq-detail-value">{w.supervisorName || w.supervisorId || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Initiated By:</span><span className="wq-detail-value">{w.initiatedBy || '\u2014'}</span></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
