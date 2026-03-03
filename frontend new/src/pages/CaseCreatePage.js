import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as casesApi from '../api/casesApi';
import { CINSearchModal }          from '../components/cin/CINSearchModal';
import { MediCalEligibilityModal } from '../components/cin/MediCalEligibilityModal';
import { CINDataMismatchModal }    from '../components/cin/CINDataMismatchModal';
import { CreateCaseWithoutCINModal } from '../components/cin/CreateCaseWithoutCINModal';
import { UserSearchModal }         from '../components/UserSearchModal';
import './WorkQueues.css';

// CIN clearance status badge styles
const BADGE_STYLE = {
  NOT_STARTED:     { background: '#e2e8f0', color: '#4a5568' },
  IN_PROGRESS:     { background: '#ebf8ff', color: '#2b6cb0' },
  CLEARED:         { background: '#f0fff4', color: '#276749' },
  EXACT_MATCH:     { background: '#f0fff4', color: '#276749' },
  POSSIBLE_MATCHES:{ background: '#fffbeb', color: '#975a16' },
  NO_MATCH:        { background: '#fff5f5', color: '#c53030' },
  MISMATCH_REVIEW: { background: '#fff5f5', color: '#c53030' },
  FAILED:          { background: '#fff5f5', color: '#c53030' },
};

const BADGE_LABEL = {
  NOT_STARTED:     'Not Performed',
  IN_PROGRESS:     'In Progress',
  CLEARED:         'CIN Cleared',
  EXACT_MATCH:     'CIN Cleared',
  POSSIBLE_MATCHES:'Matches Found',
  NO_MATCH:        'No CIN Match',
  MISMATCH_REVIEW: 'Mismatch – Review',
  FAILED:          'Failed',
};

