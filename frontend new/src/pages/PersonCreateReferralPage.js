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

const today = () => new Date().toISOString().split('T')[0];

/**
 * PersonCreateReferralPage — Full DSD CI-67784 Create Referral form.
 * 6 sections: Referral Info | Demographics | Residence Address | Mailing Address | Phone/Contact | Program Info
 * Business rules enforced: BR-1 (duplicate SSN block), BR-4 (Soundex warning), BR-20 (required fields)
 * Error messages: EM-200 through EM-210
 */
export const PersonCreateReferralPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const userId = user?.sub || user?.username || 'unknown';
  const prefill = location.state?.prefill || {};

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});
  const [showResAddrModal, setShowResAddrModal] = useState(false);
  const [showMailAddrModal, setShowMailAddrModal] = useState(false);
  const [sameAsResidence, setSameAsResidence] = useState(false);

  // Referral Information (Section 1)
  const [referralDate, setReferralDate] = useState(today());
  const [referralSource, setReferralSource] = useState('');
  const [referralReason, setReferralReason] = useState('');
  const [referringWorker, setReferringWorker] = useState('');
  const [referringAgency, setReferringAgency] = useState('');

  // Demographics (Section 2)
  const [lastName, setLastName] = useState(prefill.lastName || '');
  const [firstName, setFirstName] = useState(prefill.firstName || '');
  const [middleName, setMiddleName] = useState('');
  const [dob, setDob] = useState(prefill.dob || '');
  const [gender, setGender] = useState('');
  const [ssn, setSsn] = useState('');
  const [ssnDisplay, setSsnDisplay] = useState('');
  const [language, setLanguage] = useState('English');
  const [ethnicity, setEthnicity] = useState('');

  // Residence Address (Section 3)
  const [resAddr, setResAddr] = useState({ streetNumber: '', streetName: '', unitType: '', unitNumber: '', city: '', state: 'CA', zip: '', cassMatch: null, cassUpdates: null, cassFailed: null });

  // Mailing Address (Section 4)
  const [mailAddr, setMailAddr] = useState({ streetNumber: '', streetName: '', unitType: '', unitNumber: '', city: '', state: 'CA', zip: '', cassMatch: null, cassUpdates: null, cassFailed: null });

  // Phone/Contact (Section 5)
  const [homePhone, setHomePhone] = useState('');
  const [cellPhone, setCellPhone] = useState('');
  const [workPhone, setWorkPhone] = useState('');
  const [email, setEmail] = useState('');

  // Program Info (Section 6)
  const [programType, setProgramType] = useState('IHSS');
  const [county, setCounty] = useState('');
  const [assignedWorker, setAssignedWorker] = useState('');

  const handleSsnChange = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9);
    let masked = '';
    for (let i = 0; i < digits.length; i++) masked += i < 5 ? 'X' : digits[i];
    setSsnDisplay(masked);
    setSsn(digits);
  };

  const validate = () => {
    const errs = {};
    if (!referralDate)    errs.referralDate   = 'EM-200: Referral Date is required';
    if (!referralSource)  errs.referralSource = 'EM-201: Referral Source is required';
    if (!referralReason.trim()) errs.referralReason = 'EM-202: Referral Reason is required';
    if (!lastName.trim()) errs.lastName  = 'EM-205: Last Name is required';
    if (!firstName.trim()) errs.firstName = 'EM-206: First Name is required';
    if (dob) {
      const dobDate = new Date(dob);
      const now = new Date();
      if (dobDate > now) errs.dob = 'EM-203: Date of Birth cannot be in the future';
      else if (now.getFullYear() - dobDate.getFullYear() > 120) errs.dob = 'EM-204: Date of Birth cannot be more than 120 years ago';
    }
    if (!gender) errs.gender = 'EM-207: Gender is required';
    if (ssn.length > 0 && ssn.length < 9) errs.ssn = 'EM-240: SSN must be exactly 9 digits';
    if (ssn.length === 9 && ssn.startsWith('9')) errs.ssn = 'EM-237: SSN cannot begin with digit 9';
    if (ssn.length === 9 && /^(.)\1{8}$/.test(ssn)) errs.ssn = 'EM-238: SSN cannot consist of all identical digits';
    // Phone validation (EM-251/252)
    const validatePhone = (val, fieldName) => {
      if (val && val.trim()) {
        const digits = val.replace(/\D/g, '');
        if (digits.length !== 10) errs[fieldName] = 'EM-251/252: Phone number must be 10 digits (area code + 7-digit number)';
      }
    };
    validatePhone(homePhone, 'homePhone');
    validatePhone(cellPhone, 'cellPhone');
    validatePhone(workPhone, 'workPhone');
    // Email validation (EM-254)
    if (email && email.trim() && !/^[^@]+@[^@]+\.[^@]+$/.test(email)) {
      errs.email = 'EM-254: Not a valid email address';
    }
    // EM-208: At least address (city + ZIP) or phone required
    const hasAddress = resAddr.city.trim() && resAddr.zip.trim();
    const hasPhone = (homePhone && homePhone.replace(/\D/g, '').length === 10) ||
                     (cellPhone && cellPhone.replace(/\D/g, '').length === 10) ||
                     (workPhone && workPhone.replace(/\D/g, '').length === 10);
    if (!hasAddress && !hasPhone) errs.resCity = 'EM-208: At least a Residence Address (city and ZIP) or a Phone Number is required';
    if (hasAddress && !resAddr.city.trim()) errs.resCity = 'EM-208: Residence City is required';
    if (hasAddress && !resAddr.zip.trim())  errs.resZip  = 'EM-209: Residence ZIP is required';
    if (!county)              errs.county  = 'EM-210: County is required';
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
        referralReason,
        referringWorker,
        referringAgency,
        lastName: lastName.toUpperCase(),
        firstName: firstName.toUpperCase(),
        middleName: middleName ? middleName.toUpperCase() : null,
        dateOfBirth: dob || null,
        gender,
        ssn: ssn || null,
        spokenLanguage: language,
        ethnicity: ethnicity || null,
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
            <div className="wq-form-field" style={{ gridColumn: 'span 2' }}>
              <label>Referral Reason * {fe.referralReason && <span style={errStyle}>{fe.referralReason}</span>}</label>
              <textarea value={referralReason} onChange={e => setReferralReason(e.target.value)} rows={3}
                style={{ width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #cbd5e0', borderRadius: '4px', resize: 'vertical', boxSizing: 'border-box' }} />
            </div>
            <div className="wq-form-field">
              <label>Referring Worker</label>
              <input type="text" value={referringWorker} onChange={e => setReferringWorker(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Referring Agency</label>
              <input type="text" value={referringAgency} onChange={e => setReferringAgency(e.target.value)} />
            </div>
          </div>
        </div>
      </div>

      {/* Section 2: Demographics */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header"><h4>Section 2 — Person Demographics</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
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
              <label>Date of Birth {fe.dob && <span style={errStyle}>{fe.dob}</span>}</label>
              <input type="date" value={dob} onChange={e => setDob(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Gender * {fe.gender && <span style={errStyle}>{fe.gender}</span>}</label>
              <select value={gender} onChange={e => setGender(e.target.value)}>
                <option value="">Select...</option>
                <option value="MALE">Male</option><option value="FEMALE">Female</option>
                <option value="NON_BINARY">Non-Binary</option><option value="UNKNOWN">Unknown</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>SSN (optional) {fe.ssn && <span style={errStyle}>{fe.ssn}</span>}</label>
              <input type="text" value={ssnDisplay} onChange={e => handleSsnChange(e.target.value)}
                placeholder="XXX-XX-####" maxLength={9} style={{ fontFamily: 'monospace' }} />
            </div>
            <div className="wq-form-field">
              <label>Language</label>
              <select value={language} onChange={e => setLanguage(e.target.value)}>
                <option value="English">English</option><option value="Spanish">Spanish</option>
                <option value="Cantonese">Cantonese</option><option value="Mandarin">Mandarin</option>
                <option value="Vietnamese">Vietnamese</option><option value="Korean">Korean</option>
                <option value="Armenian">Armenian</option><option value="Tagalog">Tagalog</option>
                <option value="Russian">Russian</option><option value="Other">Other</option>
              </select>
            </div>
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

      {/* Section 6: Program Information */}
      <div className="wq-panel" style={{ marginBottom: '1rem' }}>
        <div className="wq-panel-header"><h4>Section 6 — Program Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Program Type</label>
              <select value={programType} onChange={e => setProgramType(e.target.value)}>
                <option value="IHSS">IHSS</option><option value="WAIVER">Waiver</option><option value="BOTH">Both</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>County * {fe.county && <span style={errStyle}>{fe.county}</span>}</label>
              <select value={county} onChange={e => setCounty(e.target.value)}>
                <option value="">Select County...</option>
                {COUNTIES.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div className="wq-form-field">
              <label>Assigned Worker</label>
              <input type="text" value={assignedWorker} onChange={e => setAssignedWorker(e.target.value)} placeholder="Worker ID or username" />
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
