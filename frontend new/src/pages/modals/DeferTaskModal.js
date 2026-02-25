/**
 * DeferTaskModal - Defer a task with a restart date
 * Matches legacy CMIPS "Defer Task" modal
 * Fields: Restart Date, Comment
 */

import React, { useState } from 'react';
import http from '../../api/httpClient';
import '../WorkQueues.css';

export const DeferTaskModal = ({ taskId, username, onClose, onDeferred }) => {
  const [restartDate, setRestartDate] = useState('');
  const [comment, setComment] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!restartDate) { alert('Restart Date is required'); return; }
    setSaving(true);
    http.post(`/tasks/${taskId}/defer`, { username: username || 'unknown', restartDate, comment })
      .then(() => { if (onDeferred) onDeferred(); })
      .catch(() => alert('Failed to defer task'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" style={{ minWidth: '400px' }} onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Defer Task</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>

        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem' }}>
            <label>Restart Date *</label>
            <input
              type="date"
              value={restartDate}
              onChange={e => setRestartDate(e.target.value)}
            />
          </div>

          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Comment</label>
            <textarea
              value={comment}
              onChange={e => setComment(e.target.value)}
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
