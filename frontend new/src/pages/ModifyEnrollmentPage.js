import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as providersApi from '../api/providersApi';
import { UimPageLayout } from '../shared/components/UimPageLayout';
import { UimSection } from '../shared/components/UimSection';
import { UimField } from '../shared/components/UimField';
import {
  COUNTY_OPTIONS, ELIGIBLE_OPTIONS, INELIGIBLE_REASON_OPTIONS,
  APPEAL_STATUS_OPTIONS,
} from '../lib/providerConstants';

export const ModifyEnrollmentPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const userRoles = user?.roles || user?.realm_access?.roles || [];
  const isCDSS = userRoles.some(r => ['CDSSProgramMgmt', 'CDSSModify', 'ADMIN'].includes(r));

  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState([]);

  const [form, setForm] = useState({
    eligible: '',
    ineligibleReason: '',
    soc426Completed: false,
    backgroundCheckCompleted: false,
    overtimeAgreementSigned: false,
    providerAgreementSigned: false,
    orientationCompleted: false,
    orientationDate: '',
    effectiveDate: '',
    enrollmentBeginDate: '',
    enrollmentDueDate: '',
    countyCode: '',
    goodCauseExtension: false,
    appealStatus: '',
    appealDate: '',
    adminHearingDate: '',
    countyUse1: '',
    countyUse2: '',
    countyUse3: '',
    countyUse4: '',
  });

  useEffect(() => {
    if (!id) { setLoading(false); return; }
    providersApi.getProviderById(id)
      .then(data => {
        setProvider(data);
        setForm({
          eligible: data.eligible || '',
          ineligibleReason: data.ineligibleReason || '',
          soc426Completed: data.soc426Completed || false,
          backgroundCheckCompleted: data.backgroundCheckCompleted || false,
          overtimeAgreementSigned: data.overtimeAgreementSigned || false,
          providerAgreementSigned: data.providerAgreementSigned || false,
          orientationCompleted: data.orientationCompleted || false,
          orientationDate: data.orientationDate || '',
          effectiveDate: data.effectiveDate || '',
          enrollmentBeginDate: data.enrollmentBeginDate || '',
          enrollmentDueDate: data.enrollmentDueDate || '',
          countyCode: data.countyCode || '',
          goodCauseExtension: data.goodCauseExtension || false,
          appealStatus: data.appealStatus || '',
          appealDate: data.appealDate || '',
          adminHearingDate: data.adminHearingDate || '',
          countyUse1: data.countyUse1 || '',
          countyUse2: data.countyUse2 || '',
          countyUse3: data.countyUse3 || '',
          countyUse4: data.countyUse4 || '',
        });
      })
      .catch(() => setProvider(null))
      .finally(() => setLoading(false));
  }, [id]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(f => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSave = () => {
    setError('');
    setValidationErrors([]);
    setSaving(true);
    providersApi.modifyEnrollment(id, form)
      .then(() => navigate(`/providers/${id}`))
      .catch(err => {
        const resp = err?.response?.data || err?.data;
        if (resp?.validationErrors) {
          setValidationErrors(Array.isArray(resp.validationErrors) ? resp.validationErrors : []);
        }
        setError(resp?.error || resp?.message || err.message || 'Save failed');
      })
      .finally(() => setSaving(false));
  };

  if (loading) return <div className="uim-page"><div className="container"><p>Loading enrollment data...</p></div></div>;
  if (!provider) return (
    <div className="uim-page"><div className="container">
      <p>Provider not found.</p>
      <button className="uim-btn uim-btn-secondary" onClick={() => navigate('/providers')}>Back to Providers</button>
    </div></div>
  );

  const p = provider;
  const pageTitle = `Modify Enrollment: ${p.providerNumber || [p.lastName, p.firstName].filter(Boolean).join(', ') || p.id}`;

  return (
    <UimPageLayout title={pageTitle} hidePlaceholderBanner={true}>
      {error && (
        <div className="uim-error-banner">
          <strong>Error:</strong> {error}
          {validationErrors.length > 0 && (
            <ul style={{ margin: '0.5rem 0 0 1.25rem', padding: 0 }}>
              {validationErrors.map((ve, i) => <li key={i}>{typeof ve === 'string' ? ve : ve.message}</li>)}
            </ul>
          )}
        </div>
      )}

      {/* Enrollment Section */}
      <UimSection title="Enrollment">
        <div className="uim-form-grid">
          <UimField label="Eligible" name="eligible" value={form.eligible} type="select" options={ELIGIBLE_OPTIONS} onChange={handleChange} required />
          {form.eligible === 'NO' && (
            <UimField label="Ineligible Reason" name="ineligibleReason" value={form.ineligibleReason} type="select" options={INELIGIBLE_REASON_OPTIONS} onChange={handleChange} required />
          )}
          <UimField label="Effective Date" name="effectiveDate" value={form.effectiveDate} type="date" onChange={handleChange} required />
          <UimField label="Provider Enrollment Begin Date" name="enrollmentBeginDate" value={form.enrollmentBeginDate} type="date" onChange={handleChange} />
          <UimField label="Provider Enrollment Due Date" name="enrollmentDueDate" value={form.enrollmentDueDate} type="date" onChange={handleChange} />
          <UimField
            label="Enrollment County"
            name="countyCode"
            value={form.countyCode}
            type="select"
            options={COUNTY_OPTIONS}
            onChange={handleChange}
            disabled={!isCDSS}
          />
        </div>
        <div className="uim-form-grid" style={{ marginTop: '1rem' }}>
          <UimField label="SOC 426 - Provider Enrollment" name="soc426Completed" checked={form.soc426Completed} type="checkbox" onChange={handleChange} />
          <UimField label="DOJ Background Check" name="backgroundCheckCompleted" checked={form.backgroundCheckCompleted} type="checkbox" onChange={handleChange} />
          <UimField label="SOC 846 - Overtime Agreement" name="overtimeAgreementSigned" checked={form.overtimeAgreementSigned} type="checkbox" onChange={handleChange} />
          <UimField label="SOC 846 - Provider Agreement" name="providerAgreementSigned" checked={form.providerAgreementSigned} type="checkbox" onChange={handleChange} />
          <UimField label="Provider Orientation" name="orientationCompleted" checked={form.orientationCompleted} type="checkbox" onChange={handleChange} />
          <UimField label="Good Cause Extension" name="goodCauseExtension" checked={form.goodCauseExtension} type="checkbox" onChange={handleChange} />
        </div>
        {form.orientationCompleted && (
          <div className="uim-form-grid" style={{ marginTop: '0.75rem' }}>
            <UimField label="Provider Orientation Date" name="orientationDate" value={form.orientationDate} type="date" onChange={handleChange} />
          </div>
        )}
      </UimSection>

      {/* Appeals Section */}
      <UimSection title="Appeals">
        <div className="uim-form-grid">
          <UimField label="Appeal Status" name="appealStatus" value={form.appealStatus} type="select" options={APPEAL_STATUS_OPTIONS} onChange={handleChange} />
          <UimField label="Appeal Status Date" name="appealDate" value={form.appealDate} type="date" onChange={handleChange} />
          <UimField label="Admin Hearing Date" name="adminHearingDate" value={form.adminHearingDate} type="date" onChange={handleChange} />
        </div>
      </UimSection>

      {/* County Use Section */}
      <UimSection title="County Use">
        <div className="uim-form-grid">
          <UimField label="County Use 1" name="countyUse1" value={form.countyUse1} onChange={handleChange} />
          <UimField label="County Use 2" name="countyUse2" value={form.countyUse2} onChange={handleChange} />
          <UimField label="County Use 3" name="countyUse3" value={form.countyUse3} onChange={handleChange} />
          <UimField label="County Use 4" name="countyUse4" value={form.countyUse4} onChange={handleChange} />
        </div>
      </UimSection>

      {/* Action Bar */}
      <div className="uim-action-bar" style={{ justifyContent: 'flex-end' }}>
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(`/providers/${id}`)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : 'Save'}
        </button>
      </div>
    </UimPageLayout>
  );
};
