import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import '../../../shared/components/UimPage.css';

export const ApproveEnrollmentModal = ({ providerId, providerName, onClose, onSuccess }) => {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleApprove = () => {
    setSaving(true);
    setError('');
    providersApi.approveEnrollment(providerId)
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to approve enrollment');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Approve Enrollment</h3>
        <div className="uim-cluster-body">
          <p>Are you sure you want to approve enrollment for <strong>{providerName}</strong>?</p>
          {error && <div className="uim-error-banner">{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleApprove} disabled={saving}>
            {saving ? 'Approving...' : 'Yes'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>No</button>
        </div>
      </div>
    </div>
  );
};
