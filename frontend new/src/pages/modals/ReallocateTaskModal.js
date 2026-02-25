/**
 * ReallocateTaskModal - Reallocate a task (confirm action)
 * Matches legacy CMIPS "Reallocate Task" modal
 * Fields: Comments, Yes/No confirmation
 */

import React, { useState } from 'react';
import http from '../../api/httpClient';
import '../WorkQueues.css';

export const ReallocateTaskModal = ({ taskId, username, onClose, onReallocated }) => {
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);

  const handleConfirm = () => {
    setSaving(true);
    http.post(`/tasks/${taskId}/reallocate`, { username: username || 'unknown', comments })
      .then(() => { if (onReallocated) onReallocated(); })
      .catch(() => alert('Failed to reallocate task'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" style={{ minWidth: '450px' }} onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Reallocate Task</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>

        <div className="wq-modal-body">
          <p style={{ marginBottom: '1rem', fontSize: '0.9rem', color: '#333' }}>
            Are you sure you want to reallocate this task? The task will be returned to the
            work queue for reassignment.
          </p>

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
          <button className="wq-btn wq-btn-primary" onClick={handleConfirm} disabled={saving}>
            Yes
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>No</button>
        </div>
      </div>
    </div>
  );
};
