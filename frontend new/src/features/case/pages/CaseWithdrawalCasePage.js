import React, { useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { WITHDRAWAL_REASONS } from '../../../constants/caseCodeTables';
import http from '../../../api/httpClient';

/**
 * Withdraw Case Screen - DSD Section 3.1 (CI-67696)
 * Pop-up from Case Home: user enters withdrawal date and reason.
 * Status changes to Withdrawn when the calendar date reaches the Withdrawal Date.
 * A case in 'Withdrawn' status cannot be rescinded — use 'New Application' instead.
 */

const NAV_LINKS = [{ label: 'Case Home', route: '/cases' }];

export function CaseWithdrawalCasePage() {
  const navigate = useNavigate();
  const { id: paramId } = useParams();
  const [searchParams] = useSearchParams();
  const caseId = paramId || searchParams.get('caseId');

  const today = new Date().toISOString().split('T')[0];
  const [withdrawalDate, setWithdrawalDate] = useState(today);
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSave = async () => {
    if (!reason) { setError('Reason is required.'); return; }
    if (!withdrawalDate) { setError('Withdrawal Date is required.'); return; }
    setSaving(true);
    setError('');
    try {
      await http.put(`/cases/${caseId}/withdraw`, { reason, withdrawalDate });
      alert('Case withdrawn successfully.');
      navigate(-1);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Withdrawal failed.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <UimPageLayout pageId="Case_withdrawalCase" title="Withdraw Case" navLinks={NAV_LINKS} hidePlaceholderBanner>
      {error && <div className="uim-info-banner" style={{ background: '#f8d7da', borderColor: '#f5c6cb', color: '#721c24' }}>{error}</div>}

      <div style={{ background: 'white', padding: '1.5rem', borderRadius: '8px', border: '1px solid #dee2e6', maxWidth: 600 }}>
        <h3 style={{ marginTop: 0, color: '#153554' }}>Withdraw Application</h3>
        <p style={{ color: '#666', fontSize: '0.9rem' }}>
          Enter the date and reason for withdrawing this IHSS application.
        </p>

        <div style={{ marginBottom: '1rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Withdrawal Date <span style={{ color: 'red' }}>*</span>
          </label>
          <input type="date" value={withdrawalDate} max={today}
            onChange={e => setWithdrawalDate(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }} />
        </div>

        <div style={{ marginBottom: '1.5rem' }}>
          <label style={{ display: 'block', fontWeight: 600, marginBottom: 4 }}>
            Reason <span style={{ color: 'red' }}>*</span>
          </label>
          <select value={reason} onChange={e => setReason(e.target.value)}
            style={{ width: '100%', padding: '0.5rem', border: '1px solid #ced4da', borderRadius: 4 }}>
            <option value="">-- Select Reason --</option>
            {Object.entries(WITHDRAWAL_REASONS).map(([code, desc]) => (
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

export default CaseWithdrawalCasePage;
