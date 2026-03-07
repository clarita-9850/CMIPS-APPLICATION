import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as bviApi from '../api/bviApi';
import './WorkQueues.css';

const STATUS_COLORS = {
  PENDING_RECIPIENT_REVIEW: { bg: '#fef3c7', color: '#92400e' },
  APPROVED_BY_TTS: { bg: '#d1fae5', color: '#065f46' },
  REJECTED_BY_TTS: { bg: '#fee2e2', color: '#991b1b' },
  EXPIRED: { bg: '#e5e7eb', color: '#374151' },
  CANCELLED: { bg: '#f3f4f6', color: '#6b7280' }
};

export const BviQueuePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [countyFilter, setCountyFilter] = useState('');
  const [actionModal, setActionModal] = useState(null); // { type, review }
  const [actionInput, setActionInput] = useState('');
  const [processing, setProcessing] = useState(false);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'BVI Queue' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const loadReviews = async () => {
    setLoading(true);
    try {
      const data = await bviApi.listPending(countyFilter || undefined);
      setReviews(data);
    } catch (err) {
      console.error('Failed to load BVI reviews:', err);
    } finally { setLoading(false); }
  };

  useEffect(() => { loadReviews(); }, [countyFilter]);

  const handleApprove = async (review) => {
    setProcessing(true);
    try {
      await bviApi.approveBviReview(review.id, actionInput || 'TTS-AUTO');
      setActionModal(null);
      setActionInput('');
      loadReviews();
    } catch (err) {
      alert('Failed: ' + (err?.response?.data?.error || err.message));
    } finally { setProcessing(false); }
  };

  const handleReject = async (review) => {
    if (!actionInput.trim()) { alert('Rejection reason required.'); return; }
    setProcessing(true);
    try {
      await bviApi.rejectBviReview(review.id, actionInput);
      setActionModal(null);
      setActionInput('');
      loadReviews();
    } catch (err) {
      alert('Failed: ' + (err?.response?.data?.error || err.message));
    } finally { setProcessing(false); }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>BVI Recipient Review Queue</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <input type="text" placeholder="County Code" value={countyFilter}
            onChange={e => setCountyFilter(e.target.value)}
            style={{ width: '100px', padding: '0.4rem 0.6rem', border: '1px solid #d1d5db', borderRadius: '4px', fontSize: '0.85rem' }} />
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back</button>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Pending BVI Reviews ({reviews.length})</h4>
          <span style={{ fontSize: '0.8rem', color: '#6b7280' }}>TVP Rules 62, 63, 64, 74</span>
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div> : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>Review #</th>
                  <th>TS Number</th>
                  <th>Recipient ID</th>
                  <th>Provider ID</th>
                  <th>Pay Period</th>
                  <th>Hours</th>
                  <th>Status</th>
                  <th>Deadline</th>
                  <th>Flags</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {reviews.length === 0 ? (
                  <tr><td colSpan={10} style={{ textAlign: 'center', padding: '2rem', color: '#9ca3af' }}>No pending BVI reviews</td></tr>
                ) : reviews.map(r => {
                  const sc = STATUS_COLORS[r.status] || {};
                  return (
                    <tr key={r.id}>
                      <td style={{ fontWeight: 600 }}>{r.reviewNumber}</td>
                      <td>{r.timesheetNumber || '—'}</td>
                      <td>{r.recipientId}</td>
                      <td>{r.providerId}</td>
                      <td style={{ fontSize: '0.8rem' }}>{r.payPeriodStart} – {r.payPeriodEnd}</td>
                      <td>{r.totalHoursClaimed?.toFixed(1)}</td>
                      <td><span style={{ padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600, background: sc.bg, color: sc.color }}>{(r.status || '').replace(/_/g, ' ')}</span></td>
                      <td style={{ fontSize: '0.8rem' }}>{r.reviewDeadline}</td>
                      <td style={{ fontSize: '0.75rem' }}>
                        {r.earlySubmission && <span style={{ background: '#dbeafe', color: '#1e40af', padding: '1px 6px', borderRadius: '8px', marginRight: '4px' }}>Early</span>}
                        {r.lateSubmission && <span style={{ background: '#fef3c7', color: '#92400e', padding: '1px 6px', borderRadius: '8px' }}>Late</span>}
                      </td>
                      <td>
                        {r.status === 'PENDING_RECIPIENT_REVIEW' && (
                          <div style={{ display: 'flex', gap: '0.3rem' }}>
                            <button className="wq-btn wq-btn-primary" style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem', background: '#16a34a' }}
                              onClick={() => { setActionModal({ type: 'approve', review: r }); setActionInput(''); }}>Approve</button>
                            <button className="wq-btn wq-btn-primary" style={{ padding: '0.2rem 0.5rem', fontSize: '0.75rem', background: '#dc2626' }}
                              onClick={() => { setActionModal({ type: 'reject', review: r }); setActionInput(''); }}>Reject</button>
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

      {/* Action Modal */}
      {actionModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: '8px', padding: '1.5rem', width: '420px', boxShadow: '0 8px 30px rgba(0,0,0,0.2)' }}>
            <h4 style={{ marginBottom: '1rem' }}>
              {actionModal.type === 'approve' ? 'Approve BVI Review' : 'Reject BVI Review'}
            </h4>
            <p style={{ fontSize: '0.85rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Review: {actionModal.review.reviewNumber} | TS: {actionModal.review.timesheetNumber}
            </p>
            <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              {actionModal.type === 'approve' ? 'TTS Confirmation Code' : 'Rejection Reason *'}
            </label>
            <input type="text" value={actionInput} onChange={e => setActionInput(e.target.value)}
              placeholder={actionModal.type === 'approve' ? 'e.g. TTS-2026030612345' : 'Enter reason...'}
              style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '4px', marginTop: '0.25rem', marginBottom: '1rem', fontSize: '0.85rem' }} />
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => setActionModal(null)} disabled={processing}>Cancel</button>
              <button className="wq-btn wq-btn-primary"
                style={{ background: actionModal.type === 'approve' ? '#16a34a' : '#dc2626' }}
                onClick={() => actionModal.type === 'approve' ? handleApprove(actionModal.review) : handleReject(actionModal.review)}
                disabled={processing}>
                {processing ? 'Processing...' : actionModal.type === 'approve' ? 'Approve' : 'Reject'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
