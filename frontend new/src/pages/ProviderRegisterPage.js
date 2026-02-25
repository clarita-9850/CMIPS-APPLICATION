import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as providersApi from '../api/providersApi';
import './WorkQueues.css';

export const ProviderRegisterPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    firstName: '', lastName: '', middleName: '',
    dateOfBirth: '', ssn: '', gender: '',
    address: '', city: '', countyCode: '', zipCode: '',
    phone: '', email: '',
    providerType: '', spokenLanguage: '',
  });

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    if (!form.firstName.trim() || !form.lastName.trim()) { setError('First Name and Last Name are required.'); return; }
    if (!form.countyCode.trim()) { setError('County is required.'); return; }
    setError('');
    setSaving(true);
    providersApi.createProvider({ ...form, createdBy: username })
      .then(data => {
        navigate(`/providers/${data?.id || ''}`);
      })
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to register provider');
      })
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Register Provider</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Cancel</button>
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
              <label>Middle Name</label>
              <input type="text" value={form.middleName} onChange={e => handleChange('middleName', e.target.value)} />
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
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Contact</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Address</label>
              <input type="text" value={form.address} onChange={e => handleChange('address', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>City</label>
              <input type="text" value={form.city} onChange={e => handleChange('city', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>County *</label>
              <input type="text" value={form.countyCode} onChange={e => handleChange('countyCode', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Zip Code</label>
              <input type="text" value={form.zipCode} onChange={e => handleChange('zipCode', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Phone</label>
              <input type="text" value={form.phone} onChange={e => handleChange('phone', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Email</label>
              <input type="email" value={form.email} onChange={e => handleChange('email', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Provider Details</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Provider Type</label>
              <select value={form.providerType} onChange={e => handleChange('providerType', e.target.value)}>
                <option value="">Select...</option>
                <option value="INDIVIDUAL_PROVIDER">Individual Provider</option>
                <option value="HOME_CARE_AGENCY">Home Care Agency</option>
                <option value="LIVE_IN">Live-In</option>
                <option value="RESPITE">Respite</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Language</label>
              <input type="text" value={form.spokenLanguage} onChange={e => handleChange('spokenLanguage', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-search-actions">
        <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : 'Register'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Cancel</button>
      </div>
    </div>
  );
};
