import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as providersApi from '../api/providersApi';
import './WorkQueues.css';

/**
 * IRS Live-In Provider Self-Certification — DSD Section 32 (CI-718023, CI-718024)
 *
 * Two-screen flow:
 *   1. SEARCH — Provider Number + Case Number → Continue/Cancel
 *   2. ENTRY  — Read-only details + Self-Certification Status dropdown → Save/Cancel
 */
export const LiveInProviderPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  // View state: 'search' | 'entry'
  const [view, setView] = useState('search');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Screen 1: Search form
  const [searchForm, setSearchForm] = useState({ providerNumber: '', caseNumber: '' });

  // Screen 2: Entry details + editable status
  const [detail, setDetail] = useState(null);
  const [certStatus, setCertStatus] = useState('');
  const [saving, setSaving] = useState(false);

  // Update breadcrumbs based on current view
  useEffect(() => {
    const crumbs = [
      { label: 'My Workspace', path: '/workspace' },
      { label: 'IRS Live-In Provider Self-Certification', path: '/providers/live-in' },
    ];
    if (view === 'entry') crumbs.push({ label: 'Certification Entry' });
    setBreadcrumbs(crumbs);
    return () => setBreadcrumbs([]);
  }, [view, setBreadcrumbs]);

  // ── Screen 1: Continue handler ──
  const handleContinue = async () => {
    setError('');
    if (!searchForm.providerNumber.trim()) { setError('Provider Number is required.'); return; }
    if (!searchForm.caseNumber.trim()) { setError('Case Number is required.'); return; }

    setLoading(true);
    try {
      const resp = await providersApi.lookupLiveInCert({
        providerNumber: searchForm.providerNumber.trim(),
        caseNumber: searchForm.caseNumber.trim(),
      });
      setDetail(resp);
      setCertStatus(resp.currentCertificationStatus || '');
      setView('entry');
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Lookup failed.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Screen 2: Save handler ──
  const handleSave = async () => {
    setError('');
    if (!certStatus) { setError('Self-Certification Status is required.'); return; }

    setSaving(true);
    try {
      await providersApi.saveLiveInCert({
        providerId: detail.providerId,
        caseId: detail.caseId,
        certificationStatus: certStatus,
      });
      setSuccess('Self-certification saved successfully.');
      setDetail(null);
      setCertStatus('');
      setSearchForm({ providerNumber: '', caseNumber: '' });
      setView('search');
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Save failed.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  const handleCancelSearch = () => {
    setSearchForm({ providerNumber: '', caseNumber: '' });
    setError('');
  };

  const handleCancelEntry = () => {
    setDetail(null);
    setCertStatus('');
    setError('');
    setView('search');
  };

  // Format date for display
  const formatDate = (d) => {
    if (!d) return '\u2014';
    const date = new Date(d + (d.length === 10 ? 'T00:00:00' : ''));
    return isNaN(date.getTime()) ? d : date.toLocaleDateString('en-US');
  };

  const todayFormatted = new Date().toLocaleDateString('en-US');

  const pageTitle = view === 'search'
    ? 'IRS Live-In Provider Self-Certification Search'
    : 'IRS Live-In Provider Self-Certification Entry';

  return (
    <div className="wq-page">
      {/* Page Header */}
      <div className="wq-page-header">
        <h2>{pageTitle}</h2>
        {view === 'search' && (
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/workspace')}>Back to Workspace</button>
        )}
      </div>

      {/* Success Banner */}
      {success && (
        <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.75rem 1rem', borderRadius: '6px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>{success}</span>
          <button onClick={() => setSuccess('')} style={{ background: 'none', border: 'none', cursor: 'pointer', fontSize: '1.1rem', color: '#276749' }}>&times;</button>
        </div>
      )}

      {/* Error Banner */}
      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.75rem 1rem', borderRadius: '6px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 1: SEARCH (CI-718023)               */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'search' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Search by Provider Number and Case Number</h4></div>
          <div className="wq-panel-body">
            <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
              Enter the Provider Number and Case Number from the Self-Certification form (SOC 2298 / SOC 2299)
              to locate the provider-case assignment in CMIPS.
            </p>
            <div style={{ fontSize: '0.8rem', color: '#c53030', marginBottom: '1rem' }}>* = required field</div>
            <div className="wq-search-grid" style={{ maxWidth: '500px' }}>
              <div className="wq-form-field">
                <label>Provider Number <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="text"
                  value={searchForm.providerNumber}
                  onChange={e => setSearchForm(f => ({ ...f, providerNumber: e.target.value }))}
                  placeholder="Enter provider number"
                />
              </div>
              <div className="wq-form-field">
                <label>Case Number <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="text"
                  value={searchForm.caseNumber}
                  onChange={e => setSearchForm(f => ({ ...f, caseNumber: e.target.value }))}
                  placeholder="Enter case number"
                />
              </div>
            </div>
            <div className="wq-search-actions" style={{ marginTop: '1.5rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleContinue} disabled={loading}>
                {loading ? 'Validating...' : 'Continue'}
              </button>
              <button className="wq-btn wq-btn-outline" onClick={handleCancelSearch}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 2: ENTRY (CI-718024)                */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'entry' && detail && (
        <>
          <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
            Verify the provider and case details below. Set the Self-Certification Status and click Save to complete the entry.
          </p>

          {/* Details Section */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Provider &amp; Case Details</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Provider County" value={detail.providerCounty} />
                <DetailField label="Provider Number" value={detail.providerNumber} />
                <DetailField label="Provider Name" value={detail.providerName} />
                <DetailField label="Case Number" value={detail.caseNumber} />
                <DetailField label="Recipient Name" value={detail.recipientName} />
              </div>
            </div>
          </div>

          {/* Certification Section */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Self-Certification</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <div>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#4a5568', textTransform: 'uppercase', letterSpacing: '0.03em', marginBottom: '0.25rem' }}>
                    Self-Certification Status <span style={{ color: '#c53030' }}>*</span>
                  </div>
                  <select
                    value={certStatus}
                    onChange={e => setCertStatus(e.target.value)}
                    style={{ padding: '0.375rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem', minWidth: '180px' }}
                  >
                    <option value="">-- Select --</option>
                    <option value="YES">Yes</option>
                    <option value="NO">No</option>
                  </select>
                  <div style={{ fontSize: '0.75rem', color: '#718096', marginTop: '0.25rem' }}>
                    Yes = Live-In Excluded (wages excluded from federal/state gross income).
                    No = Cancel prior certification.
                  </div>
                </div>
                <DetailField label="Status Date" value={todayFormatted} />
              </div>
              {detail.currentCertificationStatus && (
                <div style={{ marginTop: '1rem', padding: '0.5rem 0.75rem', background: '#ebf8ff', border: '1px solid #90cdf4', borderRadius: '4px', fontSize: '0.8rem', color: '#2b6cb0' }}>
                  Current certification status: <strong>{detail.currentCertificationStatus}</strong>
                  {detail.statusDate && <> (as of {formatDate(detail.statusDate.toString ? detail.statusDate.toString() : detail.statusDate)})</>}
                </div>
              )}
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={handleSave} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleCancelEntry}>Cancel</button>
          </div>
        </>
      )}
    </div>
  );
};

/** Read-only detail field */
const DetailField = ({ label, value }) => (
  <div>
    <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#4a5568', textTransform: 'uppercase', letterSpacing: '0.03em', marginBottom: '0.25rem' }}>
      {label}
    </div>
    <div style={{ fontSize: '0.875rem', color: '#1a202c' }}>
      {value || '\u2014'}
    </div>
  </div>
);
