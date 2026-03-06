import React, { useState } from 'react';
import * as providersApi from '../../../api/providersApi';
import { UimField } from '../../../shared/components/UimField';
import { CORI_TIER_OPTIONS } from '../../../lib/providerConstants';
import '../../../shared/components/UimPage.css';

export const ModifyCoriModal = ({ record, onClose, onSuccess }) => {
  const [form, setForm] = useState({
    coriDate: record.coriDate || record.checkDate || '',
    coriEndDate: record.coriEndDate || '',
    convictionReleaseDate: record.convictionReleaseDate || '',
    tier: record.tier || '',
    geBeginDate: record.geBeginDate || '',
    geEndDate: record.geEndDate || '',
    geNotes: record.geNotes || '',
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  const handleSave = () => {
    setSaving(true);
    setError('');

    // If GE fields changed, use general exception endpoint
    const geChanged = form.geBeginDate !== (record.geBeginDate || '') ||
                      form.geEndDate !== (record.geEndDate || '') ||
                      form.geNotes !== (record.geNotes || '');

    const coriId = record.id || record.coriId;
    const promises = [];

    // Always update CORI main fields
    promises.push(providersApi.modifyCori(coriId, {
      coriDate: form.coriDate,
      coriEndDate: form.coriEndDate,
      convictionReleaseDate: form.convictionReleaseDate,
      tier: form.tier,
    }));

    // If GE changed, also call general exception endpoint
    if (geChanged) {
      promises.push(providersApi.addGeneralException(coriId, {
        geBeginDate: form.geBeginDate,
        geEndDate: form.geEndDate,
        geNotes: form.geNotes,
      }));
    }

    Promise.all(promises)
      .then(() => onSuccess())
      .catch(err => {
        setError(err?.response?.data?.message || err.message || 'Failed to modify CORI record');
        setSaving(false);
      });
  };

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()} style={{ maxWidth: '600px' }}>
        <h3 className="uim-cluster-title">Modify Provider CORI</h3>
        <div className="uim-cluster-body">
          <div className="uim-form-grid">
            <UimField label="CORI Date" name="coriDate" value={form.coriDate} type="date" onChange={handleChange} />
            <UimField label="CORI End Date" name="coriEndDate" value={form.coriEndDate} type="date" onChange={handleChange} />
            <UimField label="Conviction or Release Date" name="convictionReleaseDate" value={form.convictionReleaseDate} type="date" onChange={handleChange} />
            <UimField label="Tier" name="tier" value={form.tier} type="select" options={CORI_TIER_OPTIONS} onChange={handleChange} />
          </div>

          <h4 style={{ color: 'var(--cdss-blue)', marginTop: '1rem', marginBottom: '0.5rem', fontWeight: 600, fontSize: '0.95rem' }}>
            General Exception
          </h4>
          <div className="uim-form-grid">
            <UimField label="GE Begin Date" name="geBeginDate" value={form.geBeginDate} type="date" onChange={handleChange} />
            <UimField label="GE End Date" name="geEndDate" value={form.geEndDate} type="date" onChange={handleChange} />
          </div>
          <div style={{ marginTop: '0.75rem' }}>
            <UimField label="GE Notes" name="geNotes" value={form.geNotes} type="textarea" onChange={handleChange} rows={3} />
          </div>

          <div className="uim-info-banner" style={{ marginTop: '0.75rem' }}>
            Adding a GE Begin Date sets Eligible=Yes. Adding a GE End Date sets Eligible=No.
          </div>

          {error && <div className="uim-error-banner" style={{ marginTop: '0.5rem' }}>{error}</div>}
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
