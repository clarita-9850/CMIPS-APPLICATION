/**
 * ForwardTaskModal - Forward a task to another user or work queue
 * Matches legacy CMIPS "Forward Task" modal
 * Fields: Forward To (User/Work Queue), Comments
 */

import React, { useState } from 'react';
import http from '../../api/httpClient';
import '../WorkQueues.css';

export const ForwardTaskModal = ({ taskId, username, onClose, onForwarded }) => {
  const [forwardTo, setForwardTo] = useState('');
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!forwardTo.trim()) { alert('Forward To is required'); return; }
    setSaving(true);
    http.post(`/tasks/${taskId}/forward`, { forwardTo, forwardedBy: username || 'unknown', comments })
      .then(() => { if (onForwarded) onForwarded(); })
      .catch(() => alert('Failed to forward task'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" style={{ minWidth: '450px' }} onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Forward Task</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>

        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Forward To *</label>
            <input
              type="text"
              value={forwardTo}
              onChange={e => setForwardTo(e.target.value)}
              placeholder="User or Work Queue"
              style={{ width: '100%' }}
            />
          </div>

          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Comments</label>
            <textarea
              value={comments}
              onChange={e => setComments(e.target.value)}
              rows={4}
              style={{ width: '100%' }}
            />
          </div>
        </div>

        <div className="wq-modal-footer">
          <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
            Save
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
