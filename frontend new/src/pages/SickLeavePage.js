import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as sickLeaveApi from '../api/sickLeaveApi';
import './WorkQueues.css';

/**
 * Sick Leave Claim Manual Entry — DSD Section 32
 *
 * Five-view flow:
 *   1. entry           — CI-790531: Provider Number + Case Number + Pay Period → Continue
 *   2. timeEntries     — CI-790532: Read-only details + daily HH:MM grid → Save
 *   3. list            — CI-794527: Claims table with Edit/Cancel/View
 *   4. editTimeEntries — CI-794528: Modify time entries (same-day, manual only)
 *   5. viewTimeEntries — CI-794529: Read-only time entries → Close
 *
 *   Cancellation (CI-794530) is handled inline via confirm dialog.
 */
export const SickLeavePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [view, setView] = useState('entry');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Screen 1: Manual Entry form
  const [entryForm, setEntryForm] = useState({ providerNumber: '', caseNumber: '', payPeriodBeginDate: '' });

  // Screen 2/4/5: Lookup detail + time grid
  const [detail, setDetail] = useState(null);
  const [timeGrid, setTimeGrid] = useState({}); // { "YYYY-MM-DD": minutes }
  const [saving, setSaving] = useState(false);

  // Screen 3: Claims list
  const [claims, setClaims] = useState([]);
  const [listLoading, setListLoading] = useState(false);

  // For edit/view: current claim
  const [currentClaim, setCurrentClaim] = useState(null);

  // Cancel confirmation
  const [cancelTarget, setCancelTarget] = useState(null);

  // Breadcrumbs
  useEffect(() => {
    const crumbs = [
      { label: 'My Workspace', path: '/workspace' },
      { label: 'Sick Leave Claim Manual Entry', path: '/payments/sick-leave' },
    ];
    if (view === 'timeEntries') crumbs.push({ label: 'Time Entries' });
    if (view === 'list') crumbs.push({ label: 'Claim List' });
    if (view === 'editTimeEntries') crumbs.push({ label: 'Modify Time Entries' });
    if (view === 'viewTimeEntries') crumbs.push({ label: 'View Time Entries' });
    setBreadcrumbs(crumbs);
    return () => setBreadcrumbs([]);
  }, [view, setBreadcrumbs]);

  // ── Helpers ──

  const formatDate = (d) => {
    if (!d) return '\u2014';
    const date = new Date(d + (typeof d === 'string' && d.length === 10 ? 'T00:00:00' : ''));
    return isNaN(date.getTime()) ? d : date.toLocaleDateString('en-US');
  };

  /** Convert total minutes to HH:MM string */
  const minutesToHHMM = (totalMin) => {
    if (!totalMin && totalMin !== 0) return '00:00';
    const h = Math.floor(totalMin / 60);
    const m = totalMin % 60;
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}`;
  };

  /** Parse HH:MM string to total minutes */
  const parseHHMM = (str) => {
    if (!str) return 0;
    const parts = str.split(':');
    const h = parseInt(parts[0], 10) || 0;
    const m = parseInt(parts[1], 10) || 0;
    return h * 60 + m;
  };

  /** Build calendar weeks for a pay period (2-week pay period starting on given date) */
  const buildCalendarWeeks = useCallback((payPeriodBeginDate) => {
    if (!payPeriodBeginDate) return [];
    const start = new Date(payPeriodBeginDate + 'T00:00:00');
    if (isNaN(start.getTime())) return [];

    // Find Sunday on or before start
    const sundayOffset = start.getDay();
    const firstSunday = new Date(start);
    firstSunday.setDate(firstSunday.getDate() - sundayOffset);

    // Generate 2 weeks (14 days from pay period start, but align to week boundaries)
    const endDate = new Date(start);
    endDate.setDate(endDate.getDate() + 13); // 2-week period

    // Find Saturday on or after end
    const satOffset = 6 - endDate.getDay();
    const lastSaturday = new Date(endDate);
    lastSaturday.setDate(lastSaturday.getDate() + satOffset);

    const weeks = [];
    let current = new Date(firstSunday);
    while (current <= lastSaturday) {
      const week = [];
      for (let d = 0; d < 7; d++) {
        const dateStr = current.toISOString().split('T')[0];
        const inPeriod = current >= start && current <= endDate;
        week.push({
          date: dateStr,
          dayName: ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'][current.getDay()],
          dayNum: String(current.getDate()).padStart(2, '0'),
          inPeriod,
        });
        current.setDate(current.getDate() + 1);
      }
      weeks.push(week);
    }
    return weeks;
  }, []);

  const calendarWeeks = useMemo(() => {
    const dateStr = detail?.servicePeriodFrom || entryForm.payPeriodBeginDate;
    return buildCalendarWeeks(dateStr);
  }, [detail, entryForm.payPeriodBeginDate, buildCalendarWeeks]);

  /** Calculate weekly total for a week row */
  const weekTotal = (week) =>
    week.reduce((sum, day) => sum + (day.inPeriod ? (timeGrid[day.date] || 0) : 0), 0);

  /** Grand total */
  const grandTotal = useMemo(() =>
    Object.values(timeGrid).reduce((s, v) => s + v, 0),
  [timeGrid]);

  // ── Screen 1: Continue handler (CI-790531) ──

  const handleContinue = async () => {
    setError('');
    if (!entryForm.providerNumber.trim()) { setError('Provider Number is required.'); return; }
    if (!entryForm.caseNumber.trim()) { setError('Recipient Case Number is required.'); return; }
    if (!entryForm.payPeriodBeginDate) { setError('Pay Period Begin Date is required.'); return; }

    setLoading(true);
    try {
      const resp = await sickLeaveApi.lookupForEntry({
        providerNumber: entryForm.providerNumber.trim(),
        caseNumber: entryForm.caseNumber.trim(),
        payPeriodBeginDate: entryForm.payPeriodBeginDate,
      });
      setDetail(resp);
      setTimeGrid({});
      setView('timeEntries');
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Lookup failed.');
    } finally {
      setLoading(false);
    }
  };

  // ── Screen 2: Save handler (CI-790532) ──

  const handleSaveTimeEntries = async () => {
    setError('');
    const entries = Object.entries(timeGrid)
      .filter(([, min]) => min > 0)
      .map(([date, minutes]) => ({ date, minutes }));

    if (entries.length === 0) { setError('Enter at least one time entry before saving.'); return; }

    setSaving(true);
    try {
      await sickLeaveApi.saveClaim({
        providerId: detail.providerId,
        caseId: detail.caseId,
        payPeriodBeginDate: detail.servicePeriodFrom,
        timeEntries: entries,
      });
      setSuccess('Sick leave claim saved successfully.');
      setDetail(null);
      setTimeGrid({});
      setEntryForm({ providerNumber: '', caseNumber: '', payPeriodBeginDate: '' });
      setView('entry');
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Save failed.');
    } finally {
      setSaving(false);
    }
  };

  // ── Screen 3: Load claims list (CI-794527) ──

  const loadClaimsList = async (providerId) => {
    setListLoading(true);
    setError('');
    try {
      const data = await sickLeaveApi.listClaimsByProvider(providerId);
      setClaims(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Failed to load claims.');
    } finally {
      setListLoading(false);
    }
  };

  const handleViewList = () => {
    if (detail?.providerId) {
      loadClaimsList(detail.providerId);
      setView('list');
    }
  };

  // ── Screen 4: Edit claim (CI-794528) ──

  const handleEditClaim = async (claimNumber) => {
    setError('');
    try {
      const claim = await sickLeaveApi.getClaimByNumber(claimNumber);
      setCurrentClaim(claim);
      // Parse timeEntries JSON into grid
      const grid = {};
      if (claim.timeEntries) {
        const entries = JSON.parse(claim.timeEntries);
        entries.forEach(e => { grid[e.date] = e.minutes; });
      }
      setTimeGrid(grid);
      // Build detail-like object for calendar
      setDetail(prev => ({
        ...prev,
        servicePeriodFrom: claim.payPeriodBeginDate,
        providerName: claim.providerName,
        recipientName: claim.recipientName,
        providerType: claim.providerType,
        providerId: claim.providerId,
        caseId: claim.caseId,
      }));
      setView('editTimeEntries');
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Failed to load claim.');
    }
  };

  const handleSaveEdit = async () => {
    setError('');
    const entries = Object.entries(timeGrid)
      .filter(([, min]) => min > 0)
      .map(([date, minutes]) => ({ date, minutes }));

    if (entries.length === 0) { setError('Enter at least one time entry before saving.'); return; }

    setSaving(true);
    try {
      await sickLeaveApi.updateClaim(currentClaim.claimNumber, {
        providerId: currentClaim.providerId,
        caseId: currentClaim.caseId,
        payPeriodBeginDate: currentClaim.payPeriodBeginDate,
        timeEntries: entries,
      });
      setSuccess('Claim updated successfully.');
      setCurrentClaim(null);
      setTimeGrid({});
      // Return to list
      if (detail?.providerId) {
        loadClaimsList(detail.providerId);
        setView('list');
      } else {
        setView('entry');
      }
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Update failed.');
    } finally {
      setSaving(false);
    }
  };

  // ── Screen 5: View claim (CI-794529) ──

  const handleViewClaim = async (claimNumber) => {
    setError('');
    try {
      const claim = await sickLeaveApi.getClaimByNumber(claimNumber);
      setCurrentClaim(claim);
      const grid = {};
      if (claim.timeEntries) {
        const entries = JSON.parse(claim.timeEntries);
        entries.forEach(e => { grid[e.date] = e.minutes; });
      }
      setTimeGrid(grid);
      setDetail(prev => ({
        ...prev,
        servicePeriodFrom: claim.payPeriodBeginDate,
        providerName: claim.providerName,
        recipientName: claim.recipientName,
        providerType: claim.providerType,
        providerId: claim.providerId,
        caseId: claim.caseId,
      }));
      setView('viewTimeEntries');
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Failed to load claim.');
    }
  };

  // ── Screen 6: Cancel claim (CI-794530) ──

  const handleConfirmCancel = async () => {
    if (!cancelTarget) return;
    setError('');
    try {
      await sickLeaveApi.cancelClaim(cancelTarget);
      setSuccess('Claim cancelled successfully.');
      setCancelTarget(null);
      if (detail?.providerId) loadClaimsList(detail.providerId);
    } catch (err) {
      setError(err?.data?.message || err?.message || 'Cancel failed.');
    }
  };

  // ── Navigation helpers ──

  const handleCancelEntry = () => {
    setEntryForm({ providerNumber: '', caseNumber: '', payPeriodBeginDate: '' });
    setError('');
    navigate('/workspace');
  };

  const handleCancelTimeEntries = () => {
    setTimeGrid({});
    setError('');
    setView('entry');
  };

  const handleCloseView = () => {
    setCurrentClaim(null);
    setTimeGrid({});
    if (detail?.providerId) {
      loadClaimsList(detail.providerId);
      setView('list');
    } else {
      setView('entry');
    }
  };

  const handleBackToEntry = () => {
    setError('');
    setView('entry');
  };

  // ── Page title ──

  const titles = {
    entry: 'Sick Leave Claim Manual Entry',
    timeEntries: 'Sick Leave Claim Manual Entry \u2014 Time Entries',
    list: 'Sick Leave Claims',
    editTimeEntries: 'Modify Time Entries',
    viewTimeEntries: 'View Time Entries',
  };

  // ── Check if claim is editable/cancellable ──
  const isClaimEditable = (claim) =>
    claim.modeOfEntry === 'MANUAL' &&
    claim.status === 'ACTIVE' &&
    claim.claimEnteredDate === new Date().toISOString().split('T')[0];

  // ── Render ──
  return (
    <div className="wq-page">
      {/* Page Header */}
      <div className="wq-page-header">
        <h2>{titles[view] || 'Sick Leave'}</h2>
        {view === 'entry' && (
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/workspace')}>Back to Workspace</button>
        )}
        {view === 'list' && (
          <button className="wq-btn wq-btn-outline" onClick={handleBackToEntry}>Back to Manual Entry</button>
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

      {/* Cancel Confirmation Dialog (CI-794530) */}
      {cancelTarget && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.4)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div style={{ background: '#fff', borderRadius: '8px', padding: '2rem', maxWidth: '420px', boxShadow: '0 4px 20px rgba(0,0,0,0.15)' }}>
            <h4 style={{ margin: '0 0 1rem 0' }}>Confirm Cancellation</h4>
            <p style={{ fontSize: '0.875rem', color: '#4a5568' }}>Are you sure you want to cancel this Sick Leave Claim?</p>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.5rem', justifyContent: 'flex-end' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleConfirmCancel}>Yes</button>
              <button className="wq-btn wq-btn-outline" onClick={() => setCancelTarget(null)}>No</button>
            </div>
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 1: MANUAL ENTRY (CI-790531)         */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'entry' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Manual Entry</h4></div>
          <div className="wq-panel-body">
            <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
              Enter the Provider Number, Recipient Case Number, and Pay Period Begin Date from the SOC form to begin entering sick leave time.
            </p>
            <div style={{ fontSize: '0.8rem', color: '#c53030', marginBottom: '1rem' }}>* = required field</div>
            <div className="wq-search-grid" style={{ maxWidth: '600px' }}>
              <div className="wq-form-field">
                <label>Provider Number <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="text"
                  value={entryForm.providerNumber}
                  onChange={e => setEntryForm(f => ({ ...f, providerNumber: e.target.value }))}
                  placeholder="Enter provider number"
                />
              </div>
              <div className="wq-form-field">
                <label>Recipient Case Number <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="text"
                  value={entryForm.caseNumber}
                  onChange={e => setEntryForm(f => ({ ...f, caseNumber: e.target.value }))}
                  placeholder="Enter case number"
                />
              </div>
              <div className="wq-form-field">
                <label>Pay Period Begin Date <span style={{ color: '#c53030' }}>*</span></label>
                <input
                  type="date"
                  value={entryForm.payPeriodBeginDate}
                  onChange={e => setEntryForm(f => ({ ...f, payPeriodBeginDate: e.target.value }))}
                />
              </div>
            </div>
            <div className="wq-search-actions" style={{ marginTop: '1.5rem' }}>
              <button className="wq-btn wq-btn-primary" onClick={handleContinue} disabled={loading}>
                {loading ? 'Validating...' : 'Continue'}
              </button>
              <button className="wq-btn wq-btn-outline" onClick={handleCancelEntry}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 2: TIME ENTRIES (CI-790532)          */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'timeEntries' && detail && (
        <>
          <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
            Enter the sick leave hours for each day of the pay period. Click Save when done.
          </p>

          {/* Read-only Details */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Provider &amp; Case Details</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Provider Name" value={detail.providerName} />
                <DetailField label="Recipient Name" value={detail.recipientName} />
                <DetailField label="Provider Type" value={detail.providerType} />
                <DetailField label="Service Period From" value={formatDate(detail.servicePeriodFrom)} />
              </div>
            </div>
          </div>

          {/* Time Entries Grid */}
          <TimeEntriesGrid
            calendarWeeks={calendarWeeks}
            timeGrid={timeGrid}
            setTimeGrid={setTimeGrid}
            weekTotal={weekTotal}
            grandTotal={grandTotal}
            minutesToHHMM={minutesToHHMM}
            parseHHMM={parseHHMM}
            readOnly={false}
          />

          {/* Action Buttons */}
          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={handleSaveTimeEntries} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleCancelTimeEntries}>Cancel</button>
            {detail?.providerId && (
              <button className="wq-btn wq-btn-outline" onClick={handleViewList} style={{ marginLeft: 'auto' }}>
                View Claims List
              </button>
            )}
          </div>
        </>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 3: CLAIMS LIST (CI-794527)           */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'list' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Sick Leave Claims</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {listLoading ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: '#718096' }}>Loading claims...</div>
            ) : claims.length === 0 ? (
              <div style={{ padding: '2rem', textAlign: 'center', color: '#718096' }}>No claims found for this provider.</div>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table className="wq-table">
                  <thead>
                    <tr>
                      <th>Recipient Case Number</th>
                      <th>Provider Type</th>
                      <th>Mode of Entry</th>
                      <th>Claim Number</th>
                      <th>Service Period Begin</th>
                      <th>Claimed Hours</th>
                      <th>Claim Entered Date</th>
                      <th>Issued Date</th>
                      <th>Status</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {claims.map(c => (
                      <tr key={c.id}>
                        <td>{c.caseNumber}</td>
                        <td>{c.providerType || '\u2014'}</td>
                        <td>{c.modeOfEntry || '\u2014'}</td>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{c.claimNumber}</td>
                        <td>{formatDate(c.payPeriodBeginDate)}</td>
                        <td>{minutesToHHMM(c.claimedHours)}</td>
                        <td>{formatDate(c.claimEnteredDate)}</td>
                        <td>{c.issuedDate ? formatDate(c.issuedDate) : '\u2014'}</td>
                        <td>
                          <span style={{
                            padding: '2px 8px', borderRadius: '10px', fontSize: '0.75rem', fontWeight: 600,
                            background: c.status === 'ACTIVE' ? '#c6f6d5' : '#fed7d7',
                            color: c.status === 'ACTIVE' ? '#276749' : '#c53030',
                          }}>
                            {c.status}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '0.25rem' }}>
                            <button
                              className="wq-btn wq-btn-outline"
                              style={{ fontSize: '0.75rem', padding: '2px 8px' }}
                              onClick={() => handleViewClaim(c.claimNumber)}
                            >View</button>
                            {isClaimEditable(c) && (
                              <>
                                <button
                                  className="wq-btn wq-btn-outline"
                                  style={{ fontSize: '0.75rem', padding: '2px 8px' }}
                                  onClick={() => handleEditClaim(c.claimNumber)}
                                >Edit</button>
                                <button
                                  className="wq-btn wq-btn-outline"
                                  style={{ fontSize: '0.75rem', padding: '2px 8px', color: '#c53030', borderColor: '#fc8181' }}
                                  onClick={() => setCancelTarget(c.claimNumber)}
                                >Cancel</button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 4: EDIT TIME ENTRIES (CI-794528)     */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'editTimeEntries' && currentClaim && (
        <>
          <p style={{ fontSize: '0.85rem', color: '#4a5568', marginBottom: '1rem' }}>
            Modify the sick leave hours below. Click Save to update the claim.
          </p>

          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Provider &amp; Case Details</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Provider Name" value={currentClaim.providerName} />
                <DetailField label="Recipient Name" value={currentClaim.recipientName} />
                <DetailField label="Provider Type" value={currentClaim.providerType} />
                <DetailField label="Service Period From" value={formatDate(currentClaim.payPeriodBeginDate)} />
                <DetailField label="Claim Number" value={currentClaim.claimNumber} />
              </div>
            </div>
          </div>

          <TimeEntriesGrid
            calendarWeeks={calendarWeeks}
            timeGrid={timeGrid}
            setTimeGrid={setTimeGrid}
            weekTotal={weekTotal}
            grandTotal={grandTotal}
            minutesToHHMM={minutesToHHMM}
            parseHHMM={parseHHMM}
            readOnly={false}
          />

          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={handleSaveEdit} disabled={saving}>
              {saving ? 'Saving...' : 'Save'}
            </button>
            <button className="wq-btn wq-btn-outline" onClick={handleCloseView}>Cancel</button>
          </div>
        </>
      )}

      {/* ═══════════════════════════════════════════ */}
      {/* SCREEN 5: VIEW TIME ENTRIES (CI-794529)     */}
      {/* ═══════════════════════════════════════════ */}
      {view === 'viewTimeEntries' && currentClaim && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Provider &amp; Case Details</h4></div>
            <div className="wq-panel-body">
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem 2rem' }}>
                <DetailField label="Provider Name" value={currentClaim.providerName} />
                <DetailField label="Recipient Name" value={currentClaim.recipientName} />
                <DetailField label="Provider Type" value={currentClaim.providerType} />
                <DetailField label="Service Period From" value={formatDate(currentClaim.payPeriodBeginDate)} />
                <DetailField label="Claim Number" value={currentClaim.claimNumber} />
                <DetailField label="Mode of Entry" value={currentClaim.modeOfEntry} />
                <DetailField label="Claim Entered Date" value={formatDate(currentClaim.claimEnteredDate)} />
                <DetailField label="Status" value={currentClaim.status} />
              </div>
            </div>
          </div>

          <TimeEntriesGrid
            calendarWeeks={calendarWeeks}
            timeGrid={timeGrid}
            setTimeGrid={setTimeGrid}
            weekTotal={weekTotal}
            grandTotal={grandTotal}
            minutesToHHMM={minutesToHHMM}
            parseHHMM={parseHHMM}
            readOnly={true}
          />

          <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
            <button className="wq-btn wq-btn-primary" onClick={handleCloseView}>Close</button>
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

/** Time Entries Calendar Grid component */
const TimeEntriesGrid = ({ calendarWeeks, timeGrid, setTimeGrid, weekTotal, grandTotal, minutesToHHMM, parseHHMM, readOnly }) => {
  const handleTimeChange = (dateStr, value) => {
    // Allow only digits and colon
    const clean = value.replace(/[^0-9:]/g, '');
    setTimeGrid(prev => ({ ...prev, [dateStr]: parseHHMM(clean) }));
  };

  const formatCellValue = (dateStr) => {
    const min = timeGrid[dateStr] || 0;
    return min > 0 ? minutesToHHMM(min) : '';
  };

  const cellStyle = {
    textAlign: 'center',
    padding: '0.25rem',
    border: '1px solid #e2e8f0',
    minWidth: '70px',
  };

  const headerCellStyle = {
    ...cellStyle,
    background: '#edf2f7',
    fontWeight: 600,
    fontSize: '0.75rem',
    color: '#4a5568',
  };

  const totalCellStyle = {
    ...cellStyle,
    background: '#ebf8ff',
    fontWeight: 600,
    fontSize: '0.8rem',
    color: '#2b6cb0',
  };

  return (
    <div className="wq-panel">
      <div className="wq-panel-header"><h4>Time Entries</h4></div>
      <div className="wq-panel-body" style={{ overflowX: 'auto' }}>
        <table style={{ borderCollapse: 'collapse', width: '100%' }}>
          <thead>
            <tr>
              {['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT'].map(d => (
                <th key={d} style={headerCellStyle}>{d}</th>
              ))}
              <th style={{ ...headerCellStyle, background: '#bee3f8' }}>WEEKLY TOTAL</th>
            </tr>
          </thead>
          <tbody>
            {calendarWeeks.map((week, wi) => (
              <React.Fragment key={wi}>
                {/* Date number row */}
                <tr>
                  {week.map(day => (
                    <td key={day.date} style={{ ...cellStyle, background: day.inPeriod ? '#fff' : '#f7fafc', fontSize: '0.7rem', color: '#718096' }}>
                      {day.dayNum}
                    </td>
                  ))}
                  <td rowSpan={2} style={totalCellStyle}>
                    {minutesToHHMM(weekTotal(week))}
                  </td>
                </tr>
                {/* Input row */}
                <tr>
                  {week.map(day => (
                    <td key={day.date + '-input'} style={{ ...cellStyle, background: day.inPeriod ? '#fff' : '#f7fafc' }}>
                      {day.inPeriod ? (
                        readOnly ? (
                          <span style={{ fontSize: '0.85rem' }}>{formatCellValue(day.date) || '\u2014'}</span>
                        ) : (
                          <input
                            type="text"
                            placeholder="HH:MM"
                            value={formatCellValue(day.date)}
                            onChange={e => handleTimeChange(day.date, e.target.value)}
                            style={{
                              width: '55px', textAlign: 'center', border: '1px solid #cbd5e0',
                              borderRadius: '3px', padding: '2px 4px', fontSize: '0.85rem',
                            }}
                          />
                        )
                      ) : (
                        <span style={{ color: '#cbd5e0', fontSize: '0.75rem' }}>\u2014</span>
                      )}
                    </td>
                  ))}
                </tr>
              </React.Fragment>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={7} style={{ ...totalCellStyle, textAlign: 'right', paddingRight: '1rem', background: '#bee3f8' }}>
                GRAND TOTAL
              </td>
              <td style={{ ...totalCellStyle, background: '#bee3f8', fontSize: '0.9rem' }}>
                {minutesToHHMM(grandTotal)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
  );
};
