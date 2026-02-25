import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as casesApi from '../api/casesApi';
import './WorkQueues.css';

export const CaseCreatePage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const [form, setForm] = useState({
    applicantName: '',
    cin: '',
    countyCode: '',
    zipCode: '',
    spokenLanguage: '',
    writtenLanguage: '',
    interpreterAvailable: false,
    caseOwnerId: '',
    ihssReferralDate: '',
  });

  const handleChange = (field, value) => {
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = () => {
    if (!form.applicantName.trim()) { setError('Applicant Name is required.'); return; }
    if (!form.countyCode.trim()) { setError('County is required.'); return; }
    setError('');
    setSaving(true);
    casesApi.createCase({ ...form, createdBy: username })
      .then(data => {
        navigate(`/cases/${data?.id || ''}`);
      })
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to create case');
      })
      .finally(() => setSaving(false));
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>New Case</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Cancel</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Applicant Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Applicant Name *</label>
              <input type="text" value={form.applicantName} onChange={e => handleChange('applicantName', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>CIN</label>
              <input type="text" value={form.cin} onChange={e => handleChange('cin', e.target.value)} placeholder="Client Index Number" />
            </div>
            <div className="wq-form-field">
              <label>County *</label>
              <input type="text" value={form.countyCode} onChange={e => handleChange('countyCode', e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Zip Code</label>
              <input type="text" value={form.zipCode} onChange={e => handleChange('zipCode', e.target.value)} />
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
              <label>Interpreter Available</label>
              <select value={form.interpreterAvailable ? 'yes' : 'no'} onChange={e => handleChange('interpreterAvailable', e.target.value === 'yes')}>
                <option value="no">No</option>
                <option value="yes">Yes</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Assignment</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Assigned Worker</label>
              <input type="text" value={form.caseOwnerId} onChange={e => handleChange('caseOwnerId', e.target.value)} placeholder="Worker username" />
            </div>
            <div className="wq-form-field">
              <label>IHSS Referral Date</label>
              <input type="date" value={form.ihssReferralDate} onChange={e => handleChange('ihssReferralDate', e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-search-actions">
        <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : 'Save'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Cancel</button>
      </div>
    </div>
  );
};
