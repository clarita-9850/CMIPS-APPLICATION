import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as recipientsApi from '../api/recipientsApi';
import './WorkQueues.css';

export const RecipientEditPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    title: '', firstName: '', middleName: '', lastName: '', suffix: '',
    dateOfBirth: '', gender: '', ethnicity: '', ssn: '', cin: '', taxpayerId: '',
    residenceAddress: '', city: '', countyCode: '', zipCode: '',
    phone: '', email: '',
    spokenLanguage: '', writtenLanguage: '', interpreterNeeded: false,
    personType: '', referralSource: '',
  });

  useEffect(() => {
    recipientsApi.getRecipientById(id)
      .then(data => {
        setForm({
          title: data.title || '',
          firstName: data.firstName || '',
          middleName: data.middleName || '',
          lastName: data.lastName || '',
          suffix: data.suffix || '',
          dateOfBirth: data.dateOfBirth || '',
          gender: data.gender || '',
          ethnicity: data.ethnicity || '',
          ssn: data.ssn || '',
          cin: data.cin || '',
          taxpayerId: data.taxpayerId || '',
          residenceAddress: data.residenceAddress || data.address || '',
          city: data.city || '',
          countyCode: data.countyCode || '',
          zipCode: data.zipCode || '',
          phone: data.phone || data.phoneNumber || '',
          email: data.email || '',
          spokenLanguage: data.spokenLanguage || '',
          writtenLanguage: data.writtenLanguage || '',
          interpreterNeeded: data.interpreterNeeded || false,
          personType: data.personType || '',
          referralSource: data.referralSource || '',
        });
      })
      .catch(() => setError('Failed to load person'))
      .finally(() => setLoading(false));
  }, [id]);

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    if (!form.lastName.trim()) { setError('Last Name is required.'); return; }
    setError('');
    setSaving(true);
    recipientsApi.updateRecipient(id, form)
      .then(() => navigate(`/recipients/${id}`))
      .catch(err => setError(err?.response?.data?.message || err.message || 'Failed to save'))
      .finally(() => setSaving(false));
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Edit Person</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/recipients/${id}`)}>Cancel</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Name</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Title</label>
              <select value={form.title} onChange={e => handleChange('title', e.target.value)}>
                <option value="">Select...</option>
                <option value="Mr">Mr</option>
                <option value="Mrs">Mrs</option>
                <option value="Ms">Ms</option>
                <option value="Dr">Dr</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>First Name</label>
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
              <label>Suffix</label>
              <input type="text" value={form.suffix} onChange={e => handleChange('suffix', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Demographics</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Date of Birth</label>
              <input type="date" value={form.dateOfBirth} onChange={e => handleChange('dateOfBirth', e.target.value)} />
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
              <label>Ethnicity</label>
              <input type="text" value={form.ethnicity} onChange={e => handleChange('ethnicity', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>SSN</label>
              <input type="text" value={form.ssn} onChange={e => handleChange('ssn', e.target.value)} placeholder="###-##-####" />
            </div>
            <div className="wq-form-field">
              <label>CIN</label>
              <input type="text" value={form.cin} onChange={e => handleChange('cin', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Person Type</label>
              <select value={form.personType} onChange={e => handleChange('personType', e.target.value)}>
                <option value="">Select...</option>
                <option value="RECIPIENT">Recipient</option>
                <option value="PROVIDER">Provider</option>
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
              <input type="text" value={form.residenceAddress} onChange={e => handleChange('residenceAddress', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>City</label>
              <input type="text" value={form.city} onChange={e => handleChange('city', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>County</label>
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
        <div className="wq-panel-header"><h4>Language</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Spoken Language</label>
              <input type="text" value={form.spokenLanguage} onChange={e => handleChange('spokenLanguage', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Written Language</label>
              <input type="text" value={form.writtenLanguage} onChange={e => handleChange('writtenLanguage', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Interpreter Needed</label>
              <select value={form.interpreterNeeded ? 'yes' : 'no'} onChange={e => handleChange('interpreterNeeded', e.target.value === 'yes')}>
                <option value="no">No</option>
                <option value="yes">Yes</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="wq-search-actions">
        <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : 'Save'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/recipients/${id}`)}>Cancel</button>
      </div>
    </div>
  );
};
