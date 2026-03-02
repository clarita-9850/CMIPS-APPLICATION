import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as recipientsApi from '../api/recipientsApi';
import AddressVerificationModal from '../components/AddressVerificationModal';
import './WorkQueues.css';

const REFERRAL_SOURCES = [
  'Self','Parent/Guardian','Legal Guardian','Spouse/Partner','Sibling','Other Relative',
  'Friend/Neighbor','Doctor/Physician','Hospital/Clinic','Social Worker','School',
  'Probation/Parole','Law Enforcement','Adult Protective Services','Child Protective Services',
  'Regional Center','Community Organization','Faith Organization','Mental Health Provider',
  'Substance Abuse Program','Home Health Agency','Nursing Home','Assisted Living',
  'Developmental Disability Program','Area Agency on Aging','Food Bank/Pantry',
  'Housing Authority','Veteran Services','Medicare','Medi-Cal','Insurance Company',
  'Employer','Employment Agency','Rehabilitation Program','Court/Judge',
  'Department of Labor','Department of Motor Vehicles','Other Government Agency','Anonymous','Unknown'
];

const COUNTIES = [
  'Alameda','Alpine','Amador','Butte','Calaveras','Colusa','Contra Costa','Del Norte',
  'El Dorado','Fresno','Glenn','Humboldt','Imperial','Inyo','Kern','Kings','Lake','Lassen',
  'Los Angeles','Madera','Marin','Mariposa','Mendocino','Merced','Modoc','Mono','Monterey',
  'Napa','Nevada','Orange','Placer','Plumas','Riverside','Sacramento','San Benito',
  'San Bernardino','San Diego','San Francisco','San Joaquin','San Luis Obispo','San Mateo',
  'Santa Barbara','Santa Clara','Santa Cruz','Shasta','Sierra','Siskiyou','Solano','Sonoma',
  'Stanislaus','Sutter','Tehama','Trinity','Tulare','Tuolumne','Ventura','Yolo','Yuba'
];

const LANGUAGES = [
  'English','Spanish','Cantonese','Mandarin','Vietnamese','Korean',
  'Armenian','Tagalog','Russian','Arabic','Farsi','Hmong',
  'Cambodian','Japanese','Laotian','Thai','Other'
];

const TITLE_OPTIONS = ['','Mr','Mrs','Ms','Miss','Dr','Rev','Hon','Other'];
const SUFFIX_OPTIONS = ['','Jr','Sr','I','II','III','IV','V','Esq','MD','PhD'];
const BLANK_SSN_REASONS = ['','Refused to Provide','Unable to Obtain','Applied for but not yet received','Non-citizen'];
const GENDER_IDENTITY_OPTIONS = ['','Male','Female','Non-Binary','Transgender Male','Transgender Female','Genderqueer','Not Listed','Decline to State'];
const SEXUAL_ORIENTATION_OPTIONS = ['','Straight','Gay','Lesbian','Bisexual','Queer','Not Listed','Decline to State'];
const RESIDENCE_ADDRESS_TYPES = ['','House','Apartment','Mobile Home','Hotel/Motel','Shelter','Homeless','Other'];

const today = () => new Date().toISOString().split('T')[0];

/**
 * PersonCreateReferralPage — Full DSD CI-67784 Create Referral form.
 * 5 sections: Referral Info | Demographics | Residence Address | Mailing Address | Phone/Contact
 * Business rules enforced: BR-1 (duplicate SSN block), BR-28/29/30 (UPPERCASE names), BR-20 (required fields)
 * Error messages: EM OS 001 through EM OS 080
 *
 * DSD alignment:
 * - Removed non-DSD fields: Referral Reason, Referring Worker, Referring Agency, Program Type, Assigned Worker
 * - Added DSD fields: Title, Suffix, Blank SSN Reason, Gender Identity, Sexual Orientation,
 *   Written Language, Other Language Details, Medi-Cal Pseudo Number, Residence Address Type,
 *   Meets Residency Requirements
 * - DOB and Gender are optional for Referral (required only for Application)
 * - EM OS 007: SSN + Blank SSN Reason mutual exclusion
 * - County auto-set from logged-in user
 */
