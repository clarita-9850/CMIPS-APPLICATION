import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as paymentApi from '../api/paymentApi';
import './WorkQueues.css';

/**
 * Payment Search — DSD Section 27
 *
 * Two search modes:
 *   - By Person: payeeId, service period, issue dates, warrant number
 *   - By Case:   caseNumber, service period, issue dates, payee name, warrant number
 *
 * Results table → click row → View Payment Details
 * From detail: Void/Stop/Reissue, Cashed Warrant Copy Request, Forged Endorsement Affidavit
 */
export const PaymentSearchPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();

  const [tab, setTab] = useState('person'); // 'person' | 'case'
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [detail, setDetail] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // Search forms
  const [personForm, setPersonForm] = useState({
    payeeId: '', servicePeriodFrom: '', servicePeriodTo: '',
    issueFrom: '', issueTo: '', warrantNumber: ''
  });
  const [caseForm, setCaseForm] = useState({
    caseNumber: '', servicePeriodFrom: '', servicePeriodTo: '',
    issueFrom: '', issueTo: '', payeeName: '', warrantNumber: ''
  });

  // Modals
  const [voidModal, setVoidModal] = useState(false);
  const [voidForm, setVoidForm] = useState({ requestType: 'STOP', voidReason: 'CANCELLED', notes: '' });
  const [cashedModal, setCashedModal] = useState(false);
  const [cashedReason, setCashedReason] = useState('');
  const [affidavitModal, setAffidavitModal] = useState(false);
  const [affidavitForm, setAffidavitForm] = useState({ affidavitSignedDate: '', notes: '' });
  const [actionSuccess, setActionSuccess] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'My Workspace', path: '/workspace' },
      { label: 'Payment Search', path: '/payments/search' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handlePersonSearch = async (e) => {
    e.preventDefault();
    setError(''); setResults([]); setDetail(null);
    setLoading(true);
    try {
      const data = await paymentApi.searchByPerson(personForm);
      setResults(Array.isArray(data) ? data : []);
    } catch {
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleCaseSearch = async (e) => {
    e.preventDefault();
    setError(''); setResults([]); setDetail(null);
    setLoading(true);
    try {
      const data = await paymentApi.searchByCase(caseForm);
      setResults(Array.isArray(data) ? data : []);
    } catch {
      setError('Search failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleViewDetail = async (warrantId) => {
    setDetailLoading(true); setError(''); setDetail(null);
    setActionSuccess(''); setActionError('');
    try {
      const data = await paymentApi.getPaymentDetails(warrantId);
      setDetail(data);
    } catch {
      setError('Could not load payment details.');
    } finally {
      setDetailLoading(false);
    }
  };

  const handleVoidReissue = async () => {
    setActionLoading(true); setActionError('');
    try {
      await paymentApi.requestVoidOrReissue(detail.id, voidForm);
      setActionSuccess('Request submitted successfully.');
      setVoidModal(false);
      handleViewDetail(detail.id);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Action failed.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleCashedCopy = async () => {
    setActionLoading(true); setActionError('');
    try {
      await paymentApi.requestCashedCopy(detail.id, { reason: cashedReason });
      setActionSuccess('Cashed warrant copy request submitted.');
      setCashedModal(false);
      handleViewDetail(detail.id);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Action failed.');
    } finally {
      setActionLoading(false);
    }
  };

  const handleAffidavit = async () => {
    setActionLoading(true); setActionError('');
    try {
      await paymentApi.createAffidavit(detail.id, affidavitForm);
      setActionSuccess('Forged endorsement affidavit recorded.');
      setAffidavitModal(false);
      handleViewDetail(detail.id);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Action failed.');
    } finally {
      setActionLoading(false);
    }
  };

  const fmt = (val) => val || '—';
  const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '—';
  const statusBadge = (s) => {
    const map = {
      ISSUED: '#bee3f8', PAID: '#c6f6d5', VOIDED: '#fed7d7',
      STALE: '#feebc8', PENDING_REPLACEMENT: '#e9d8fd'
    };
    return (
      <span style={{
        background: map[s] || '#e2e8f0', padding: '2px 8px',
        borderRadius: 12, fontSize: 12, fontWeight: 600
      }}>{s}</span>
    );
  };

  return (
    <div className="wq-page">
      <div className="wq-header">
        <h1 className="wq-title">Payment Search</h1>
        <p className="wq-subtitle">DSD Section 27 — Search warrants by person or case</p>
      </div>

      {/* Search Tabs */}
      <div className="wq-card" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', gap: 8, marginBottom: 20 }}>
          {['person', 'case'].map(t => (
            <button key={t} onClick={() => { setTab(t); setResults([]); setDetail(null); }}
              className={tab === t ? 'wq-btn wq-btn-primary' : 'wq-btn wq-btn-secondary'}>
              {t === 'person' ? 'Search by Person' : 'Search by Case'}
            </button>
          ))}
        </div>

        {tab === 'person' && (
          <form onSubmit={handlePersonSearch}>
            <div className="wq-form-grid">
              <div className="wq-form-group">
                <label className="wq-label">Payee ID</label>
                <input className="wq-input" value={personForm.payeeId}
                  onChange={e => setPersonForm(f => ({ ...f, payeeId: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Warrant Number</label>
                <input className="wq-input" value={personForm.warrantNumber}
                  onChange={e => setPersonForm(f => ({ ...f, warrantNumber: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Service Period From</label>
                <input type="date" className="wq-input" value={personForm.servicePeriodFrom}
                  onChange={e => setPersonForm(f => ({ ...f, servicePeriodFrom: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Service Period To</label>
                <input type="date" className="wq-input" value={personForm.servicePeriodTo}
                  onChange={e => setPersonForm(f => ({ ...f, servicePeriodTo: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Issue Date From</label>
                <input type="date" className="wq-input" value={personForm.issueFrom}
                  onChange={e => setPersonForm(f => ({ ...f, issueFrom: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Issue Date To</label>
                <input type="date" className="wq-input" value={personForm.issueTo}
                  onChange={e => setPersonForm(f => ({ ...f, issueTo: e.target.value }))} />
              </div>
            </div>
            <button type="submit" className="wq-btn wq-btn-primary" disabled={loading}>
              {loading ? 'Searching…' : 'Search'}
            </button>
          </form>
        )}

        {tab === 'case' && (
          <form onSubmit={handleCaseSearch}>
            <div className="wq-form-grid">
              <div className="wq-form-group">
                <label className="wq-label">Case Number</label>
                <input className="wq-input" value={caseForm.caseNumber}
                  onChange={e => setCaseForm(f => ({ ...f, caseNumber: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Payee Name</label>
                <input className="wq-input" value={caseForm.payeeName}
                  onChange={e => setCaseForm(f => ({ ...f, payeeName: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Warrant Number</label>
                <input className="wq-input" value={caseForm.warrantNumber}
                  onChange={e => setCaseForm(f => ({ ...f, warrantNumber: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Service Period From</label>
                <input type="date" className="wq-input" value={caseForm.servicePeriodFrom}
                  onChange={e => setCaseForm(f => ({ ...f, servicePeriodFrom: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Service Period To</label>
                <input type="date" className="wq-input" value={caseForm.servicePeriodTo}
                  onChange={e => setCaseForm(f => ({ ...f, servicePeriodTo: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Issue Date From</label>
                <input type="date" className="wq-input" value={caseForm.issueFrom}
                  onChange={e => setCaseForm(f => ({ ...f, issueFrom: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Issue Date To</label>
                <input type="date" className="wq-input" value={caseForm.issueTo}
                  onChange={e => setCaseForm(f => ({ ...f, issueTo: e.target.value }))} />
              </div>
            </div>
            <button type="submit" className="wq-btn wq-btn-primary" disabled={loading}>
              {loading ? 'Searching…' : 'Search'}
            </button>
          </form>
        )}
      </div>

      {error && <div className="wq-error-msg">{error}</div>}

      {/* Results */}
      {results.length > 0 && !detail && (
        <div className="wq-card" style={{ marginBottom: 24 }}>
          <div className="wq-card-header">
            <h2 className="wq-section-title">Results ({results.length})</h2>
          </div>
          <table className="wq-table">
            <thead>
              <tr>
                <th>Warrant Number</th>
                <th>Payee Name</th>
                <th>Case Number</th>
                <th>Amount</th>
                <th>Issue Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {results.map(r => (
                <tr key={r.id}>
                  <td>{fmt(r.warrantNumber)}</td>
                  <td>{fmt(r.payeeName)}</td>
                  <td>{fmt(r.caseNumber)}</td>
                  <td>{r.amount != null ? `$${Number(r.amount).toFixed(2)}` : '—'}</td>
                  <td>{fmtDate(r.issueDate)}</td>
                  <td>{statusBadge(r.status)}</td>
                  <td>
                    <button className="wq-btn wq-btn-secondary wq-btn-sm"
                      onClick={() => handleViewDetail(r.id)}>
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Payment Detail */}
      {detailLoading && <div className="wq-loading">Loading payment details…</div>}

      {detail && (
        <div className="wq-card">
          <div className="wq-card-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 className="wq-section-title">Payment Detail — Warrant #{detail.warrantNumber}</h2>
            <div style={{ display: 'flex', gap: 8 }}>
              <button className="wq-btn wq-btn-secondary wq-btn-sm" onClick={() => setDetail(null)}>
                ← Back to Results
              </button>
              {(detail.status === 'ISSUED' || detail.status === 'STALE') && (
                <button className="wq-btn wq-btn-primary wq-btn-sm" onClick={() => setVoidModal(true)}>
                  Void / Reissue
                </button>
              )}
              <button className="wq-btn wq-btn-secondary wq-btn-sm" onClick={() => setCashedModal(true)}>
                Cashed Copy Request
              </button>
              <button className="wq-btn wq-btn-secondary wq-btn-sm" onClick={() => setAffidavitModal(true)}>
                Forged Endorsement
              </button>
            </div>
          </div>

          {actionSuccess && <div className="wq-success-msg">{actionSuccess}</div>}
          {actionError && <div className="wq-error-msg">{actionError}</div>}

          <div className="wq-form-grid" style={{ marginBottom: 24 }}>
            <div><span className="wq-label">Warrant Number</span><div>{fmt(detail.warrantNumber)}</div></div>
            <div><span className="wq-label">Status</span><div>{statusBadge(detail.status)}</div></div>
            <div><span className="wq-label">Amount</span><div>${detail.amount != null ? Number(detail.amount).toFixed(2) : '—'}</div></div>
            <div><span className="wq-label">Issue Date</span><div>{fmtDate(detail.issueDate)}</div></div>
            <div><span className="wq-label">Pay Type</span><div>{fmt(detail.payType)}</div></div>
            <div><span className="wq-label">Payee Name</span><div>{fmt(detail.payeeName)}</div></div>
            <div><span className="wq-label">Recipient Name</span><div>{fmt(detail.recipientName)}</div></div>
            <div><span className="wq-label">Case Number</span><div>{fmt(detail.caseNumber)}</div></div>
            <div><span className="wq-label">Service Period</span><div>{fmtDate(detail.payPeriodStart)} – {fmtDate(detail.payPeriodEnd)}</div></div>
            <div><span className="wq-label">County Code</span><div>{fmt(detail.countyCode)}</div></div>
            <div><span className="wq-label">Funding Source</span><div>{fmt(detail.fundingSource)}</div></div>
          </div>

          {/* Void/Reissue Activity */}
          {detail.voidReissueActivity && detail.voidReissueActivity.length > 0 && (
            <div style={{ marginBottom: 20 }}>
              <h3 className="wq-section-title" style={{ fontSize: 14 }}>Void / Reissue Activity</h3>
              <table className="wq-table">
                <thead>
                  <tr><th>Request Type</th><th>Void Reason</th><th>Status</th><th>Requested By</th><th>Date</th></tr>
                </thead>
                <tbody>
                  {detail.voidReissueActivity.map(v => (
                    <tr key={v.id}>
                      <td>{v.requestType}</td>
                      <td>{v.voidReason}</td>
                      <td>{v.requestStatus}</td>
                      <td>{v.requestedBy}</td>
                      <td>{fmtDate(v.requestedAt)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Cashed Warrant Copies */}
          {detail.cashedWarrantCopies && detail.cashedWarrantCopies.length > 0 && (
            <div style={{ marginBottom: 20 }}>
              <h3 className="wq-section-title" style={{ fontSize: 14 }}>Cashed Warrant Copy Requests</h3>
              <table className="wq-table">
                <thead>
                  <tr><th>Request Date</th><th>Reason</th><th>Status</th><th>SCO Response</th></tr>
                </thead>
                <tbody>
                  {detail.cashedWarrantCopies.map(c => (
                    <tr key={c.id}>
                      <td>{fmtDate(c.requestDate)}</td>
                      <td>{c.reason}</td>
                      <td>{c.status}</td>
                      <td>{c.scoResponseNotes || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Forged Endorsement Affidavits */}
          {detail.forgedEndorsementAffidavits && detail.forgedEndorsementAffidavits.length > 0 && (
            <div>
              <h3 className="wq-section-title" style={{ fontSize: 14 }}>Forged Endorsement Affidavits</h3>
              <table className="wq-table">
                <thead>
                  <tr><th>Signed Date</th><th>Submitted to SCO</th><th>SCO Response</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {detail.forgedEndorsementAffidavits.map(a => (
                    <tr key={a.id}>
                      <td>{fmtDate(a.affidavitSignedDate)}</td>
                      <td>{fmtDate(a.submittedToScoDate)}</td>
                      <td>{a.scoResponse || '—'}</td>
                      <td>{a.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Void/Reissue Modal */}
      {voidModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header">
              <h3>Void / Stop / Reissue Payment</h3>
              <button className="wq-modal-close" onClick={() => setVoidModal(false)}>✕</button>
            </div>
            <div className="wq-modal-body">
              {actionError && <div className="wq-error-msg">{actionError}</div>}
              <div className="wq-form-group">
                <label className="wq-label">Request Type *</label>
                <select className="wq-input" value={voidForm.requestType}
                  onChange={e => setVoidForm(f => ({ ...f, requestType: e.target.value }))}>
                  <option value="STOP">Stop (Void, No Replacement)</option>
                  <option value="REISSUE">Reissue (Stop + New Warrant)</option>
                  <option value="REPLACEMENT">Replacement (STD 435, Same Warrant #)</option>
                  <option value="REDEPOSIT">Redeposit (Undeliverable)</option>
                </select>
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Void Reason *</label>
                <select className="wq-input" value={voidForm.voidReason}
                  onChange={e => setVoidForm(f => ({ ...f, voidReason: e.target.value }))}>
                  <option value="CANCELLED">Cancelled</option>
                  <option value="INCORRECT_INFORMATION">Incorrect Information</option>
                  <option value="PAYEE_INELIGIBLE">Payee Ineligible</option>
                  <option value="DAMAGED">Damaged</option>
                  <option value="LOST">Lost</option>
                  <option value="STOLEN">Stolen</option>
                  <option value="DESTROYED">Destroyed</option>
                  <option value="NEVER_RECEIVED">Never Received</option>
                  <option value="UNDELIVERABLE">Undeliverable</option>
                </select>
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={3} value={voidForm.notes}
                  onChange={e => setVoidForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setVoidModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleVoidReissue} disabled={actionLoading}>
                {actionLoading ? 'Submitting…' : 'Submit Request'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Cashed Copy Modal */}
      {cashedModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header">
              <h3>Request Cashed Warrant Copy</h3>
              <button className="wq-modal-close" onClick={() => setCashedModal(false)}>✕</button>
            </div>
            <div className="wq-modal-body">
              {actionError && <div className="wq-error-msg">{actionError}</div>}
              <p style={{ color: '#4a5568', marginBottom: 16 }}>
                Request a copy of the cashed warrant from SCO (State Controller's Office).
              </p>
              <div className="wq-form-group">
                <label className="wq-label">Reason</label>
                <textarea className="wq-input" rows={3} value={cashedReason}
                  onChange={e => setCashedReason(e.target.value)}
                  placeholder="Reason for requesting cashed warrant copy…" />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setCashedModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCashedCopy} disabled={actionLoading}>
                {actionLoading ? 'Submitting…' : 'Submit Request'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Affidavit Modal */}
      {affidavitModal && (
        <div className="wq-modal-overlay">
          <div className="wq-modal">
            <div className="wq-modal-header">
              <h3>Record Forged Endorsement Affidavit (STO CA 0034)</h3>
              <button className="wq-modal-close" onClick={() => setAffidavitModal(false)}>✕</button>
            </div>
            <div className="wq-modal-body">
              {actionError && <div className="wq-error-msg">{actionError}</div>}
              <div className="wq-form-group">
                <label className="wq-label">Affidavit Signed Date *</label>
                <input type="date" className="wq-input" value={affidavitForm.affidavitSignedDate}
                  onChange={e => setAffidavitForm(f => ({ ...f, affidavitSignedDate: e.target.value }))} />
              </div>
              <div className="wq-form-group">
                <label className="wq-label">Notes</label>
                <textarea className="wq-input" rows={3} value={affidavitForm.notes}
                  onChange={e => setAffidavitForm(f => ({ ...f, notes: e.target.value }))} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-secondary" onClick={() => setAffidavitModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleAffidavit} disabled={actionLoading}>
                {actionLoading ? 'Saving…' : 'Save Affidavit'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
