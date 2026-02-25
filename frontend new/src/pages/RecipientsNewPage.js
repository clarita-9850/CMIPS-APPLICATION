import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as referralsApi from '../api/referralsApi';
import './WorkQueues.css';

export const RecipientsNewPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    firstName: '', lastName: '', dateOfBirth: '', ssn: '', gender: '',
    countyCode: '', referralSource: '', priority: 'NORMAL',
    spokenLanguage: '', phone: '', address: '', city: '', zipCode: '',
    notes: '',
  });

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    if (!form.firstName.trim() || !form.lastName.trim()) { setError('First Name and Last Name are required.'); return; }
    if (!form.countyCode.trim()) { setError('County is required.'); return; }
    setError('');
    setSaving(true);
    referralsApi.createReferral({ ...form, createdBy: username })
      .then(data => {
        navigate(`/recipients/${data?.recipientId || data?.personId || data?.id || ''}`);
      })
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to create referral');
      })
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>New Referral</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Cancel</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Person Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>First Name *</label>
              <input type="text" value={form.firstName} onChange={e => handleChange('firstName', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Last Name *</label>
              <input type="text" value={form.lastName} onChange={e => handleChange('lastName', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Date of Birth</label>
              <input type="date" value={form.dateOfBirth} onChange={e => handleChange('dateOfBirth', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>SSN</label>
              <input type="text" value={form.ssn} onChange={e => handleChange('ssn', e.target.value)} placeholder="###-##-####" />
            </div>
            <div className="wq-form-field">
              <label>Gender</label>
              <select value={form.gender} onChange={e => handleChange('gender', e.target.value)}>
                <option value="">Select...</option>
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="NON_BINARY">Non-Binary</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>County *</label>
              <input type="text" value={form.countyCode} onChange={e => handleChange('countyCode', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Referral Details</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Referral Source</label>
              <select value={form.referralSource} onChange={e => handleChange('referralSource', e.target.value)}>
                <option value="">Select...</option>
                <option value="SELF">Self</option>
                <option value="FAMILY">Family Member</option>
                <option value="MEDICAL">Medical Provider</option>
                <option value="SOCIAL_SERVICES">Social Services</option>
                <option value="COMMUNITY_AGENCY">Community Agency</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Priority</label>
              <select value={form.priority} onChange={e => handleChange('priority', e.target.value)}>
                <option value="LOW">Low</option>
                <option value="NORMAL">Normal</option>
                <option value="HIGH">High</option>
                <option value="URGENT">Urgent</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Language</label>
              <input type="text" value={form.spokenLanguage} onChange={e => handleChange('spokenLanguage', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Phone</label>
              <input type="text" value={form.phone} onChange={e => handleChange('phone', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Address</label>
              <input type="text" value={form.address} onChange={e => handleChange('address', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>City</label>
              <input type="text" value={form.city} onChange={e => handleChange('city', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Zip Code</label>
              <input type="text" value={form.zipCode} onChange={e => handleChange('zipCode', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Initial Notes</h4></div>
        <div className="wq-panel-body">
          <div className="wq-form-field" style={{ width: '100%' }}>
            <textarea value={form.notes} onChange={e => handleChange('notes', e.target.value)} rows={4}
              style={{ width: '100%' }} placeholder="Enter initial notes for this referral..." />
          </div>
        </div>
      </div>

      <div className="wq-search-actions">
        <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : 'Save Referral'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Cancel</button>
      </div>
    </div>
  );
};
