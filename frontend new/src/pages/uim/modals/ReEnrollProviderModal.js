import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import '../../../shared/components/UimPage.css';

export const ReEnrollProviderModal = ({ providerId, providerName, onClose, onSuccess }) => {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleReEnroll = () => {
    setSaving(true);
    setError('');
    providersApi.reEnrollProvider(providerId)
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to re-enroll provider');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Re-Enroll Provider</h3>
        <div className="uim-cluster-body">
          <p>Re-enrolling provider <strong>{providerName}</strong>.</p>

          <div className="uim-warning-text" style={{ marginTop: '0.75rem' }}>
            <strong>Warning:</strong> All enrollment requirements will be reset to blank.
            Provider status will return to <strong>Pending</strong>.
          </div>

          {error && <div className="uim-error-banner" style={{ marginTop: '0.5rem' }}>{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleReEnroll} disabled={saving}>
            {saving ? 'Processing...' : 'Re-Enroll'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
