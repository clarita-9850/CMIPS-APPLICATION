import React, { useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { TERMINATION_REASONS } from '../../../constants/caseCodeTables';
import http from '../../../api/httpClient';

/**
 * Terminate Case Screen - DSD Section 3.3 (CI-67716)
 * Pop-up from Case Home: user enters Authorization End Date and Termination Reason.
 * Payments will not be allowed after the Authorization End Date.
 * Status changes to Terminated when the calendar date reaches the Authorization End Date.
 */

const NAV_LINKS = [{ label: 'Case Home', route: '/cases' }];

export function CaseTerminateCasePage() {
  const navigate = useNavigate();
  const { id: paramId } = useParams();
  const [searchParams] = useSearchParams();
  const caseId = paramId || searchParams.get('caseId');

  const [authorizationEndDate, setAuthorizationEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSave = async () => {
    if (!authorizationEndDate) { setError('Authorization End Date is required.'); return; }
    if (!reason) { setError('Reason is required.'); return; }
    setSaving(true);
    setError('');
    try {
      await http.put(`/cases/${caseId}/terminate`, { reason, authorizationEndDate });
      alert('Case terminated successfully.');
      navigate(-1);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Termination failed.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <UimPageLayout pageId="Case_terminateCase" title="Terminate Case" navLinks={NAV_LINKS} hidePlaceholderBanner>
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <div style={{ background: 'white', padding: '1.5rem', borderRadius: '8px', border: '1px solid #dee2e6', maxWidth: 600 }}>
        <h3 style={{ marginTop: 0, color: '#153554' }}>Terminate Case</h3>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          Enter the last date services are authorized and the termination reason.
          A terminated authorization segment will be created.
        </p>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Authorization End Date <span style={{ color: 'red' }}>*</span>
          </label>
          <input type="date" value={authorizationEndDate}
            onChange={e => setAuthorizationEndDate(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
          <small style={{ color: '#666' }}>Last date the case should be paid. May not be more than one month in the future.</small>
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Reason <span style={{ color: 'red' }}>*</span>
          </label>
          <select value={reason} onChange={e => setReason(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }}>
            <option value="">-- Select Reason --</option>
            {Object.entries(TERMINATION_REASONS).map(([code, desc]) => (
              <option key={code} value={code}>{code} - {desc}</option>
            ))}
          </select>
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

export default CaseTerminateCasePage;
