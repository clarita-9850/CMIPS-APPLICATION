import React, { useState } from 'react';
import * as casesApi from '../../api/casesApi';
import '../WorkQueues.css';

export const AddContactModal = ({ caseId, onClose, onSaved }) => {
  const [name, setName] = useState('');
  const [relationship, setRelationship] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!name.trim()) { alert('Contact name is required'); return; }
    setSaving(true);
    casesApi.addCaseContact(caseId, { name, relationship, phone, email, address })
      .then(() => { if (onSaved) onSaved(); })
      .catch(() => alert('Failed to add contact'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Add Contact</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Name *</label>
            <input type="text" value={name} onChange={e => setName(e.target.value)} style={{ width: '100%' }} />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Relationship</label>
            <select value={relationship} onChange={e => setRelationship(e.target.value)} style={{ width: '100%' }}>
              <option value="">Select...</option>
              <option value="PARENT">Parent</option>
              <option value="SPOUSE">Spouse</option>
              <option value="CHILD">Child</option>
              <option value="SIBLING">Sibling</option>
              <option value="GUARDIAN">Guardian</option>
              <option value="CAREGIVER">Caregiver</option>
              <option value="SOCIAL_WORKER">Social Worker</option>
              <option value="ATTORNEY">Attorney</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Phone</label>
            <input type="text" value={phone} onChange={e => setPhone(e.target.value)} style={{ width: '100%' }} placeholder="(###) ###-####" />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Email</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} style={{ width: '100%' }} />
          </div>
          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Address</label>
            <textarea value={address} onChange={e => setAddress(e.target.value)} rows={2} style={{ width: '100%' }} />
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
