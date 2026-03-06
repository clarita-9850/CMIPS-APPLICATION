import React, { useState } from 'react';
import * as casesApi from '../../api/casesApi';
import '../WorkQueues.css';

export const AssignCaseModal = ({ caseId, onClose, onAssigned }) => {
  const [assignedTo, setAssignedTo] = useState('');
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!assignedTo.trim()) { alert('Assigned Worker is required'); return; }
    setSaving(true);
    casesApi.assignCase(caseId, { assignedTo, comments })
      .then(() => { if (onAssigned) onAssigned(); })
      .catch(() => alert('Failed to assign case'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Assign Case</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Assigned Worker *</label>
            <input type="text" value={assignedTo} onChange={e => setAssignedTo(e.target.value)}
              style={{ width: '100%' }} placeholder="Enter worker username" />
          </div>
          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Comments</label>
            <textarea value={comments} onChange={e => setComments(e.target.value)} rows={3} style={{ width: '100%' }} />
          </div>
        </div>
        <div className="wq-modal-footer">
          <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Assigning...' : 'Assign'}
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
