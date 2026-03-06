import React, { useState } from 'react';
import * as casesApi from '../../api/casesApi';
import '../WorkQueues.css';

export const DenyCaseModal = ({ caseId, onClose, onDenied }) => {
  const [denialReason, setDenialReason] = useState('');
  const [denialCode, setDenialCode] = useState('');
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!denialReason.trim()) { alert('Denial reason is required'); return; }
    setSaving(true);
    casesApi.denyCase(caseId, { denialReason, denialCode, comments })
      .then(() => { if (onDenied) onDenied(); })
      .catch(() => alert('Failed to deny case'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Deny Case</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Denial Reason *</label>
            <select value={denialReason} onChange={e => setDenialReason(e.target.value)} style={{ width: '100%' }}>
              <option value="">Select reason...</option>
              <option value="NOT_ELIGIBLE">Not Eligible</option>
              <option value="INCOMPLETE_APPLICATION">Incomplete Application</option>
              <option value="DUPLICATE_CASE">Duplicate Case</option>
              <option value="WITHDRAWN_BY_APPLICANT">Withdrawn by Applicant</option>
              <option value="OVER_INCOME">Over Income Limit</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Denial Code</label>
            <input type="text" value={denialCode} onChange={e => setDenialCode(e.target.value)} style={{ width: '100%' }} />
          </div>
          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Comments</label>
            <textarea value={comments} onChange={e => setComments(e.target.value)} rows={3} style={{ width: '100%' }} />
          </div>
        </div>
        <div className="wq-modal-footer">
          <button className="wq-btn wq-btn-danger" onClick={handleSave} disabled={saving}>
            {saving ? 'Denying...' : 'Deny Case'}
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
