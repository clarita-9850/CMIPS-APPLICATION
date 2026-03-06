import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as providersApi from '../api/providersApi';
import { UimPageLayout } from '../shared/components/UimPageLayout';
import { UimSection } from '../shared/components/UimSection';
import { UimField } from '../shared/components/UimField';
import {
  COUNTY_OPTIONS, ELIGIBLE_OPTIONS, INELIGIBLE_REASON_OPTIONS,
  TITLE_OPTIONS, SUFFIX_OPTIONS, GENDER_OPTIONS, LANGUAGE_OPTIONS,
  PROVIDER_TYPE_OPTIONS, PHONE_TYPE_OPTIONS, ADDRESS_TYPE_OPTIONS,
  BLANK_SSN_REASON_OPTIONS, STATE_OPTIONS,
} from '../lib/providerConstants';

export const ProviderRegisterPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [validationErrors, setValidationErrors] = useState([]);

  const [form, setForm] = useState({
    // Name
    title: '',
    firstName: '',
    middleName: '',
    lastName: '',
    suffix: '',
    // Residence Address
    addressType: 'RESIDENTIAL',
    streetAddress: '',
    city: '',
    state: 'CA',
    zipCode: '',
    // Mailing Address
    sameAsResidence: true,
    mailingAddressType: 'MAILING',
    mailingStreetAddress: '',
    mailingCity: '',
    mailingState: 'CA',
    mailingZipCode: '',
    // Phone
    phoneType: '',
    phone: '',
    phoneExtension: '',
    email: '',
    // Details
    providerType: 'INDIVIDUAL_PROVIDER',
    ssn: '',
    blankSsnReason: '',
    dateSsnAppliedFor: '',
    spokenLanguage: '',
    otherSpokenLanguage: '',
    writtenLanguage: '',
    otherWrittenLanguage: '',
    gender: '',
    dateOfBirth: '',
    // Enrollment
    eligible: 'PENDING',
    ineligibleReason: '',
    soc426Completed: false,
    backgroundCheckCompleted: false,
    overtimeAgreementSigned: false,
    providerAgreementSigned: false,
    orientationCompleted: false,
    orientationDate: '',
    countyCode: '',
    effectiveDate: '',
  });

  // Pre-fill from URL query params (from duplicate check flow)
  useEffect(() => {
    const prefill = {};
    if (searchParams.get('firstName')) prefill.firstName = searchParams.get('firstName');
    if (searchParams.get('lastName')) prefill.lastName = searchParams.get('lastName');
    if (searchParams.get('ssn')) prefill.ssn = searchParams.get('ssn');
    if (searchParams.get('dob')) prefill.dateOfBirth = searchParams.get('dob');
    if (searchParams.get('gender')) prefill.gender = searchParams.get('gender');
    if (searchParams.get('county')) prefill.countyCode = searchParams.get('county');
    if (Object.keys(prefill).length > 0) {
      setForm(prev => ({ ...prev, ...prefill }));
    }
  }, [searchParams]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSave = () => {
    // Basic frontend validation
    const errors = [];
    if (!form.firstName.trim()) errors.push('First Name is required.');
    if (!form.lastName.trim()) errors.push('Last Name is required.');
    if (!form.gender) errors.push('Gender is required.');
    if (!form.dateOfBirth) errors.push('Date of Birth is required.');
    if (!form.spokenLanguage) errors.push('Spoken Language is required.');
    if (!form.writtenLanguage) errors.push('Written Language is required.');
    if (!form.countyCode) errors.push('Enrollment County is required.');
    if (!form.effectiveDate) errors.push('Effective Date is required.');

    if (errors.length > 0) {
      setError('Please fix the following errors:');
      setValidationErrors(errors.map(msg => ({ message: msg })));
      return;
    }

    setError('');
    setValidationErrors([]);
    setSaving(true);

    const payload = { ...form, createdBy: username };
    // Copy mailing if same as residence
    if (form.sameAsResidence) {
      payload.mailingStreetAddress = form.streetAddress;
      payload.mailingCity = form.city;
      payload.mailingState = form.state;
      payload.mailingZipCode = form.zipCode;
    }

    providersApi.createProvider(payload)
      .then(data => {
        navigate(`/providers/${data?.id || ''}`);
      })
      .catch(err => {
        const respData = err?.data || err?.response?.data;
        if (respData?.validationErrors?.length) {
          setValidationErrors(respData.validationErrors);
          setError('Please fix the following errors:');
        } else {
          setError(respData?.message || respData?.error || err.message || 'Failed to register provider');
        }
      })
      .finally(() => setSaving(false));
  };

  return (
    <UimPageLayout title="Create Provider" hidePlaceholderBanner={true}>
      {(error || validationErrors.length > 0) && (
        <div className="uim-error-banner">
          {error && <div style={{ fontWeight: 600, marginBottom: validationErrors.length ? '0.5rem' : 0 }}>{error}</div>}
          {validationErrors.length > 0 && (
            <ul style={{ margin: 0, paddingLeft: '1.25rem' }}>
              {validationErrors.map((ve, i) => (
                <li key={i} style={{ marginBottom: '0.25rem' }}>
                  {ve.errorCode && <span style={{ fontWeight: 600, marginRight: '0.5rem' }}>{ve.errorCode}:</span>}
                  {ve.message}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {/* Name Section */}
      <UimSection title="Name">
        <div className="uim-form-grid">
          <UimField label="Title" name="title" value={form.title} type="select" options={TITLE_OPTIONS} onChange={handleChange} />
          <UimField label="First Name" name="firstName" value={form.firstName} onChange={handleChange} required />
          <UimField label="Middle Name" name="middleName" value={form.middleName} onChange={handleChange} />
          <UimField label="Last Name" name="lastName" value={form.lastName} onChange={handleChange} required />
          <UimField label="Suffix" name="suffix" value={form.suffix} type="select" options={SUFFIX_OPTIONS} onChange={handleChange} />
        </div>
      </UimSection>

      {/* Residence Address Section */}
      <UimSection title="Residence Address">
        <div className="uim-form-grid">
          <UimField label="Address Type" name="addressType" value={form.addressType} type="select" options={ADDRESS_TYPE_OPTIONS} onChange={handleChange} />
          <UimField label="Street Address" name="streetAddress" value={form.streetAddress} onChange={handleChange} />
          <UimField label="City" name="city" value={form.city} onChange={handleChange} />
          <UimField label="State" name="state" value={form.state} type="select" options={STATE_OPTIONS} onChange={handleChange} />
          <UimField label="Zip Code" name="zipCode" value={form.zipCode} onChange={handleChange} maxLength={10} />
        </div>
      </UimSection>

      {/* Mailing Address Section */}
      <UimSection title="Mailing Address">
        <UimField label="Same as Residence" name="sameAsResidence" checked={form.sameAsResidence} type="checkbox" onChange={handleChange} />
        {!form.sameAsResidence && (
          <div className="uim-form-grid" style={{ marginTop: '0.75rem' }}>
            <UimField label="Address Type" name="mailingAddressType" value={form.mailingAddressType} type="select" options={ADDRESS_TYPE_OPTIONS} onChange={handleChange} />
            <UimField label="Street Address" name="mailingStreetAddress" value={form.mailingStreetAddress} onChange={handleChange} />
            <UimField label="City" name="mailingCity" value={form.mailingCity} onChange={handleChange} />
            <UimField label="State" name="mailingState" value={form.mailingState} type="select" options={STATE_OPTIONS} onChange={handleChange} />
            <UimField label="Zip Code" name="mailingZipCode" value={form.mailingZipCode} onChange={handleChange} maxLength={10} />
          </div>
        )}
      </UimSection>

      {/* Phone Section */}
      <UimSection title="Phone">
        <div className="uim-form-grid">
          <UimField label="Type" name="phoneType" value={form.phoneType} type="select" options={PHONE_TYPE_OPTIONS} onChange={handleChange} required />
          <UimField label="Phone" name="phone" value={form.phone} onChange={handleChange} required placeholder="(###) ###-####" />
          <UimField label="Extension" name="phoneExtension" value={form.phoneExtension} onChange={handleChange} />
          <UimField label="Email Address" name="email" value={form.email} type="email" onChange={handleChange} />
        </div>
      </UimSection>

      {/* Details Section */}
      <UimSection title="Details">
        <div className="uim-form-grid">
          <UimField label="Person Type" value="Provider" disabled />
          <UimField label="Provider Type" name="providerType" value={form.providerType} type="select" options={PROVIDER_TYPE_OPTIONS} onChange={handleChange} />
          <UimField label="SSN" name="ssn" value={form.ssn} onChange={handleChange} placeholder="###-##-####" />
          <UimField label="Blank SSN Reason" name="blankSsnReason" value={form.blankSsnReason} type="select" options={BLANK_SSN_REASON_OPTIONS} onChange={handleChange} />
          {form.blankSsnReason && (
            <UimField label="Date SSN Applied For" name="dateSsnAppliedFor" value={form.dateSsnAppliedFor} type="date" onChange={handleChange} />
          )}
          <UimField label="Date of Birth" name="dateOfBirth" value={form.dateOfBirth} type="date" onChange={handleChange} required />
          <UimField label="Gender" name="gender" value={form.gender} type="select" options={GENDER_OPTIONS} onChange={handleChange} required />
          <UimField label="Spoken Language" name="spokenLanguage" value={form.spokenLanguage} type="select" options={LANGUAGE_OPTIONS} onChange={handleChange} required />
          {form.spokenLanguage === 'OTHER' && (
            <UimField label="Other Spoken Language" name="otherSpokenLanguage" value={form.otherSpokenLanguage} onChange={handleChange} />
          )}
          <UimField label="Written Language" name="writtenLanguage" value={form.writtenLanguage} type="select" options={LANGUAGE_OPTIONS} onChange={handleChange} required />
          {form.writtenLanguage === 'OTHER' && (
            <UimField label="Other Written Language" name="otherWrittenLanguage" value={form.otherWrittenLanguage} onChange={handleChange} />
          )}
        </div>
      </UimSection>

      {/* Enrollment Section */}
      <UimSection title="Enrollment">
        <div className="uim-form-grid">
          <UimField label="Eligible" name="eligible" value={form.eligible} type="select" options={ELIGIBLE_OPTIONS} onChange={handleChange} required />
          {form.eligible === 'NO' && (
            <UimField label="Ineligible Reason" name="ineligibleReason" value={form.ineligibleReason} type="select" options={INELIGIBLE_REASON_OPTIONS} onChange={handleChange} required />
          )}
          <UimField label="Enrollment County" name="countyCode" value={form.countyCode} type="select" options={COUNTY_OPTIONS} onChange={handleChange} required />
          <UimField label="Effective Date" name="effectiveDate" value={form.effectiveDate} type="date" onChange={handleChange} required />
        </div>
        <div className="uim-form-grid" style={{ marginTop: '1rem' }}>
          <UimField label="SOC 426 - Provider Enrollment" name="soc426Completed" checked={form.soc426Completed} type="checkbox" onChange={handleChange} />
          <UimField label="DOJ Background Check" name="backgroundCheckCompleted" checked={form.backgroundCheckCompleted} type="checkbox" onChange={handleChange} />
          <UimField label="SOC 846 - Overtime Agreement" name="overtimeAgreementSigned" checked={form.overtimeAgreementSigned} type="checkbox" onChange={handleChange} />
          <UimField label="SOC 846 - Provider Agreement" name="providerAgreementSigned" checked={form.providerAgreementSigned} type="checkbox" onChange={handleChange} />
          <UimField label="Provider Orientation" name="orientationCompleted" checked={form.orientationCompleted} type="checkbox" onChange={handleChange} />
        </div>
        {form.orientationCompleted && (
          <div className="uim-form-grid" style={{ marginTop: '0.75rem' }}>
            <UimField label="Provider Orientation Date" name="orientationDate" value={form.orientationDate} type="date" onChange={handleChange} />
          </div>
        )}
      </UimSection>

      {/* Action Bar */}
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : 'Save'}
        </button>
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate('/providers')}>Cancel</button>
      </div>
    </UimPageLayout>
  );
};
