import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as issuanceApi from '../api/issuanceApi';
import './WorkQueues.css';

const STATUS_COLORS = {
  PENDING_GENERATION: { bg: '#fef3c7', color: '#92400e' },
  GENERATED: { bg: '#dbeafe', color: '#1e40af' },
  PENDING_MAIL: { bg: '#fef3c7', color: '#92400e' },
  MAILED: { bg: '#d1fae5', color: '#065f46' },
  DELIVERED_ELECTRONIC: { bg: '#d1fae5', color: '#065f46' },
  DELIVERED_IN_PERSON: { bg: '#d1fae5', color: '#065f46' },
  CANCELLED: { bg: '#f3f4f6', color: '#6b7280' },
  REISSUED: { bg: '#e0e7ff', color: '#3730a3' }
};

export const IssuancePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [tab, setTab] = useState('pending');
  const [issuances, setIssuances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchType, setSearchType] = useState('case');
  const [searchId, setSearchId] = useState('');
  const [processing, setProcessing] = useState(false);
  const [actionModal, setActionModal] = useState(null);
  const [actionInput, setActionInput] = useState('');
  const [createModal, setCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    caseId: '', recipientId: '', providerId: '', payPeriodStart: '', payPeriodEnd: '',
    timesheetType: 'STANDARD', programType: 'IHSS', issuanceMethod: 'ELECTRONIC', countyCode: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'Timesheet Issuance' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const loadPending = async (type) => {
    setLoading(true);
    try {
      let data;
      if (type === 'generation') data = await issuanceApi.listPendingGeneration();
      else data = await issuanceApi.listPendingPrint();
      setIssuances(data);
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const loadSearch = async () => {
    if (!searchId.trim()) return;
    setLoading(true);
    try {
      let data;
      if (searchType === 'case') data = await issuanceApi.listByCase(searchId.trim());
      else if (searchType === 'provider') data = await issuanceApi.listByProvider(searchId.trim());
      else data = [await issuanceApi.getByIssuanceNumber(searchId.trim())];
      setIssuances(data.filter(Boolean));
    } catch (err) { setIssuances([]); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    if (tab === 'pending') loadPending('generation');
    else if (tab === 'print') loadPending('print');
  }, [tab]);

  const handleGenerate = async (id) => {
    setProcessing(true);
    try {
      await issuanceApi.generateTimesheet(id);
      setSuccess('Timesheet generated.');
      if (tab === 'pending') loadPending('generation');
      else loadSearch();
    } catch (err) { alert('Failed: ' + (err?.response?.data?.error || err.message)); }
    finally { setProcessing(false); }
  };

  const handleMail = async (id) => {
    setProcessing(true);
    try {
      await issuanceApi.markMailed(id);
      setSuccess('Marked as mailed.');
      if (tab === 'print') loadPending('print');
      else loadSearch();
    } catch (err) { alert('Failed.'); }
    finally { setProcessing(false); }
  };

  const handleDeliverElectronic = async (id) => {
    setProcessing(true);
    try {
      await issuanceApi.deliverElectronic(id);
      setSuccess('Delivered electronically.');
      loadSearch();
    } catch (err) { alert('Failed.'); }
    finally { setProcessing(false); }
  };

  const handleCancel = async () => {
    if (!actionInput.trim()) { alert('Cancellation reason required.'); return; }
    setProcessing(true);
    try {
      await issuanceApi.cancelIssuance(actionModal.id, actionInput);
      setActionModal(null); setActionInput('');
      setSuccess('Issuance cancelled.');
      loadSearch();
    } catch (err) { alert('Failed.'); }
    finally { setProcessing(false); }
  };

  const handleReissue = async () => {
    if (!actionInput.trim()) { alert('Reissue reason required.'); return; }
    setProcessing(true);
    try {
      await issuanceApi.reissue(actionModal.id, actionInput);
      setActionModal(null); setActionInput('');
      setSuccess('Timesheet reissued.');
      loadSearch();
    } catch (err) { alert('Failed.'); }
    finally { setProcessing(false); }
  };

  const handleBatchGenerate = async () => {
    setProcessing(true);
    try {
      const result = await issuanceApi.batchGenerate();
      setSuccess(`Batch generated: ${result.generatedCount} timesheets.`);
      loadPending('generation');
    } catch (err) { alert('Batch generate failed.'); }
    finally { setProcessing(false); }
  };

  const handleCreate = async () => {
    setError('');
    if (!createForm.caseId || !createForm.recipientId || !createForm.providerId || !createForm.payPeriodStart || !createForm.payPeriodEnd) {
      setError('Case ID, Recipient ID, Provider ID, and Pay Period are required.'); return;
    }
    setProcessing(true);
    try {
      await issuanceApi.createIssuance(createForm);
      setCreateModal(false);
      setSuccess('Issuance created.');
      setCreateForm({ caseId: '', recipientId: '', providerId: '', payPeriodStart: '', payPeriodEnd: '', timesheetType: 'STANDARD', programType: 'IHSS', issuanceMethod: 'ELECTRONIC', countyCode: '' });
      loadPending('generation');
    } catch (err) { setError('Failed: ' + (err?.response?.data?.error || err.message)); }
    finally { setProcessing(false); }
  };

  const setF = (k, v) => setCreateForm(prev => ({ ...prev, [k]: v }));

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Timesheet Issuance Management</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="wq-btn wq-btn-primary" onClick={() => setCreateModal(true)}>New Issuance</button>
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back</button>
        </div>
      </div>

      {success && (
        <div style={{ padding: '0.5rem', background: '#f0fdf4', border: '1px solid #86efac', borderRadius: '4px', color: '#16a34a', marginBottom: '0.75rem', fontSize: '0.85rem', display: 'flex', justifyContent: 'space-between' }}>
          <span>{success}</span>
          <button onClick={() => setSuccess('')} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>&times;</button>
        </div>
      )}

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 0, marginBottom: '1rem', borderBottom: '2px solid #e5e7eb' }}>
        {[['pending', 'Pending Generation'], ['print', 'Pending Print'], ['search', 'Search']].map(([k, label]) => (
          <button key={k} onClick={() => setTab(k)}
            style={{ padding: '0.6rem 1.2rem', border: 'none', borderBottom: tab === k ? '2px solid #2563eb' : '2px solid transparent',
              background: 'transparent', fontWeight: tab === k ? 600 : 400, color: tab === k ? '#2563eb' : '#6b7280',
              cursor: 'pointer', fontSize: '0.85rem', marginBottom: '-2px' }}>
            {label}
          </button>
        ))}
      </div>

      {/* Search Tab */}
      {tab === 'search' && (
        <div className="wq-panel" style={{ marginBottom: '1rem' }}>
          <div className="wq-panel-body">
            <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end' }}>
              <div className="wq-form-field">
                <label>Search By</label>
                <select value={searchType} onChange={e => setSearchType(e.target.value)} style={{ padding: '0.4rem', fontSize: '0.85rem' }}>
                  <option value="case">Case ID</option>
                  <option value="provider">Provider ID</option>
                  <option value="number">Issuance Number</option>
                </select>
              </div>
              <div className="wq-form-field">
                <label>Value</label>
                <input type="text" value={searchId} onChange={e => setSearchId(e.target.value)} placeholder="Enter ID or number" style={{ padding: '0.4rem', fontSize: '0.85rem' }} />
              </div>
              <button className="wq-btn wq-btn-primary" onClick={loadSearch} style={{ height: '34px' }}>Search</button>
            </div>
          </div>
        </div>
      )}

      {/* Batch Generate button */}
      {tab === 'pending' && (
        <div style={{ marginBottom: '0.75rem' }}>
          <button className="wq-btn wq-btn-primary" onClick={handleBatchGenerate} disabled={processing}>
            {processing ? 'Processing...' : 'Batch Generate All Pending'}
          </button>
        </div>
      )}

      {/* Issuance Table */}
      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>
            {tab === 'pending' ? 'Pending Generation' : tab === 'print' ? 'Pending Print/Mail' : 'Search Results'}
            {' '}({issuances.length})
          </h4>
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div> : (
            <table className="wq-table" style={{ fontSize: '0.8rem' }}>
              <thead>
                <tr>
                  <th>Issuance #</th>
                  <th>TS Number</th>
                  <th>Case</th>
                  <th>Provider</th>
                  <th>Pay Period</th>
                  <th>Type</th>
                  <th>Method</th>
                  <th>Status</th>
                  <th>Generated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {issuances.length === 0 ? (
                  <tr><td colSpan={10} style={{ textAlign: 'center', padding: '2rem', color: '#9ca3af' }}>No issuances found</td></tr>
                ) : issuances.map(iss => {
                  const sc = STATUS_COLORS[iss.status] || {};
                  return (
                    <tr key={iss.id}>
                      <td style={{ fontWeight: 600, fontFamily: 'monospace' }}>{iss.issuanceNumber}</td>
                      <td>{iss.timesheetNumber || '\u2014'}</td>
                      <td>{iss.caseId}</td>
                      <td>{iss.providerId}</td>
                      <td style={{ fontSize: '0.75rem' }}>{iss.payPeriodStart} \u2013 {iss.payPeriodEnd}</td>
                      <td>{iss.timesheetType || 'STD'}</td>
                      <td>{(iss.issuanceMethod || '').replace(/_/g, ' ')}</td>
                      <td>
                        <span style={{ padding: '2px 8px', borderRadius: '12px', fontSize: '0.7rem', fontWeight: 600, background: sc.bg, color: sc.color }}>
                          {(iss.status || '').replace(/_/g, ' ')}
                        </span>
                        {iss.isReissue && <span style={{ marginLeft: '4px', fontSize: '0.65rem', color: '#7c3aed' }}>REISSUE</span>}
                      </td>
                      <td style={{ fontSize: '0.75rem' }}>{iss.generationDate || '\u2014'}</td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.2rem', flexWrap: 'wrap' }}>
                          {iss.status === 'PENDING_GENERATION' && (
                            <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem' }}
                              onClick={() => handleGenerate(iss.id)} disabled={processing}>Generate</button>
                          )}
                          {iss.status === 'PENDING_MAIL' && (
                            <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem' }}
                              onClick={() => handleMail(iss.id)} disabled={processing}>Mark Mailed</button>
                          )}
                          {iss.status === 'GENERATED' && (
                            <>
                              <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem' }}
                                onClick={() => handleMail(iss.id)} disabled={processing}>Mail</button>
                              <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem', background: '#2563eb' }}
                                onClick={() => handleDeliverElectronic(iss.id)} disabled={processing}>Electronic</button>
                            </>
                          )}
                          {['PENDING_GENERATION', 'GENERATED', 'PENDING_MAIL'].includes(iss.status) && (
                            <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem', background: '#dc2626' }}
                              onClick={() => { setActionModal({ type: 'cancel', id: iss.id, num: iss.issuanceNumber }); setActionInput(''); }}>Cancel</button>
                          )}
                          {['MAILED', 'DELIVERED_ELECTRONIC', 'DELIVERED_IN_PERSON'].includes(iss.status) && (
                            <button className="wq-btn wq-btn-primary" style={{ padding: '0.15rem 0.4rem', fontSize: '0.7rem', background: '#7c3aed' }}
                              onClick={() => { setActionModal({ type: 'reissue', id: iss.id, num: iss.issuanceNumber }); setActionInput(''); }}>Reissue</button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* Action Modal (Cancel / Reissue) */}
      {actionModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: '8px', padding: '1.5rem', width: '420px', boxShadow: '0 8px 30px rgba(0,0,0,0.2)' }}>
            <h4 style={{ marginBottom: '1rem' }}>
              {actionModal.type === 'cancel' ? 'Cancel Issuance' : 'Reissue Timesheet'}
            </h4>
            <p style={{ fontSize: '0.85rem', color: '#6b7280', marginBottom: '0.75rem' }}>
              Issuance: {actionModal.num}
            </p>
            <label style={{ fontSize: '0.85rem', fontWeight: 600 }}>
              {actionModal.type === 'cancel' ? 'Cancellation Reason *' : 'Reissue Reason *'}
            </label>
            <textarea rows={3} value={actionInput} onChange={e => setActionInput(e.target.value)}
              placeholder="Enter reason..."
              style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '4px', marginTop: '0.25rem', marginBottom: '1rem', fontSize: '0.85rem' }} />
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => setActionModal(null)} disabled={processing}>Cancel</button>
              <button className="wq-btn wq-btn-primary"
                style={{ background: actionModal.type === 'cancel' ? '#dc2626' : '#7c3aed' }}
                onClick={actionModal.type === 'cancel' ? handleCancel : handleReissue}
                disabled={processing}>
                {processing ? 'Processing...' : actionModal.type === 'cancel' ? 'Cancel Issuance' : 'Reissue'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Create Modal */}
      {createModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: '8px', padding: '1.5rem', width: '520px', boxShadow: '0 8px 30px rgba(0,0,0,0.2)' }}>
            <h4 style={{ marginBottom: '1rem' }}>Create New Issuance</h4>
            {error && <div style={{ padding: '0.5rem', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '4px', color: '#dc2626', marginBottom: '0.75rem', fontSize: '0.85rem' }}>{error}</div>}
            <div className="wq-search-grid">
              <div className="wq-form-field"><label>Case ID *</label><input type="number" value={createForm.caseId} onChange={e => setF('caseId', e.target.value)} /></div>
              <div className="wq-form-field"><label>Recipient ID *</label><input type="number" value={createForm.recipientId} onChange={e => setF('recipientId', e.target.value)} /></div>
              <div className="wq-form-field"><label>Provider ID *</label><input type="number" value={createForm.providerId} onChange={e => setF('providerId', e.target.value)} /></div>
              <div className="wq-form-field"><label>County Code</label><input type="text" value={createForm.countyCode} onChange={e => setF('countyCode', e.target.value)} placeholder="e.g. 19" /></div>
              <div className="wq-form-field"><label>Pay Period Start *</label><input type="date" value={createForm.payPeriodStart} onChange={e => setF('payPeriodStart', e.target.value)} /></div>
              <div className="wq-form-field"><label>Pay Period End *</label><input type="date" value={createForm.payPeriodEnd} onChange={e => setF('payPeriodEnd', e.target.value)} /></div>
              <div className="wq-form-field">
                <label>Timesheet Type</label>
                <select value={createForm.timesheetType} onChange={e => setF('timesheetType', e.target.value)}>
                  <option value="STANDARD">Standard (SOC 2261)</option>
                  <option value="LARGE_FONT">Large Font (SOC 2261L)</option>
                  <option value="EVV_EXCEPTION">EVV Exception (SOC 2361EVV)</option>
                </select>
              </div>
              <div className="wq-form-field">
                <label>Program</label>
                <select value={createForm.programType} onChange={e => setF('programType', e.target.value)}>
                  <option value="IHSS">IHSS</option>
                  <option value="WPCS">WPCS</option>
                </select>
              </div>
              <div className="wq-form-field">
                <label>Issuance Method</label>
                <select value={createForm.issuanceMethod} onChange={e => setF('issuanceMethod', e.target.value)}>
                  <option value="ELECTRONIC">Electronic (PO004)</option>
                  <option value="MAIL">Print/Mail Center (PO003)</option>
                  <option value="IN_PERSON">Print Now CMIPS II (PO002)</option>
                  <option value="BATCH_PRINT">Nightly Batch (PO001)</option>
                </select>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => { setCreateModal(false); setError(''); }} disabled={processing}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={processing}>
                {processing ? 'Creating...' : 'Create Issuance'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
