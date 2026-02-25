import React, { useState } from 'react';
import * as providersApi from '../../api/providersApi';
import '../WorkQueues.css';

export const AssignProviderModal = ({ providerId, onClose, onAssigned }) => {
  const [caseId, setCaseId] = useState('');
  const [assignedHours, setAssignedHours] = useState('');
  const [beginDate, setBeginDate] = useState('');
  const [providerType, setProviderType] = useState('');
  const [payRate, setPayRate] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!caseId.trim()) { alert('Case ID is required'); return; }
    setSaving(true);
    providersApi.assignProviderToCase({
      providerId,
      caseId,
      assignedHours: assignedHours ? Number(assignedHours) : undefined,
      beginDate,
      providerType,
      payRate: payRate ? Number(payRate) : undefined
    })
      .then(() => { if (onAssigned) onAssigned(); })
      .catch(() => alert('Failed to assign provider to case'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Assign Provider to Case</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Case ID *</label>
            <input type="text" value={caseId} onChange={e => setCaseId(e.target.value)}
              style={{ width: '100%' }} placeholder="Enter case ID or number" />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Assigned Hours</label>
            <input type="number" value={assignedHours} onChange={e => setAssignedHours(e.target.value)}
              style={{ width: '100%' }} min="0" step="0.5" />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Begin Date</label>
            <input type="date" value={beginDate} onChange={e => setBeginDate(e.target.value)} style={{ width: '100%' }} />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Provider Type</label>
            <select value={providerType} onChange={e => setProviderType(e.target.value)} style={{ width: '100%' }}>
              <option value="">Select...</option>
              <option value="INDIVIDUAL_PROVIDER">Individual Provider</option>
              <option value="HOME_CARE_AGENCY">Home Care Agency</option>
              <option value="LIVE_IN">Live-In</option>
              <option value="RESPITE">Respite</option>
            </select>
          </div>
          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Pay Rate</label>
            <input type="number" value={payRate} onChange={e => setPayRate(e.target.value)}
              style={{ width: '100%' }} min="0" step="0.01" placeholder="Hourly rate" />
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
