import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as tsApi from '../api/timesheetApi';
import './WorkQueues.css';

const statusColor = (s) => {
  if (!s) return '#888';
  if (s.startsWith('HOLD_')) return '#b45309';
  if (s === 'EXCEPTION' || s === 'REJECTED') return '#dc2626';
  if (s === 'APPROVED_FOR_PAYROLL' || s === 'PROCESSED') return '#16a34a';
  if (s === 'SENT_TO_PAYROLL') return '#2563eb';
  if (s === 'VOID' || s === 'CANCELLED') return '#6b7280';
  return '#0369a1';
};

const exColor = (t) => t === 'HARD_EDIT' ? '#dc2626' : t === 'HOLD_CONDITION' ? '#b45309' : '#0369a1';

export const TimesheetDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { roles } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const isSupervisor = roles?.some(r => r.includes('SUPERVISOR'));

  const [data, setData] = useState(null);
  const [overtime, setOvertime] = useState(null);
  const [loading, setLoading] = useState(true);
  const [actionError, setActionError] = useState('');
  const [activeTab, setActiveTab] = useState('details');

  const load = useCallback(async () => {
    try {
      const d = await tsApi.getTimesheetById(id);
      setData(d);
    } catch (err) {
      console.warn('[TSDetail] Error:', err?.message);
    } finally { setLoading(false); }
  }, [id]);

  useEffect(() => { load(); }, [load]);

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'IHSS Timesheets', path: '/payments/timesheets' },
      { label: `Timesheet ${id}` }
    ]);
    return () => setBreadcrumbs([]);
  }, [id, setBreadcrumbs]);

  const loadOvertime = async () => {
    try { setOvertime(await tsApi.getOvertimeBreakdown(id)); } catch {}
  };

  const doAction = async (fn, label) => {
    setActionError('');
    try { await fn(); await load(); } catch (err) {
      setActionError(`${label} failed: ${err?.response?.data?.error || err?.message || 'Unknown error'}`);
    }
  };

  if (loading) return <div className="wq-page"><p style={{ padding: '2rem', color: '#888' }}>Loading timesheet...</p></div>;
  if (!data) return <div className="wq-page"><p style={{ padding: '2rem', color: '#dc2626' }}>Timesheet not found.</p></div>;

  const ts = data.timesheet || data;
  const entries = data.timeEntries || [];
  const exceptions = data.exceptions || [];
  const hardEdits = exceptions.filter(e => e.exceptionType === 'HARD_EDIT');
  const holds = exceptions.filter(e => e.exceptionType === 'HOLD_CONDITION');
  const softEdits = exceptions.filter(e => e.exceptionType === 'SOFT_EDIT');
  const canValidate = ['RECEIVED', 'VALIDATING'].includes(ts.status);
  const canRelease = (ts.status || '').startsWith('HOLD_');
  const canSendPayroll = ts.status === 'APPROVED_FOR_PAYROLL';
  const canReject = !['REJECTED', 'VOID', 'CANCELLED', 'PROCESSED', 'SENT_TO_PAYROLL'].includes(ts.status);
  const canVoid = !['VOID', 'CANCELLED'].includes(ts.status);

  const fmtDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';
  const fmtHrs = (h) => h != null ? Number(h).toFixed(1) : '\u2014';
  const dow = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <div>
          <h2 style={{ margin: 0 }}>Timesheet: {ts.timesheetNumber || id}</h2>
          <span style={{ fontSize: '0.8rem', color: '#666' }}>Case {ts.caseId} | Provider {ts.providerId} | {ts.programType}</span>
        </div>
        <span style={{ padding: '4px 12px', borderRadius: '4px', fontSize: '0.85rem', fontWeight: 700, color: '#fff', background: statusColor(ts.status) }}>
          {ts.status}
        </span>
      </div>

      {actionError && <div style={{ padding: '0.75rem', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '6px', color: '#dc2626', marginBottom: '1rem' }}>{actionError}</div>}

      {/* Action Buttons */}
      <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap', marginBottom: '1rem' }}>
        {canValidate && <button className="wq-btn wq-btn-primary" onClick={() => doAction(() => tsApi.validateTimesheet(id), 'Validate')}>Run Validation</button>}
        {canRelease && <button className="wq-btn wq-btn-primary" onClick={() => doAction(() => tsApi.releaseTimesheet(id, 'CURRENT_USER'), 'Release')}>Release Hold</button>}
        {canSendPayroll && <button className="wq-btn wq-btn-primary" style={{ background: '#16a34a' }} onClick={() => doAction(() => tsApi.sendToPayroll(id), 'Send to Payroll')}>Send to Payroll</button>}
        {canReject && <button className="wq-btn wq-btn-outline" style={{ color: '#dc2626', borderColor: '#dc2626' }} onClick={() => { const r = prompt('Rejection reason:'); if (r) doAction(() => tsApi.rejectTimesheet(id, r, 'CURRENT_USER'), 'Reject'); }}>Reject</button>}
        {canVoid && <button className="wq-btn wq-btn-outline" style={{ color: '#6b7280', borderColor: '#6b7280' }} onClick={() => { const r = prompt('Void reason:'); if (r) doAction(() => tsApi.voidTimesheet(id, r, 'CURRENT_USER'), 'Void'); }}>Void</button>}
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to List</button>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: 0, borderBottom: '2px solid #e2e8f0', marginBottom: '1rem' }}>
        {['details', 'daily', 'exceptions', 'overtime'].map(tab => (
          <button key={tab} onClick={() => { setActiveTab(tab); if (tab === 'overtime' && !overtime) loadOvertime(); }}
            style={{
              padding: '0.6rem 1.2rem', border: 'none', cursor: 'pointer', fontWeight: 500, fontSize: '0.85rem',
              borderBottom: activeTab === tab ? '3px solid #153554' : '3px solid transparent',
              background: activeTab === tab ? '#f0f7ff' : 'transparent',
              color: activeTab === tab ? '#153554' : '#666'
            }}>
            {tab === 'details' ? 'Details' : tab === 'daily' ? `Daily Hours (${entries.length})` :
             tab === 'exceptions' ? `Exceptions (${exceptions.length})` : 'FLSA Overtime'}
          </button>
        ))}
      </div>

      {/* DETAILS TAB */}
      {activeTab === 'details' && (
        <div className="wq-panel">
          <div className="wq-panel-body">
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1rem' }}>
              <Field label="Timesheet Number" val={ts.timesheetNumber} />
              <Field label="Case ID" val={ts.caseId} />
              <Field label="Recipient ID" val={ts.recipientId} />
              <Field label="Provider ID" val={ts.providerId} />
              <Field label="Program Type" val={ts.programType} />
              <Field label="Timesheet Type" val={ts.timesheetType} />
              <Field label="Source Type" val={ts.sourceType} />
              <Field label="Mode of Entry" val={ts.modeOfEntry} />
              <Field label="Pay Period" val={`${fmtDate(ts.payPeriodStart)} - ${fmtDate(ts.payPeriodEnd)}`} />
              <Field label="Service Month" val={ts.serviceMonth} />
              <Field label="Total Hours Claimed" val={fmtHrs(ts.totalHoursClaimed)} />
              <Field label="Total Hours Approved" val={fmtHrs(ts.totalHoursApproved)} />
              <Field label="Authorized Hours/Mo" val={fmtHrs(ts.authorizedHoursMonthly)} />
              <Field label="Assigned Hours" val={fmtHrs(ts.assignedHours)} />
              <Field label="Remaining Recipient Hrs" val={fmtHrs(ts.remainingRecipientHours)} />
              <Field label="Remaining Provider Hrs" val={fmtHrs(ts.remainingProviderHours)} />
              <Field label="Regular Hours" val={fmtHrs(ts.regularHours)} />
              <Field label="Overtime Hours" val={fmtHrs(ts.overtimeHours)} />
              <Field label="SOC Deduction" val={ts.socDeductionApplies ? `Yes ($${ts.socAmount || 0})` : 'No'} />
              <Field label="Provider Signature" val={ts.providerSignaturePresent ? 'Yes' : 'No'} />
              <Field label="Recipient Signature" val={ts.recipientSignaturePresent ? 'Yes' : 'No'} />
              <Field label="BVI Recipient" val={ts.recipientIsBvi ? 'Yes' : 'No'} />
              <Field label="Supplemental" val={ts.isSupplemental ? 'Yes' : 'No'} />
              <Field label="Flagged for Review" val={ts.flaggedForReview ? 'Yes' : 'No'} />
              <Field label="Date Received" val={fmtDate(ts.dateReceived)} />
              <Field label="Date Issued" val={fmtDate(ts.dateIssued)} />
              <Field label="Date Sent to Payroll" val={fmtDate(ts.dateSentToPayroll)} />
              <Field label="County Code" val={ts.countyCode} />
              <Field label="Created By" val={ts.createdBy} />
              <Field label="Notes" val={ts.notes} />
            </div>
          </div>
        </div>
      )}

      {/* DAILY HOURS TAB */}
      {activeTab === 'daily' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Daily Time Entries</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {entries.length === 0 ? <p style={{ padding: '1rem', color: '#888' }}>No daily entries recorded.</p> : (
              <table className="wq-table" style={{ fontSize: '0.8rem' }}>
                <thead>
                  <tr>
                    <th>Day</th>
                    <th>Date</th>
                    <th>DOW</th>
                    <th>Hrs Claimed</th>
                    <th>Min</th>
                    <th>Hrs Approved</th>
                    <th>Cutback</th>
                    <th>Cutback Reason</th>
                    <th>Wk #</th>
                    <th>R Elig</th>
                    <th>P Elig</th>
                    <th>R Leave</th>
                    <th>P Leave</th>
                  </tr>
                </thead>
                <tbody>
                  {entries.map((e, i) => (
                    <tr key={i} style={{ background: e.hoursCutback > 0 ? '#fef3c7' : e.isFutureDay ? '#fee2e2' : 'transparent' }}>
                      <td>{e.dayOfPeriod || (i + 1)}</td>
                      <td>{fmtDate(e.entryDate)}</td>
                      <td>{e.dayOfWeek ? dow[e.dayOfWeek % 7] : '\u2014'}</td>
                      <td style={{ fontWeight: 600 }}>{fmtHrs(e.hoursClaimed)}</td>
                      <td>{e.minutesClaimed ?? '\u2014'}</td>
                      <td style={{ color: '#16a34a', fontWeight: 600 }}>{fmtHrs(e.hoursApproved)}</td>
                      <td style={{ color: e.hoursCutback > 0 ? '#dc2626' : '#888' }}>{fmtHrs(e.hoursCutback)}</td>
                      <td style={{ fontSize: '0.7rem', color: '#888' }}>{e.cutbackReason || '\u2014'}</td>
                      <td>{e.workWeekNumber || '\u2014'}</td>
                      <td>{e.recipientEligible === false ? <span style={{ color: '#dc2626' }}>No</span> : 'Yes'}</td>
                      <td>{e.providerEligible === false ? <span style={{ color: '#dc2626' }}>No</span> : 'Yes'}</td>
                      <td>{e.recipientOnLeave ? <span style={{ color: '#b45309' }}>Yes</span> : 'No'}</td>
                      <td>{e.providerOnLeave ? <span style={{ color: '#b45309' }}>Yes</span> : 'No'}</td>
                    </tr>
                  ))}
                </tbody>
                <tfoot>
                  <tr style={{ fontWeight: 700, background: '#f1f5f9' }}>
                    <td colSpan={3}>Totals</td>
                    <td>{fmtHrs(entries.reduce((s, e) => s + (e.hoursClaimed || 0), 0))}</td>
                    <td></td>
                    <td style={{ color: '#16a34a' }}>{fmtHrs(entries.reduce((s, e) => s + (e.hoursApproved || 0), 0))}</td>
                    <td style={{ color: '#dc2626' }}>{fmtHrs(entries.reduce((s, e) => s + (e.hoursCutback || 0), 0))}</td>
                    <td colSpan={6}></td>
                  </tr>
                </tfoot>
              </table>
            )}
          </div>
        </div>
      )}

      {/* EXCEPTIONS TAB */}
      {activeTab === 'exceptions' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Validation Exceptions</h4></div>
          <div className="wq-panel-body">
            {exceptions.length === 0 ? <p style={{ color: '#16a34a' }}>No exceptions. Timesheet is clean.</p> : (
              <>
                {hardEdits.length > 0 && (
                  <div style={{ marginBottom: '1rem' }}>
                    <h5 style={{ color: '#dc2626', margin: '0 0 0.5rem' }}>Hard Edits ({hardEdits.length}) — Block Payment</h5>
                    {hardEdits.map((ex, i) => <ExRow key={i} ex={ex} />)}
                  </div>
                )}
                {holds.length > 0 && (
                  <div style={{ marginBottom: '1rem' }}>
                    <h5 style={{ color: '#b45309', margin: '0 0 0.5rem' }}>Hold Conditions ({holds.length}) — Require Review</h5>
                    {holds.map((ex, i) => <ExRow key={i} ex={ex} />)}
                  </div>
                )}
                {softEdits.length > 0 && (
                  <div>
                    <h5 style={{ color: '#0369a1', margin: '0 0 0.5rem' }}>Soft Edits ({softEdits.length}) — Informational</h5>
                    {softEdits.map((ex, i) => <ExRow key={i} ex={ex} />)}
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {/* OVERTIME TAB */}
      {activeTab === 'overtime' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>FLSA Overtime Breakdown</h4></div>
          <div className="wq-panel-body">
            {!overtime ? <p style={{ color: '#888' }}>Loading overtime data...</p> : (
              <>
                <div style={{ display: 'flex', gap: '2rem', marginBottom: '1rem' }}>
                  <div><strong>Total Regular:</strong> {fmtHrs(overtime.totalRegularHours)}</div>
                  <div><strong>Total Overtime:</strong> <span style={{ color: overtime.totalOvertimeHours > 0 ? '#dc2626' : '#16a34a' }}>{fmtHrs(overtime.totalOvertimeHours)}</span></div>
                  <div><strong>FLSA Applicable:</strong> {overtime.flsaApplicable ? 'Yes (1.5x rate)' : 'No'}</div>
                </div>
                <table className="wq-table">
                  <thead><tr><th>Work Week</th><th>Total Hours</th><th>Regular (≤40)</th><th>Overtime (&gt;40)</th></tr></thead>
                  <tbody>
                    {(overtime.weekBreakdown || []).map((w, i) => (
                      <tr key={i}>
                        <td>Week {w.week}</td>
                        <td>{fmtHrs(w.totalHours)}</td>
                        <td>{fmtHrs(w.regularHours)}</td>
                        <td style={{ color: w.overtimeHours > 0 ? '#dc2626' : '#16a34a', fontWeight: 600 }}>{fmtHrs(w.overtimeHours)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const Field = ({ label, val }) => (
  <div>
    <div style={{ fontSize: '0.7rem', color: '#888', textTransform: 'uppercase', letterSpacing: '0.5px' }}>{label}</div>
    <div style={{ fontSize: '0.9rem', fontWeight: 500, color: '#1e293b' }}>{val || '\u2014'}</div>
  </div>
);

const ExRow = ({ ex }) => (
  <div style={{ padding: '0.5rem 0.75rem', margin: '0.25rem 0', borderRadius: '4px', border: `1px solid ${exColor(ex.exceptionType)}22`, background: `${exColor(ex.exceptionType)}08`, display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
    <span style={{ fontWeight: 700, fontSize: '0.75rem', color: exColor(ex.exceptionType), minWidth: '60px' }}>Rule {ex.ruleNumber}</span>
    <span style={{ fontSize: '0.75rem', color: '#4a5568', padding: '1px 6px', background: '#f1f5f9', borderRadius: '3px' }}>{ex.errorCode}</span>
    <span style={{ fontSize: '0.8rem', color: '#1e293b' }}>{ex.message}</span>
    {ex.resolved && <span style={{ fontSize: '0.7rem', color: '#16a34a', marginLeft: 'auto' }}>Resolved</span>}
  </div>
);
