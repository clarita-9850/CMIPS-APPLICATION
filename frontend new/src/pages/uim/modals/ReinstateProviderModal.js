import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import '../../../shared/components/UimPage.css';

export const ReinstateProviderModal = ({ providerId, providerName, provider, onClose, onSuccess }) => {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Calculate days remaining in 30-day reinstatement window
  const ineligibleDate = provider?.updatedAt || provider?.ineligibleDate;
  let daysRemaining = null;
  if (ineligibleDate) {
    const diffMs = Date.now() - new Date(ineligibleDate).getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    daysRemaining = Math.max(0, 30 - diffDays);
  }

  const handleReinstate = () => {
    setSaving(true);
    setError('');
    providersApi.reinstateProvider(providerId)
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to reinstate provider');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Reinstate Provider</h3>
        <div className="uim-cluster-body">
          <p>Reinstating provider <strong>{providerName}</strong>.</p>

          {ineligibleDate && (
            <div className="uim-form-grid" style={{ marginTop: '0.75rem' }}>
              <div className="uim-field">
                <label>Date Made Ineligible</label>
                <span className="uim-field-value">{new Date(ineligibleDate).toLocaleDateString()}</span>
              </div>
              {daysRemaining !== null && (
                <div className="uim-field">
                  <label>Days Remaining in Window</label>
                  <span className="uim-field-value">
                    <span className={`uim-badge ${daysRemaining > 0 ? 'uim-badge-green' : 'uim-badge-red'}`}>
                      {daysRemaining} days
                    </span>
                  </span>
                </div>
              )}
            </div>
          )}

          <div className="uim-info-banner" style={{ marginTop: '0.75rem' }}>
            Provider will be reinstated. This requires supervisor approval.
          </div>

          {error && <div className="uim-error-banner" style={{ marginTop: '0.5rem' }}>{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleReinstate} disabled={saving}>
            {saving ? 'Processing...' : 'Reinstate'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
