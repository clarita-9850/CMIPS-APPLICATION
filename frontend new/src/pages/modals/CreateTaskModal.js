/**
 * CreateTaskModal - Create a new user task
 * Matches legacy CMIPS "Create User Task" modal
 * Fields: Subject, Priority, Deadline, Case Participant, Case Number,
 *         Reserve To Me, Assign To, Comments
 */

import React, { useState } from 'react';
import '../WorkQueues.css';

export const CreateTaskModal = ({ onClose, onSave }) => {
  const [form, setForm] = useState({
    subject: '',
    priority: 'Medium',
    deadline: '',
    caseParticipant: '',
    caseNumber: '',
    reserveToMe: false,
    assignTo: '',
    comments: ''
  });

  const set = (field) => (e) => {
    const val = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
    setForm(prev => ({ ...prev, [field]: val }));
  };

  const handleSave = () => {
    if (!form.subject.trim()) { alert('Subject is required'); return; }
    onSave({
      subject: form.subject,
      title: form.subject,
      priority: form.priority,
      deadline: form.deadline || null,
      dueDate: form.deadline || null,
      caseParticipant: form.caseParticipant || null,
      caseNumber: form.caseNumber || null,
      reserveToMe: form.reserveToMe,
      assignedTo: form.assignTo || null,
      comments: form.comments || null
    });
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Create User Task</h3>
          <div className="wq-modal-header-actions">
            <button className="wq-modal-close" onClick={onClose}>&times;</button>
          </div>
        </div>

        <div className="wq-modal-body">
          <p className="wq-modal-required">* indicates a required field</p>

          {/* Task Details */}
          <div className="wq-panel" style={{ marginBottom: '1rem' }}>
            <div className="wq-panel-header"><h4>Task Details</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-form" style={{ flexDirection: 'column', gap: '0.75rem' }}>
                <div className="wq-form-field" style={{ width: '100%' }}>
                  <label>Subject *</label>
                  <input type="text" value={form.subject} onChange={set('subject')} />
                </div>
                <div style={{ display: 'flex', gap: '1rem' }}>
                  <div className="wq-form-field">
                    <label>Priority *</label>
                    <select value={form.priority} onChange={set('priority')}>
                      <option value="High">High</option>
                      <option value="Medium">Medium</option>
                      <option value="Low">Low</option>
                    </select>
                  </div>
                  <div className="wq-form-field">
                    <label>Deadline</label>
                    <input type="date" value={form.deadline} onChange={set('deadline')} />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Concerning */}
          <div className="wq-panel" style={{ marginBottom: '1rem' }}>
            <div className="wq-panel-header"><h4>Concerning</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'flex', gap: '1rem' }}>
                <div className="wq-form-field">
                  <label>Case Participant</label>
                  <input type="text" value={form.caseParticipant} onChange={set('caseParticipant')} />
                </div>
                <div className="wq-form-field">
                  <label>Case Number</label>
                  <input type="text" value={form.caseNumber} onChange={set('caseNumber')} />
                </div>
              </div>
            </div>
          </div>

          {/* Assignment Details */}
          <div className="wq-panel" style={{ marginBottom: '1rem' }}>
            <div className="wq-panel-header"><h4>Assignment Details</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem' }}>
                  <input type="checkbox" checked={form.reserveToMe} onChange={set('reserveToMe')} />
                  Reserve To Me
                </label>
                <div className="wq-form-field">
                  <label>Assign To</label>
                  <input type="text" value={form.assignTo} onChange={set('assignTo')} placeholder="User or Work Queue" />
                </div>
              </div>
            </div>
          </div>

          {/* Comments */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Comments</h4></div>
            <div className="wq-panel-body">
              <div className="wq-form-field" style={{ width: '100%' }}>
                <textarea value={form.comments} onChange={set('comments')} rows={3} />
              </div>
            </div>
          </div>
        </div>

        <div className="wq-modal-footer">
          <button className="wq-btn wq-btn-primary" onClick={handleSave}>Save</button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
