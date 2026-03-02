import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { REFERRAL_SOURCES, RESIDENCY_REQUIREMENTS } from '../../../constants/caseCodeTables';
import http from '../../../api/httpClient';
import { CINSearchModal } from '../../../components/cin/CINSearchModal';
import { MediCalEligibilityModal } from '../../../components/cin/MediCalEligibilityModal';
import { CINDataMismatchModal } from '../../../components/cin/CINDataMismatchModal';
import { CreateCaseWithoutCINModal } from '../../../components/cin/CreateCaseWithoutCINModal';
import { UserSearchModal } from '../../../components/UserSearchModal';

/**
 * Reactivate Case Screen - DSD Section 3.6 (CI-67688)
 * "New Application" link from Case Home for Terminated/Denied/Withdrawn cases.
 * Save changes case status from Terminated/Denied/Withdrawn to Pending.
 * Fields: Interpreter Available (checkbox), IHSS Referral Date (date),
 *         Meets Residency Requirement (dropdown), Referral Source (dropdown),
 *         Assigned Worker (search, required), CIN Clearance (required per EM#117)
 */

const NAV_LINKS = [{ label: 'Case Home', route: '/cases' }];

export function CaseReactivateCasePage() {
  const navigate = useNavigate();
  const { id: paramId } = useParams();
  const [searchParams] = useSearchParams();
  const caseId = paramId || searchParams.get('caseId');

  const today = new Date().toISOString().split('T')[0];

  // Form fields
  const [interpreterAvailable, setInterpreterAvailable] = useState(false);
  const [referralDate, setReferralDate] = useState(today);
  const [meetsResidencyRequirement, setMeetsResidencyRequirement] = useState('');
  const [referralSource, setReferralSource] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  // Worker search
  const [assignedWorkerId, setAssignedWorkerId] = useState('');
  const [selectedWorker, setSelectedWorker] = useState(null);
  const [showWorkerSearch, setShowWorkerSearch] = useState(false);

  // CIN flow
  const [cinClearanceStatus, setCinClearanceStatus] = useState('NOT_STARTED');
  const [cin, setCin] = useState('');
  const [mediCalStatus, setMediCalStatus] = useState('');
  const [aidCode, setAidCode] = useState('');
  const [showCINSearch, setShowCINSearch] = useState(false);
  const [showEligibility, setShowEligibility] = useState(false);
  const [showMismatch, setShowMismatch] = useState(false);
  const [showWithoutCIN, setShowWithoutCIN] = useState(false);
  const [eligibilityData, setEligibilityData] = useState(null);
  const [applicantData, setApplicantData] = useState(null);

  // Load case + recipient data on mount for CIN search
  useEffect(() => {
    if (!caseId) return;
    http.get(`/cases/${caseId}`).then(res => {
      const c = res.data;
      setApplicantData({
        lastName: c.recipientLastName || '',
        firstName: c.recipientFirstName || '',
        dob: c.recipientDob || '',
        recipientId: c.recipientId,
      });
    }).catch(() => {});
  }, [caseId]);

  // --- CIN Flow Handlers (mirrors ApplicationsNewPage) ---
  const handleStartCINSearch = () => {
    setShowCINSearch(true);
  };

  const handleCINSearchCancel = () => {
    setShowCINSearch(false);
    // When user cancels after a NO_MATCH search, mark CIN clearance as performed
    // so EM#117 doesn't block save. An S1 referral will be sent to county SAWS (BR-9).
    setCinClearanceStatus('NO_MATCH');
  };

  const handleShowEligibility = (data) => {
    setEligibilityData(data);
    setShowCINSearch(false);
    setShowEligibility(true);
  };

  const handleSelectSuccess = (data) => {
    setCin(data.cin || '');
    setMediCalStatus(data.mediCalStatus || '');
    setAidCode(data.aidCode || '');
    setCinClearanceStatus('CLEARED');
    setShowEligibility(false);
    setShowCINSearch(false);
  };

  const handleEligibilityMismatch = () => {
    setShowEligibility(false);
    setShowMismatch(true);
  };

  const handleReturnToCINSelect = () => {
    setShowMismatch(false);
    setShowCINSearch(true);
  };

  const handleProceedWithoutCIN = () => {
    setShowMismatch(false);
    setCinClearanceStatus('WITHOUT_CIN');
    setShowWithoutCIN(true);
  };

  const handleCancelReactivation = () => {
    setShowMismatch(false);
    navigate(-1);
  };

  const handleWithoutCINContinue = () => {
    setShowWithoutCIN(false);
  };

  // --- Worker Search Handlers ---
  const handleWorkerSelect = (worker) => {
    setSelectedWorker(worker);
    setAssignedWorkerId(worker.id?.toString() || worker.userId || '');
    setShowWorkerSearch(false);
  };

  const handleClearWorker = () => {
    setSelectedWorker(null);
    setAssignedWorkerId('');
  };

  // --- Save ---
  const handleSave = async () => {
    if (!referralDate) { setError('IHSS Referral Date is required.'); return; }
    if (!meetsResidencyRequirement) { setError('Meets Residency Requirement is required.'); return; }
    if (!referralSource) { setError('Referral Source is required.'); return; }
    if (!assignedWorkerId) { setError('Assigned Worker is required. Please use the Search button to select a worker.'); return; }
    if (cinClearanceStatus === 'NOT_STARTED') { setError('EM#117: CIN Clearance must be performed before case reactivation.'); return; }

    setSaving(true);
    setError('');
    try {
      await http.put(`/cases/${caseId}/reactivate`, {
        referralDate,
        meetsResidencyRequirement,
        referralSource,
        interpreterAvailable,
        assignedWorkerId,
        cinClearanceStatus,
      });
      alert('Case reactivated successfully. Status changed to Pending.');
      navigate(-1);
    } catch (err) {
      const msg = err.response?.data?.error || err.response?.data?.message || err.message || 'Reactivation failed.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  // CIN status badge
  const cinStatusLabel = {
    NOT_STARTED: 'Not Started',
    CLEARED: 'Cleared',
    WITHOUT_CIN: 'Proceeding without CIN',
    NO_MATCH: 'No CIN Found',
  }[cinClearanceStatus] || cinClearanceStatus;

  const cinStatusColor = {
    NOT_STARTED: '#6c757d',
    CLEARED: '#28a745',
    WITHOUT_CIN: '#fd7e14',
    NO_MATCH: '#dd6b20',
  }[cinClearanceStatus] || '#6c757d';

  return (
    <UimPageLayout pageId="Case_reactivateCase" title="Reactivate Case (New Application)" navLinks={NAV_LINKS} hidePlaceholderBanner>
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <div style={{ background: 'white', padding: '1.5rem', borderRadius: '8px', border: '1px solid #dee2e6', maxWidth: 600 }}>
        <h3 style={{ marginTop: 0, color: '#153554' }}>Reactivate Case</h3>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          This creates a new application for a previously Terminated, Denied, or Withdrawn case.
          All evidence must be re-entered as a new case.
        </p>

        {/* Interpreter Available */}
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
            <input type="checkbox" checked={interpreterAvailable}
              onChange={e => setInterpreterAvailable(e.target.checked)} />
            Interpreter Available
          </label>
        </div>

        {/* IHSS Referral Date */}
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            IHSS Referral Date <span style={{ color: 'red' }}>*</span>
          </label>
          <input type="date" value={referralDate} max={today}
            onChange={e => setReferralDate(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
          <small style={{ color: '#666' }}>May not be more than 14 days prior to today or in the future.</small>
        </div>

        {/* Meets Residency Requirement */}
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Meets Residency Requirement <span style={{ color: 'red' }}>*</span>
          </label>
          <select value={meetsResidencyRequirement}
            onChange={e => setMeetsResidencyRequirement(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }}>
            <option value="">-- Select --</option>
            {Object.entries(RESIDENCY_REQUIREMENTS).map(([code, desc]) => (
              <option key={code} value={code}>{desc}</option>
            ))}
          </select>
        </div>

        {/* Referral Source */}
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Referral Source <span style={{ color: 'red' }}>*</span>
          </label>
          <select value={referralSource} onChange={e => setReferralSource(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }}>
            <option value="">-- Select --</option>
            {Object.entries(REFERRAL_SOURCES).map(([code, desc]) => (
              <option key={code} value={code}>{desc}</option>
            ))}
          </select>
        </div>

        {/* Assigned Worker (required, via UserSearchModal) */}
        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Assigned Worker <span style={{ color: 'red' }}>*</span>
          </label>
          {selectedWorker ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', padding: '0.5rem',
              background: '#e8f5e9', border: '1px solid #a5d6a7', borderRadius: 4 }}>
              <span style={{ fontWeight: 500 }}>
                {selectedWorker.firstName} {selectedWorker.lastName}
                {selectedWorker.userId && <span style={{ color: '#666', marginLeft: 4 }}>({selectedWorker.userId})</span>}
              </span>
              <button type="button" onClick={handleClearWorker}
                style={{ marginLeft: 'auto', background: 'none', border: 'none', color: '#c62828', cursor: 'pointer', fontWeight: 600 }}>
                Clear
              </button>
            </div>
          ) : (
            <button type="button" className="uim-btn uim-btn-secondary" onClick={() => setShowWorkerSearch(true)}>
              Search Worker...
            </button>
          )}
        </div>

        {/* CIN Clearance (required per EM#117) */}
        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            CIN Clearance <span style={{ color: 'red' }}>*</span>
          </label>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <button type="button" className="uim-btn uim-btn-secondary"
              onClick={handleStartCINSearch}
              disabled={cinClearanceStatus === 'CLEARED'}>
              {cinClearanceStatus === 'NOT_STARTED' ? 'Search CIN...' : 'Re-search CIN'}
            </button>
            <span style={{
              display: 'inline-block', padding: '0.25rem 0.75rem', borderRadius: 12,
              fontSize: '0.85rem', fontWeight: 600, color: 'white', background: cinStatusColor,
            }}>
              {cinStatusLabel}
            </span>
          </div>
          {cin && (
            <div style={{ marginTop: 6, fontSize: '0.9rem', color: '#333' }}>
              CIN: <strong>{cin}</strong>
              {mediCalStatus && <> &mdash; Medi-Cal: {mediCalStatus}</>}
              {aidCode && <> &mdash; Aid Code: {aidCode}</>}
            </div>
          )}
          {cinClearanceStatus === 'NO_MATCH' && (
            <div style={{ background: '#fffbeb', border: '1px solid #f6ad55', borderLeft: '4px solid #dd6b20',
              borderRadius: '4px', padding: '0.5rem 1rem', fontSize: '0.875rem', color: '#744210', marginTop: '0.5rem' }}>
              <strong>No CIN found.</strong> You may proceed without a CIN. An S1 referral will be sent to county SAWS (BR-9) when the case is reactivated.
            </div>
          )}
          <small style={{ color: '#666' }}>EM#117: CIN Clearance must be performed before reactivation.</small>
        </div>

        {/* Action Buttons */}
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
          <button className="uim-btn" onClick={() => navigate(-1)}>Cancel</button>
        </div>
      </div>

      {/* CIN Modals */}
      {showCINSearch && (
        <CINSearchModal
          applicantData={applicantData}
          applicationId={caseId}
          onShowEligibility={handleShowEligibility}
          onCancel={handleCINSearchCancel}
        />
      )}
      {showEligibility && (
        <MediCalEligibilityModal
          eligibilityData={eligibilityData}
          applicantData={applicantData}
          applicationId={caseId}
          onSelectSuccess={handleSelectSuccess}
          onMismatch={handleEligibilityMismatch}
          onCancel={() => setShowEligibility(false)}
        />
      )}
      {showMismatch && (
        <CINDataMismatchModal
          onReturnToCINSelect={handleReturnToCINSelect}
          onProceedWithoutCIN={handleProceedWithoutCIN}
          onCancelCreateCase={handleCancelReactivation}
        />
      )}
      {showWithoutCIN && (
        <CreateCaseWithoutCINModal
          onContinue={handleWithoutCINContinue}
          onCancel={() => { setShowWithoutCIN(false); setCinClearanceStatus('NOT_STARTED'); }}
          saving={false}
        />
      )}

      {/* Worker Search Modal */}
      {showWorkerSearch && (
        <UserSearchModal
          onSelect={handleWorkerSelect}
          onCancel={() => setShowWorkerSearch(false)}
        />
      )}
    </UimPageLayout>
  );
}

export default CaseReactivateCasePage;