export const PersonCreateReferralPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const userId = user?.sub || user?.username || 'unknown';
  const userCounty = user?.county || '';
  const prefill = location.state?.prefill || {};

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [showResAddrModal, setShowResAddrModal] = useState(false);
  const [showMailAddrModal, setShowMailAddrModal] = useState(false);
  const [sameAsResidence, setSameAsResidence] = useState(false);

  // Referral Information (Section 1) — DSD fields only
  const [referralDate, setReferralDate] = useState(today());
  const [referralSource, setReferralSource] = useState('');

  // Demographics (Section 2) — DSD CI-67784 fields
  const [title, setTitle] = useState('');
  const [lastName, setLastName] = useState(prefill.lastName || '');
  const [firstName, setFirstName] = useState(prefill.firstName || '');
  const [middleName, setMiddleName] = useState('');
  const [suffix, setSuffix] = useState('');
  const [dob, setDob] = useState(prefill.dob || '');
  const [gender, setGender] = useState('');
  const [genderIdentity, setGenderIdentity] = useState('');
  const [sexualOrientation, setSexualOrientation] = useState('');
  const [ssn, setSsn] = useState('');
  const [ssnDisplay, setSsnDisplay] = useState('');
  const [blankSsnReason, setBlankSsnReason] = useState('');
  const [language, setLanguage] = useState('English');
  const [otherSpokenLanguageDetail, setOtherSpokenLanguageDetail] = useState('');
  const [writtenLanguage, setWrittenLanguage] = useState('English');
  const [otherWrittenLanguageDetail, setOtherWrittenLanguageDetail] = useState('');
  const [ethnicity, setEthnicity] = useState('');
  const [mediCalPseudoNumber, setMediCalPseudoNumber] = useState('');

  // Residence Address (Section 3)
  const [resAddr, setResAddr] = useState({ streetNumber: '', streetName: '', unitType: '', unitNumber: '', city: '', state: 'CA', zip: '', cassMatch: null, cassUpdates: null, cassFailed: null });
  const [residenceAddressType, setResidenceAddressType] = useState('');
  const [meetsResidencyRequirements, setMeetsResidencyRequirements] = useState(false);

  // Mailing Address (Section 4)
  const [mailAddr, setMailAddr] = useState({ streetNumber: '', streetName: '', unitType: '', unitNumber: '', city: '', state: 'CA', zip: '', cassMatch: null, cassUpdates: null, cassFailed: null });

  // Phone/Contact (Section 5)
  const [homePhone, setHomePhone] = useState('');
  const [cellPhone, setCellPhone] = useState('');
  const [workPhone, setWorkPhone] = useState('');
  const [email, setEmail] = useState('');

  // County — auto-set from user, editable as fallback
  const [county, setCounty] = useState(userCounty || '');

  const handleSsnChange = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9);
    let masked = '';
    for (let i = 0; i < digits.length; i++) masked += i < 5 ? 'X' : digits[i];
    setSsnDisplay(masked);
    setSsn(digits);
    // Clear Blank SSN Reason if SSN is being entered (EM OS 007)
    if (digits.length > 0) setBlankSsnReason('');
  };

  const handleBlankSsnReasonChange = (val) => {
    setBlankSsnReason(val);
    // Clear SSN if Blank SSN Reason is selected (EM OS 007)
    if (val) { setSsn(''); setSsnDisplay(''); }
  };

  const validate = () => {
    const errs = {};
    if (!referralDate)    errs.referralDate   = 'EM OS 200: Referral Date is required';
    if (!referralSource)  errs.referralSource = 'EM OS 001: Referral Source is required';
    if (!lastName.trim()) errs.lastName  = 'EM OS 005: Last Name is required';
    if (!firstName.trim()) errs.firstName = 'EM OS 006: First Name is required';

    // DOB validation — optional for Referral, but validate if provided
    if (dob) {
      const dobDate = new Date(dob);
      const now = new Date();
      if (dobDate > now) errs.dob = 'EM OS 003: Date of Birth cannot be in the future';
      else if (now.getFullYear() - dobDate.getFullYear() > 120) errs.dob = 'EM OS 004: Date of Birth cannot be more than 120 years ago';
    }

    // EM OS 007: SSN + Blank SSN Reason mutual exclusion
    if (ssn.length > 0 && blankSsnReason) {
      errs.ssn = 'EM OS 007: SSN must be blank when Blank SSN Reason is indicated';
    }
    if (ssn.length > 0 && ssn.length < 9) errs.ssn = 'EM OS 010: SSN must be exactly 9 digits';
    if (ssn.length === 9 && ssn.startsWith('9')) errs.ssn = 'EM OS 010: SSN cannot begin with digit 9';
    if (ssn.length === 9 && /^(.)\1{8}$/.test(ssn)) errs.ssn = 'EM OS 010: SSN cannot consist of all identical digits';

    // Phone validation (EM OS 264/265 + EM-256)
    const validatePhone = (val, fieldName) => {
      if (val && val.trim()) {
        const digits = val.replace(/\D/g, '');
        if (digits.length !== 10) errs[fieldName] = 'EM OS 264/265: Phone number must be 10 digits (area code + 7-digit number)';
        else if (digits === '0000000000' || digits === '9999999999') errs[fieldName] = 'EM OS 256: Not a valid phone number. Please enter valid phone number.';
      }
    };
    validatePhone(homePhone, 'homePhone');
    validatePhone(cellPhone, 'cellPhone');
    validatePhone(workPhone, 'workPhone');

    // Email validation (EM OS 209-211, EM-216, EM-217)
    if (email && email.trim()) {
      const emailVal = email.trim();
      const atCount = (emailVal.match(/@/g) || []).length;
      if (atCount !== 1) { errs.email = 'EM OS 209: Not a valid email address. Please enter valid email address.'; }
      else {
        const [local, domain] = emailVal.split('@');
        if (local.length < 2) { errs.email = 'EM OS 210: Not a valid email address. Please enter valid email address.'; }
        else if (!domain || !domain.includes('.')) { errs.email = 'EM OS 211: Not a valid email address. Please enter valid email address.'; }
        else if (/\.\.|^\.|\.$/.test(domain)) { errs.email = 'EM OS 216: Not a valid email address. Please enter valid email address.'; }
        else if (/^[^a-zA-Z0-9]|[^a-zA-Z0-9]$/.test(local.replace(/[.+\-_]/g, 'x'))) { errs.email = 'EM OS 217: Not a valid email address. Please enter valid email address.'; }
      }
    }

    // EM OS 080: At least address (city + ZIP) or phone required
    const hasAddress = resAddr.city.trim() && resAddr.zip.trim();
    const hasPhone = (homePhone && homePhone.replace(/\D/g, '').length === 10) ||
                     (cellPhone && cellPhone.replace(/\D/g, '').length === 10) ||
                     (workPhone && workPhone.replace(/\D/g, '').length === 10);
    if (!hasAddress && !hasPhone) errs.resCity = 'EM OS 080: At least a Residence Address (city and ZIP) or a Phone Number is required';

    // EM-242/243: Other Language fields must contain only alpha characters
    if (language === 'Other' && otherSpokenLanguageDetail && /[^a-zA-Z\s\-']/.test(otherSpokenLanguageDetail)) {
      errs.otherSpokenLanguageDetail = 'EM OS 243: Other Spoken Language Details field allows only English language alpha characters.';
    }
    if (writtenLanguage === 'Other' && otherWrittenLanguageDetail && /[^a-zA-Z\s\-']/.test(otherWrittenLanguageDetail)) {
      errs.otherWrittenLanguageDetail = 'EM OS 242: Other Written Language Details field allows only English language alpha characters.';
    }

    if (!county) errs.county = 'EM OS 210: County is required';

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async () => {
    setError('');
    if (!validate()) {
      setError('Please correct the highlighted errors below.');
      return;
    }
    setSaving(true);
    try {
      const effectiveMailAddr = sameAsResidence ? resAddr : mailAddr;
      const payload = {
        personType: 'OPEN_REFERRAL',
        referralDate,
        referralSource,
        title: title || null,
        lastName: lastName.toUpperCase(),
        firstName: firstName.toUpperCase(),
        middleName: middleName ? middleName.toUpperCase() : null,
        suffix: suffix || null,
        dateOfBirth: dob || null,
        gender: gender || null,
        genderIdentity: genderIdentity || null,
        sexualOrientation: sexualOrientation || null,
        ssn: ssn || null,
        blankSsnReason: blankSsnReason || null,
        spokenLanguage: language,
        otherSpokenLanguageDetail: language === 'Other' ? otherSpokenLanguageDetail : null,
        writtenLanguage: writtenLanguage || null,
        otherWrittenLanguageDetail: writtenLanguage === 'Other' ? otherWrittenLanguageDetail : null,
        ethnicity: ethnicity || null,
        mediCalPseudo: mediCalPseudoNumber || null,
        residenceStreetNumber: resAddr.streetNumber,
        residenceStreetName:   resAddr.streetName,
        residenceUnitType:     resAddr.unitType,
        residenceUnitNumber:   resAddr.unitNumber,
        residenceCity:         resAddr.city,
        residenceState:        resAddr.state,
        residenceZip:          resAddr.zip,
        residenceCassMatch:    resAddr.cassMatch,
        residenceCassUpdates:  resAddr.cassUpdates,
        residenceCassFailed:   resAddr.cassFailed,
        residenceAddressType:  residenceAddressType || null,
        meetsResidencyRequirements: meetsResidencyRequirements || null,
        mailingStreetNumber:   effectiveMailAddr.streetNumber,
        mailingStreetName:     effectiveMailAddr.streetName,
        mailingUnitType:       effectiveMailAddr.unitType,
        mailingUnitNumber:     effectiveMailAddr.unitNumber,
        mailingCity:           effectiveMailAddr.city,
        mailingState:          effectiveMailAddr.state,
        mailingZip:            effectiveMailAddr.zip,
        mailingCassMatch:      effectiveMailAddr.cassMatch,
        mailingCassUpdates:    effectiveMailAddr.cassUpdates,
        mailingCassFailed:     effectiveMailAddr.cassFailed,
        primaryPhone:   homePhone  || null,
        secondaryPhone: cellPhone  || null,
        email:          email      || null,
        countyCode:  county,
        countyName:  county,
        createdBy:   userId,
      };
      const saved = await recipientsApi.createReferral(payload);
      navigate(`/recipients/${saved.id}`);
    } catch (err) {
      setError(err?.response?.data?.error || err.message || 'Failed to save referral');
    } finally {
      setSaving(false);
    }
  };

  const fe = fieldErrors;
  const errStyle = { color: '#c53030', fontSize: '0.75rem', marginTop: '0.2rem' };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Create Referral (CI-67784)</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/persons/search/referral')}>Cancel</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.75rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      {/* Section 1: Referral Information */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header"><h4>Section 1 — Referral Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Referral Date * {fe.referralDate && <span style={errStyle}>{fe.referralDate}</span>}</label>
              <input type="date" value={referralDate} onChange={e => setReferralDate(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Referral Source * {fe.referralSource && <span style={errStyle}>{fe.referralSource}</span>}</label>
              <select value={referralSource} onChange={e => setReferralSource(e.target.value)}>
                <option value="">Select Source...</option>
                {REFERRAL_SOURCES.map(s => <option key={s} value={s}>{s}</option>)}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Section 2: Demographics — DSD CI-67784 fields */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header"><h4>Section 2 — Person Demographics</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Title</label>
              <select value={title} onChange={e => setTitle(e.target.value)}>
                {TITLE_OPTIONS.map(t => <option key={t} value={t}>{t || 'Select...'}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>Last Name * {fe.lastName && <span style={errStyle}>{fe.lastName}</span>}</label>
              <input type="text" value={lastName} onChange={e => setLastName(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>First Name * {fe.firstName && <span style={errStyle}>{fe.firstName}</span>}</label>
              <input type="text" value={firstName} onChange={e => setFirstName(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Middle Name</label>
              <input type="text" value={middleName} onChange={e => setMiddleName(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Suffix</label>
              <select value={suffix} onChange={e => setSuffix(e.target.value)}>
                {SUFFIX_OPTIONS.map(s => <option key={s} value={s}>{s || 'Select...'}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>Date of Birth {fe.dob && <span style={errStyle}>{fe.dob}</span>}</label>
              <input type="date" value={dob} onChange={e => setDob(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Gender {fe.gender && <span style={errStyle}>{fe.gender}</span>}</label>
              <select value={gender} onChange={e => setGender(e.target.value)}>
                <option value="">Select...</option>
                <option value="MALE">Male</option><option value="FEMALE">Female</option>
                <option value="NON_BINARY">Non-Binary</option><option value="UNKNOWN">Unknown</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Gender Identity</label>
              <select value={genderIdentity} onChange={e => setGenderIdentity(e.target.value)}>
                {GENDER_IDENTITY_OPTIONS.map(g => <option key={g} value={g}>{g || 'Select...'}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>Sexual Orientation</label>
              <select value={sexualOrientation} onChange={e => setSexualOrientation(e.target.value)}>
                {SEXUAL_ORIENTATION_OPTIONS.map(s => <option key={s} value={s}>{s || 'Select...'}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>SSN (optional) {fe.ssn && <span style={errStyle}>{fe.ssn}</span>}</label>
              <input type="text" value={ssnDisplay} onChange={e => handleSsnChange(e.target.value)}
                placeholder="XXX-XX-####" maxLength={9} style={{ fontFamily: 'monospace' }}
                disabled={!!blankSsnReason} />
            </div>
            <div className="wq-form-field">
              <label>Blank SSN Reason {fe.blankSsnReason && <span style={errStyle}>{fe.blankSsnReason}</span>}</label>
              <select value={blankSsnReason} onChange={e => handleBlankSsnReasonChange(e.target.value)}
                disabled={ssn.length > 0}>
                {BLANK_SSN_REASONS.map(r => <option key={r} value={r}>{r || 'Select...'}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>Spoken Language</label>
              <select value={language} onChange={e => setLanguage(e.target.value)}>
                {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
            {language === 'Other' && (
              <div className="wq-form-field">
                <label>Other Spoken Language (specify)</label>
                <input type="text" value={otherSpokenLanguageDetail} onChange={e => setOtherSpokenLanguageDetail(e.target.value)} />
              </div>
            )}
            <div className="wq-form-field">
              <label>Written Language</label>
              <select value={writtenLanguage} onChange={e => setWrittenLanguage(e.target.value)}>
                {LANGUAGES.map(l => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
            {writtenLanguage === 'Other' && (
              <div className="wq-form-field">
                <label>Other Written Language (specify)</label>
                <input type="text" value={otherWrittenLanguageDetail} onChange={e => setOtherWrittenLanguageDetail(e.target.value)} />
              </div>
            )}
            <div className="wq-form-field">
              <label>Ethnicity</label>
              <select value={ethnicity} onChange={e => setEthnicity(e.target.value)}>
                <option value="">Select...</option>
                <option value="Hispanic/Latino">Hispanic/Latino</option>
                <option value="White">White</option>
                <option value="Black/African American">Black/African American</option>
                <option value="Asian">Asian</option>
                <option value="Pacific Islander">Pacific Islander</option>
                <option value="Native American">Native American</option>
                <option value="Multiracial">Multiracial</option>
                <option value="Other">Other</option>
                <option value="Decline to State">Decline to State</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Medi-Cal Pseudo Number</label>
              <input type="text" value={mediCalPseudoNumber} onChange={e => setMediCalPseudoNumber(e.target.value.slice(0, 14))}
                maxLength={14} placeholder="Up to 14 characters" />
            </div>
          </div>
        </div>
      </div>

      {/* Section 3: Residence Address */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h4>Section 3 — Residence Address</h4>
          <button className="wq-btn wq-btn-outline" onClick={() => setShowResAddrModal(true)}>
            {resAddr.cassMatch === true ? 'Verified' : resAddr.cassFailed ? 'Unverified — Edit' : 'Enter / Verify Address'}
          </button>
        </div>
        <div className="wq-panel-body">
          {resAddr.streetName ? (
            <div className="wq-detail-grid">
              <div className="wq-detail-row">
                <span className="wq-detail-label">Address:</span>
                <span className="wq-detail-value">
                  {[resAddr.streetNumber, resAddr.streetName, resAddr.unitType, resAddr.unitNumber].filter(Boolean).join(' ')}
                  {resAddr.cassMatch === true && <span style={{ marginLeft: '0.5rem', background: '#c6f6d5', color: '#276749', padding: '0.1rem 0.4rem', borderRadius: '10px', fontSize: '0.75rem' }}>CASS Verified</span>}
                  {resAddr.cassUpdates && <span style={{ marginLeft: '0.5rem', background: '#feebc8', color: '#c05621', padding: '0.1rem 0.4rem', borderRadius: '10px', fontSize: '0.75rem' }}>CASS Note</span>}
                  {resAddr.cassFailed === true && !resAddr.cassUpdates && <span style={{ marginLeft: '0.5rem', background: '#fed7d7', color: '#c53030', padding: '0.1rem 0.4rem', borderRadius: '10px', fontSize: '0.75rem' }}>Unverified</span>}
                </span>
              </div>
              <div className="wq-detail-row">
                <span className="wq-detail-label">City/State/ZIP:</span>
                <span className="wq-detail-value">{resAddr.city}, {resAddr.state} {resAddr.zip}</span>
              </div>
            </div>
          ) : (
            <p style={{ color: '#999', fontSize: '0.875rem' }}>No address entered. {fe.resCity && <span style={{ color: '#c53030' }}>{fe.resCity}</span>}</p>
          )}
          <div className="wq-search-grid" style={{ marginTop: '0.75rem' }}>
            <div className="wq-form-field">
              <label>Residence Address Type</label>
              <select value={residenceAddressType} onChange={e => setResidenceAddressType(e.target.value)}>
                {RESIDENCE_ADDRESS_TYPES.map(t => <option key={t} value={t}>{t || 'Select...'}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <input type="checkbox" checked={meetsResidencyRequirements}
                  onChange={e => setMeetsResidencyRequirements(e.target.checked)} />
                Meets Residency Requirements
              </label>
            </div>
          </div>
        </div>
      </div>

      {/* Section 4: Mailing Address */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h4>Section 4 — Mailing Address</h4>
          {!sameAsResidence && (
            <button className="wq-btn wq-btn-outline" onClick={() => setShowMailAddrModal(true)}>
              Enter / Verify Address
            </button>
          )}
        </div>
        <div className="wq-panel-body">
          <div className="wq-form-field" style={{ marginBottom: '0.75rem' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', fontWeight: 400 }}>
              <input type="checkbox" checked={sameAsResidence} onChange={e => setSameAsResidence(e.target.checked)} />
              Same as Residence Address
            </label>
          </div>
          {!sameAsResidence && mailAddr.streetName && (
            <div className="wq-detail-grid">
              <div className="wq-detail-row">
                <span className="wq-detail-label">Address:</span>
                <span className="wq-detail-value">{[mailAddr.streetNumber, mailAddr.streetName, mailAddr.unitType, mailAddr.unitNumber].filter(Boolean).join(' ')}</span>
              </div>
              <div className="wq-detail-row">
                <span className="wq-detail-label">City/State/ZIP:</span>
                <span className="wq-detail-value">{mailAddr.city}, {mailAddr.state} {mailAddr.zip}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Section 5: Phone / Contact */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header"><h4>Section 5 — Phone / Contact</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Home Phone {fe.homePhone && <span style={errStyle}>{fe.homePhone}</span>}</label>
              <input type="tel" value={homePhone} onChange={e => setHomePhone(e.target.value)} placeholder="(###) ###-####"
                style={fe.homePhone ? { borderColor: '#fc8181' } : {}} />
            </div>
            <div className="wq-form-field">
              <label>Cell Phone {fe.cellPhone && <span style={errStyle}>{fe.cellPhone}</span>}</label>
              <input type="tel" value={cellPhone} onChange={e => setCellPhone(e.target.value)} placeholder="(###) ###-####"
                style={fe.cellPhone ? { borderColor: '#fc8181' } : {}} />
            </div>
            <div className="wq-form-field">
              <label>Work Phone {fe.workPhone && <span style={errStyle}>{fe.workPhone}</span>}</label>
              <input type="tel" value={workPhone} onChange={e => setWorkPhone(e.target.value)} placeholder="(###) ###-####"
                style={fe.workPhone ? { borderColor: '#fc8181' } : {}} />
            </div>
            <div className="wq-form-field">
              <label>Email Address {fe.email && <span style={errStyle}>{fe.email}</span>}</label>
              <input type="email" value={email} onChange={e => setEmail(e.target.value)}
                style={fe.email ? { borderColor: '#fc8181' } : {}} />
            </div>
          </div>
        </div>
      </div>

      {/* County Selection */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header"><h4>County</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>County * {fe.county && <span style={errStyle}>{fe.county}</span>}</label>
              <select value={county} onChange={e => setCounty(e.target.value)}>
                <option value="">Select County...</option>
                {COUNTIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Submit Actions */}
      <div className="wq-search-actions" style={{ justifyContent: 'flex-end' }}>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/persons/search/referral')}>Cancel</button>
        <button className="wq-btn wq-btn-primary" onClick={handleSubmit} disabled={saving}>
          {saving ? 'Saving...' : 'Save Referral'}
        </button>
      </div>

      {/* Address Verification Modals */}
      <AddressVerificationModal
        isOpen={showResAddrModal}
        onClose={() => setShowResAddrModal(false)}
        onConfirm={addr => { setResAddr(addr); setShowResAddrModal(false); }}
        initialData={resAddr}
        title="Residence Address Verification"
      />
      <AddressVerificationModal
        isOpen={showMailAddrModal}
        onClose={() => setShowMailAddrModal(false)}
        onConfirm={addr => { setMailAddr(addr); setShowMailAddrModal(false); }}
        initialData={mailAddr}
        title="Mailing Address Verification"
      />
    </div>
  );
};
