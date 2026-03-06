import React, { useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { RESCIND_REASONS } from '../../../constants/caseCodeTables';
import http from '../../../api/httpClient';

/**
 * Rescind Case Screen - DSD Section 3.4 (CI-67690)
 * From Case Home: rescind a termination or denial.
 * Case returns to the status prior to Termination or Denial.
 * Fields: Rescind Date (read-only, today), Last Medi-Cal Eligibility Month (read-only),
 *         Reason (dropdown), Status (read-only)
 * Actions: Save, Cancel, CIN Look-up (conditional)
 */

const NAV_LINKS = [{ label: 'Case Home', route: '/case/ihss-case-home' }];

export function CaseRescindCasePage() {
  const navigate = useNavigate();
  const { id: paramId } = useParams();
  const [searchParams] = useSearchParams();
  const caseId = paramId || searchParams.get('caseId');

  const today = new Date().toLocaleDateString('en-US');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSave = async () => {
    if (!reason) { setError('Rescind Reason is required.'); return; }
    setSaving(true);
    setError('');
    try {
      await http.put(`/cases/${caseId}/rescind`, { reason });
      alert('Case rescinded successfully. Case status restored to prior status.');
      navigate(-1);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Rescind failed.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <UimPageLayout pageId="Case_rescindCase" title="Rescind Case" navLinks={NAV_LINKS} hidePlaceholderBanner>
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <div style={{ background: 'white', padding: '1.5rem', borderRadius: '8px', border: '1px solid #dee2e6', maxWidth: 600 }}>
        <h3 style={{ marginTop: 0, color: '#153554' }}>Rescind Case</h3>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          Rescinding restores the case to its status prior to Termination or Denial.
          Depending on the reason, specific NOA messages will be generated.
        </p>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>Rescind Date</label>
          <input type="text" value={today} readOnly disabled
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4, background: '#e9ecef' }} />
          <small style={{ color: '#666' }}>Automatically set to today's date.</small>
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Reason <span style={{ color: 'red' }}>*</span>
          </label>
          <select value={reason} onChange={e => setReason(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }}>
            <option value="">-- Select Rescind Reason --</option>
            {Object.entries(RESCIND_REASONS).map(([code, desc]) => (
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

export default CaseRescindCasePage;
