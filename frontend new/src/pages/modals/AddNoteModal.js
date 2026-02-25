import React, { useState } from 'react';
import { useAuth } from '../../auth/AuthContext';
import * as casesApi from '../../api/casesApi';
import * as notesApi from '../../api/notesApi';
import '../WorkQueues.css';

export const AddNoteModal = ({ entityType, entityId, onClose, onSaved }) => {
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';
  const [priority, setPriority] = useState('NORMAL');
  const [sensitivity, setSensitivity] = useState('STANDARD');
  const [text, setText] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!text.trim()) { alert('Note text is required'); return; }
    setSaving(true);
    const data = { priority, sensitivity, text, createdBy: username };
    let promise;
    if (entityType === 'case') {
      promise = casesApi.addCaseNote(entityId, data);
    } else if (entityType === 'recipient') {
      promise = notesApi.createRecipientNote(entityId, data);
    } else if (entityType === 'provider') {
      promise = notesApi.createProviderNote(entityId, data);
    } else {
      promise = casesApi.addCaseNote(entityId, data);
    }
    promise
      .then(() => { if (onSaved) onSaved(); })
      .catch(() => alert('Failed to save note'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Add Note</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Priority</label>
            <select value={priority} onChange={e => setPriority(e.target.value)} style={{ width: '100%' }}>
              <option value="LOW">Low</option>
              <option value="NORMAL">Normal</option>
              <option value="HIGH">High</option>
              <option value="URGENT">Urgent</option>
            </select>
          </div>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Sensitivity</label>
            <select value={sensitivity} onChange={e => setSensitivity(e.target.value)} style={{ width: '100%' }}>
              <option value="STANDARD">Standard</option>
              <option value="SENSITIVE">Sensitive</option>
              <option value="HIGHLY_SENSITIVE">Highly Sensitive</option>
            </select>
          </div>

          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Note Text *</label>
            <textarea value={text} onChange={e => setText(e.target.value)} rows={5} style={{ width: '100%' }} placeholder="Enter note text..." />
          </div>
        </div>
        <div className="wq-modal-footer">
          <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
