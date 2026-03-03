import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as applicationsApi from '../api/applicationsApi';
import * as casesApi from '../api/casesApi';
import * as recipientsApi from '../api/recipientsApi';
import AddressVerificationModal from '../components/AddressVerificationModal';
import { CINSearchModal }          from '../components/cin/CINSearchModal';
import { MediCalEligibilityModal } from '../components/cin/MediCalEligibilityModal';
import { CINDataMismatchModal }    from '../components/cin/CINDataMismatchModal';
import { CreateCaseWithoutCINModal } from '../components/cin/CreateCaseWithoutCINModal';
import { UserSearchModal }         from '../components/UserSearchModal';
import './WorkQueues.css';

// ── PersonType badge ────────────────────────────────────────────────────────
const PERSON_TYPE_STYLES = {
  OPEN_REFERRAL:   { background: '#bee3f8', color: '#2b6cb0' },
  CLOSED_REFERRAL: { background: '#e2e8f0', color: '#4a5568' },
  APPLICANT:       { background: '#feebc8', color: '#c05621' },
  RECIPIENT:       { background: '#c6f6d5', color: '#276749' },
};
const PersonTypeBadge = ({ type }) => {
  if (!type) return <span style={{ color: '#999' }}>—</span>;
  const style = PERSON_TYPE_STYLES[type] || { background: '#e2e8f0', color: '#4a5568' };
  const label = type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  return (
    <span style={{ ...style, padding: '0.15rem 0.5rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600, whiteSpace: 'nowrap' }}>
      {label}
    </span>
  );
};

// ── CIN clearance badge ─────────────────────────────────────────────────────
const CIN_BADGE_STYLE = {
  NOT_STARTED:      { background: '#e2e8f0', color: '#4a5568' },
  IN_PROGRESS:      { background: '#ebf8ff', color: '#2b6cb0' },
  CLEARED:          { background: '#f0fff4', color: '#276749' },
  EXACT_MATCH:      { background: '#f0fff4', color: '#276749' },
  POSSIBLE_MATCHES: { background: '#fffbeb', color: '#975a16' },
  NO_MATCH:         { background: '#fff5f5', color: '#c53030' },
  MISMATCH_REVIEW:  { background: '#fff5f5', color: '#c53030' },
  FAILED:           { background: '#fff5f5', color: '#c53030' },
};
const CIN_BADGE_LABEL = {
  NOT_STARTED:      'Not Performed',
  IN_PROGRESS:      'In Progress',
  CLEARED:          'CIN Cleared',
  EXACT_MATCH:      'CIN Cleared',
  POSSIBLE_MATCHES: 'Matches Found',
  NO_MATCH:         'No CIN Match',
  MISMATCH_REVIEW:  'Mismatch – Review',
  FAILED:           'Failed',
};

const STEPS = ['Duplicate Check', 'Applicant Info', 'CIN Clearance', 'Review & Submit'];

// ── CA counties ─────────────────────────────────────────────────────────────
const CA_COUNTIES = [
  'Alameda','Alpine','Amador','Butte','Calaveras','Colusa','Contra Costa','Del Norte',
  'El Dorado','Fresno','Glenn','Humboldt','Imperial','Inyo','Kern','Kings','Lake',
  'Lassen','Los Angeles','Madera','Marin','Mariposa','Mendocino','Merced','Modoc',
  'Mono','Monterey','Napa','Nevada','Orange','Placer','Plumas','Riverside',
  'Sacramento','San Benito','San Bernardino','San Diego','San Francisco','San Joaquin',
  'San Luis Obispo','San Mateo','Santa Barbara','Santa Clara','Santa Cruz','Shasta',
  'Sierra','Siskiyou','Solano','Sonoma','Stanislaus','Sutter','Tehama','Trinity',
  'Tulare','Tuolumne','Ventura','Yolo','Yuba',
];

// ── SSN masking helper ──────────────────────────────────────────────────────
const maskSsn = (digits) => {
  let masked = '';
  for (let i = 0; i < digits.length; i++) masked += i < 5 ? 'X' : digits[i];
  return masked;
};

export const ApplicationsNewPage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const source            = searchParams.get('source') || 'new';
  const preloadRecipientId = searchParams.get('recipientId');

  // Start at Step 2 if pre-loading an existing person
  const initialStep = (source === 'existing' && preloadRecipientId) ? 2 : 1;
  const [step,        setStep]       = useState(initialStep);
  const [recipientId, setRecipientId] = useState(preloadRecipientId || null);
  const [loading,     setLoading]    = useState(false);
  const [saving,      setSaving]     = useState(false);
  const [error,       setError]      = useState('');

  // ── Step 1 state: Duplicate check ─────────────────────────────────────────
  const [dupForm, setDupForm] = useState({ lastName: '', firstName: '', dob: '', ssn: '' });
  const [dupDisplaySsn,  setDupDisplaySsn]  = useState('');
  const [dupResults,     setDupResults]     = useState([]);
  const [dupChecked,     setDupChecked]     = useState(false);
  const [dupChecking,    setDupChecking]    = useState(false);
  const [ssnExactMatch,  setSsnExactMatch]  = useState(false); // BR-1 block

  // ── Step 2 state: Demographics + addresses ────────────────────────────────
  const [form, setForm] = useState({
    lastName: '', firstName: '', middleName: '',
    title: '', suffix: '',
    dateOfBirth: '', gender: '', genderIdentity: '', sexualOrientation: '',
    ssn: '', blankSsnReason: '',
    spokenLanguage: '', otherSpokenLanguageDetail: '',
    writtenLanguage: '', otherWrittenLanguageDetail: '',
    ethnicity: '', mediCalPseudo: '',
    residenceAddressType: '', meetsResidencyRequirements: false,
    homePhone: '', cellPhone: '', workPhone: '', email: '',
    // Residence address
    residenceStreetNumber: '', residenceStreetName: '',
    residenceUnitType: '', residenceUnitNumber: '',
    residenceCity: '', residenceState: 'CA', residenceZip: '',
    residenceCassMatch: false, residenceCassUpdates: null, residenceCassFailed: false,
    // Mailing address
    sameAsResidence: false,
    mailingStreetNumber: '', mailingStreetName: '',
    mailingUnitType: '', mailingUnitNumber: '',
    mailingCity: '', mailingState: 'CA', mailingZip: '',
    mailingCassMatch: false, mailingCassUpdates: null, mailingCassFailed: false,
  });
  const [displaySsn,        setDisplaySsn]        = useState('');
  const [showResidenceModal, setShowResidenceModal] = useState(false);
  const [showMailingModal,   setShowMailingModal]   = useState(false);

  // ── Step 3 state: CIN clearance + case details ────────────────────────────
  const [cin,                   setCin]                   = useState('');
  const [cinClearancePerformed,  setCinClearancePerformed]  = useState(false);
  const [cinClearanceStatus,     setCinClearanceStatus]     = useState('NOT_STARTED');
  const [mediCalStatus,          setMediCalStatus]          = useState('');
  const [aidCode,                setAidCode]                = useState('');
  const [showCINSearch,          setShowCINSearch]          = useState(false);
  const [showEligibility,        setShowEligibility]        = useState(false);
  const [showMismatch,           setShowMismatch]           = useState(false);
  const [showWithoutCIN,         setShowWithoutCIN]         = useState(false);
  const [eligibilityData,        setEligibilityData]        = useState(null);
  const [caseForm, setCaseForm] = useState({
    programType: '', countyCode: '', caseOwnerId: '',
    caseOpenDate: new Date().toISOString().split('T')[0],
    writtenLanguage: '', interpreterAvailable: false,
    ihssReferralDate: new Date().toISOString().split('T')[0],  // DSD: default to today
  });

  // Worker Search (DSD CI-67746)
  const [selectedWorker, setSelectedWorker] = useState(null);
  const [showWorkerSearch, setShowWorkerSearch] = useState(false);

  // ── Load existing recipient if source=existing ────────────────────────────
  useEffect(() => {
    if (source === 'existing' && preloadRecipientId) {
      setLoading(true);
      recipientsApi.getRecipientById(preloadRecipientId)
        .then(r => {
          setForm(prev => ({
            ...prev,
            lastName:              r.lastName || '',
            firstName:             r.firstName || '',
            middleName:            r.middleName || '',
            dateOfBirth:           r.dateOfBirth ? r.dateOfBirth.split('T')[0] : '',
            gender:                r.gender || '',
            ssn:                   r.ssn || '',
            spokenLanguage:        r.spokenLanguage || '',
            ethnicity:             r.ethnicity || '',
            homePhone:             r.homePhone || r.phone || '',
            cellPhone:             r.cellPhone || '',
            email:                 r.email || '',
            residenceStreetNumber: r.residenceStreetNumber || '',
            residenceStreetName:   r.residenceStreetName || '',
            residenceCity:         r.residenceCity || '',
            residenceState:        r.residenceState || 'CA',
            residenceZip:          r.residenceZip || r.zipCode || '',
          }));
          if (r.ssn) {
            const digits = r.ssn.replace(/\D/g, '').slice(0, 9);
            setDisplaySsn(maskSsn(digits));
          }
          setCaseForm(prev => ({ ...prev, countyCode: r.countyCode || '' }));
        })
        .catch(() => {})
        .finally(() => setLoading(false));
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // ── Step 1 handlers ───────────────────────────────────────────────────────
  const handleDupSsnChange = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9);
    setDupDisplaySsn(maskSsn(digits));
    setDupForm(prev => ({ ...prev, ssn: digits }));
  };

  const handleDuplicateCheck = async () => {
    if (!dupForm.lastName.trim()) {
      setError('Last Name is required for duplicate check.');
      return;
    }
    setError('');
    setDupChecking(true);
    setDupChecked(false);
    setSsnExactMatch(false);
    try {
      const result = await applicationsApi.checkDuplicate({
        lastName:    dupForm.lastName,
        firstName:   dupForm.firstName,
        dateOfBirth: dupForm.dob,
        ssn:         dupForm.ssn,
      });
      const matches = Array.isArray(result) ? result : (result?.matches || []);
      setDupResults(matches);
      // BR-1: if any match is an exact SSN match, block "Continue as New"
      if (dupForm.ssn && matches.some(m => m.matchType === 'SSN_EXACT' || m.ssn === dupForm.ssn)) {
        setSsnExactMatch(true);
      }
    } catch {
      setDupResults([]);
    } finally {
      setDupChecking(false);
      setDupChecked(true);
    }
  };

  const handleSelectExisting = (r) => {
    setRecipientId(r.id);
    setForm(prev => ({
      ...prev,
      lastName:              r.lastName || '',
      firstName:             r.firstName || '',
      middleName:            r.middleName || '',
      dateOfBirth:           r.dateOfBirth ? r.dateOfBirth.split('T')[0] : '',
      gender:                r.gender || '',
      ssn:                   r.ssn || '',
      spokenLanguage:        r.spokenLanguage || '',
      homePhone:             r.homePhone || r.phone || '',
      email:                 r.email || '',
      residenceStreetNumber: r.residenceStreetNumber || '',
      residenceStreetName:   r.residenceStreetName || '',
      residenceCity:         r.residenceCity || '',
      residenceState:        r.residenceState || 'CA',
      residenceZip:          r.residenceZip || '',
    }));
    if (r.ssn) {
      const digits = r.ssn.replace(/\D/g, '').slice(0, 9);
      setDisplaySsn(maskSsn(digits));
    }
    setCaseForm(prev => ({ ...prev, countyCode: r.countyCode || '' }));
    setStep(2);
  };

  // ── Step 2 handlers ───────────────────────────────────────────────────────
  const handleFormChange = (field, value) => {
    // BR-14: changing demographics resets CIN clearance
    if (['lastName', 'firstName', 'gender', 'dateOfBirth'].includes(field)) {
      setCin('');
      setCinClearancePerformed(false);
      setCinClearanceStatus('NOT_STARTED');
      setMediCalStatus('');
      setAidCode('');
    }
    setForm(prev => ({ ...prev, [field]: value }));
  };

  const handleSsnChange = (raw) => {
    const digits = raw.replace(/\D/g, '').slice(0, 9);
    setDisplaySsn(maskSsn(digits));
    handleFormChange('ssn', digits);
  };

  const handleSameAsResidence = (checked) => {
    if (checked) {
      setForm(prev => ({
        ...prev,
        sameAsResidence:      true,
        mailingStreetNumber:  prev.residenceStreetNumber,
        mailingStreetName:    prev.residenceStreetName,
        mailingUnitType:      prev.residenceUnitType,
        mailingUnitNumber:    prev.residenceUnitNumber,
        mailingCity:          prev.residenceCity,
        mailingState:         prev.residenceState,
        mailingZip:           prev.residenceZip,
        mailingCassMatch:     prev.residenceCassMatch,
        mailingCassUpdates:   prev.residenceCassUpdates,
        mailingCassFailed:    prev.residenceCassFailed,
      }));
    } else {
      setForm(prev => ({
        ...prev,
        sameAsResidence:    false,
        mailingStreetNumber: '', mailingStreetName: '',
        mailingUnitType: '',    mailingUnitNumber: '',
        mailingCity: '',        mailingState: 'CA', mailingZip: '',
        mailingCassMatch: false, mailingCassUpdates: null, mailingCassFailed: false,
      }));
    }
  };

  const handleResidenceConfirm = (addr) => {
    setForm(prev => ({
      ...prev,
      residenceStreetNumber: addr.streetNumber || '',
      residenceStreetName:   addr.streetName   || '',
      residenceUnitType:     addr.unitType      || '',
      residenceUnitNumber:   addr.unitNumber    || '',
      residenceCity:         addr.city          || '',
      residenceState:        addr.state         || 'CA',
      residenceZip:          addr.zip           || '',
      residenceCassMatch:    addr.cassMatch     || false,
      residenceCassUpdates:  addr.cassUpdates   || null,
      residenceCassFailed:   addr.cassFailed    || false,
    }));
    setShowResidenceModal(false);
  };

  const handleMailingConfirm = (addr) => {
    setForm(prev => ({
      ...prev,
      mailingStreetNumber: addr.streetNumber || '',
      mailingStreetName:   addr.streetName   || '',
      mailingUnitType:     addr.unitType      || '',
      mailingUnitNumber:   addr.unitNumber    || '',
      mailingCity:         addr.city          || '',
      mailingState:        addr.state         || 'CA',
      mailingZip:          addr.zip           || '',
      mailingCassMatch:    addr.cassMatch     || false,
      mailingCassUpdates:  addr.cassUpdates   || null,
      mailingCassFailed:   addr.cassFailed    || false,
    }));
    setShowMailingModal(false);
  };

  const validateStep2 = () => {
    if (!form.lastName.trim())  { setError('EM OS 005: Last Name is required.'); return false; }
    if (!form.firstName.trim()) { setError('EM OS 006: First Name is required.'); return false; }
    // EM OS 003/004: DOB required for Application
    if (!form.dateOfBirth) { setError('EM OS 003: Date of Birth must be indicated for Application.'); return false; }
    if (form.dateOfBirth) {
      const dob = new Date(form.dateOfBirth);
      const now = new Date();
      if (dob > now) { setError('EM OS 003: Date of Birth cannot be in the future.'); return false; }
      if (now.getFullYear() - dob.getFullYear() > 120) { setError('EM OS 004: Date of Birth cannot be more than 120 years ago.'); return false; }
    }
    // EM OS 008: Gender required for Application
    if (!form.gender) { setError('EM OS 008: Gender must be indicated.'); return false; }
    // EM OS 013: Either SSN or Blank SSN Reason must be indicated for Application
    if (!form.ssn && !form.blankSsnReason) {
      setError('EM OS 013: Either SSN or Blank SSN Reason must be indicated.'); return false;
    }
    // EM OS 007: SSN + Blank SSN Reason mutual exclusion
    if (form.ssn && form.blankSsnReason) {
      setError('EM OS 007: SSN must be blank when Blank SSN Reason is indicated.'); return false;
    }
    if (form.ssn && form.ssn.replace(/\D/g, '').length !== 9) {
      setError('EM OS 010: SSN must be 9 digits.'); return false;
    }
    if (form.ssn && /^(.)\1{8}$/.test(form.ssn.replace(/\D/g, ''))) {
      setError('EM OS 010: Invalid SSN — all same digits.'); return false;
    }
    if (form.ssn && form.ssn.replace(/\D/g, '').startsWith('9')) {
      setError('EM OS 010: SSN cannot begin with 9.'); return false;
    }
    // EM OS 009: Ethnicity required for Application
    if (!form.ethnicity) { setError('EM OS 009: Ethnicity must be indicated.'); return false; }
    // EM OS 011: Spoken Language required for Application
    if (!form.spokenLanguage) { setError('EM OS 011: Spoken Language must be indicated.'); return false; }
    // EM OS 018: Mailing Address or Same as Residence must be indicated
    if (!form.sameAsResidence && !form.mailingStreetName.trim()) {
      setError('EM OS 018: Mailing Address or Same as Residence Address must be indicated.'); return false;
    }
    // Phone validation (EM OS 264/265 + EM-258)
    const phoneFields = [
      { val: form.homePhone, label: 'Home Phone' },
      { val: form.cellPhone, label: 'Cell Phone' },
      { val: form.workPhone, label: 'Work Phone' },
    ];
    for (const pf of phoneFields) {
      if (pf.val && pf.val.trim()) {
        const digits = pf.val.replace(/\D/g, '');
        if (digits.length !== 10) {
          setError(`EM OS 264/265: ${pf.label} must be 10 digits (area code + 7-digit number).`);
          return false;
        }
        if (digits === '0000000000' || digits === '9999999999') {
          setError(`EM OS 258: ${pf.label} is not a valid phone number. Please enter valid phone number.`);
          return false;
        }
      }
    }
    // Email validation (EM OS 209-211, EM-216, EM-217)
    if (form.email && form.email.trim()) {
      const emailVal = form.email.trim();
      const atCount = (emailVal.match(/@/g) || []).length;
      if (atCount !== 1) { setError('EM OS 209: Not a valid email address. Please enter valid email address.'); return false; }
      const [local, domain] = emailVal.split('@');
      if (local.length < 2) { setError('EM OS 210: Not a valid email address. Please enter valid email address.'); return false; }
      if (!domain || !domain.includes('.')) { setError('EM OS 211: Not a valid email address. Please enter valid email address.'); return false; }
      if (/\.\.|^\.|\.$/.test(domain)) { setError('EM OS 216: Not a valid email address. Please enter valid email address.'); return false; }
    }
    // EM-242/243: Other Language fields must contain only alpha characters
    if (form.spokenLanguage === 'Other' && form.otherSpokenLanguageDetail && /[^a-zA-Z\s\-']/.test(form.otherSpokenLanguageDetail)) {
      setError('EM OS 243: Other Spoken Language Details field allows only English language alpha characters.'); return false;
    }
    if (form.writtenLanguage === 'Other' && form.otherWrittenLanguageDetail && /[^a-zA-Z\s\-']/.test(form.otherWrittenLanguageDetail)) {
      setError('EM OS 242: Other Written Language Details field allows only English language alpha characters.'); return false;
    }
    return true;
  };

  const goToStep3 = () => {
    setError('');
    if (!validateStep2()) return;
    setStep(3);
  };

  // ── Step 3 CIN modal handlers (same pattern as CaseCreatePage.js) ─────────
  const handleCINLookup = () => {
    if (!form.lastName.trim())  { setError('Last Name is required before CIN Clearance.'); return; }
    if (!form.firstName.trim()) { setError('First Name is required before CIN Clearance.'); return; }
    setError('');
    setCinClearanceStatus('IN_PROGRESS');
    setShowCINSearch(true);
  };

  const handleShowEligibility = (cinValue, data) => {
    setEligibilityData(data);
    setShowCINSearch(false);
    setShowEligibility(true);
  };

  const handleCINSearchCancel = () => {
    setShowCINSearch(false);
    setCinClearancePerformed(true);
    setCinClearanceStatus('NO_MATCH');
  };

  const handleSelectSuccess = ({ cin: selectedCin, mediCalStatus: ms, aidCode: ac }) => {
    setCin(selectedCin);
    setMediCalStatus(ms);
    setAidCode(ac);
    setCinClearancePerformed(true);
    setCinClearanceStatus('CLEARED');
    setShowEligibility(false);
    setEligibilityData(null);
  };

  const handleEligibilityMismatch = () => {
    setShowEligibility(false);
    setCinClearanceStatus('MISMATCH_REVIEW');
    setShowMismatch(true);
  };

  const handleEligibilityCancel = () => {
    setShowEligibility(false);
    setShowCINSearch(true);
  };

  const handleReturnToCINSelect = () => {
    setShowMismatch(false);
    setShowCINSearch(true);
    setCinClearanceStatus('IN_PROGRESS');
  };

  const validateStep3 = () => {
    if (!caseForm.countyCode.trim()) { setError('EM OS 210: County is required.'); return false; }
    // EM OS 175: IHSS Referral Date may not be more than 2 weeks in the future
    // (EM-175 strict no-future rule was CANCELLED in ASR Sprint 43; DSD screen design allows post-dating up to 2 weeks)
    if (caseForm.ihssReferralDate) {
      const refDate = new Date(caseForm.ihssReferralDate + 'T00:00:00');
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const maxDate = new Date(today);
      maxDate.setDate(maxDate.getDate() + 14);
      if (refDate > maxDate) {
        setError('EM OS 175: IHSS Referral Date may not be more than two weeks in the future.');
        return false;
      }
    }
    return true;
  };

  const goToStep4 = () => {
    setError('');
    if (!validateStep3()) return;
    setStep(4);
  };

  // ── Step 4 submit ─────────────────────────────────────────────────────────
  const buildApplicationPayload = () => ({
    recipientId:  recipientId || undefined,
    title:        form.title || null,
    lastName:     form.lastName,
    firstName:    form.firstName,
    middleName:   form.middleName,
    suffix:       form.suffix || null,
    dateOfBirth:  form.dateOfBirth,
    gender:       form.gender,
    genderIdentity:     form.genderIdentity || null,
    sexualOrientation:  form.sexualOrientation || null,
    ssn:          form.ssn,
    blankSsnReason:     form.blankSsnReason || null,
    spokenLanguage:  form.spokenLanguage,
    otherSpokenLanguageDetail: form.spokenLanguage === 'Other' ? form.otherSpokenLanguageDetail : null,
    writtenLanguage: form.writtenLanguage || null,
    otherWrittenLanguageDetail: form.writtenLanguage === 'Other' ? form.otherWrittenLanguageDetail : null,
    ethnicity:    form.ethnicity,
    mediCalPseudo: form.mediCalPseudo || null,
    residenceAddressType: form.residenceAddressType || null,
    meetsResidencyRequirements: form.meetsResidencyRequirements || null,
    homePhone:    form.homePhone,
    cellPhone:    form.cellPhone,
    workPhone:    form.workPhone,
    email:        form.email,
    // Residence
    residenceStreetNumber: form.residenceStreetNumber,
    residenceStreetName:   form.residenceStreetName,
    residenceUnitType:     form.residenceUnitType,
    residenceUnitNumber:   form.residenceUnitNumber,
    residenceCity:         form.residenceCity,
    residenceState:        form.residenceState,
    residenceZip:          form.residenceZip,
    residenceCassMatch:    form.residenceCassMatch,
    // Mailing
    mailingStreetNumber:   form.sameAsResidence ? form.residenceStreetNumber : form.mailingStreetNumber,
    mailingStreetName:     form.sameAsResidence ? form.residenceStreetName   : form.mailingStreetName,
    mailingCity:           form.sameAsResidence ? form.residenceCity         : form.mailingCity,
    mailingState:          form.sameAsResidence ? form.residenceState        : form.mailingState,
    mailingZip:            form.sameAsResidence ? form.residenceZip          : form.mailingZip,
    mailingCassMatch:      form.sameAsResidence ? form.residenceCassMatch    : form.mailingCassMatch,
    // Case fields
    cin:                 cin,
    countyCode:          caseForm.countyCode,
    programType:         caseForm.programType,
    caseOwnerId:         caseForm.caseOwnerId,
    spokenLanguage:      form.spokenLanguage,
    writtenLanguage:     caseForm.writtenLanguage,
    interpreterAvailable: caseForm.interpreterAvailable,
    ihssReferralDate:    caseForm.ihssReferralDate,
    cinClearanceStatus,
    mediCalStatus,
    aidCode,
    createdBy:           username,
  });

  const handleSaveApplicationOnly = async () => {
    setError('');
    if (!validateStep2() || !validateStep3()) return;
    setSaving(true);
    try {
      const appData = await applicationsApi.createApplication(buildApplicationPayload());
      navigate(`/recipients/${appData.recipientId || recipientId || ''}`);
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Failed to save application.');
    } finally {
      setSaving(false);
    }
  };

  const handleCreateApplicationAndCase = async () => {
    setError('');
    if (!validateStep2() || !validateStep3()) return;

    // EM OS 176: CIN clearance must be performed
    if (!cinClearancePerformed) {
      setError('EM OS 176: CIN Clearance must be performed before creating a case. Click 🔍 in Step 3.');
      return;
    }

    // No CIN found → show BR-9 modal
    if (!cin.trim()) {
      setShowWithoutCIN(true);
      return;
    }

    setSaving(true);
    try {
      const appData = await applicationsApi.createApplication(buildApplicationPayload());
      const casePayload = {
        applicationId: appData.id,
        recipientId:   appData.recipientId || recipientId,
        cin,
        lastName:      form.lastName,
        firstName:     form.firstName,
        gender:        form.gender,
        dateOfBirth:   form.dateOfBirth,
        ssn:           form.ssn,
        countyCode:    caseForm.countyCode,
        zipCode:       form.residenceZip,
        programType:   caseForm.programType,
        caseOwnerId:   caseForm.caseOwnerId,
        spokenLanguage:  form.spokenLanguage,
        writtenLanguage: caseForm.writtenLanguage,
        interpreterAvailable: caseForm.interpreterAvailable,
        ihssReferralDate: caseForm.ihssReferralDate,
        cinClearanceStatus,
        mediCalStatus,
        aidCode,
        createdBy:     username,
        applicantName: `${form.firstName} ${form.lastName}`.trim(),
      };
      const caseData = await casesApi.createCase(casePayload);
      const caseId = caseData?.id || caseData?.case?.id || '';
      if (caseData?.infoMessage) {
        sessionStorage.setItem('caseInfoMessage', caseData.infoMessage);
      }
      navigate(`/cases/${caseId}`);
    } catch (err) {
      setError(err?.response?.data?.error || err?.response?.data?.message || err.message || 'Failed to create application and case.');
    } finally {
      setSaving(false);
    }
  };

  // ── Worker Search select handler (DSD CI-67746) ─────────────────────────────
  const handleWorkerSelect = (worker) => {
    setSelectedWorker(worker);
    setCaseForm(prev => ({ ...prev, caseOwnerId: worker.workerNumber }));
    setShowWorkerSearch(false);
  };

  // ── CreateCaseWithoutCINModal handler (BR-9) ──────────────────────────────
  const handleWithoutCINContinue = async () => {
    setShowWithoutCIN(false);
    setSaving(true);
    try {
      const appData = await applicationsApi.createApplication(buildApplicationPayload());
      const caseData = await casesApi.createCase({
        applicationId: appData.id,
        recipientId:   appData.recipientId || recipientId,
        lastName:      form.lastName,
        firstName:     form.firstName,
        gender:        form.gender,
        dateOfBirth:   form.dateOfBirth,
        ssn:           form.ssn,
        countyCode:    caseForm.countyCode,
        zipCode:       form.residenceZip,
        programType:   caseForm.programType,
        caseOwnerId:   caseForm.caseOwnerId,
        spokenLanguage:  form.spokenLanguage,
        writtenLanguage: caseForm.writtenLanguage,
        interpreterAvailable: caseForm.interpreterAvailable,
        ihssReferralDate: caseForm.ihssReferralDate,
        cinClearanceStatus,
        mediCalStatus: 'PENDING_SAWS',
        createdBy:     username,
        applicantName: `${form.firstName} ${form.lastName}`.trim(),
      });
      const caseId = caseData?.id || caseData?.case?.id || '';
      if (caseData?.infoMessage) {
        sessionStorage.setItem('caseInfoMessage', caseData.infoMessage);
      }
      navigate(`/cases/${caseId}`);
    } catch (err) {
      setError(err?.response?.data?.error || err?.response?.data?.message || err.message || 'Failed to create case.');
    } finally {
      setSaving(false);
    }
  };

  // ── Helpers ───────────────────────────────────────────────────────────────
  const cinBadge = CIN_BADGE_STYLE[cinClearanceStatus] || CIN_BADGE_STYLE.NOT_STARTED;
  const cinLabel = CIN_BADGE_LABEL[cinClearanceStatus] || cinClearanceStatus;

  const applicantData = {
    lastName:      form.lastName,
    firstName:     form.firstName,
    dob:           form.dateOfBirth,
    gender:        form.gender,
    cin,
    ssn:           form.ssn,
    mediCalPseudo: false,
  };

  const residenceLabel = [
    form.residenceStreetNumber, form.residenceStreetName,
    form.residenceUnitType && form.residenceUnitNumber ? `${form.residenceUnitType} ${form.residenceUnitNumber}` : '',
    form.residenceCity, form.residenceState, form.residenceZip,
  ].filter(Boolean).join(' ') || '(not entered)';

  const mailingLabel = form.sameAsResidence ? '(Same as Residence)' : [
    form.mailingStreetNumber, form.mailingStreetName,
    form.mailingCity, form.mailingState, form.mailingZip,
  ].filter(Boolean).join(' ') || '(not entered)';

  // ── Render ─────────────────────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="wq-page">
        <div className="wq-page-header"><h2>New Application</h2></div>
        <div style={{ padding: '2rem', textAlign: 'center', color: '#4a5568' }}>Loading applicant data...</div>
      </div>
    );
  }

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>New Application</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Cancel</button>
      </div>

      {/* Step indicator */}
      <div style={{ display: 'flex', gap: '0.4rem', marginBottom: '1.25rem' }}>
        {STEPS.map((label, i) => {
          const idx = i + 1;
          const isActive    = step === idx;
          const isCompleted = step > idx;
          return (
            <div key={i} style={{
              flex: 1, padding: '0.5rem 0.25rem', textAlign: 'center', borderRadius: '4px',
              fontSize: '0.78rem', fontWeight: isActive ? 600 : 400,
              background: isActive ? '#153554' : isCompleted ? '#c6f6d5' : '#e2e8f0',
              color:      isActive ? '#fff'    : isCompleted ? '#276749' : '#666',
              border:     isActive ? 'none'    : isCompleted ? '1px solid #9ae6b4' : '1px solid #cbd5e0',
            }}>
              {isCompleted ? '✓ ' : ''}{label}
            </div>
          );
        })}
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      {/* ════════════════════════════════════════════════════════════════════
          STEP 1 — Duplicate Check
      ════════════════════════════════════════════════════════════════════ */}
      {step === 1 && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Step 1: Duplicate Check</h4></div>
          <div className="wq-panel-body">
            <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '4px', padding: '0.5rem 1rem', marginBottom: '1rem', fontSize: '0.8rem', color: '#1e40af' }}>
              Search for an existing person to avoid creating duplicates. Select a match to pre-fill the form, or proceed to create a new applicant.
            </div>
            <div className="wq-search-grid">
              <div className="wq-form-field">
                <label>Last Name *</label>
                <input type="text" value={dupForm.lastName}
                  onChange={e => setDupForm(prev => ({ ...prev, lastName: e.target.value }))}
                  onKeyDown={e => e.key === 'Enter' && handleDuplicateCheck()}
                  autoFocus />
              </div>
              <div className="wq-form-field">
                <label>First Name</label>
                <input type="text" value={dupForm.firstName}
                  onChange={e => setDupForm(prev => ({ ...prev, firstName: e.target.value }))}
                  onKeyDown={e => e.key === 'Enter' && handleDuplicateCheck()} />
              </div>
              <div className="wq-form-field">
                <label>Date of Birth</label>
                <input type="date" value={dupForm.dob}
                  onChange={e => setDupForm(prev => ({ ...prev, dob: e.target.value }))} />
              </div>
              <div className="wq-form-field">
                <label>SSN (masked)</label>
                <input type="text" value={dupDisplaySsn}
                  onChange={e => handleDupSsnChange(e.target.value)}
                  placeholder="XXX-XX-####" maxLength={9}
                  style={{ fontFamily: 'monospace' }} />
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleDuplicateCheck} disabled={dupChecking}>
                {dupChecking ? 'Checking...' : 'Check for Duplicates'}
              </button>
            </div>

            {dupChecked && (
              <div style={{ marginTop: '1rem' }}>
                {dupResults.length === 0 ? (
                  <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.75rem', borderRadius: '4px', color: '#276749', fontSize: '0.875rem' }}>
                    No duplicate records found. Click "Continue as New" to proceed.
                  </div>
                ) : (
                  <>
                    <div style={{ background: '#fffbeb', border: '1px solid #f6ad55', padding: '0.6rem 1rem', borderRadius: '4px', color: '#c05621', fontSize: '0.8rem', marginBottom: '0.75rem' }}>
                      {dupResults.length} potential match(es) found. Select an existing record to avoid duplicates, or continue as new.
                    </div>
                    <table className="wq-table">
                      <thead>
                        <tr><th>Name</th><th>DOB</th><th>CIN</th><th>County</th><th>Person Type</th><th>Action</th></tr>
                      </thead>
                      <tbody>
                        {dupResults.map((r, i) => (
                          <tr key={r.id || i}>
                            <td><strong>{[r.lastName, r.firstName].filter(Boolean).join(', ')}</strong></td>
                            <td>{r.dateOfBirth ? new Date(r.dateOfBirth).toLocaleDateString() : '—'}</td>
                            <td>{r.cin || '—'}</td>
                            <td>{r.countyCode || '—'}</td>
                            <td><PersonTypeBadge type={r.personType} /></td>
                            <td>
                              <button className="wq-btn wq-btn-outline"
                                onClick={() => handleSelectExisting(r)}>
                                Use This Person
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </>
                )}

                {ssnExactMatch ? (
                  <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.75rem 1rem', borderRadius: '4px', color: '#c53030', fontSize: '0.875rem', marginTop: '0.75rem' }}>
                    <strong>EM OS 010:</strong> An exact SSN match was found. You must use the existing person record. Select "Use This Person" above.
                  </div>
                ) : (
                  <div className="wq-search-actions" style={{ marginTop: '0.75rem' }}>
                    <button className="wq-btn wq-btn-primary" onClick={() => { setRecipientId(null); setStep(2); }}>
                      Continue as New Applicant
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      )}

      {/* ════════════════════════════════════════════════════════════════════
          STEP 2 — Applicant Demographics + Addresses
      ════════════════════════════════════════════════════════════════════ */}
      {step === 2 && (
        <>
          {source === 'existing' && preloadRecipientId && (
            <div style={{ background: '#f0fff4', border: '1px solid #9ae6b4', borderRadius: '4px', padding: '0.5rem 1rem', marginBottom: '1rem', fontSize: '0.8rem', color: '#276749' }}>
              Pre-filled from existing record. Review and update as needed before continuing.
            </div>
          )}

          {/* Section: Demographics — DSD CI-67788 */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Applicant Demographics</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Title</label>
                  <select value={form.title || ''} onChange={e => handleFormChange('title', e.target.value)}>
                    <option value="">Select...</option>
                    <option value="Mr">Mr</option><option value="Mrs">Mrs</option>
                    <option value="Ms">Ms</option><option value="Miss">Miss</option>
                    <option value="Dr">Dr</option><option value="Rev">Rev</option>
                    <option value="Hon">Hon</option><option value="Other">Other</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Last Name *</label>
                  <input type="text" value={form.lastName}
                    onChange={e => handleFormChange('lastName', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>First Name *</label>
                  <input type="text" value={form.firstName}
                    onChange={e => handleFormChange('firstName', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>Middle Name</label>
                  <input type="text" value={form.middleName}
                    onChange={e => handleFormChange('middleName', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>Suffix</label>
                  <select value={form.suffix || ''} onChange={e => handleFormChange('suffix', e.target.value)}>
                    <option value="">Select...</option>
                    <option value="Jr">Jr</option><option value="Sr">Sr</option>
                    <option value="I">I</option><option value="II">II</option>
                    <option value="III">III</option><option value="IV">IV</option>
                    <option value="V">V</option><option value="Esq">Esq</option>
                    <option value="MD">MD</option><option value="PhD">PhD</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Date of Birth *</label>
                  <input type="date" value={form.dateOfBirth}
                    onChange={e => handleFormChange('dateOfBirth', e.target.value)} />
                </div>
                <div className="wq-form-field">
                  <label>Gender *</label>
                  <select value={form.gender} onChange={e => handleFormChange('gender', e.target.value)}>
                    <option value="">-- Select --</option>
                    <option value="Male">Male</option>
                    <option value="Female">Female</option>
                    <option value="Non-Binary">Non-Binary</option>
                    <option value="Unknown">Unknown</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Gender Identity</label>
                  <select value={form.genderIdentity || ''} onChange={e => handleFormChange('genderIdentity', e.target.value)}>
                    <option value="">Select...</option>
                    <option value="Male">Male</option><option value="Female">Female</option>
                    <option value="Non-Binary">Non-Binary</option>
                    <option value="Transgender Male">Transgender Male</option>
                    <option value="Transgender Female">Transgender Female</option>
                    <option value="Genderqueer">Genderqueer</option>
                    <option value="Not Listed">Not Listed</option>
                    <option value="Decline to State">Decline to State</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Sexual Orientation</label>
                  <select value={form.sexualOrientation || ''} onChange={e => handleFormChange('sexualOrientation', e.target.value)}>
                    <option value="">Select...</option>
                    <option value="Straight">Straight</option><option value="Gay">Gay</option>
                    <option value="Lesbian">Lesbian</option><option value="Bisexual">Bisexual</option>
                    <option value="Queer">Queer</option>
                    <option value="Not Listed">Not Listed</option>
                    <option value="Decline to State">Decline to State</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>SSN * (or select Blank SSN Reason)</label>
                  <input type="text" value={displaySsn}
                    onChange={e => handleSsnChange(e.target.value)}
                    placeholder="XXX-XX-####" maxLength={9}
                    style={{ fontFamily: 'monospace' }}
                    disabled={!!form.blankSsnReason} />
                </div>
                <div className="wq-form-field">
                  <label>Blank SSN Reason</label>
                  <select value={form.blankSsnReason || ''}
                    onChange={e => {
                      const val = e.target.value;
                      handleFormChange('blankSsnReason', val);
                      if (val) { handleFormChange('ssn', ''); setDisplaySsn(''); }
                    }}
                    disabled={form.ssn && form.ssn.length > 0}>
                    <option value="">Select...</option>
                    <option value="Refused to Provide">Refused to Provide</option>
                    <option value="Unable to Obtain">Unable to Obtain</option>
                    <option value="Applied for but not yet received">Applied for but not yet received</option>
                    <option value="Non-citizen">Non-citizen</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Spoken Language *</label>
                  <select value={form.spokenLanguage}
                    onChange={e => handleFormChange('spokenLanguage', e.target.value)}>
                    <option value="">-- Select --</option>
                    <option value="English">English</option>
                    <option value="Spanish">Spanish</option>
                    <option value="Cantonese">Cantonese</option>
                    <option value="Mandarin">Mandarin</option>
                    <option value="Vietnamese">Vietnamese</option>
                    <option value="Korean">Korean</option>
                    <option value="Armenian">Armenian</option>
                    <option value="Tagalog">Tagalog</option>
                    <option value="Russian">Russian</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                {form.spokenLanguage === 'Other' && (
                  <div className="wq-form-field">
                    <label>Other Spoken Language (specify)</label>
                    <input type="text" value={form.otherSpokenLanguageDetail || ''}
                      onChange={e => handleFormChange('otherSpokenLanguageDetail', e.target.value)} />
                  </div>
                )}
                <div className="wq-form-field">
                  <label>Written Language</label>
                  <select value={form.writtenLanguage || ''}
                    onChange={e => handleFormChange('writtenLanguage', e.target.value)}>
                    <option value="">-- Select --</option>
                    <option value="English">English</option>
                    <option value="Spanish">Spanish</option>
                    <option value="Cantonese">Cantonese</option>
                    <option value="Mandarin">Mandarin</option>
                    <option value="Vietnamese">Vietnamese</option>
                    <option value="Korean">Korean</option>
                    <option value="Armenian">Armenian</option>
                    <option value="Tagalog">Tagalog</option>
                    <option value="Russian">Russian</option>
                    <option value="Other">Other</option>
                  </select>
                </div>
                {form.writtenLanguage === 'Other' && (
                  <div className="wq-form-field">
                    <label>Other Written Language (specify)</label>
                    <input type="text" value={form.otherWrittenLanguageDetail || ''}
                      onChange={e => handleFormChange('otherWrittenLanguageDetail', e.target.value)} />
                  </div>
                )}
                <div className="wq-form-field">
                  <label>Ethnicity *</label>
                  <select value={form.ethnicity}
                    onChange={e => handleFormChange('ethnicity', e.target.value)}>
                    <option value="">-- Select --</option>
                    <option value="Hispanic/Latino">Hispanic/Latino</option>
                    <option value="White">White (Non-Hispanic)</option>
                    <option value="Black/African American">Black/African American</option>
                    <option value="Asian">Asian</option>
                    <option value="Pacific Islander">Pacific Islander</option>
                    <option value="American Indian">American Indian/Alaska Native</option>
                    <option value="Two or More Races">Two or More Races</option>
                    <option value="Unknown">Unknown/Decline</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Medi-Cal Pseudo Number</label>
                  <input type="text" value={form.mediCalPseudo || ''}
                    onChange={e => handleFormChange('mediCalPseudo', e.target.value.slice(0, 14))}
                    maxLength={14} placeholder="Up to 14 characters" />
                </div>
              </div>
            </div>
          </div>

          {/* Section: Contact */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Contact Information</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Home Phone</label>
                  <input type="text" value={form.homePhone}
                    onChange={e => handleFormChange('homePhone', e.target.value)}
                    placeholder="(###) ###-####" />
                </div>
                <div className="wq-form-field">
                  <label>Cell Phone</label>
                  <input type="text" value={form.cellPhone}
                    onChange={e => handleFormChange('cellPhone', e.target.value)}
                    placeholder="(###) ###-####" />
                </div>
                <div className="wq-form-field">
                  <label>Work Phone</label>
                  <input type="text" value={form.workPhone}
                    onChange={e => handleFormChange('workPhone', e.target.value)}
                    placeholder="(###) ###-####" />
                </div>
                <div className="wq-form-field">
                  <label>Email Address</label>
                  <input type="email" value={form.email}
                    onChange={e => handleFormChange('email', e.target.value)} />
                </div>
              </div>
            </div>
          </div>

          {/* Section: Residence Address */}
          <div className="wq-panel">
            <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4>Residence Address</h4>
              {form.residenceCassMatch && (
                <span style={{ background: '#f0fff4', color: '#276749', fontSize: '0.75rem', padding: '2px 8px', borderRadius: '12px', fontWeight: 600 }}>✓ CASS Verified</span>
              )}
              {form.residenceCassFailed && !form.residenceCassMatch && (
                <span style={{ background: '#fff5f5', color: '#c53030', fontSize: '0.75rem', padding: '2px 8px', borderRadius: '12px', fontWeight: 600 }}>Unverified</span>
              )}
            </div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Street Number</label>
                  <input type="text" value={form.residenceStreetNumber}
                    onChange={e => setForm(prev => ({ ...prev, residenceStreetNumber: e.target.value }))} />
                </div>
                <div className="wq-form-field">
                  <label>Street Name</label>
                  <input type="text" value={form.residenceStreetName}
                    onChange={e => setForm(prev => ({ ...prev, residenceStreetName: e.target.value }))} />
                </div>
                <div className="wq-form-field">
                  <label>City</label>
                  <input type="text" value={form.residenceCity}
                    onChange={e => setForm(prev => ({ ...prev, residenceCity: e.target.value }))} />
                </div>
                <div className="wq-form-field">
                  <label>State</label>
                  <input type="text" value={form.residenceState} maxLength={2}
                    onChange={e => setForm(prev => ({ ...prev, residenceState: e.target.value.toUpperCase() }))} />
                </div>
                <div className="wq-form-field">
                  <label>ZIP</label>
                  <input type="text" value={form.residenceZip}
                    onChange={e => setForm(prev => ({ ...prev, residenceZip: e.target.value }))} />
                </div>
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <button className="wq-btn wq-btn-outline" onClick={() => setShowResidenceModal(true)}>
                  Verify Residence Address (CASS)
                </button>
              </div>
            </div>
          </div>

          {/* Section: Mailing Address */}
          <div className="wq-panel">
            <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4>Mailing Address</h4>
              {form.mailingCassMatch && !form.sameAsResidence && (
                <span style={{ background: '#f0fff4', color: '#276749', fontSize: '0.75rem', padding: '2px 8px', borderRadius: '12px', fontWeight: 600 }}>✓ CASS Verified</span>
              )}
            </div>
            <div className="wq-panel-body">
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.875rem', marginBottom: '0.75rem', cursor: 'pointer' }}>
                <input type="checkbox" checked={form.sameAsResidence}
                  onChange={e => handleSameAsResidence(e.target.checked)} />
                Same as Residence Address
              </label>
              {!form.sameAsResidence && (
                <>
                  <div className="wq-search-grid">
                    <div className="wq-form-field">
                      <label>Street Number</label>
                      <input type="text" value={form.mailingStreetNumber}
                        onChange={e => setForm(prev => ({ ...prev, mailingStreetNumber: e.target.value }))} />
                    </div>
                    <div className="wq-form-field">
                      <label>Street Name</label>
                      <input type="text" value={form.mailingStreetName}
                        onChange={e => setForm(prev => ({ ...prev, mailingStreetName: e.target.value }))} />
                    </div>
                    <div className="wq-form-field">
                      <label>City</label>
                      <input type="text" value={form.mailingCity}
                        onChange={e => setForm(prev => ({ ...prev, mailingCity: e.target.value }))} />
                    </div>
                    <div className="wq-form-field">
                      <label>State</label>
                      <input type="text" value={form.mailingState} maxLength={2}
                        onChange={e => setForm(prev => ({ ...prev, mailingState: e.target.value.toUpperCase() }))} />
                    </div>
                    <div className="wq-form-field">
                      <label>ZIP</label>
                      <input type="text" value={form.mailingZip}
                        onChange={e => setForm(prev => ({ ...prev, mailingZip: e.target.value }))} />
                    </div>
                  </div>
                  <div style={{ marginTop: '0.75rem' }}>
                    <button className="wq-btn wq-btn-outline" onClick={() => setShowMailingModal(true)}>
                      Verify Mailing Address (CASS)
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>

          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-outline" onClick={() => setStep(1)}>Back</button>
            <button className="wq-btn wq-btn-primary" onClick={goToStep3}>Next: CIN Clearance</button>
          </div>
        </>
      )}

      {/* ════════════════════════════════════════════════════════════════════
          STEP 3 — CIN Clearance + Case Details
      ════════════════════════════════════════════════════════════════════ */}
      {step === 3 && (
        <>
          {/* CIN Clearance Panel */}
          <div className="wq-panel">
            <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h4>CIN Clearance</h4>
              <span style={{ fontSize: '0.75rem', padding: '2px 10px', borderRadius: '12px', fontWeight: 600, ...cinBadge }}>
                {cinLabel}
              </span>
            </div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Client Index Number (CIN)</label>
                  <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                    <input type="text" value={cin} readOnly
                      placeholder="Run CIN Clearance →"
                      style={{ flex: 1, backgroundColor: '#f7f9fb', cursor: 'not-allowed' }} />
                    <button type="button" className="wq-btn wq-btn-outline"
                      onClick={handleCINLookup}
                      title="Run SCI CIN Clearance"
                      style={{ padding: '0.35rem 0.6rem', fontSize: '1rem', lineHeight: 1 }}>
                      🔍
                    </button>
                  </div>
                  <span style={{ fontSize: '0.72rem', color: '#666', marginTop: '2px' }}>
                    Click 🔍 to search Statewide Client Index (SCI OI transaction)
                  </span>
                </div>
                {mediCalStatus && (
                  <div className="wq-form-field">
                    <label>Medi-Cal Status</label>
                    <span style={{
                      fontSize: '0.875rem', padding: '4px 12px', borderRadius: '12px', fontWeight: 600,
                      ...(mediCalStatus === 'ACTIVE'
                        ? { background: '#f0fff4', color: '#276749' }
                        : { background: '#fff5f5', color: '#c53030' }),
                    }}>
                      {mediCalStatus}
                    </span>
                  </div>
                )}
                {aidCode && (
                  <div className="wq-form-field">
                    <label>Aid Code</label>
                    <span style={{ fontSize: '0.875rem', fontWeight: 600, color: '#153554' }}>{aidCode}</span>
                  </div>
                )}
              </div>

              {cinClearanceStatus === 'NO_MATCH' && (
                <div style={{ background: '#fffbeb', border: '1px solid #f6ad55', borderLeft: '4px solid #dd6b20', borderRadius: '4px', padding: '0.5rem 1rem', fontSize: '0.875rem', color: '#744210', marginTop: '0.5rem' }}>
                  <strong>No CIN found.</strong> You may proceed without a CIN. An S1 referral will be sent to county SAWS (BR-9) when the application is submitted.
                </div>
              )}
              {cinClearanceStatus === 'MISMATCH_REVIEW' && (
                <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', borderRadius: '4px', padding: '0.5rem 1rem', fontSize: '0.875rem', color: '#c53030', marginTop: '0.5rem' }}>
                  <strong>Demographic mismatch detected.</strong> Click 🔍 to retry with corrected demographics.
                </div>
              )}
            </div>
          </div>

          {/* Case Details Panel */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Case Details</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Program Type</label>
                  <select value={caseForm.programType}
                    onChange={e => setCaseForm(prev => ({ ...prev, programType: e.target.value }))}>
                    <option value="">-- Select --</option>
                    <option value="IHSS">IHSS</option>
                    <option value="PCSP">PCSP</option>
                    <option value="WPCS">WPCS</option>
                    <option value="IHSS_PCSP">IHSS + PCSP</option>
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>County *</label>
                  <select value={caseForm.countyCode}
                    onChange={e => setCaseForm(prev => ({ ...prev, countyCode: e.target.value }))}>
                    <option value="">-- Select County --</option>
                    {CA_COUNTIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div className="wq-form-field">
                  <label>Case Opening Date</label>
                  <input type="date" value={caseForm.caseOpenDate}
                    onChange={e => setCaseForm(prev => ({ ...prev, caseOpenDate: e.target.value }))} />
                </div>
                <div className="wq-form-field">
                  <label>Assigned Worker</label>
                  <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                    <input type="text"
                      value={selectedWorker
                        ? `${selectedWorker.lastName}, ${selectedWorker.firstName} (${selectedWorker.workerNumber})`
                        : ''}
                      readOnly
                      placeholder="Search for a worker →"
                      style={{ flex: 1, backgroundColor: '#f7f9fb', cursor: 'not-allowed' }} />
                    <button type="button" className="wq-btn wq-btn-outline"
                      onClick={() => setShowWorkerSearch(true)}
                      title="Search for a worker (User Search)"
                      style={{ padding: '0.35rem 0.6rem', fontSize: '1rem', lineHeight: 1 }}>
                      🔍
                    </button>
                  </div>
                </div>
                <div className="wq-form-field">
                  <label>IHSS Referral Date</label>
                  <input type="date" value={caseForm.ihssReferralDate}
                    onChange={e => setCaseForm(prev => ({ ...prev, ihssReferralDate: e.target.value }))} />
                </div>
                <div className="wq-form-field">
                  <label>Written Language</label>
                  <input type="text" value={caseForm.writtenLanguage}
                    onChange={e => setCaseForm(prev => ({ ...prev, writtenLanguage: e.target.value }))} />
                </div>
                <div className="wq-form-field">
                  <label>Interpreter Available</label>
                  <select value={caseForm.interpreterAvailable ? 'yes' : 'no'}
                    onChange={e => setCaseForm(prev => ({ ...prev, interpreterAvailable: e.target.value === 'yes' }))}>
                    <option value="no">No</option>
                    <option value="yes">Yes</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-outline" onClick={() => setStep(2)}>Back</button>
            <button className="wq-btn wq-btn-primary" onClick={goToStep4}>Next: Review</button>
          </div>
        </>
      )}

      {/* ════════════════════════════════════════════════════════════════════
          STEP 4 — Review & Submit
      ════════════════════════════════════════════════════════════════════ */}
      {step === 4 && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Review: Applicant Information</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-grid">
                <div className="wq-detail-row"><span className="wq-detail-label">Full Name</span><span className="wq-detail-value">{[form.firstName, form.middleName, form.lastName].filter(Boolean).join(' ')}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Date of Birth</span><span className="wq-detail-value">{form.dateOfBirth || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Gender</span><span className="wq-detail-value">{form.gender || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">SSN</span><span className="wq-detail-value">{form.ssn ? `XXX-XX-${form.ssn.slice(-4)}` : '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Language</span><span className="wq-detail-value">{form.spokenLanguage || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Phone</span><span className="wq-detail-value">{form.homePhone || form.cellPhone || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Email</span><span className="wq-detail-value">{form.email || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Residence</span><span className="wq-detail-value">{residenceLabel}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Mailing</span><span className="wq-detail-value">{mailingLabel}</span></div>
              </div>
            </div>
          </div>

          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Review: CIN & Case Details</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-grid">
                <div className="wq-detail-row">
                  <span className="wq-detail-label">CIN Clearance</span>
                  <span className="wq-detail-value">
                    <span style={{ fontSize: '0.75rem', padding: '2px 10px', borderRadius: '12px', fontWeight: 600, ...cinBadge }}>{cinLabel}</span>
                  </span>
                </div>
                <div className="wq-detail-row"><span className="wq-detail-label">CIN</span><span className="wq-detail-value">{cin || '(none — S1 to SAWS)'}</span></div>
                {mediCalStatus && <div className="wq-detail-row"><span className="wq-detail-label">Medi-Cal Status</span><span className="wq-detail-value">{mediCalStatus}</span></div>}
                {aidCode && <div className="wq-detail-row"><span className="wq-detail-label">Aid Code</span><span className="wq-detail-value">{aidCode}</span></div>}
                <div className="wq-detail-row"><span className="wq-detail-label">Program Type</span><span className="wq-detail-value">{caseForm.programType || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">County</span><span className="wq-detail-value">{caseForm.countyCode}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Case Opening Date</span><span className="wq-detail-value">{caseForm.caseOpenDate || '—'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">Assigned Worker</span><span className="wq-detail-value">{selectedWorker ? `${selectedWorker.lastName}, ${selectedWorker.firstName} (${selectedWorker.workerNumber})` : '(unassigned)'}</span></div>
                <div className="wq-detail-row"><span className="wq-detail-label">IHSS Referral Date</span><span className="wq-detail-value">{caseForm.ihssReferralDate || '—'}</span></div>
              </div>
            </div>
          </div>

          {!cinClearancePerformed && (
            <div style={{ background: '#fffbeb', border: '1px solid #f6ad55', borderLeft: '4px solid #dd6b20', padding: '0.6rem 1rem', borderRadius: '4px', fontSize: '0.875rem', color: '#744210', marginBottom: '1rem' }}>
              <strong>Note:</strong> CIN clearance has not been performed. You may still save as an application only. To create a case, go back to Step 3 and run CIN clearance.
            </div>
          )}

          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-outline" onClick={() => setStep(3)}>Back</button>
            <button className="wq-btn wq-btn-outline" onClick={handleSaveApplicationOnly} disabled={saving}>
              {saving ? 'Saving...' : 'Save Application Only'}
            </button>
            <button className="wq-btn wq-btn-primary" onClick={handleCreateApplicationAndCase} disabled={saving}>
              {saving ? 'Creating...' : 'Create Application + Case'}
            </button>
          </div>
        </>
      )}

      {/* ── Address Verification Modals ─────────────────────────────────── */}
      {showResidenceModal && (
        <AddressVerificationModal
          isOpen={showResidenceModal}
          title="Verify Residence Address"
          initialData={{
            streetNumber: form.residenceStreetNumber,
            streetName:   form.residenceStreetName,
            unitType:     form.residenceUnitType,
            unitNumber:   form.residenceUnitNumber,
            city:         form.residenceCity,
            state:        form.residenceState,
            zip:          form.residenceZip,
          }}
          onConfirm={handleResidenceConfirm}
          onClose={() => setShowResidenceModal(false)}
        />
      )}
      {showMailingModal && (
        <AddressVerificationModal
          isOpen={showMailingModal}
          title="Verify Mailing Address"
          initialData={{
            streetNumber: form.mailingStreetNumber,
            streetName:   form.mailingStreetName,
            unitType:     form.mailingUnitType,
            unitNumber:   form.mailingUnitNumber,
            city:         form.mailingCity,
            state:        form.mailingState,
            zip:          form.mailingZip,
          }}
          onConfirm={handleMailingConfirm}
          onClose={() => setShowMailingModal(false)}
        />
      )}

      {/* ── CIN Search Modal (OI Transaction) ──────────────────────────── */}
      {showCINSearch && (
        <CINSearchModal
          applicantData={applicantData}
          applicationId=""
          onShowEligibility={handleShowEligibility}
          onCancel={handleCINSearchCancel}
        />
      )}

      {/* ── Medi-Cal Eligibility Modal (EL/OM Transaction) ─────────────── */}
      {showEligibility && eligibilityData && (
        <MediCalEligibilityModal
          eligibilityData={eligibilityData}
          applicantData={applicantData}
          applicationId=""
          onSelectSuccess={handleSelectSuccess}
          onMismatch={handleEligibilityMismatch}
          onCancel={handleEligibilityCancel}
        />
      )}

      {/* ── Demographic Mismatch Modal ──────────────────────────────────── */}
      {showMismatch && (
        <CINDataMismatchModal
          onReturnToCINSelect={handleReturnToCINSelect}
          onProceedWithoutCIN={() => {
            setShowMismatch(false);
            setCinClearanceStatus('WITHOUT_CIN');
            setShowWithoutCIN(true);
          }}
          onCancelCreateCase={() => {
            setShowMismatch(false);
            navigate(-1);
          }}
        />
      )}

      {/* ── Create Case Without CIN Modal (BR-9 / S1 to SAWS) ─────────── */}
      {showWithoutCIN && (
        <CreateCaseWithoutCINModal
          onContinue={handleWithoutCINContinue}
          onCancel={() => setShowWithoutCIN(false)}
          saving={saving}
        />
      )}

      {/* ── User Search Modal (DSD CI-67746 — Assign Case Owner) ─────────── */}
      {showWorkerSearch && (
        <UserSearchModal
          onSelect={handleWorkerSelect}
          onCancel={() => setShowWorkerSearch(false)}
        />
      )}
    </div>
  );
};
