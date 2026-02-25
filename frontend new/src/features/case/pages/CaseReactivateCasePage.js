import React, { useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { REFERRAL_SOURCES, RESIDENCY_REQUIREMENTS } from '../../../constants/caseCodeTables';
import http from '../../../api/httpClient';

/**
 * Reactivate Case Screen - DSD Section 3.6 (CI-67688)
 * "New Application" link from Case Home for Terminated/Denied/Withdrawn cases.
 * Save changes case status from Terminated/Denied/Withdrawn to Pending.
 * Fields: Interpreter Available (checkbox), IHSS Referral Date (date),
 *         Meets Residency Requirement (dropdown), Referral Source (dropdown),
 *         Assigned Worker (search), Client Index Number (search)
 */

const NAV_LINKS = [{ label: 'Case Home', route: '/cases' }];

export function CaseReactivateCasePage() {
  const navigate = useNavigate();
  const { id: paramId } = useParams();
  const [searchParams] = useSearchParams();
  const caseId = paramId || searchParams.get('caseId');

  const today = new Date().toISOString().split('T')[0];
  const [interpreterAvailable, setInterpreterAvailable] = useState(false);
  const [referralDate, setReferralDate] = useState(today);
  const [meetsResidencyRequirement, setMeetsResidencyRequirement] = useState('');
  const [referralSource, setReferralSource] = useState('');
  const [assignedWorkerId, setAssignedWorkerId] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSave = async () => {
    if (!referralDate) { setError('IHSS Referral Date is required.'); return; }
    if (!meetsResidencyRequirement) { setError('Meets Residency Requirement is required.'); return; }
    if (!referralSource) { setError('Referral Source is required.'); return; }
    setSaving(true);
    setError('');
    try {
      await http.put(`/cases/${caseId}/reactivate`, {
        referralDate,
        meetsResidencyRequirement,
        referralSource,
        interpreterAvailable,
        assignedWorkerId: assignedWorkerId || null,
      });
      alert('Case reactivated successfully. Status changed to Pending.');
      navigate(-1);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Reactivation failed.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <UimPageLayout pageId="Case_reactivateCase" title="Reactivate Case (New Application)" navLinks={NAV_LINKS} hidePlaceholderBanner>
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <div style={{ background: 'white', padding: '1.5rem', borderRadius: '8px', border: '1px solid #dee2e6', maxWidth: 600 }}>
        <h3 style={{ marginTop: 0, color: '#153554' }}>Reactivate Case</h3>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          This creates a new application for a previously Terminated, Denied, or Withdrawn case.
          All evidence must be re-entered as a new case.
        </p>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 600 }}>
            <input type="checkbox" checked={interpreterAvailable}
              onChange={e => setInterpreterAvailable(e.target.checked)} />
            Interpreter Available
          </label>
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            IHSS Referral Date <span style={{ color: 'red' }}>*</span>
          </label>
          <input type="date" value={referralDate} max={today}
            onChange={e => setReferralDate(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
          <small style={{ color: '#666' }}>May not be more than 14 days prior to today or in the future.</small>
        </div>

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

        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>Assigned Worker ID</label>
          <input type="text" value={assignedWorkerId} placeholder="Enter worker ID or leave blank"
            onChange={e => setAssignedWorkerId(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
          <button className="uim-btn" onClick={() => navigate(-1)}>Cancel</button>
        </div>
      </div>
    </UimPageLayout>
  );
}

export default CaseReactivateCasePage;
