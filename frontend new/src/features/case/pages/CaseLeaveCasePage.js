import React, { useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { LEAVE_REASONS } from '../../../constants/caseCodeTables';
import http from '../../../api/httpClient';

/**
 * Leave Case Screen - DSD Section 3.2 (CI-67679)
 * Pop-up from Case Home: user enters Authorization End Date, Leave Reason,
 * and conditionally Resource Suspension End Date (if reason is L0006).
 * When saved, case status updates to Leave and NOA is produced.
 */

const NAV_LINKS = [{ label: 'Case Home', route: '/cases' }];

export function CaseLeaveCasePage() {
  const navigate = useNavigate();
  const { id: paramId } = useParams();
  const [searchParams] = useSearchParams();
  const caseId = paramId || searchParams.get('caseId');

  const [authorizationEndDate, setAuthorizationEndDate] = useState('');
  const [resourceSuspensionEndDate, setResourceSuspensionEndDate] = useState('');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const isUndervalueDisposal = reason === 'L0006';

  const handleSave = async () => {
    if (!authorizationEndDate) { setError('Authorization End Date is required.'); return; }
    if (!reason) { setError('Reason is required.'); return; }
    if (isUndervalueDisposal && !resourceSuspensionEndDate) {
      setError('Resource Suspension End Date is required when reason is "Undervalue disposal of resources".');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await http.put(`/cases/${caseId}/leave`, {
        reason,
        authorizationEndDate,
        resourceSuspensionEndDate: isUndervalueDisposal ? resourceSuspensionEndDate : null,
      });
      alert('Case placed on Leave successfully.');
      navigate(-1);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Leave action failed.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <UimPageLayout pageId="Case_leaveCase" title="Leave Case" navLinks={NAV_LINKS} hidePlaceholderBanner>
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <div style={{ background: 'white', padding: '1.5rem', borderRadius: '8px', border: '1px solid #dee2e6', maxWidth: 600 }}>
        <h3 style={{ marginTop: 0, color: '#153554' }}>Place Case on Leave</h3>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          Enter the Authorization End Date and leave reason. Payments will not be made after this date.
        </p>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Authorization End Date <span style={{ color: 'red' }}>*</span>
          </label>
          <input type="date" value={authorizationEndDate}
            onChange={e => setAuthorizationEndDate(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Reason <span style={{ color: 'red' }}>*</span>
          </label>
          <select value={reason} onChange={e => setReason(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }}>
            <option value="">-- Select Reason --</option>
            {Object.entries(LEAVE_REASONS).map(([code, desc]) => (
              <option key={code} value={code}>{code} - {desc}</option>
            ))}
          </select>
        </div>

        {isUndervalueDisposal && (
          <div style={{ marginBottom: '1rem', background: '#fff3cd', padding: '1rem', borderRadius: 4 }}>
            <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
              Resource Suspension End Date <span style={{ color: 'red' }}>*</span>
            </label>
            <input type="date" value={resourceSuspensionEndDate}
              onChange={e => setResourceSuspensionEndDate(e.target.value)}
              style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
            <small style={{ color: '#856404' }}>Required for "Undervalue disposal of resources" reason.</small>
          </div>
        )}

        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
          <button className="uim-btn uim-btn-primary" onClick={handleSave} disabled={saving}>
            {saving ? 'Saving...' : 'Save'}
          </button>
          <button className="uim-btn" onClick={() => navigate(-1)}>Cancel</button>
        </div>
      </div>
    </UimPageLayout>
  );
}

export default CaseLeaveCasePage;
