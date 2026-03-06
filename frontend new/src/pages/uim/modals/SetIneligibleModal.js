import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import { INELIGIBLE_REASON_OPTIONS } from '../../../lib/providerConstants';
import '../../../shared/components/UimPage.css';

export const SetIneligibleModal = ({ providerId, providerName, onClose, onSuccess }) => {
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleConfirm = () => {
    if (!reason) {
      setError('Ineligible Reason is required.');
      return;
    }
    setSaving(true);
    setError('');
    providersApi.setIneligible(providerId, { reason })
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to set ineligible');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Set Provider Ineligible</h3>
        <div className="uim-cluster-body">
          <p>Setting <strong>{providerName}</strong> as ineligible.</p>

          <div className="uim-field" style={{ marginTop: '0.75rem' }}>
            <label>Ineligible Reason <span style={{ color: '#e53e3e' }}>*</span></label>
            <select
              value={reason}
              onChange={e => setReason(e.target.value)}
              style={{ width: '100%' }}
            >
              {INELIGIBLE_REASON_OPTIONS.map(o => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </div>

          <div className="uim-warning-text" style={{ marginTop: '0.75rem' }}>
            <strong>Warning:</strong> This action will:
            <ul style={{ margin: '0.25rem 0 0 1rem', padding: 0 }}>
              <li>Terminate all active case assignments</li>
              <li>End all general exception waivers</li>
              <li>End all recipient waivers</li>
              <li>End all workweek agreements</li>
            </ul>
          </div>

          {error && <div className="uim-error-banner" style={{ marginTop: '0.5rem' }}>{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-danger" onClick={handleConfirm} disabled={saving}>
            {saving ? 'Processing...' : 'Confirm'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
