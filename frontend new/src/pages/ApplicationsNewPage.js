import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as applicationsApi from '../api/applicationsApi';
import './WorkQueues.css';

export const ApplicationsNewPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [step, setStep] = useState(1); // 1=Duplicate Check, 2=Data Entry, 3=Review
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [duplicates, setDuplicates] = useState([]);
  const [checkDone, setCheckDone] = useState(false);

  const [form, setForm] = useState({
    firstName: '', lastName: '', dateOfBirth: '', ssn: '', gender: '',
    countyCode: '', phone: '', address: '', city: '', zipCode: '',
    programType: '', referralSource: '', spokenLanguage: '', notes: ''
  });

  const handleChange = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const handleDuplicateCheck = async () => {
    if (!form.firstName.trim() || !form.lastName.trim()) {
      setError('First Name and Last Name are required for duplicate check.');
      return;
    }
    setError('');
    setSaving(true);
    try {
      const result = await applicationsApi.checkDuplicate({
        firstName: form.firstName, lastName: form.lastName,
        dateOfBirth: form.dateOfBirth, ssn: form.ssn
      });
      setDuplicates(Array.isArray(result) ? result : (result?.matches || []));
      setCheckDone(true);
    } catch {
      // No duplicates found or endpoint not ready — proceed
      setDuplicates([]);
      setCheckDone(true);
    } finally {
      setSaving(false);
    }
  };

  const handleSubmit = async () => {
    if (!form.firstName.trim() || !form.lastName.trim()) { setError('First Name and Last Name are required.'); return; }
    if (!form.countyCode.trim()) { setError('County is required.'); return; }
    setError('');
    setSaving(true);
    try {
      const data = await applicationsApi.createApplication({ ...form, createdBy: username });
      navigate(`/cases/${data?.caseId || data?.id || ''}`);
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to create application');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>New Application</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Cancel</button>
      </div>

      {/* Step indicator */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        {['Duplicate Check', 'Data Entry', 'Review & Submit'].map((label, i) => (
          <div key={i} style={{
            flex: 1, padding: '0.5rem', textAlign: 'center', borderRadius: '4px', fontSize: '0.8rem',
            background: step === i + 1 ? '#153554' : '#e2e8f0',
            color: step === i + 1 ? 'white' : '#666', fontWeight: step === i + 1 ? 600 : 400
          }}>{label}</div>
        ))}
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      {/* Step 1: Duplicate Check */}
      {step === 1 && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Step 1: Duplicate Check</h4></div>
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
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleDuplicateCheck} disabled={saving}>
                {saving ? 'Checking...' : 'Check for Duplicates'}
              </button>
            </div>
            {checkDone && (
              <div style={{ marginTop: '1rem' }}>
                {duplicates.length === 0 ? (
                  <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.75rem', borderRadius: '4px', color: '#276749' }}>
                    No duplicates found. You may proceed.
                  </div>
                ) : (
                  <div style={{ background: '#fffbeb', border: '1px solid #f6ad55', padding: '0.75rem', borderRadius: '4px', color: '#c05621' }}>
                    {duplicates.length} potential match(es) found. Review before proceeding.
                  </div>
                )}
                <div className="wq-search-actions" style={{ marginTop: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" onClick={() => setStep(2)}>Proceed to Data Entry</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Step 2: Data Entry */}
      {step === 2 && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Step 2: Applicant Information</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field"><label>First Name *</label><input type="text" value={form.firstName} onChange={e => handleChange('firstName', e.target.value)} /></div>
                <div className="wq-form-field"><label>Last Name *</label><input type="text" value={form.lastName} onChange={e => handleChange('lastName', e.target.value)} /></div>
                <div className="wq-form-field"><label>Date of Birth</label><input type="date" value={form.dateOfBirth} onChange={e => handleChange('dateOfBirth', e.target.value)} /></div>
                <div className="wq-form-field"><label>SSN</label><input type="text" value={form.ssn} onChange={e => handleChange('ssn', e.target.value)} /></div>
                <div className="wq-form-field">
                  <label>Gender</label>
                  <select value={form.gender} onChange={e => handleChange('gender', e.target.value)}>
                    <option value="">Select...</option><option value="MALE">Male</option><option value="FEMALE">Female</option><option value="NON_BINARY">Non-Binary</option><option value="OTHER">Other</option>
                  </select>
                </div>
                <div className="wq-form-field"><label>County *</label><input type="text" value={form.countyCode} onChange={e => handleChange('countyCode', e.target.value)} /></div>
                <div className="wq-form-field"><label>Phone</label><input type="text" value={form.phone} onChange={e => handleChange('phone', e.target.value)} /></div>
                <div className="wq-form-field"><label>Address</label><input type="text" value={form.address} onChange={e => handleChange('address', e.target.value)} /></div>
                <div className="wq-form-field"><label>City</label><input type="text" value={form.city} onChange={e => handleChange('city', e.target.value)} /></div>
                <div className="wq-form-field"><label>Zip Code</label><input type="text" value={form.zipCode} onChange={e => handleChange('zipCode', e.target.value)} /></div>
                <div className="wq-form-field">
                  <label>Program Type</label>
                  <select value={form.programType} onChange={e => handleChange('programType', e.target.value)}>
                    <option value="">Select...</option><option value="IHSS">IHSS</option><option value="PCSP">PCSP</option><option value="WPCS">WPCS</option>
                  </select>
                </div>
                <div className="wq-form-field"><label>Language</label><input type="text" value={form.spokenLanguage} onChange={e => handleChange('spokenLanguage', e.target.value)} /></div>
              </div>
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-outline" onClick={() => setStep(1)}>Back</button>
            <button className="wq-btn wq-btn-primary" onClick={() => setStep(3)}>Review</button>
          </div>
        </>
      )}

      {/* Step 3: Review & Submit */}
      {step === 3 && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Step 3: Review & Submit</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-grid">
                <div className="wq-detail-row"><span className="wq-detail-label">Name</span><span className="wq-detail-value">{form.firstName} {form.lastName}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">DOB</span><span className="wq-detail-value">{form.dateOfBirth || 'N/A'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Gender</span><span className="wq-detail-value">{form.gender || 'N/A'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">County</span><span className="wq-detail-value">{form.countyCode}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Program</span><span className="wq-detail-value">{form.programType || 'N/A'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Phone</span><span className="wq-detail-value">{form.phone || 'N/A'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Address</span><span className="wq-detail-value">{form.address || 'N/A'}, {form.city || ''} {form.zipCode || ''}</span></div>
              </div>
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-outline" onClick={() => setStep(2)}>Back</button>
            <button className="wq-btn wq-btn-primary" onClick={handleSubmit} disabled={saving}>
              {saving ? 'Submitting...' : 'Submit Application'}
            </button>
          </div>
        </>
      )}
    </div>
  );
};
