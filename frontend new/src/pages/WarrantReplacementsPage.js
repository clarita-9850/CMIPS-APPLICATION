import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as warrantApi from '../api/warrantApi';
import './WorkQueues.css';

/**
 * Enter Warrant Replacements — DSD Section 27 (CI-459396, CI-459400, CI-459401)
 *
 * Three-screen flow:
 *   1. LIST  — Previously entered warrant replacements + "New" button
 *   2. ENTER — Data entry: Replacement Date, Warrant Number, Issue Date + Continue/Cancel
 *   3. DETAILS — Confirmation: read-only warrant details + editable Replacement Date + Save/Save & New/Cancel
 */
export const WarrantReplacementsPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  // View state: 'list' | 'enter' | 'details'
  const [view, setView] = useState('list');
  const [replacements, setReplacements] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Screen 2: Enter form
  const [entryForm, setEntryForm] = useState({ replacementDate: '', warrantNumber: '', issueDate: '' });

  // Screen 3: Detail from lookup
  const [detail, setDetail] = useState(null);
  const [editReplacementDate, setEditReplacementDate] = useState('');
  const [saving, setSaving] = useState(false);

  // Load replacements list when on list view
  useEffect(() => {
    if (view === 'list') loadReplacements();
  }, [view]);

  // Update breadcrumbs based on current view
  useEffect(() => {
    const crumbs = [
      { label: 'My Workspace', path: '/workspace' },
      { label: 'Warrant Replacements', path: '/payments/warrant-replacements' },
    ];
    if (view === 'enter') crumbs.push({ label: 'Enter Warrant Replacement' });
    if (view === 'details') {
      crumbs.push({ label: 'Enter Warrant Replacement' });
      crumbs.push({ label: 'Details' });
    }
    setBreadcrumbs(crumbs);
    return () => setBreadcrumbs([]);
  }, [view, setBreadcrumbs]);

  const loadReplacements = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await warrantApi.listReplacements();
      setReplacements(Array.isArray(data) ? data : []);
    } catch (err) {
      setReplacements([]);
    } finally {
      setLoading(false);
    }
  };

  // ── Screen 2: Continue handler ──
  const handleContinue = async () => {
    setError('');
    if (!entryForm.replacementDate) { setError('Replacement Date is required.'); return; }
    if (!entryForm.warrantNumber.trim()) { setError('Warrant Number is required.'); return; }
    if (!entryForm.issueDate) { setError('Issue Date is required.'); return; }

    setLoading(true);
    try {
      const resp = await warrantApi.lookupForReplacement({
        replacementDate: entryForm.replacementDate,
        warrantNumber: entryForm.warrantNumber.trim(),
        issueDate: entryForm.issueDate,
      });
      setDetail(resp);
      setEditReplacementDate(entryForm.replacementDate);
      setView('details');
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Lookup failed.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // ── Screen 3: Save handler ──
  const handleSave = async (thenNew = false) => {
    setError('');
    if (!editReplacementDate) { setError('Replacement Date is required.'); return; }

    setSaving(true);
    try {
      await warrantApi.saveWarrantReplacement({
        warrantId: detail.warrantId,
        replacementDate: editReplacementDate,
      });
      if (thenNew) {
        setSuccess('Warrant replacement saved successfully.');
        setDetail(null);
        setEntryForm({ replacementDate: '', warrantNumber: '', issueDate: '' });
        setView('enter');
      } else {
        setSuccess('Warrant replacement saved successfully.');
        setDetail(null);
        setEntryForm({ replacementDate: '', warrantNumber: '', issueDate: '' });
        setView('list');
      }
    } catch (err) {
      const msg = err?.response?.data?.message || err?.message || 'Save failed.';
      setError(msg);
    } finally {
      setSaving(false);
    }
  };

  const handleCancelEntry = () => {
    setEntryForm({ replacementDate: '', warrantNumber: '', issueDate: '' });
    setError('');
    setView('list');
  };

  const handleCancelDetails = () => {
    setDetail(null);
    setError('');
    setView('list');
  };

  const handleNew = () => {
    setSuccess('');
    setError('');
    setEntryForm({ replacementDate: '', warrantNumber: '', issueDate: '' });
    setView('enter');
  };

  // ── Formatters ──
  const formatDate = (d) => {
    if (!d) return '\u2014';
    const date = new Date(d + (d.length === 10 ? 'T00:00:00' : ''));
    return isNaN(date.getTime()) ? d : date.toLocaleDateString('en-US');
  };
  const formatAmount = (a) => a != null ? `$${Number(a).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : '\u2014';

  const pageTitle = view === 'list' ? 'Warrant Replacements'
    : view === 'enter' ? 'Enter Warrant Replacement'
    : 'Enter Warrant Replacement \u2013 Details';

  return (
    <div className="wq-page">
      {/* Page Header */}
      <div className="wq-page-header">
        <h2>{pageTitle}</h2>
        {view === 'list' && (
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
      {/* SCREEN 1: WARRANT REPLACEMENTS LIST        */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'list' && (
        <div className="wq-panel">
          <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h4>Previously Entered Warrant Replacements</h4>
            <button className="wq-btn wq-btn-primary" onClick={handleNew}>New</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? (
              <div style={{ textAlign: 'center', padding: '2rem', color: '#718096' }}>Loading...</div>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Entry Date</th>
                    <th>Replacement Date</th>
                    <th>Warrant Number</th>
                    <th>Issue Date</th>
                    <th>Net Amount</th>
                    <th>County</th>
                    <th>Case Number</th>
                    <th>Recipient Name</th>
                    <th>Payee Number</th>
                    <th>Payee Name</th>
                  </tr>
                </thead>
                <tbody>
                  {replacements.length === 0 ? (
                    <tr>
                      <td colSpan={10} style={{ textAlign: 'center', padding: '2rem', color: '#a0aec0' }}>
                        No warrant replacements have been entered yet.
                      </td>
                    </tr>
                  ) : replacements.map((w, i) => (
                    <tr key={w.id || i}>
                      <td>{formatDate(w.replacementEntryDate)}</td>
                      <td>{formatDate(w.replacementDate)}</td>
                      <td>{w.warrantNumber}</td>
                      <td>{formatDate(w.issueDate)}</td>
                      <td>{formatAmount(w.amount)}</td>
                      <td>{w.countyCode || '\u2014'}</td>
                      <td>{w.caseNumber || '\u2014'}</td>
                      <td>{w.recipientName || '\u2014'}</td>
                      <td>{w.providerId || '\u2014'}</td>
                      <td>{w.payeeName || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 2: ENTER WARRANT REPLACEMENT        */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'enter' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Enter Warrant Replacement Information</h4></div>
          <div className="wq-panel-body">
            <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
              Enter the information from the Replacement Warrant Detail Report provided by SCO.
              Based on Warrant Number and Issue Date, the system will look up the matching warrant.
            </p>
            <div style={{ fontSize: '0.8rem', color: '#c53030', marginBottom: '1rem' }}>* = required field</div>
            <div className="wq-search-grid" style={{ maxWidth: '600px' }}>
              <div className="wq-form-field">
                <label>Replacement Date <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="date"
                  value={entryForm.replacementDate}
                  onChange={e => setEntryForm(f => ({ ...f, replacementDate: e.target.value }))}
                />
                <span style={{ fontSize: '0.75rem', color: '#718096' }}>Date on which the warrant was replaced by SCO</span>
              </div>
              <div className="wq-form-field">
                <label>Warrant Number <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="text"
                  value={entryForm.warrantNumber}
                  onChange={e => setEntryForm(f => ({ ...f, warrantNumber: e.target.value }))}
                  placeholder="Enter warrant number"
                />
                <span style={{ fontSize: '0.75rem', color: '#718096' }}>Identifier of the warrant issued by SCO</span>
              </div>
              <div className="wq-form-field">
                <label>Issue Date <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="date"
                  value={entryForm.issueDate}
                  onChange={e => setEntryForm(f => ({ ...f, issueDate: e.target.value }))}
                />
                <span style={{ fontSize: '0.75rem', color: '#718096' }}>Date on which the payment was issued by SCO</span>
              </div>
            </div>
            <div className="wq-search-actions" style={{ marginTop: '1.5rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleContinue} disabled={loading}>
                {loading ? 'Looking up...' : 'Continue'}
              </button>
              <button className="wq-btn wq-btn-outline" onClick={handleCancelEntry}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 3: ENTER WARRANT REPLACEMENT – DETAILS */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'details' && detail && (
        <>
          <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
            Verify the information below. The Replacement Date can be edited if needed. Click Save to complete the entry.
          </p>

          {/* Payee Section */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Payee</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Payee Number" value={detail.payeeNumber} />
                <DetailField label="Payee Name" value={detail.payeeName} />
              </div>
            </div>
          </div>

          {/* Case Section */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Case</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Case Number" value={detail.caseNumber} />
                <DetailField label="Recipient Name" value={detail.recipientName} />
                <DetailField label="County" value={detail.county} />
              </div>
            </div>
          </div>

          {/* Payment / Warrant Information */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Payment / Warrant Information</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Warrant Number" value={detail.warrantNumber} />
                <DetailField label="Net Amount" value={formatAmount(detail.netAmount)} />
                <DetailField label="Issue Date" value={formatDate(detail.issueDate)} />
                <DetailField label="Funding Source" value={detail.fundingSource} />
                <div>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: '#4a5568', textTransform: 'uppercase', letterSpacing: '0.03em', marginBottom: '0.25rem' }}>
                    Replacement Date <span style={{ color: '#c53030' }}>*</span>
                  </div>
                  <input
                    type="date"
                    value={editReplacementDate}
                    onChange={e => setEditReplacementDate(e.target.value)}
                    style={{ padding: '0.375rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem' }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Pay Event Section */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Pay Event</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Pay Type" value={detail.payType} />
                <DetailField label="Pay Period" value={
                  detail.payPeriodFrom || detail.payPeriodTo
                    ? `${formatDate(detail.payPeriodFrom)} \u2013 ${formatDate(detail.payPeriodTo)}`
                    : null
                } />
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={() => handleSave(false)} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button className="wq-btn wq-btn-primary" onClick={() => handleSave(true)} disabled={saving}>
              Save & New
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleCancelDetails}>Cancel</button>
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
