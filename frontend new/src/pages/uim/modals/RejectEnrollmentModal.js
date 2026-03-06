import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import '../../../shared/components/UimPage.css';

export const RejectEnrollmentModal = ({ providerId, providerName, onClose, onSuccess }) => {
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleReject = () => {
    if (!comments.trim()) {
      setError('Rejection comments are required.');
      return;
    }
    if (comments.length > 200) {
      setError('Comments must not exceed 200 characters.');
      return;
    }
    setSaving(true);
    setError('');
    providersApi.rejectEnrollment(providerId, { comments })
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to reject enrollment');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Reject Enrollment</h3>
        <div className="uim-cluster-body">
          <p>Rejecting enrollment for <strong>{providerName}</strong>.</p>
          <div className="uim-field" style={{ marginTop: '0.75rem' }}>
            <label>Rejection Comments <span style={{ color: '#e53e3e' }}>*</span></label>
            <textarea
              value={comments}
              onChange={e => setComments(e.target.value)}
              maxLength={200}
              rows={3}
              style={{
                width: '100%', padding: '0.4rem 0.6rem',
                border: '1px solid var(--border-color-dark)', borderRadius: '3px',
                fontSize: '0.9rem', fontFamily: 'inherit', resize: 'vertical',
              }}
              placeholder="Enter reason for rejection..."
            />
            <span style={{ fontSize: '0.75rem', color: '#718096' }}>{comments.length}/200 characters</span>
          </div>
          {error && <div className="uim-error-banner" style={{ marginTop: '0.5rem' }}>{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleReject} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
