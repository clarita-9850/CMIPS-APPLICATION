import React, { useState } from 'react';
import * as casesApi from '../../api/casesApi';
import '../WorkQueues.css';

export const TransferCaseModal = ({ caseId, username, onClose, onTransferred }) => {
  const [destinationCounty, setDestinationCounty] = useState('');
  const [receivingWorker, setReceivingWorker] = useState('');
  const [transferReason, setTransferReason] = useState('');
  const [comments, setComments] = useState('');
  const [saving, setSaving] = useState(false);

  const handleSave = () => {
    if (!destinationCounty.trim()) { alert('Destination County is required'); return; }
    if (!transferReason.trim()) { alert('Transfer Reason is required'); return; }
    setSaving(true);
    casesApi.initiateTransfer(caseId, {
      destinationCounty,
      receivingWorker,
      transferReason,
      comments,
      initiatedBy: username
    })
      .then(() => { if (onTransferred) onTransferred(); })
      .catch(() => alert('Failed to initiate transfer'))
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Transfer Case</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>
        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Destination County *</label>
            <input type="text" value={destinationCounty} onChange={e => setDestinationCounty(e.target.value)}
              style={{ width: '100%' }} placeholder="County code" />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Receiving Worker</label>
            <input type="text" value={receivingWorker} onChange={e => setReceivingWorker(e.target.value)}
              style={{ width: '100%' }} placeholder="Worker username" />
          </div>
          <div className="wq-form-field" style={{ marginBottom: '1rem', width: '100%' }}>
            <label>Transfer Reason *</label>
            <select value={transferReason} onChange={e => setTransferReason(e.target.value)} style={{ width: '100%' }}>
              <option value="">Select reason...</option>
              <option value="RECIPIENT_RELOCATED">Recipient Relocated</option>
              <option value="COUNTY_BOUNDARY_CHANGE">County Boundary Change</option>
              <option value="ADMINISTRATIVE">Administrative</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="wq-form-field" style={{ width: '100%' }}>
            <label>Comments</label>
            <textarea value={comments} onChange={e => setComments(e.target.value)} rows={3} style={{ width: '100%' }} />
          </div>
        </div>
        <div className="wq-modal-footer">
          <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Transferring...' : 'Initiate Transfer'}
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
