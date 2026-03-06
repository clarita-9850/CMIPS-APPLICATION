import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import '../../../shared/components/UimPage.css';

export const InactivateCoriModal = ({ record, onClose, onSuccess }) => {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleInactivate = () => {
    setSaving(true);
    setError('');
    const coriId = record.id || record.coriId;
    providersApi.inactivateCori(coriId)
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to inactivate CORI record');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Inactivate CORI Record</h3>
        <div className="uim-cluster-body">
          <p>Are you sure you want to inactivate this CORI record?</p>
          {error && <div className="uim-error-banner" style={{ marginTop: '0.5rem' }}>{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-danger" onClick={handleInactivate} disabled={saving}>
            {saving ? 'Processing...' : 'Yes'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>No</button>
        </div>
      </div>
    </div>
  );
};
