import React, { useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import * as recipientsApi from '../api/recipientsApi';
import './WorkQueues.css';

/* ───── California Counties (58 + special options) ───── */
const CA_COUNTIES = [
  { code: '', name: 'All' },
  { code: '01', name: 'Alameda' }, { code: '02', name: 'Alpine' }, { code: '03', name: 'Amador' },
  { code: '04', name: 'Butte' }, { code: '05', name: 'Calaveras' }, { code: '06', name: 'Colusa' },
  { code: '07', name: 'Contra Costa' }, { code: '08', name: 'Del Norte' }, { code: '09', name: 'El Dorado' },
  { code: '10', name: 'Fresno' }, { code: '11', name: 'Glenn' }, { code: '12', name: 'Humboldt' },
  { code: '13', name: 'Imperial' }, { code: '14', name: 'Inyo' }, { code: '15', name: 'Kern' },
  { code: '16', name: 'Kings' }, { code: '17', name: 'Lake' }, { code: '18', name: 'Lassen' },
  { code: '19', name: 'Los Angeles' }, { code: '20', name: 'Madera' }, { code: '21', name: 'Marin' },
  { code: '22', name: 'Mariposa' }, { code: '23', name: 'Mendocino' }, { code: '24', name: 'Merced' },
  { code: '25', name: 'Modoc' }, { code: '26', name: 'Mono' }, { code: '27', name: 'Monterey' },
  { code: '28', name: 'Napa' }, { code: '29', name: 'Nevada' }, { code: '30', name: 'Orange' },
  { code: '31', name: 'Placer' }, { code: '32', name: 'Plumas' }, { code: '33', name: 'Riverside' },
  { code: '34', name: 'Sacramento' }, { code: '35', name: 'San Benito' }, { code: '36', name: 'San Bernardino' },
  { code: '37', name: 'San Diego' }, { code: '38', name: 'San Francisco' }, { code: '39', name: 'San Joaquin' },
  { code: '40', name: 'San Luis Obispo' }, { code: '41', name: 'San Mateo' }, { code: '42', name: 'Santa Barbara' },
  { code: '43', name: 'Santa Clara' }, { code: '44', name: 'Santa Cruz' }, { code: '45', name: 'Shasta' },
  { code: '46', name: 'Sierra' }, { code: '47', name: 'Siskiyou' }, { code: '48', name: 'Solano' },
  { code: '49', name: 'Sonoma' }, { code: '50', name: 'Stanislaus' }, { code: '51', name: 'Sutter' },
  { code: '52', name: 'Tehama' }, { code: '53', name: 'Trinity' }, { code: '54', name: 'Tulare' },
  { code: '55', name: 'Tuolumne' }, { code: '56', name: 'Ventura' }, { code: '57', name: 'Yolo' },
  { code: '58', name: 'Yuba' },
  { code: '99', name: 'Out of State' }, { code: '00', name: 'Undetermined' },
];

const UNIT_TYPES = ['', 'APT', 'STE', 'UNIT', 'RM', 'FL', 'BLDG', 'DEPT', 'LOT', 'SPC'];

const PERSON_TYPES = [
  { value: '', label: '' },
  { value: 'OPEN_REFERRAL', label: 'Open Referral' },
  { value: 'CLOSED_REFERRAL', label: 'Closed Referral' },
  { value: 'APPLICANT', label: 'Applicant' },
  { value: 'RECIPIENT', label: 'Recipient' },
  { value: 'PROVIDER', label: 'Provider' },
];

const PERSON_TYPE_LABELS = {
  OPEN_REFERRAL: 'Open Referral', CLOSED_REFERRAL: 'Closed Referral',
  APPLICANT: 'Applicant', RECIPIENT: 'Recipient', PROVIDER: 'Provider',
};

const PAGE_SIZE = 50;

/* ───── helpers ───── */
const countyName = (code) => {
  const c = CA_COUNTIES.find(x => x.code === code);
  return c ? c.name : code || '';
};

const maskSsn = (ssn) => {
  if (!ssn) return '';
  const digits = ssn.replace(/\D/g, '');
  if (digits.length < 4) return '';
  return 'XXX-XX-' + digits.slice(-4);
};

const formatDob = (dob) => {
  if (!dob) return '';
  const d = new Date(dob + 'T00:00:00');
  if (isNaN(d.getTime())) return dob;
  return String(d.getMonth() + 1).padStart(2, '0') + '/' +
         String(d.getDate()).padStart(2, '0') + '/' + d.getFullYear();
};

const getStatusByPersonType = (r) => {
  const pt = r.personType;
  if (pt === 'RECIPIENT') return r.caseStatus || '';
  if (pt === 'APPLICANT') return r.applicationStatus || 'Pending';
  if (pt === 'PROVIDER')  return r.providerStatus || '';
  return '';
};

/**
 * Person Search — Referral Mode (DSD CI-67784)
 *
 * Full DSD-compliant search screen with:
 * - 3 collapsible search sections: General, Address, Other Contact Information
 * - Search hierarchy: SSN > CIN > Provider# > all other combined (BR-2, BR-3, BR-20, BR-4/5)
 * - Soundex toggle (SX checkbox), All SSN / Last 4 SSN checkboxes
 * - 11-column results grid, 50 per page, alphabetical sort by Last Name, First Name
 * - "Continue Referral" button to proceed to Create Referral (CI-67784)
 * - Full validation (EM OS 220/221/264/265/266/267/268/279/281/282)
 */
export const PersonSearchReferralPage = () => {
  const navigate = useNavigate();

  /* ── Search criteria state ── */
  const [lastName, setLastName] = useState('');
  const [soundexOn, setSoundexOn] = useState(false);
  const [firstName, setFirstName] = useState('');
  const [ssn, setSsn] = useState('');
  const [allSsn, setAllSsn] = useState(false);
  const [last4Ssn, setLast4Ssn] = useState(false);
  const [cin, setCin] = useState('');
  const [providerNumber, setProviderNumber] = useState('');
  const [personType, setPersonType] = useState('');
  const [dob, setDob] = useState('');
  const [gender, setGender] = useState('');
  const [county, setCounty] = useState('');
  const [districtOffice, setDistrictOffice] = useState('');

  // Address section
  const [streetNumber, setStreetNumber] = useState('');
  const [streetName, setStreetName] = useState('');
  const [unitType, setUnitType] = useState('');
  const [unitNumber, setUnitNumber] = useState('');
  const [addrCity, setAddrCity] = useState('');

  // Other Contact
  const [phoneAreaCode, setPhoneAreaCode] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [emailAddr, setEmailAddr] = useState('');

  /* ── Results state ── */
  const [results, setResults] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [validationErrors, setValidationErrors] = useState([]);

  /* ── Section collapse state ── */
  const [generalOpen, setGeneralOpen] = useState(true);
  const [addressOpen, setAddressOpen] = useState(true);
  const [contactOpen, setContactOpen] = useState(true);

  /* ── Validation (all DSD EMs) ── */
  const validate = useCallback(() => {
    const errors = [];

    const hasSsn = ssn.trim().length > 0;
    const hasCin = cin.trim().length > 0;
    const hasProvider = providerNumber.trim().length > 0;
    const hasLastName = lastName.trim().length > 0;
    const hasCompleteAddr = streetNumber.trim() && streetName.trim() && addrCity.trim();
    const hasPhone = phoneAreaCode.trim() || phoneNumber.trim();
    const hasEmail = emailAddr.trim().length > 0;

    // No criteria at all
    if (!hasSsn && !hasCin && !hasProvider && !hasLastName && !hasCompleteAddr && !hasPhone && !hasEmail) {
      errors.push('Please enter one or more of these fields: SSN, Full or partial last name, CIN, Complete address, Provider Number, Phone Number, or Email Address.');
      return errors;
    }

    // SSN validations
    if (hasSsn) {
      const ssnDigits = ssn.replace(/\D/g, '');
      if (last4Ssn) {
        if (ssnDigits.length !== 4) {
          errors.push('EM OS 279: You must enter only last four digits when "Last 4 SSN" option is checked.');
        }
      } else if (allSsn) {
        if (ssnDigits.length !== 9) {
          errors.push('EM OS 281: You must enter all nine digits when "All SSN" option is checked.');
        }
      } else {
        if (ssnDigits.length !== 9) {
          errors.push('EM OS 282: SSN must be nine digits.');
        }
      }
    }

    // Phone validations
    if (hasPhone) {
      const ac = phoneAreaCode.replace(/\D/g, '');
      const pn = phoneNumber.replace(/\D/g, '');
      if ((ac.length > 0 && pn.length === 0) || (pn.length > 0 && ac.length === 0)) {
        errors.push("EM OS 266: 'Phone number' must be entered.");
      }
      if (ac.length > 0 && ac.length !== 3) {
        errors.push('EM OS 264: Area Code must be three numeric digits.');
      }
      if (pn.length > 0 && pn.length !== 7) {
        errors.push('EM OS 265: Phone Number must be seven numeric digits.');
      }
      const fullPhone = ac + pn;
      if (fullPhone === '0000000000' || fullPhone === '9999999999') {
        errors.push('EM OS 268: Not a valid phone number. Please enter valid phone number.');
      }
    }

    // Email validation (EM OS 267)
    if (hasEmail) {
      const em = emailAddr.trim();
      const atCount = (em.match(/@/g) || []).length;
      const parts = em.split('@');
      let invalid = false;
      if (atCount !== 1) invalid = true;
      else if (parts[0].length < 2) invalid = true;
      else if (!parts[1] || !parts[1].includes('.')) invalid = true;
      else {
        const domainParts = parts[1].split('.');
        if (domainParts.some(p => p.length === 0)) invalid = true;
      }
      if (parts[0] && (/^[*^%$#]/.test(parts[0]) || /[*^%$#]$/.test(parts[0]))) invalid = true;
      if (invalid) {
        errors.push('EM OS 267: Not a valid email address. Please enter valid email address.');
      }
    }

    // Address field validations (EM OS 220, EM OS 221)
    if (unitType && !unitNumber.trim()) {
      errors.push('EM OS 220: Both the Unit Type and Unit Number are required when either is used as search criteria.');
    }
    if (unitNumber.trim() && !unitType) {
      errors.push('EM OS 221: Both the Unit Type and Unit Number are required when either is used as search criteria.');
    }

    return errors;
  }, [ssn, cin, providerNumber, lastName, streetNumber, streetName, addrCity,
      phoneAreaCode, phoneNumber, emailAddr, allSsn, last4Ssn, unitType, unitNumber]);

  /* ── Execute search ── */
  const doSearch = async (pageNum = 0) => {
    const errs = validate();
    if (errs.length > 0) {
      setValidationErrors(errs);
      return;
    }
    setValidationErrors([]);
    setLoading(true);
    setSearched(true);
    setPage(pageNum);

    try {
      const params = {};
      if (ssn.trim()) params.ssn = ssn.replace(/\D/g, '');
      if (cin.trim()) params.cin = cin.trim();
      if (providerNumber.trim()) params.providerNumber = providerNumber.trim();
      if (lastName.trim()) params.lastName = lastName.trim();
      if (firstName.trim()) params.firstName = firstName.trim();
      if (dob) params.dob = dob;
      if (gender) params.gender = gender;
      if (county) params.countyCode = county;
      if (personType) params.personType = personType;
      if (streetNumber.trim()) params.streetNumber = streetNumber.trim();
      if (streetName.trim()) params.streetName = streetName.trim();
      if (addrCity.trim()) params.city = addrCity.trim();
      if (soundexOn) params.soundex = 'true';
      if (allSsn) params.allSsn = 'true';
      if (last4Ssn) params.last4Ssn = 'true';

      // Phone: combine area code + number
      const ac = phoneAreaCode.replace(/\D/g, '');
      const pn = phoneNumber.replace(/\D/g, '');
      if (ac && pn) params.phone = ac + pn;

      if (emailAddr.trim()) params.email = emailAddr.trim();

      params.page = String(pageNum);
      params.size = String(PAGE_SIZE);

      const data = await recipientsApi.searchRecipients(params);
      const content = data?.content || [];
      setResults(content);
      setTotalElements(data?.totalElements || content.length);
    } catch (err) {
      console.error('[PersonSearchReferral] Search failed:', err);
      const errMsg = err?.response?.data?.error || err?.message || 'Search failed';
      setValidationErrors([errMsg]);
      setResults([]);
      setTotalElements(0);
    } finally {
      setLoading(false);
    }
  };

  /* ── Reset all criteria (Scenario 3) ── */
  const handleReset = () => {
    setLastName(''); setSoundexOn(false); setFirstName('');
    setSsn(''); setAllSsn(false); setLast4Ssn(false);
    setCin(''); setProviderNumber('');
    setPersonType(''); setDob(''); setGender('');
    setCounty(''); setDistrictOffice('');
    setStreetNumber(''); setStreetName('');
    setUnitType(''); setUnitNumber('');
    setAddrCity('');
    setPhoneAreaCode(''); setPhoneNumber(''); setEmailAddr('');
    setResults([]); setTotalElements(0);
    setSearched(false); setValidationErrors([]);
    setPage(0);
  };

  const totalPages = Math.ceil(totalElements / PAGE_SIZE);

  /* ───── Inline styles ───── */
  const fieldStyle = { display: 'flex', flexDirection: 'column', gap: '0.2rem' };
  const labelStyle = { fontSize: '0.8rem', fontWeight: 600, color: '#333' };
  const inputStyle = { padding: '0.35rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '3px', fontSize: '0.85rem' };
  const checkStyle = { display: 'flex', alignItems: 'center', gap: '0.35rem', fontSize: '0.8rem', cursor: 'pointer' };
  const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '0.75rem 1rem' };

  return (
    <div className="wq-page">
      {/* ── Page header ── */}
      <div className="wq-page-header">
        <h2>New Referral – Duplicate Check</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {searched && (
            <button className="wq-btn wq-btn-primary" onClick={() => navigate('/persons/referral/new')}>
              Continue Referral
            </button>
          )}
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/workspace')}>Cancel</button>
        </div>
      </div>

      <div style={{ background: '#eff6ff', border: '1px solid #bfdbfe', borderRadius: '4px', padding: '0.5rem 1rem', marginBottom: '1rem', fontSize: '0.825rem', color: '#1e40af' }}>
        Search for an existing person before creating a new referral. If no match is found, click "Continue Referral" to create a new person record.
      </div>

      {/* ── Validation errors ── */}
      {validationErrors.length > 0 && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderRadius: '4px', padding: '0.75rem 1rem', marginBottom: '1rem' }}>
          {validationErrors.map((e, i) => (
            <div key={i} style={{ color: '#c53030', fontSize: '0.85rem', marginBottom: i < validationErrors.length - 1 ? '0.3rem' : 0 }}>{e}</div>
          ))}
        </div>
      )}

      {/* ═══════ SECTION 1: General ═══════ */}
      <div className="wq-panel" style={{ marginBottom: '0.75rem' }}>
        <div className="wq-panel-header" onClick={() => setGeneralOpen(!generalOpen)}>
          <h4>General</h4>
          <span className="wq-panel-toggle">{generalOpen ? '\u25B2' : '\u25BC'}</span>
        </div>
        {generalOpen && (
          <div className="wq-panel-body">
            <div style={gridStyle}>
              {/* Last Name + SX checkbox */}
              <div style={fieldStyle}>
                <label style={labelStyle}>Last Name</label>
                <div style={{ display: 'flex', gap: '0.35rem', alignItems: 'center' }}>
                  <input style={{ ...inputStyle, flex: 1 }} type="text" value={lastName}
                    onChange={e => setLastName(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && doSearch()} />
                  <label style={checkStyle} title="Soundex phonetic matching">
                    <input type="checkbox" checked={soundexOn} onChange={e => setSoundexOn(e.target.checked)} />
                    SX
                  </label>
                </div>
              </div>

              {/* First Name */}
              <div style={fieldStyle}>
                <label style={labelStyle}>First Name</label>
                <input style={inputStyle} type="text" value={firstName}
                  onChange={e => setFirstName(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && doSearch()} />
              </div>

              {/* SSN + All SSN / Last 4 SSN checkboxes */}
              <div style={fieldStyle}>
                <label style={labelStyle}>SSN</label>
                <input style={{ ...inputStyle, fontFamily: 'monospace' }} type="text"
                  value={ssn} onChange={e => setSsn(e.target.value.replace(/\D/g, '').slice(0, 9))}
                  maxLength={9} placeholder={last4Ssn ? '####' : '#########'}
                  onKeyDown={e => e.key === 'Enter' && doSearch()} />
                <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.15rem' }}>
                  <label style={checkStyle}>
                    <input type="checkbox" checked={allSsn}
                      onChange={e => { setAllSsn(e.target.checked); if (e.target.checked) setLast4Ssn(false); }} />
                    All SSN
                  </label>
                  <label style={checkStyle}>
                    <input type="checkbox" checked={last4Ssn}
                      onChange={e => { setLast4Ssn(e.target.checked); if (e.target.checked) setAllSsn(false); }} />
                    Last 4 SSN
                  </label>
                </div>
              </div>

              {/* CIN */}
              <div style={fieldStyle}>
                <label style={labelStyle}>CIN</label>
                <input style={inputStyle} type="text" value={cin}
                  onChange={e => setCin(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && doSearch()} />
              </div>

              {/* Provider Number */}
              <div style={fieldStyle}>
                <label style={labelStyle}>Provider Number</label>
                <input style={inputStyle} type="text" value={providerNumber}
                  onChange={e => setProviderNumber(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && doSearch()} />
              </div>

              {/* Person Type */}
              <div style={fieldStyle}>
                <label style={labelStyle}>Person Type</label>
                <select style={inputStyle} value={personType} onChange={e => setPersonType(e.target.value)}>
                  {PERSON_TYPES.map(pt => <option key={pt.value} value={pt.value}>{pt.label}</option>)}
                </select>
              </div>

              {/* Date of Birth */}
              <div style={fieldStyle}>
                <label style={labelStyle}>Date of Birth</label>
                <input style={inputStyle} type="date" value={dob} onChange={e => setDob(e.target.value)} />
              </div>

              {/* Gender */}
              <div style={fieldStyle}>
                <label style={labelStyle}>Gender</label>
                <select style={inputStyle} value={gender} onChange={e => setGender(e.target.value)}>
                  <option value=""></option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                </select>
              </div>

              {/* County */}
              <div style={fieldStyle}>
                <label style={labelStyle}>County</label>
                <select style={inputStyle} value={county}
                  onChange={e => { setCounty(e.target.value); setDistrictOffice(''); }}>
                  {CA_COUNTIES.map(c => <option key={c.code} value={c.code}>{c.name}</option>)}
                </select>
              </div>

              {/* District Office */}
              <div style={fieldStyle}>
                <label style={labelStyle}>District Office</label>
                <select style={inputStyle} value={districtOffice}
                  onChange={e => setDistrictOffice(e.target.value)} disabled={!county}>
                  <option value="">{county ? '(Select District Office)' : '(Select County first)'}</option>
                  {county && <option value="MAIN">Main Office</option>}
                  {county && <option value="BRANCH">Branch Office</option>}
                </select>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ═══════ SECTION 2: Address ═══════ */}
      <div className="wq-panel" style={{ marginBottom: '0.75rem' }}>
        <div className="wq-panel-header" onClick={() => setAddressOpen(!addressOpen)}>
          <h4>Address</h4>
          <span className="wq-panel-toggle">{addressOpen ? '\u25B2' : '\u25BC'}</span>
        </div>
        {addressOpen && (
          <div className="wq-panel-body">
            <div style={gridStyle}>
              <div style={fieldStyle}>
                <label style={labelStyle}>Street Number</label>
                <input style={inputStyle} type="text" value={streetNumber}
                  onChange={e => setStreetNumber(e.target.value)} />
              </div>
              <div style={fieldStyle}>
                <label style={labelStyle}>Street Name</label>
                <input style={inputStyle} type="text" value={streetName}
                  onChange={e => setStreetName(e.target.value)} />
              </div>
              <div style={fieldStyle}>
                <label style={labelStyle}>Unit Type</label>
                <select style={inputStyle} value={unitType} onChange={e => setUnitType(e.target.value)}>
                  {UNIT_TYPES.map(u => <option key={u} value={u}>{u || '(None)'}</option>)}
                </select>
              </div>
              <div style={fieldStyle}>
                <label style={labelStyle}>Unit Number</label>
                <input style={inputStyle} type="text" value={unitNumber}
                  onChange={e => setUnitNumber(e.target.value)} />
              </div>
              <div style={fieldStyle}>
                <label style={labelStyle}>City</label>
                <input style={inputStyle} type="text" value={addrCity}
                  onChange={e => setAddrCity(e.target.value)} />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ═══════ SECTION 3: Other Contact Information ═══════ */}
      <div className="wq-panel" style={{ marginBottom: '0.75rem' }}>
        <div className="wq-panel-header" onClick={() => setContactOpen(!contactOpen)}>
          <h4>Other Contact Information</h4>
          <span className="wq-panel-toggle">{contactOpen ? '\u25B2' : '\u25BC'}</span>
        </div>
        {contactOpen && (
          <div className="wq-panel-body">
            <div style={gridStyle}>
              <div style={fieldStyle}>
                <label style={labelStyle}>Phone Number</label>
                <div style={{ display: 'flex', gap: '0.35rem', alignItems: 'center' }}>
                  <input style={{ ...inputStyle, width: '70px' }} type="text" placeholder="Area"
                    value={phoneAreaCode} maxLength={3}
                    onChange={e => setPhoneAreaCode(e.target.value.replace(/\D/g, '').slice(0, 3))} />
                  <span style={{ color: '#666' }}>-</span>
                  <input style={{ ...inputStyle, width: '110px' }} type="text" placeholder="Number"
                    value={phoneNumber} maxLength={7}
                    onChange={e => setPhoneNumber(e.target.value.replace(/\D/g, '').slice(0, 7))} />
                </div>
              </div>
              <div style={fieldStyle}>
                <label style={labelStyle}>Email Address</label>
                <input style={inputStyle} type="text" value={emailAddr}
                  onChange={e => setEmailAddr(e.target.value)} />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* ═══════ Action Buttons ═══════ */}
      <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={() => doSearch(0)} disabled={loading}>
          {loading ? 'Searching...' : 'Search'}
        </button>
        <button className="wq-btn wq-btn-outline" onClick={handleReset}>Reset</button>
      </div>

      {/* ═══════ Search Results ═══════ */}
      {searched && (
        <div className="wq-panel">
          <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h4>Search Results {totalElements > 0 ? `(${totalElements})` : ''}</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => navigate('/persons/referral/new')}>
              Continue Referral
            </button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {results.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: '#4a5568', fontSize: '0.9rem' }}>
                No Match Found
              </div>
            ) : (
              <>
                <div style={{ overflowX: 'auto' }}>
                  <table className="wq-table" style={{ minWidth: '1100px' }}>
                    <thead>
                      <tr>
                        <th>Full Name</th>
                        <th>SSN</th>
                        <th>Type</th>
                        <th>CIN</th>
                        <th>Date of Birth</th>
                        <th>Gender</th>
                        <th>Person Type</th>
                        <th>Status</th>
                        <th>Residence Address</th>
                        <th>City</th>
                        <th>County</th>
                      </tr>
                    </thead>
                    <tbody>
                      {results.map((r, i) => (
                        <tr key={r.id || i}>
                          <td>
                            <a href="#" className="action-link" style={{ fontWeight: 600 }}
                              onClick={e => { e.preventDefault(); navigate(`/recipients/${r.id}`); }}>
                              {[r.lastName, r.firstName].filter(Boolean).join(', ')}
                            </a>
                          </td>
                          <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{maskSsn(r.ssn)}</td>
                          <td style={{ fontSize: '0.8rem' }}>
                            {r.ssnType === 'DUPLICATE_SSN' ? 'Duplicate SSN' : r.ssnType === 'SUSPECT_SSN' ? 'Suspect SSN' : ''}
                          </td>
                          <td>{r.cin || ''}</td>
                          <td>{formatDob(r.dateOfBirth)}</td>
                          <td>{r.gender || ''}</td>
                          <td>{PERSON_TYPE_LABELS[r.personType] || r.personType || ''}</td>
                          <td>{getStatusByPersonType(r)}</td>
                          <td style={{ fontSize: '0.8rem' }}>
                            {[r.residenceStreetNumber, r.residenceStreetName].filter(Boolean).join(' ')}
                          </td>
                          <td>{r.residenceCity || ''}</td>
                          <td>{countyName(r.countyCode)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* Pagination: <<Previous / Next>> */}
                {totalPages > 1 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 1rem', borderTop: '1px solid #e2e8f0' }}>
                    <div>
                      {page > 0 && (
                        <a href="#" className="action-link" onClick={e => { e.preventDefault(); doSearch(page - 1); }}>
                          &laquo;&laquo;Previous
                        </a>
                      )}
                    </div>
                    <span style={{ fontSize: '0.825rem', color: '#4a5568' }}>
                      Showing {page * PAGE_SIZE + 1}-{Math.min((page + 1) * PAGE_SIZE, totalElements)} of {totalElements}
                    </span>
                    <div>
                      {page < totalPages - 1 && (
                        <a href="#" className="action-link" onClick={e => { e.preventDefault(); doSearch(page + 1); }}>
                          Next&raquo;&raquo;
                        </a>
                      )}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
