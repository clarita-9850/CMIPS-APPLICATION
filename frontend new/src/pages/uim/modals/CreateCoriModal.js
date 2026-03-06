import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import { UimField } from '../../../shared/components/UimField';
import { CORI_TIER_OPTIONS } from '../../../lib/providerConstants';
import '../../../shared/components/UimPage.css';

export const CreateCoriModal = ({ providerId, onClose, onSuccess }) => {
  const today = new Date().toISOString().split('T')[0];
  const [form, setForm] = useState({
    coriDate: today,
    coriEndDate: '',
    convictionReleaseDate: '',
    tier: '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  const handleSave = () => {
    if (!form.convictionReleaseDate) {
      setError('Conviction or Release Date is required.');
      return;
    }
    if (!form.tier) {
      setError('Tier is required.');
      return;
    }
    setSaving(true);
    setError('');
    providersApi.createCoriRecord(providerId, form)
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to create CORI record');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()}>
        <h3 className="uim-cluster-title">Create Provider CORI</h3>
        <div className="uim-cluster-body">
          <div className="uim-form-grid">
            <UimField label="CORI Date" name="coriDate" value={form.coriDate} type="date" onChange={handleChange} required />
            <UimField label="CORI End Date" name="coriEndDate" value={form.coriEndDate} type="date" onChange={handleChange} />
            <UimField label="Conviction or Release Date" name="convictionReleaseDate" value={form.convictionReleaseDate} type="date" onChange={handleChange} required />
            <UimField label="Tier" name="tier" value={form.tier} type="select" options={CORI_TIER_OPTIONS} onChange={handleChange} required />
          </div>
          {error && <div className="uim-error-banner" style={{ marginTop: '0.75rem' }}>{error}</div>}
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
          <button className="uim-btn uim-btn-secondary" onClick={onClose} disabled={saving}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