export const CaseCreatePage = () => {
  const navigate  = useNavigate();
  const { user }  = useAuth();
  const username  = user?.username || user?.preferred_username || 'unknown';

  const [searchParams] = useSearchParams();

  // Demographics pre-populated from Application record (DSD CI-67772: System Populated, Not Editable)
  const isPrePopulated = !!(searchParams.get('lastName') || searchParams.get('firstName'));

  const [saving, setSaving] = useState(false);
  const [error,  setError]  = useState('');

  // Core form fields — demographics pre-filled from URL params when coming from Application flow
  const [form, setForm] = useState({
    // Demographics (system-populated from application when isPrePopulated)
    lastName:          searchParams.get('lastName') || '',
    firstName:         searchParams.get('firstName') || '',
    gender:            searchParams.get('gender') || '',
    dateOfBirth:       searchParams.get('dateOfBirth') || '',
    ssn:               searchParams.get('ssn') || '',
    mediCalPseudo:     false,
    // CIN (populated after clearance)
    cin:               '',
    // Case fields (system-populated from application)
    countyCode:        searchParams.get('countyCode') || '',
    zipCode:           searchParams.get('zipCode') || '',
    spokenLanguage:    searchParams.get('spokenLanguage') || '',
    writtenLanguage:   searchParams.get('writtenLanguage') || '',
    interpreterAvailable: false,
    caseOwnerId:       '',
    ihssReferralDate:  new Date().toISOString().slice(0, 10),  // DSD: default to today
  });

  // Selected worker from User Search modal (DSD CI-67746)
  const [selectedWorker, setSelectedWorker] = useState(null);
  const [showWorkerSearch, setShowWorkerSearch] = useState(false);

  // CIN Clearance state
  const [applicationId,         setApplicationId]        = useState('');
  const [cinClearancePerformed,  setCinClearancePerformed] = useState(false);
  const [cinClearanceStatus,     setCinClearanceStatus]    = useState('NOT_STARTED');
  const [mediCalStatus,          setMediCalStatus]         = useState('');
  const [aidCode,                setAidCode]               = useState('');

  // Modal visibility
  const [showCINSearch,   setShowCINSearch]   = useState(false);
  const [showEligibility, setShowEligibility] = useState(false);
  const [showMismatch,    setShowMismatch]    = useState(false);
  const [showWithoutCIN,  setShowWithoutCIN]  = useState(false);

  // Data passed between modals
  const [eligibilityData, setEligibilityData] = useState(null);

  // ── Field change handler ──────────────────────────────────────────────────
  const handleChange = (field, value) => {
    // BR-14: changing any demographic field resets CIN clearance status
    if (['lastName','firstName','gender','dateOfBirth'].includes(field)) {
      setForm(prev => ({ ...prev, [field]: value, cin: '' }));
      setCinClearancePerformed(false);
      setCinClearanceStatus('NOT_STARTED');
      setMediCalStatus('');
      setAidCode('');
    } else {
      setForm(prev => ({ ...prev, [field]: value }));
    }
  };

  // ── Worker Search select handler (DSD CI-67746) ─────────────────────────────
  const handleWorkerSelect = (worker) => {
    setSelectedWorker(worker);
    setForm(prev => ({ ...prev, caseOwnerId: worker.workerNumber }));
    setShowWorkerSearch(false);
  };

  // ── Open CIN Search modal ─────────────────────────────────────────────────
  const handleCINLookup = () => {
    if (!form.lastName.trim()) { setError('Last Name is required before CIN Clearance.'); return; }
    if (!form.firstName.trim()) { setError('First Name is required before CIN Clearance.'); return; }
    setError('');
    setCinClearanceStatus('IN_PROGRESS');
    setShowCINSearch(true);
  };

  // ── CINSearchModal → user clicks "MEDS Eligibility" on a row ─────────────
  const handleShowEligibility = (cin, data) => {
    setEligibilityData(data);
    setShowCINSearch(false);
    setShowEligibility(true);
  };

  // ── CINSearchModal → Cancel (Scenario 2 / no match) ──────────────────────
  const handleCINSearchCancel = () => {
    setShowCINSearch(false);
    setCinClearancePerformed(true);
    setCinClearanceStatus('NO_MATCH');
  };

  // ── MediCalEligibilityModal → Select succeeded (Scenario 4) ──────────────
  const handleSelectSuccess = ({ cin, mediCalStatus: ms, aidCode: ac }) => {
    setForm(prev => ({ ...prev, cin }));
    setMediCalStatus(ms);
    setAidCode(ac);
    setCinClearancePerformed(true);
    setCinClearanceStatus('CLEARED');
    setShowEligibility(false);
    setEligibilityData(null);
  };

  // ── MediCalEligibilityModal → demographic mismatch (Scenario 5) ──────────
  const handleEligibilityMismatch = () => {
    setShowEligibility(false);
    setCinClearanceStatus('MISMATCH_REVIEW');
    setShowMismatch(true);
  };

  // ── MediCalEligibilityModal → Cancel (back to search) ────────────────────
  const handleEligibilityCancel = () => {
    setShowEligibility(false);
    setShowCINSearch(true);
  };

  // ── CINDataMismatchModal → Return to CIN Select ───────────────────────────
  const handleReturnToCINSelect = () => {
    setShowMismatch(false);
    setShowCINSearch(true);
    setCinClearanceStatus('IN_PROGRESS');
  };

  // ── CreateCaseWithoutCINModal → Continue (BR 9 / S1 path) ────────────────
  const handleWithoutCINContinue = () => {
    setShowWithoutCIN(false);
    setSaving(true);
    casesApi.createCase({
      ...form,
      createdBy: username,
      applicantName: `${form.firstName} ${form.lastName}`.trim(),
      cinClearanceStatus,
      mediCalStatus: 'PENDING_SAWS',
    })
      .then(data => {
        const caseId = data?.id || data?.case?.id || '';
        // EM OS 186: Store informational message for Case Home display
        if (data?.infoMessage) {
          sessionStorage.setItem('caseInfoMessage', data.infoMessage);
        }
        navigate(`/cases/${caseId}`);
      })
      .catch(err => setError(err?.response?.data?.error || err?.response?.data?.message || err.message || 'Failed to create case'))
      .finally(() => setSaving(false));
  };

  // ── CreateCaseWithoutCINModal → Cancel (return to Create Case form) ────────
  // Bug fix: previously navigated to /applications/new, discarding the form.
  // Correct behaviour: close the modal and return the worker to the Create Case form.
  const handleWithoutCINCancel = () => {
    setShowWithoutCIN(false);
  };

  // ── Main Save ─────────────────────────────────────────────────────────────
  const handleSave = () => {
    if (!form.firstName.trim() || !form.lastName.trim()) {
      setError('First Name and Last Name are required.');
      return;
    }
    if (!form.countyCode.trim()) { setError('County is required.'); return; }

    // EM OS 067: Assigned Worker is required
    if (!form.caseOwnerId.trim()) {
      setError('EM OS 067: Assigned Worker must be indicated.');
      return;
    }

    // EM OS 175: IHSS Referral Date may not be more than 2 weeks in the future
    if (form.ihssReferralDate) {
      const refDate = new Date(form.ihssReferralDate + 'T00:00:00');
      const today = new Date(); today.setHours(0,0,0,0);
      const maxDate = new Date(today); maxDate.setDate(maxDate.getDate() + 14);
      if (refDate > maxDate) {
        setError('EM OS 175: IHSS Referral Date may not be more than two weeks in the future.');
        return;
      }
    }

    // CIN present → save normally
    if (form.cin.trim()) {
      setError('');
      setSaving(true);
      casesApi.createCase({
        ...form,
        createdBy: username,
        applicantName: `${form.firstName} ${form.lastName}`.trim(),
        cinClearanceStatus,
        mediCalStatus,
        aidCode,
      })
        .then(data => {
          const caseId = data?.id || data?.case?.id || '';
          // EM OS 186: Show informational message if SAWS referral sent
          if (data?.infoMessage) {
            sessionStorage.setItem('caseInfoMessage', data.infoMessage);
          }
          navigate(`/cases/${caseId}`);
        })
        .catch(err => setError(err?.response?.data?.error || err?.response?.data?.message || err.message || 'Failed to create case'))
        .finally(() => setSaving(false));
      return;
    }

    // EM OS 176: CIN clearance not yet performed
    if (!cinClearancePerformed) {
      setError('EM OS 176: CIN Clearance must be performed before saving. Click the 🔍 icon next to the CIN field.');
      return;
    }

    // EM OS 185: Clearance done but no CIN found → prompt to continue without CIN
    setError('');
    setShowWithoutCIN(true);
  };

  // ── Helpers ───────────────────────────────────────────────────────────────
  const badgeStyle = BADGE_STYLE[cinClearanceStatus] || BADGE_STYLE.NOT_STARTED;
  const badgeLabel = BADGE_LABEL[cinClearanceStatus] || cinClearanceStatus;

  const applicantData = {
    lastName:      form.lastName,
    firstName:     form.firstName,
    dob:           form.dateOfBirth,
    gender:        form.gender,
    cin:           form.cin,
    ssn:           form.ssn,
    mediCalPseudo: form.mediCalPseudo,
  };

  // ── Render ─────────────────────────────────────────────────────────────────
  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>New Case</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Cancel</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      {/* ── Applicant Demographics (DSD CI-67772: System Populated, Not Editable when from Application) ── */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Applicant Demographics</h4></div>
        <div className="wq-panel-body">
          {isPrePopulated && (
            <div style={{ fontSize: '0.75rem', color: '#718096', marginBottom: '0.5rem', fontStyle: 'italic' }}>
              Demographics are system-populated from the application record and cannot be edited here.
            </div>
          )}
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label htmlFor="cc-lastName">Last Name *</label>
              <input
                id="cc-lastName"
                type="text"
                value={form.lastName}
                readOnly={isPrePopulated}
                onChange={e => handleChange('lastName', e.target.value)}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-firstName">First Name *</label>
              <input
                id="cc-firstName"
                type="text"
                value={form.firstName}
                readOnly={isPrePopulated}
                onChange={e => handleChange('firstName', e.target.value)}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-gender">Gender</label>
              <select id="cc-gender" value={form.gender}
                disabled={isPrePopulated}
                onChange={e => handleChange('gender', e.target.value)}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}>
                <option value="">-- Select --</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-dob">Date of Birth</label>
              <input
                id="cc-dob"
                type="date"
                value={form.dateOfBirth}
                readOnly={isPrePopulated}
                onChange={e => handleChange('dateOfBirth', e.target.value)}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-ssn">SSN</label>
              <input
                id="cc-ssn"
                type="text"
                value={form.ssn}
                readOnly={isPrePopulated}
                onChange={e => setForm(prev => ({ ...prev, ssn: e.target.value }))}
                placeholder="xxx-xx-xxxx"
                maxLength={11}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field" style={{ justifyContent: 'flex-end' }}>
              <label style={{ visibility: 'hidden' }}>_</label>
              <label htmlFor="cc-mediCalPseudo" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: isPrePopulated ? 'not-allowed' : 'pointer', fontSize: '0.875rem' }}>
                <input
                  id="cc-mediCalPseudo"
                  type="checkbox"
                  checked={form.mediCalPseudo}
                  disabled={isPrePopulated}
                  onChange={e => setForm(prev => ({ ...prev, mediCalPseudo: e.target.checked }))}
                />
                Medi-Cal Pseudo (no SSN to SCI)
              </label>
            </div>
          </div>
        </div>
      </div>

      {/* ── CIN Clearance ── */}
      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>CIN Clearance</h4>
          <span style={{
            fontSize: '0.75rem', padding: '2px 10px', borderRadius: '12px', fontWeight: 600,
            ...badgeStyle,
          }}>
            {badgeLabel}
          </span>
        </div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Client Index Number (CIN)</label>
              <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                <input
                  type="text"
                  value={form.cin}
                  readOnly
                  placeholder="Run CIN Clearance →"
                  style={{ flex: 1, backgroundColor: '#f7f9fb', cursor: 'not-allowed' }}
                />
                <button
                  type="button"
                  className="wq-btn wq-btn-outline"
                  onClick={handleCINLookup}
                  title="Run SCI CIN Clearance"
                  style={{ padding: '0.35rem 0.6rem', fontSize: '1rem', lineHeight: 1 }}
                >
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
              <strong>No CIN found.</strong> You may save this case without a CIN. An S1 referral will be sent to county SAWS (BR 9).
            </div>
          )}

          {cinClearanceStatus === 'MISMATCH_REVIEW' && (
            <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', borderRadius: '4px', padding: '0.5rem 1rem', fontSize: '0.875rem', color: '#c53030', marginTop: '0.5rem' }}>
              <strong>Demographic mismatch detected.</strong> Please click 🔍 to retry CIN clearance with corrected demographics.
            </div>
          )}
        </div>
      </div>

      {/* ── Location (DSD: System Populated from Application) ── */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Location</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label htmlFor="cc-county">County *</label>
              <input
                id="cc-county"
                type="text"
                value={form.countyCode}
                readOnly={isPrePopulated}
                onChange={e => setForm(prev => ({ ...prev, countyCode: e.target.value }))}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-zip">Zip Code</label>
              <input
                id="cc-zip"
                type="text"
                value={form.zipCode}
                readOnly={isPrePopulated}
                onChange={e => setForm(prev => ({ ...prev, zipCode: e.target.value }))}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
          </div>
        </div>
      </div>

      {/* ── Language (DSD: Spoken/Written System Populated; Interpreter Available is editable) ── */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Language</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label htmlFor="cc-spokenLang">Spoken Language</label>
              <input
                id="cc-spokenLang"
                type="text"
                value={form.spokenLanguage}
                readOnly={isPrePopulated}
                onChange={e => setForm(prev => ({ ...prev, spokenLanguage: e.target.value }))}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-writtenLang">Written Language</label>
              <input
                id="cc-writtenLang"
                type="text"
                value={form.writtenLanguage}
                readOnly={isPrePopulated}
                onChange={e => setForm(prev => ({ ...prev, writtenLanguage: e.target.value }))}
                style={isPrePopulated ? { backgroundColor: '#f7f9fb', cursor: 'not-allowed' } : {}}
              />
            </div>
            <div className="wq-form-field">
              <label htmlFor="cc-interpreter">Interpreter Available</label>
              <select
                id="cc-interpreter"
                value={form.interpreterAvailable ? 'yes' : 'no'}
                onChange={e => setForm(prev => ({ ...prev, interpreterAvailable: e.target.value === 'yes' }))}
              >
                <option value="no">No</option>
                <option value="yes">Yes</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* ── Assignment (DSD CI-67746 / CI-116204: Worker via User Search popup) ── */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Assignment</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label htmlFor="cc-worker">Assigned Worker *</label>
              <div style={{ display: 'flex', gap: '0.4rem', alignItems: 'center' }}>
                <input
                  id="cc-worker"
                  type="text"
                  value={selectedWorker
                    ? `${selectedWorker.lastName}, ${selectedWorker.firstName} (${selectedWorker.workerNumber})`
                    : ''}
                  readOnly
                  placeholder="Search for a worker →"
                  style={{ flex: 1, backgroundColor: '#f7f9fb', cursor: 'not-allowed' }}
                />
                <button
                  type="button"
                  className="wq-btn wq-btn-outline"
                  onClick={() => setShowWorkerSearch(true)}
                  title="Search for a worker (User Search)"
                  style={{ padding: '0.35rem 0.6rem', fontSize: '1rem', lineHeight: 1 }}
                >
                  🔍
                </button>
              </div>
              <span style={{ fontSize: '0.72rem', color: '#666', marginTop: '2px' }}>
                Click 🔍 to search and assign a case owner (DSD CI-67746)
              </span>
            </div>
            {selectedWorker && (
              <div className="wq-form-field">
                <label>Worker Details</label>
                <div style={{ fontSize: '0.85rem', color: '#4a5568', lineHeight: 1.6 }}>
                  <div><strong>District Office:</strong> {selectedWorker.districtOffice}</div>
                  <div><strong>Language:</strong> {selectedWorker.language}{selectedWorker.language2 ? `, ${selectedWorker.language2}` : ''}</div>
                  <div><strong>Case Count:</strong> {selectedWorker.caseCount || '—'}</div>
                </div>
              </div>
            )}
            <div className="wq-form-field">
              <label htmlFor="cc-referralDate">IHSS Referral Date</label>
              <input
                id="cc-referralDate"
                type="date"
                value={form.ihssReferralDate}
                onChange={e => setForm(prev => ({ ...prev, ihssReferralDate: e.target.value }))}
              />
              <span style={{ fontSize: '0.72rem', color: '#666', marginTop: '2px' }}>
                Defaults to today. May be post-dated up to 2 weeks.
              </span>
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

      {/* ── CIN Search Modal (OI Transaction) ── */}
      {showCINSearch && (
        <CINSearchModal
          applicantData={applicantData}
          applicationId={applicationId}
          onShowEligibility={handleShowEligibility}
          onCancel={handleCINSearchCancel}
        />
      )}

      {/* ── Medi-Cal Eligibility Modal (EL/OM Transaction) ── */}
      {showEligibility && eligibilityData && (
        <MediCalEligibilityModal
          eligibilityData={eligibilityData}
          applicantData={applicantData}
          applicationId={applicationId}
          onSelectSuccess={handleSelectSuccess}
          onMismatch={handleEligibilityMismatch}
          onCancel={handleEligibilityCancel}
        />
      )}

      {/* ── Demographic Mismatch Modal (Scenario 5) ── */}
      {showMismatch && (
        <CINDataMismatchModal onReturnToCINSelect={handleReturnToCINSelect} />
      )}

      {/* ── Create Case Without CIN Modal (EM OS 185 / BR 9) ── */}
      {showWithoutCIN && (
        <CreateCaseWithoutCINModal
          onContinue={handleWithoutCINContinue}
          onCancel={handleWithoutCINCancel}
          saving={saving}
        />
      )}

      {/* ── User Search Modal (DSD CI-67746 — Assign Case Owner) ── */}
      {showWorkerSearch && (
        <UserSearchModal
          onSelect={handleWorkerSelect}
          onCancel={() => setShowWorkerSearch(false)}
        />
      )}
    </div>
  );
};
