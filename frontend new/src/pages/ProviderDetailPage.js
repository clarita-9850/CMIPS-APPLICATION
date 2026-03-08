import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as providersApi from '../api/providersApi';
import * as notesApi from '../api/notesApi';
import { AddNoteModal } from './modals/AddNoteModal';
import { AssignProviderModal } from './modals/AssignProviderModal';
import './WorkQueues.css';

const fmt = (d) => {
  if (!d) return '—';
  const dt = new Date(d.length === 10 ? d + 'T00:00:00' : d);
  return isNaN(dt) ? d : dt.toLocaleDateString('en-US');
};
const badgeStyle = (s = '') => {
  const m = { active: '#c6f6d5/#276749', pending: '#feebc8/#c05621', inactive: '#e2e8f0/#4a5568',
              no: '#fed7d7/#c53030', yes: '#c6f6d5/#276749', terminated: '#fed7d7/#c53030',
              pending_review: '#feebc8/#c05621', upheld: '#fed7d7/#c53030', override: '#c6f6d5/#276749' };
  const [bg, color] = (m[s.toLowerCase()] || '#e2e8f0/#4a5568').split('/');
  return { background: bg, color, padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600 };
};

export const ProviderDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [assignments, setAssignments] = useState([]);
  const [cori, setCori] = useState([]);
  const [violations, setViolations] = useState([]);
  const [exemptions, setExemptions] = useState([]);
  const [workweekAgreements, setWorkweekAgreements] = useState([]);
  const [travelTimes, setTravelTimes] = useState([]);
  const [benefits, setBenefits] = useState([]);
  const [notes, setNotes] = useState([]);
  const [actionError, setActionError] = useState('');
  const [actionSuccess, setActionSuccess] = useState('');
  const [showAddNote, setShowAddNote] = useState(false);
  const [showAssign, setShowAssign] = useState(false);

  // New DSD Section 23 state
  const [sickLeaveAccrual, setSickLeaveAccrual] = useState(null);
  const [sickLeaveClaims, setSickLeaveClaims] = useState([]);
  const [notifPrefs, setNotifPrefs] = useState(null);
  const [waivers, setWaivers] = useState([]);

  // Violation review inline forms
  const [reviewingViolation, setReviewingViolation] = useState(null);
  const [reviewForm, setReviewForm] = useState({ outcome: '', comments: '' });
  const [reviewType, setReviewType] = useState(''); // 'county' | 'supervisor' | 'dispute' | 'cdss' | 'state' | 'state_supervisor'

  // Exemption inline form
  const [showExemptionForm, setShowExemptionForm] = useState(false);
  const [exemptionForm, setExemptionForm] = useState({ beginDate: '', endDate: '', exemptionType: 'EXTRAORDINARY_CIRCUMSTANCES', comments: '' });

  // Workweek agreement inline form
  const [showWorkweekForm, setShowWorkweekForm] = useState(false);
  const [workweekForm, setWorkweekForm] = useState({ recipientName: '', caseNumber: '', workweekStartDay: 'SUNDAY', agreedHoursWeekly: '', beginDate: '', includesTravelTime: false, travelHoursWeekly: '' });

  // Travel time inline form
  const [showTravelForm, setShowTravelForm] = useState(false);
  const [travelForm, setTravelForm] = useState({ toRecipientName: '', toCaseNumber: '', fromRecipientName: '', fromCaseNumber: '', travelHoursWeekly: '', travelMinutes: '', beginDate: '', programType: 'IHSS' });

  // Benefit inline form
  const [showBenefitForm, setShowBenefitForm] = useState(false);
  const [benefitForm, setBenefitForm] = useState({ benefitType: 'HEALTH', planName: '', coverageType: 'SINGLE', monthlyDeductionAmount: '', beginDate: '' });

  // Attachments
  const [attachments, setAttachments] = useState([]);
  const [showAttachmentForm, setShowAttachmentForm] = useState(false);
  const [attachmentForm, setAttachmentForm] = useState({ documentType: 'SOC_426', description: '', originalFileName: '' });

  // Backup provider hours
  const [backupHours, setBackupHours] = useState([]);
  const [showBackupForm, setShowBackupForm] = useState(false);
  const [backupForm, setBackupForm] = useState({ authorizedHoursWeekly: '', beginDate: '', programType: 'IHSS', primaryProviderName: '', caseNumber: '', recipientName: '' });

  // Monthly paid hours
  const [monthlyHours, setMonthlyHours] = useState(null);

  // Qualification
  const [qualSummary, setQualSummary] = useState(null);
  const [trainingRecords, setTrainingRecords] = useState([]);
  const [showTrainingForm, setShowTrainingForm] = useState(false);
  const [trainingForm, setTrainingForm] = useState({ trainingType: 'ANNUAL_REFRESHER', completionDate: '', hoursCompletedMinutes: '', certificateNumber: '', notes: '' });

  // CORI inline edit/inactivate
  const [editingCori, setEditingCori] = useState(null);
  const [coriEditForm, setCoriEditForm] = useState({});
  const [inactivatingCoriId, setInactivatingCoriId] = useState(null);

  const [saving, setSaving] = useState(false);

  const loadProvider = useCallback(() => {
    if (!id) { setLoading(false); return; }
    providersApi.getProviderById(id)
      .then(data => setProvider(data))
      .catch(() => setProvider(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { loadProvider(); }, [loadProvider]);

  useEffect(() => {
    if (!id) return;
    if (activeTab === 'assignments') {
      providersApi.getProviderAssignments(id).then(d => setAssignments(Array.isArray(d) ? d : [])).catch(() => setAssignments([]));
    } else if (activeTab === 'cori') {
      providersApi.getProviderCori(id).then(d => setCori(Array.isArray(d) ? d : [])).catch(() => setCori([]));
    } else if (activeTab === 'violations') {
      providersApi.getProviderViolations(id).then(d => setViolations(Array.isArray(d) ? d : [])).catch(() => setViolations([]));
    } else if (activeTab === 'exemptions') {
      providersApi.getProviderExemptions(id).then(d => setExemptions(Array.isArray(d) ? d : [])).catch(() => setExemptions([]));
    } else if (activeTab === 'workweek') {
      providersApi.getWorkweekAgreements(id).then(d => setWorkweekAgreements(Array.isArray(d) ? d : [])).catch(() => setWorkweekAgreements([]));
    } else if (activeTab === 'traveltime') {
      providersApi.getTravelTimes(id).then(d => setTravelTimes(Array.isArray(d) ? d : [])).catch(() => setTravelTimes([]));
    } else if (activeTab === 'benefits') {
      providersApi.getProviderBenefits(id).then(d => setBenefits(Array.isArray(d) ? d : [])).catch(() => setBenefits([]));
    } else if (activeTab === 'notes') {
      notesApi.getPersonNotes(id).then(d => setNotes(Array.isArray(d) ? d : [])).catch(() => setNotes([]));
    } else if (activeTab === 'attachments') {
      providersApi.getProviderAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : [])).catch(() => setAttachments([]));
    } else if (activeTab === 'backuphours') {
      providersApi.getBackupProviderHours(id).then(d => setBackupHours(Array.isArray(d) ? d : [])).catch(() => setBackupHours([]));
    } else if (activeTab === 'monthlyhours') {
      providersApi.getMonthlyPaidHours(id).then(d => setMonthlyHours(d)).catch(() => setMonthlyHours(null));
    } else if (activeTab === 'qualification') {
      providersApi.getQualificationSummary(id).then(d => setQualSummary(d)).catch(() => setQualSummary(null));
      providersApi.getProviderTraining(id).then(d => setTrainingRecords(Array.isArray(d) ? d : [])).catch(() => setTrainingRecords([]));
    } else if (activeTab === 'sickleave') {
      providersApi.getCurrentSickLeaveAccrual(id).then(d => setSickLeaveAccrual(d)).catch(() => setSickLeaveAccrual(null));
      providersApi.getSickLeaveClaims(id).then(d => setSickLeaveClaims(Array.isArray(d) ? d : [])).catch(() => setSickLeaveClaims([]));
    } else if (activeTab === 'notifications') {
      providersApi.getNotificationPreferences(id).then(d => setNotifPrefs(d)).catch(() => setNotifPrefs(null));
    } else if (activeTab === 'waivers') {
      providersApi.getProviderWaivers(id).then(d => setWaivers(Array.isArray(d) ? d : [])).catch(() => setWaivers([]));
    }
  }, [id, activeTab]);

  const handleAction = (actionFn, successMsg) => {
    setActionError(''); setActionSuccess('');
    actionFn()
      .then(() => { loadProvider(); if (successMsg) setActionSuccess(successMsg); })
      .catch(err => setActionError(err?.response?.data?.message || err?.message || 'Action failed'));
  };

  const handleViolationReview = async () => {
    setSaving(true); setActionError('');
    try {
      if (reviewType === 'county') await providersApi.countyReviewViolation(reviewingViolation.id, reviewForm);
      else if (reviewType === 'supervisor') await providersApi.supervisorReviewViolation(reviewingViolation.id, reviewForm);
      else if (reviewType === 'dispute') await providersApi.fileCountyDispute(reviewingViolation.id, reviewForm);
      else if (reviewType === 'dispute-resolve') await providersApi.resolveCountyDispute(reviewingViolation.id, reviewForm);
      else if (reviewType === 'cdss-request') await providersApi.requestCdssReview(reviewingViolation.id);
      else if (reviewType === 'cdss-outcome') await providersApi.recordCdssOutcome(reviewingViolation.id, reviewForm);
      else if (reviewType === 'training') await providersApi.recordTrainingCompletion(reviewingViolation.id);
      else if (reviewType === 'state') await providersApi.stateReviewViolation(reviewingViolation.id, reviewForm);
      else if (reviewType === 'state_supervisor') await providersApi.stateSupervisorReviewViolation(reviewingViolation.id, reviewForm);
      setReviewingViolation(null); setReviewForm({ outcome: '', comments: '' });
      setActionSuccess('Violation review updated.');
      providersApi.getProviderViolations(id).then(d => setViolations(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Review failed');
    } finally { setSaving(false); }
  };

  const handleSaveExemption = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.createExemption(id, exemptionForm);
      setShowExemptionForm(false);
      setExemptionForm({ beginDate: '', endDate: '', exemptionType: 'EXTRAORDINARY_CIRCUMSTANCES', comments: '' });
      setActionSuccess('Overtime exemption created.');
      providersApi.getProviderExemptions(id).then(d => setExemptions(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Save failed');
    } finally { setSaving(false); }
  };

  const handleSaveWorkweek = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.createWorkweekAgreement(id, workweekForm);
      setShowWorkweekForm(false);
      setWorkweekForm({ recipientName: '', caseNumber: '', workweekStartDay: 'SUNDAY', agreedHoursWeekly: '', beginDate: '', includesTravelTime: false, travelHoursWeekly: '' });
      setActionSuccess('Workweek agreement saved.');
      providersApi.getWorkweekAgreements(id).then(d => setWorkweekAgreements(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Save failed');
    } finally { setSaving(false); }
  };

  const handleSaveTravelTime = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.createTravelTime(id, travelForm);
      setShowTravelForm(false);
      setTravelForm({ toRecipientName: '', toCaseNumber: '', fromRecipientName: '', fromCaseNumber: '', travelHoursWeekly: '', travelMinutes: '', beginDate: '', programType: 'IHSS' });
      setActionSuccess('Travel time record created.');
      providersApi.getTravelTimes(id).then(d => setTravelTimes(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Save failed');
    } finally { setSaving(false); }
  };

  const handleSaveBenefit = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.createProviderBenefit(id, benefitForm);
      setShowBenefitForm(false);
      setBenefitForm({ benefitType: 'HEALTH', planName: '', coverageType: 'SINGLE', monthlyDeductionAmount: '', beginDate: '' });
      setActionSuccess('Benefit/deduction added.');
      providersApi.getProviderBenefits(id).then(d => setBenefits(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Save failed');
    } finally { setSaving(false); }
  };

  const handleSaveAttachment = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.uploadAttachment(id, attachmentForm);
      setShowAttachmentForm(false);
      setAttachmentForm({ documentType: 'SOC_426', description: '', originalFileName: '' });
      setActionSuccess('Attachment uploaded.');
      providersApi.getProviderAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Upload failed');
    } finally { setSaving(false); }
  };

  const handleArchiveAttachment = async (attachmentId) => {
    setSaving(true); setActionError('');
    try {
      await providersApi.archiveAttachment(attachmentId);
      setActionSuccess('Attachment archived.');
      providersApi.getProviderAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Archive failed');
    } finally { setSaving(false); }
  };

  const handleRestoreAttachment = async (attachmentId) => {
    setSaving(true); setActionError('');
    try {
      await providersApi.restoreAttachment(attachmentId);
      setActionSuccess('Attachment restored.');
      providersApi.getProviderAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Restore failed (same-day only)');
    } finally { setSaving(false); }
  };

  const handleSaveBackupHours = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.createBackupProviderHours(id, backupForm);
      setShowBackupForm(false);
      setBackupForm({ authorizedHoursWeekly: '', beginDate: '', programType: 'IHSS', primaryProviderName: '', caseNumber: '', recipientName: '' });
      setActionSuccess('Backup provider hours created.');
      providersApi.getBackupProviderHours(id).then(d => setBackupHours(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Save failed');
    } finally { setSaving(false); }
  };

  const handleSaveCoriEdit = async () => {
    setSaving(true); setActionError('');
    try {
      await providersApi.modifyCoriRecord(editingCori.id, coriEditForm);
      setEditingCori(null); setCoriEditForm({});
      setActionSuccess('CORI record updated.');
      providersApi.getProviderCori(id).then(d => setCori(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Update failed');
    } finally { setSaving(false); }
  };

  const handleInactivateCori = async (coriId) => {
    setSaving(true); setActionError('');
    try {
      await providersApi.inactivateCoriRecord(coriId, { reason: 'Manual inactivation' });
      setInactivatingCoriId(null);
      setActionSuccess('CORI record inactivated.');
      providersApi.getProviderCori(id).then(d => setCori(Array.isArray(d) ? d : []));
    } catch (err) {
      setActionError(err?.response?.data?.error || err?.message || 'Inactivation failed');
    } finally { setSaving(false); }
  };

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '—';

  if (loading) return <div className="wq-page"><p>Loading provider...</p></div>;
  if (!provider) return <div className="wq-page"><p>Provider not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Back</button></div>;

  const p = provider;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Provider: {p.providerNumber || [p.lastName, p.firstName].filter(Boolean).join(', ') || p.id}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Back to Providers</button>
      </div>

      {actionError && <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{actionError}</div>}
      {actionSuccess && <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem', display: 'flex', justifyContent: 'space-between' }}><span>{actionSuccess}</span><button onClick={() => setActionSuccess('')} style={{ background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>×</button></div>}

      {/* Action Bar */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Actions</h4></div>
        <div className="wq-manage-bar">
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.approveEnrollment(id), 'Enrollment approved.')}>✓ Approve Enrollment</button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.setIneligible(id, { reason: 'Manual review', setBy: username }), 'Provider set ineligible.')}>✗ Set Ineligible</button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.reinstateProvider(id), 'Provider reinstated.')}>↺ Reinstate</button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.reEnrollProvider(id), 'Provider re-enrolled.')}>↻ Re-Enroll</button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.triggerSsnVerification(id), 'SSN verification triggered.')}>🔍 Verify SSN</button>
        </div>
      </div>

      {/* Tabs */}
      <div className="wq-tabs" style={{ flexWrap: 'wrap' }}>
        {['overview','enrollment','qualification','assignments','cori','violations','exemptions','workweek','traveltime','benefits','sickleave','waivers','notifications','attachments','backuphours','monthlyhours','notes'].map(tab => (
          <button key={tab} className={`wq-tab ${activeTab === tab ? 'active' : ''}`} onClick={() => setActiveTab(tab)}>
            {{ overview:'Overview', enrollment:'Enrollment', qualification:'Qualification', assignments:'Assignments', cori:'CORI', violations:'Violations', exemptions:'OT Exemptions', workweek:'Workweek Agmt', traveltime:'Travel Time', benefits:'Benefits', attachments:'Attachments', backuphours:'Backup Hours', monthlyhours:'Paid Hours', notes:'Notes' }[tab]}
          </button>
        ))}
      </div>

      {/* ── OVERVIEW ── */}
      {activeTab === 'overview' && (
        <>
          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Provider Information</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  {[['Provider Number', p.providerNumber || p.id], ['Name', [p.firstName, p.middleName, p.lastName].filter(Boolean).join(' ') || '—'], ['Eligible Status', p.eligible], ['Provider Status', p.providerStatus], ['County', p.dojCountyName || p.dojCountyCode], ['Enrollment Date', fmt(p.effectiveDate)], ['Active Cases', p.numberOfActiveCases ?? '—'], ['Overtime Violations', p.overtimeViolationCount ?? '—'], ['Has OT Exemption', p.hasOvertimeExemption ? 'Yes' : 'No'], ['SSN Verification', p.ssnVerificationStatus]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
              </div>
            </div>
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Contact</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  {[['Address', p.streetAddress], ['City', p.city], ['State', p.state], ['Zip', p.zipCode], ['Phone', p.primaryPhone || p.secondaryPhone], ['Email', p.email], ['SSN', maskSsn(p.ssn)], ['Date of Birth', fmt(p.dateOfBirth)], ['Gender', p.gender], ['Language', p.spokenLanguage]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
              </div>
            </div>
          </div>
          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Sick Leave</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  {[['Accrued Hours', p.sickLeaveAccruedHours ?? '—'], ['Accrual Date', fmt(p.sickLeaveAccruedDate)], ['Eligible Date', fmt(p.sickLeaveEligibleDate)], ['Service Hours Worked', p.totalServiceHoursWorked ?? '—'], ['Eligibility Period Start', fmt(p.sickLeaveEligibilityPeriodStart)], ['Eligibility Period End', fmt(p.sickLeaveEligibilityPeriodEnd)]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v}</span></div>
                  ))}
                </div>
              </div>
            </div>
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>PA &amp; ESP</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  {[['PA Registered', p.paRegistered ? 'Yes' : 'No'], ['PA Training', p.paTrainingCompleted ? 'Complete' : 'Pending'], ['PA Fingerprinting', p.paFingerprintingCompleted ? 'Complete' : 'Pending'], ['Original Hire Date', fmt(p.originalHireDate)], ['ESP Registered', p.espRegistered ? 'Yes' : 'No'], ['e-Timesheet Status', p.eTimesheetStatus]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* ── ENROLLMENT ── */}
      {activeTab === 'enrollment' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Enrollment Status &amp; Requirements</h4></div>
          <div className="wq-panel-body">
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
              <div>
                <h5 style={{ marginBottom: '0.75rem', color: '#153554' }}>Eligibility</h5>
                <div className="wq-detail-grid">
                  {[['Eligible Status', p.eligible], ['Ineligible Reason', p.ineligibleReason], ['Effective Date', fmt(p.effectiveDate)], ['Enrollment County', p.dojCountyName]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
                <h5 style={{ margin: '1rem 0 0.75rem', color: '#153554' }}>SOC Forms &amp; Agreements</h5>
                <table className="wq-table">
                  <thead><tr><th>Form</th><th>Completed</th><th>Date</th></tr></thead>
                  <tbody>
                    {[
                      ['SOC 426 – Provider Enrollment', p.soc426Completed, p.soc426Date],
                      ['Provider Orientation', p.orientationCompleted, p.orientationDate],
                      ['SOC 846 – Overtime Agreement', p.soc846Completed, p.soc846Date],
                      ['Provider Agreement Signed', p.providerAgreementSigned, null],
                      ['Overtime Agreement Signed', p.overtimeAgreementSigned, null],
                    ].map(([label, done, date]) => (
                      <tr key={label}>
                        <td>{label}</td>
                        <td><span style={badgeStyle(done ? 'yes' : 'no')}>{done ? 'Yes' : 'No'}</span></td>
                        <td>{fmt(date)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div>
                <h5 style={{ marginBottom: '0.75rem', color: '#153554' }}>Background Check (DOJ)</h5>
                <div className="wq-detail-grid">
                  {[['Check Completed', p.backgroundCheckCompleted ? 'Yes' : 'No'], ['Check Date', fmt(p.backgroundCheckDate)], ['Check Status', p.backgroundCheckStatus]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
                <h5 style={{ margin: '1rem 0 0.75rem', color: '#153554' }}>Medi-Cal Status</h5>
                <div className="wq-detail-grid">
                  {[['Medi-Cal Suspended', p.mediCalSuspended ? 'Yes' : 'No'], ['Suspension Begin', fmt(p.mediCalSuspendedBeginDate)], ['Suspension End', fmt(p.mediCalSuspendedEndDate)]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
                <h5 style={{ margin: '1rem 0 0.75rem', color: '#153554' }}>SSN Verification</h5>
                <div className="wq-detail-grid">
                  {[['Status', p.ssnVerificationStatus], ['Taxpayer ID', p.taxpayerId]].map(([l, v]) => (
                    <div key={l} className="wq-detail-row"><span className="wq-detail-label">{l}:</span><span className="wq-detail-value">{v || '—'}</span></div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── ASSIGNMENTS ── */}
      {activeTab === 'assignments' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Case Assignments ({assignments.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAssign(true)}>Assign to Case</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {assignments.length === 0 ? <p className="wq-empty">No case assignments.</p> : (
              <table className="wq-table">
                <thead><tr><th>Case</th><th>Recipient</th><th>Type</th><th>Begin</th><th>End</th><th>Hours</th><th>Pay Rate</th><th>Relationship</th><th>Status</th></tr></thead>
                <tbody>
                  {assignments.map((a, i) => (
                    <tr key={a.id || i} className="wq-clickable-row" onClick={() => a.caseId && navigate(`/cases/${a.caseId}`)}>
                      <td><button className="action-link">{a.caseNumber || a.caseId || '—'}</button></td>
                      <td>{a.recipientName || '—'}</td>
                      <td>{a.providerType || '—'}</td>
                      <td>{fmt(a.beginDate)}</td>
                      <td>{fmt(a.endDate)}</td>
                      <td>{a.assignedHours ?? '—'}</td>
                      <td>{a.payRate ? `$${a.payRate}` : '—'}</td>
                      <td>{a.relationshipToRecipient || '—'}</td>
                      <td><span style={badgeStyle(a.status)}>{a.status || '—'}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── CORI ── */}
      {activeTab === 'cori' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>CORI Records ({cori.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {cori.length === 0 ? <p className="wq-empty">No CORI records.</p> : (
              <table className="wq-table">
                <thead><tr><th>Conviction Date</th><th>Tier</th><th>Crime</th><th>CORI End</th><th>Status</th><th>General Exception</th><th>Exception Dates</th><th>Actions</th></tr></thead>
                <tbody>
                  {cori.map((c, i) => (
                    <tr key={c.id || i}>
                      <td>{fmt(c.convictionDate)}</td>
                      <td><span style={badgeStyle(c.tier === 'TIER_1' ? 'no' : 'pending')}>{c.tier || '—'}</span></td>
                      <td>{c.crimeDescription || c.crimeCode || '—'}</td>
                      <td>{fmt(c.coriEndDate)}</td>
                      <td><span style={badgeStyle(c.status?.toLowerCase())}>{c.status || '—'}</span></td>
                      <td>{c.generalExceptionGranted ? <span style={badgeStyle('yes')}>Granted</span> : 'None'}</td>
                      <td>{c.generalExceptionGranted ? `${fmt(c.generalExceptionBeginDate)} – ${c.generalExceptionEndDate ? fmt(c.generalExceptionEndDate) : 'Open'}` : '—'}</td>
                      <td>
                        {c.status !== 'INACTIVE' && (
                          <div style={{ display: 'flex', gap: '0.25rem' }}>
                            <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '2px 6px' }}
                              onClick={() => { setEditingCori(c); setCoriEditForm({ tier: c.tier, crimeDescription: c.crimeDescription, crimeCode: c.crimeCode, coriEndDate: c.coriEndDate || '' }); }}>
                              Edit
                            </button>
                            <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '2px 6px', color: '#c53030', borderColor: '#fc8181' }}
                              onClick={() => setInactivatingCoriId(c.id)}>
                              Inactivate
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            {/* CORI Edit Form */}
            {editingCori && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', margin: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Modify CORI Record — CI-117567</h5>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field"><label>Tier</label>
                    <select value={coriEditForm.tier || ''} onChange={e => setCoriEditForm(f => ({ ...f, tier: e.target.value }))}>
                      <option value="TIER_1">Tier 1 (Auto Ineligible)</option>
                      <option value="TIER_2">Tier 2 (Exception Possible)</option>
                    </select>
                  </div>
                  <div className="wq-form-field"><label>Crime Code</label>
                    <input value={coriEditForm.crimeCode || ''} onChange={e => setCoriEditForm(f => ({ ...f, crimeCode: e.target.value }))} />
                  </div>
                  <div className="wq-form-field"><label>CORI End Date</label>
                    <input type="date" value={coriEditForm.coriEndDate || ''} onChange={e => setCoriEditForm(f => ({ ...f, coriEndDate: e.target.value }))} />
                  </div>
                  <div className="wq-form-field" style={{ gridColumn: 'span 3' }}><label>Crime Description</label>
                    <input value={coriEditForm.crimeDescription || ''} onChange={e => setCoriEditForm(f => ({ ...f, crimeDescription: e.target.value }))} style={{ width: '100%' }} />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveCoriEdit}>{saving ? 'Saving...' : 'Save Changes'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => { setEditingCori(null); setCoriEditForm({}); }}>Cancel</button>
                </div>
              </div>
            )}

            {/* CORI Inactivate Confirm */}
            {inactivatingCoriId && (
              <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderRadius: '6px', padding: '1rem', margin: '1rem' }}>
                <p style={{ color: '#c53030', marginBottom: '0.75rem' }}>Are you sure you want to inactivate this CORI record? This will remove it from active eligibility calculations.</p>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" style={{ background: '#c53030', borderColor: '#c53030' }} disabled={saving} onClick={() => handleInactivateCori(inactivatingCoriId)}>{saving ? 'Processing...' : 'Yes, Inactivate'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setInactivatingCoriId(null)}>Cancel</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── VIOLATIONS ── */}
      {activeTab === 'violations' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Overtime Violations ({violations.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {violations.length === 0 ? <p className="wq-empty">No violations recorded.</p> : (
              violations.map((v, i) => (
                <div key={v.id || i} style={{ borderBottom: '1px solid #e2e8f0', padding: '1rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                    <div>
                      <strong>Violation #{v.violationNumber}</strong> — {v.violationType || 'Overtime'} &nbsp;
                      <span style={badgeStyle(v.status?.toLowerCase())}>{v.status}</span>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      {!v.countyReviewDate && v.status === 'PENDING_REVIEW' && (
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('county'); setReviewForm({ outcome: '', comments: '' }); }}>County Review</button>
                      )}
                      {v.countyReviewOutcome === 'OVERRIDE_REQUESTED' && !v.supervisorReviewDate && (
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('supervisor'); setReviewForm({ outcome: '', comments: '' }); }}>Supervisor Review</button>
                      )}
                      {v.countyReviewOutcome === 'UPHELD' && !v.countyDisputeFiled && (
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('dispute'); setReviewForm({ comments: '' }); }}>File County Dispute</button>
                      )}
                      {v.countyDisputeFiled && v.countyDisputeOutcome === 'PENDING_REVIEW' && (
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('dispute-resolve'); setReviewForm({ outcome: '', comments: '' }); }}>Resolve Dispute</button>
                      )}
                      {v.countyDisputeOutcome === 'UPHELD' && !v.cdssReviewRequested && (
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('cdss-request'); }}>Request SAR (CDSS)</button>
                      )}
                      {v.cdssReviewRequested && !v.cdssReviewDate && (
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('cdss-outcome'); setReviewForm({ outcome: '', comments: '' }); }}>Record CDSS Outcome</button>
                      )}
                      {v.trainingOffered && !v.trainingCompleted && (
                        <button className="wq-btn wq-btn-primary" style={{ fontSize: '0.8rem' }} onClick={() => { setReviewingViolation(v); setReviewType('training'); }}>Mark Training Complete</button>
                      )}
                    </div>
                  </div>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '0.5rem', marginTop: '0.5rem', fontSize: '0.85rem', color: '#555' }}>
                    <span>Date: {fmt(v.violationDate)}</span>
                    <span>Hours Claimed: {v.hoursClaimed}</span>
                    <span>Max Allowed: {v.maximumAllowed}</span>
                    <span>Hours Over: {v.hoursExceeded}</span>
                    {v.countyReviewDate && <span>County Review: {fmt(v.countyReviewDate)} — {v.countyReviewOutcome}</span>}
                    {v.supervisorReviewDate && <span>Supervisor Review: {fmt(v.supervisorReviewDate)} — {v.supervisorReviewOutcome}</span>}
                    {v.countyDisputeFiled && <span>Dispute Filed: {fmt(v.countyDisputeFiledDate)} — {v.countyDisputeOutcome}</span>}
                    {v.cdssReviewRequested && <span>CDSS SAR: {v.cdssReviewDate ? fmt(v.cdssReviewDate) + ' — ' + v.cdssReviewOutcome : 'Pending'}</span>}
                    {v.trainingOffered && <span>Training Due: {fmt(v.trainingDueDate)}{v.trainingCompleted ? ' ✓ Complete' : ''}</span>}
                    {v.terminationEffectiveDate && <span style={{ color: '#c53030' }}>Suspension Effective: {fmt(v.terminationEffectiveDate)}</span>}
                    {v.reinstatementDate && <span style={{ color: '#276749' }}>Reinstatement: {fmt(v.reinstatementDate)}</span>}
                  </div>
                </div>
              ))
            )}

            {/* Inline review form */}
            {reviewingViolation && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', margin: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>
                  {{ county: 'County Review', supervisor: 'Supervisor Review', dispute: 'File County Dispute', 'dispute-resolve': 'Resolve County Dispute', 'cdss-request': 'Request CDSS SAR', 'cdss-outcome': 'Record CDSS Outcome', training: 'Confirm Training Completion' }[reviewType]} — Violation #{reviewingViolation.violationNumber}
                </h5>
                {reviewType !== 'cdss-request' && reviewType !== 'training' && (
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                    {reviewType !== 'dispute' && (
                      <div className="wq-form-field">
                        <label>Outcome *</label>
                        <select value={reviewForm.outcome} onChange={e => setReviewForm(f => ({ ...f, outcome: e.target.value }))}>
                          <option value="">Select outcome...</option>
                          {reviewType === 'county' && <><option value="UPHELD">Upheld</option><option value="OVERRIDE_REQUESTED">Request Override (Supervisor)</option></>}
                          {reviewType === 'supervisor' && <><option value="APPROVED">Approved (Inactivate Violation)</option><option value="REJECTED">Rejected (Violation Stands)</option></>}
                          {reviewType === 'dispute-resolve' && <><option value="UPHELD">Upheld</option><option value="OVERRIDE">Override (Inactivate Violation)</option></>}
                          {reviewType === 'cdss-outcome' && <><option value="UPHELD">Upheld</option><option value="OVERRIDE">Override (CDSS Final)</option></>}
                        </select>
                      </div>
                    )}
                    <div className="wq-form-field">
                      <label>Comments</label>
                      <textarea rows={3} value={reviewForm.comments} onChange={e => setReviewForm(f => ({ ...f, comments: e.target.value }))} style={{ width: '100%', padding: '0.375rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
                    </div>
                  </div>
                )}
                {reviewType === 'cdss-request' && <p style={{ color: '#555', marginBottom: '1rem' }}>Confirm requesting CDSS State Administrative Review for Violation #{reviewingViolation.violationNumber}?</p>}
                {reviewType === 'training' && <p style={{ color: '#555', marginBottom: '1rem' }}>Confirm that training for Violation #{reviewingViolation.violationNumber} has been completed?</p>}
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleViolationReview}>{saving ? 'Saving...' : 'Submit'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setReviewingViolation(null)}>Cancel</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── OT EXEMPTIONS ── */}
      {activeTab === 'exemptions' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Overtime Exemptions ({exemptions.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowExemptionForm(true)}>Create Exemption</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {showExemptionForm && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', margin: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Create Overtime Exemption (CI-668111)</h5>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field">
                    <label>Begin Date *</label>
                    <input type="date" value={exemptionForm.beginDate} onChange={e => setExemptionForm(f => ({ ...f, beginDate: e.target.value }))} />
                  </div>
                  <div className="wq-form-field">
                    <label>End Date</label>
                    <input type="date" value={exemptionForm.endDate} onChange={e => setExemptionForm(f => ({ ...f, endDate: e.target.value }))} />
                  </div>
                  <div className="wq-form-field">
                    <label>Exemption Type *</label>
                    <select value={exemptionForm.exemptionType} onChange={e => setExemptionForm(f => ({ ...f, exemptionType: e.target.value }))}>
                      <option value="EXTRAORDINARY_CIRCUMSTANCES">Extraordinary Circumstances (SOC 2305)</option>
                      <option value="RECIPIENT_WAIVER">Recipient Waiver</option>
                      <option value="GENERAL">General</option>
                    </select>
                  </div>
                  <div className="wq-form-field">
                    <label>Comments (max 1,000 chars)</label>
                    <textarea rows={3} maxLength={1000} value={exemptionForm.comments} onChange={e => setExemptionForm(f => ({ ...f, comments: e.target.value }))} style={{ width: '100%', padding: '0.375rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveExemption}>{saving ? 'Saving...' : 'Save Exemption'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setShowExemptionForm(false)}>Cancel</button>
                </div>
              </div>
            )}
            {exemptions.length === 0 && !showExemptionForm ? <p className="wq-empty">No overtime exemptions.</p> : (
              <table className="wq-table">
                <thead><tr><th>Type</th><th>Begin</th><th>End</th><th>Status</th><th>Comments</th><th>Callback Hrs</th><th>Actions</th></tr></thead>
                <tbody>
                  {exemptions.map((e, i) => (
                    <tr key={e.id || i}>
                      <td>{e.exemptionType}</td>
                      <td>{fmt(e.beginDate)}</td>
                      <td>{fmt(e.endDate) || 'Open'}</td>
                      <td><span style={badgeStyle(e.status?.toLowerCase())}>{e.status}</span></td>
                      <td style={{ maxWidth: '200px', whiteSpace: 'pre-wrap', fontSize: '0.8rem' }}>{e.comments || '—'}</td>
                      <td>{e.callbackHours || '—'}</td>
                      <td>{e.status === 'ACTIVE' && <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem' }} onClick={() => handleAction(() => providersApi.inactivateExemption(e.id, { reason: 'Manual inactivation' }), 'Exemption inactivated.')}>Inactivate</button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── WORKWEEK AGREEMENTS ── */}
      {activeTab === 'workweek' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Workweek Agreements ({workweekAgreements.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowWorkweekForm(true)}>Create Agreement</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {showWorkweekForm && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', margin: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Provider Workweek Agreement (CI-480910)</h5>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field"><label>Recipient Name</label><input value={workweekForm.recipientName} onChange={e => setWorkweekForm(f => ({ ...f, recipientName: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Case Number</label><input value={workweekForm.caseNumber} onChange={e => setWorkweekForm(f => ({ ...f, caseNumber: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Workweek Start Day *</label>
                    <select value={workweekForm.workweekStartDay} onChange={e => setWorkweekForm(f => ({ ...f, workweekStartDay: e.target.value }))}>
                      {['SUNDAY','MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY'].map(d => <option key={d} value={d}>{d}</option>)}
                    </select>
                  </div>
                  <div className="wq-form-field"><label>Agreed Hours/Week</label><input type="number" min="0" step="0.25" value={workweekForm.agreedHoursWeekly} onChange={e => setWorkweekForm(f => ({ ...f, agreedHoursWeekly: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Begin Date *</label><input type="date" value={workweekForm.beginDate} onChange={e => setWorkweekForm(f => ({ ...f, beginDate: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Includes Travel Time</label>
                    <select value={workweekForm.includesTravelTime ? 'yes' : 'no'} onChange={e => setWorkweekForm(f => ({ ...f, includesTravelTime: e.target.value === 'yes' }))}>
                      <option value="no">No</option><option value="yes">Yes</option>
                    </select>
                  </div>
                  {workweekForm.includesTravelTime && <div className="wq-form-field"><label>Travel Hours/Week</label><input type="number" min="0" max="7" step="0.25" value={workweekForm.travelHoursWeekly} onChange={e => setWorkweekForm(f => ({ ...f, travelHoursWeekly: e.target.value }))} /></div>}
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveWorkweek}>{saving ? 'Saving...' : 'Save Agreement'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setShowWorkweekForm(false)}>Cancel</button>
                </div>
              </div>
            )}
            {workweekAgreements.length === 0 && !showWorkweekForm ? <p className="wq-empty">No workweek agreements.</p> : (
              <table className="wq-table">
                <thead><tr><th>Recipient</th><th>Case</th><th>Start Day</th><th>Agreed Hrs/Wk</th><th>Begin</th><th>End</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {workweekAgreements.map((w, i) => (
                    <tr key={w.id || i}>
                      <td>{w.recipientName || '—'}</td>
                      <td>{w.caseNumber || '—'}</td>
                      <td>{w.workweekStartDay || '—'}</td>
                      <td>{w.agreedHoursWeekly || '—'}</td>
                      <td>{fmt(w.beginDate)}</td>
                      <td>{fmt(w.endDate) || 'Open'}</td>
                      <td><span style={badgeStyle(w.status?.toLowerCase())}>{w.status}</span></td>
                      <td>{w.status === 'ACTIVE' && <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem' }} onClick={() => handleAction(() => providersApi.inactivateWorkweekAgreement(w.id, { reason: 'Manual inactivation' }), 'Agreement inactivated.')}>Inactivate</button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── TRAVEL TIME ── */}
      {activeTab === 'traveltime' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Travel Time Records ({travelTimes.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowTravelForm(true)}>Add Travel Time</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {showTravelForm && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', margin: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Travel Time (CI-480867)</h5>
                <p style={{ fontSize: '0.85rem', color: '#718096', marginBottom: '1rem' }}>7-hour rule: total weekly travel time across all recipients must not exceed 7 hours.</p>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field"><label>To Recipient Name</label><input value={travelForm.toRecipientName} onChange={e => setTravelForm(f => ({ ...f, toRecipientName: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>To Case Number</label><input value={travelForm.toCaseNumber} onChange={e => setTravelForm(f => ({ ...f, toCaseNumber: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Traveling From Recipient *</label><input value={travelForm.fromRecipientName} onChange={e => setTravelForm(f => ({ ...f, fromRecipientName: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>From Case Number</label><input value={travelForm.fromCaseNumber} onChange={e => setTravelForm(f => ({ ...f, fromCaseNumber: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Travel Hours/Week</label><input type="number" min="0" max="7" step="0.25" value={travelForm.travelHoursWeekly} onChange={e => setTravelForm(f => ({ ...f, travelHoursWeekly: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>One-way Travel Minutes</label><input type="number" min="0" value={travelForm.travelMinutes} onChange={e => setTravelForm(f => ({ ...f, travelMinutes: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Begin Date *</label><input type="date" value={travelForm.beginDate} onChange={e => setTravelForm(f => ({ ...f, beginDate: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Program Type</label>
                    <select value={travelForm.programType} onChange={e => setTravelForm(f => ({ ...f, programType: e.target.value }))}>
                      <option value="IHSS">IHSS</option><option value="WPCS">WPCS</option>
                    </select>
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveTravelTime}>{saving ? 'Saving...' : 'Save Travel Time'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setShowTravelForm(false)}>Cancel</button>
                </div>
              </div>
            )}
            {travelTimes.length === 0 && !showTravelForm ? <p className="wq-empty">No travel time records.</p> : (
              <table className="wq-table">
                <thead><tr><th>From Recipient</th><th>To Recipient</th><th>Program</th><th>Hrs/Wk</th><th>Minutes</th><th>Begin</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {travelTimes.map((t, i) => (
                    <tr key={t.id || i}>
                      <td>{t.fromRecipientName || '—'}</td>
                      <td>{t.toRecipientName || '—'}</td>
                      <td>{t.programType || '—'}</td>
                      <td>{t.travelHoursWeekly || '—'}</td>
                      <td>{t.travelMinutes || '—'}</td>
                      <td>{fmt(t.beginDate)}</td>
                      <td><span style={badgeStyle(t.status?.toLowerCase())}>{t.status}</span></td>
                      <td>{t.status === 'ACTIVE' && <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem' }} onClick={() => handleAction(() => providersApi.inactivateTravelTime(t.id), 'Travel time inactivated.')}>Inactivate</button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── BENEFITS / DEDUCTIONS ── */}
      {activeTab === 'benefits' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Benefits &amp; Deductions ({benefits.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowBenefitForm(true)}>Add Benefit</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {showBenefitForm && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', margin: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Provider Benefit/Deduction (CI-117534)</h5>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field"><label>Benefit Type *</label>
                    <select value={benefitForm.benefitType} onChange={e => setBenefitForm(f => ({ ...f, benefitType: e.target.value }))}>
                      {['HEALTH','DENTAL','VISION','LIFE_INSURANCE','SDI','CALPERS'].map(t => <option key={t} value={t}>{t}</option>)}
                    </select>
                  </div>
                  <div className="wq-form-field"><label>Plan Name</label><input value={benefitForm.planName} onChange={e => setBenefitForm(f => ({ ...f, planName: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Coverage Type</label>
                    <select value={benefitForm.coverageType} onChange={e => setBenefitForm(f => ({ ...f, coverageType: e.target.value }))}>
                      <option value="SINGLE">Single</option><option value="EMPLOYEE_PLUS_ONE">Employee + One</option><option value="FAMILY">Family</option>
                    </select>
                  </div>
                  <div className="wq-form-field"><label>Monthly Deduction Amount</label><input type="number" min="0" step="0.01" value={benefitForm.monthlyDeductionAmount} onChange={e => setBenefitForm(f => ({ ...f, monthlyDeductionAmount: e.target.value }))} /></div>
                  <div className="wq-form-field"><label>Begin Date *</label><input type="date" value={benefitForm.beginDate} onChange={e => setBenefitForm(f => ({ ...f, beginDate: e.target.value }))} /></div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveBenefit}>{saving ? 'Saving...' : 'Save Benefit'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setShowBenefitForm(false)}>Cancel</button>
                </div>
              </div>
            )}
            {benefits.length === 0 && !showBenefitForm ? <p className="wq-empty">No benefit deductions.</p> : (
              <table className="wq-table">
                <thead><tr><th>Type</th><th>Plan</th><th>Coverage</th><th>Monthly Amt</th><th>Begin</th><th>End</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {benefits.map((b, i) => (
                    <tr key={b.id || i}>
                      <td>{b.benefitType}</td>
                      <td>{b.planName || '—'}</td>
                      <td>{b.coverageType || '—'}</td>
                      <td>{b.monthlyDeductionAmount ? `$${b.monthlyDeductionAmount}` : '—'}</td>
                      <td>{fmt(b.beginDate)}</td>
                      <td>{fmt(b.endDate) || 'Active'}</td>
                      <td><span style={badgeStyle(b.status?.toLowerCase())}>{b.status}</span></td>
                      <td>{b.status === 'ACTIVE' && <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem' }} onClick={() => handleAction(() => providersApi.terminateProviderBenefit(b.id, { reason: 'Manual termination' }), 'Benefit terminated.')}>Terminate</button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── ATTACHMENTS (CI-117642-117650) ── */}
      {activeTab === 'attachments' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Provider Attachments ({attachments.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAttachmentForm(true)}>Upload Document</button>
          </div>
          <div className="wq-panel-body">
            <p style={{ fontSize: '0.8rem', color: '#718096', marginBottom: '0.75rem' }}>
              Allowed: PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG (max 5 MB). Attachments are archived nightly. Same-day restore allowed.
            </p>
            {showAttachmentForm && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', marginBottom: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Upload Attachment — CI-117643</h5>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field"><label>Document Type *</label>
                    <select value={attachmentForm.documentType} onChange={e => setAttachmentForm(f => ({ ...f, documentType: e.target.value }))}>
                      <option value="SOC_426">SOC 426 – Provider Enrollment</option>
                      <option value="SOC_426A">SOC 426A – Designation of Provider</option>
                      <option value="SOC_846">SOC 846 – Overtime Agreement</option>
                      <option value="SOC_2305">SOC 2305 – OT Exemption Notification</option>
                      <option value="SOC_2303">SOC 2303</option>
                      <option value="SOC_2313">SOC 2313</option>
                      <option value="OTHER">Other</option>
                    </select>
                  </div>
                  <div className="wq-form-field"><label>File Name</label>
                    <input value={attachmentForm.originalFileName} onChange={e => setAttachmentForm(f => ({ ...f, originalFileName: e.target.value }))} placeholder="filename.pdf" />
                  </div>
                  <div className="wq-form-field"><label>Description</label>
                    <input value={attachmentForm.description} onChange={e => setAttachmentForm(f => ({ ...f, description: e.target.value }))} placeholder="Brief description..." />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveAttachment}>{saving ? 'Uploading...' : 'Upload'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setShowAttachmentForm(false)}>Cancel</button>
                </div>
              </div>
            )}
            {attachments.length === 0 ? <p className="wq-empty">No attachments uploaded.</p> : (
              <table className="wq-table">
                <thead><tr><th>Type</th><th>File Name</th><th>Description</th><th>Upload Date</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  {attachments.map((a, i) => (
                    <tr key={a.id || i}>
                      <td>{a.documentType?.replace(/_/g, ' ') || '—'}</td>
                      <td>{a.originalFileName || '—'}</td>
                      <td>{a.description || '—'}</td>
                      <td>{fmt(a.uploadDate)}</td>
                      <td><span style={badgeStyle(a.status?.toLowerCase())}>{a.status || '—'}</span></td>
                      <td>
                        <div style={{ display: 'flex', gap: '0.25rem' }}>
                          {(a.status === 'ACTIVE' || a.status === 'RESTORED') && (
                            <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '2px 6px' }} onClick={() => handleArchiveAttachment(a.id)}>Archive</button>
                          )}
                          {a.status === 'ARCHIVED' && (
                            <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '2px 6px' }} onClick={() => handleRestoreAttachment(a.id)}>Restore (Same-Day)</button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── BACKUP PROVIDER HOURS (CI-117646/117647) ── */}
      {activeTab === 'backuphours' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Backup Provider Hours ({backupHours.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowBackupForm(true)}>Add Backup Hours</button>
          </div>
          <div className="wq-panel-body">
            {showBackupForm && (
              <div style={{ background: '#f8f9fa', border: '1px solid #c8d6e0', borderRadius: '6px', padding: '1.25rem', marginBottom: '1rem' }}>
                <h5 style={{ marginBottom: '1rem', color: '#153554' }}>Create Backup Provider Hours — CI-117647</h5>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                  <div className="wq-form-field"><label>Authorized Hours/Week *</label>
                    <input type="number" step="0.5" min="0" value={backupForm.authorizedHoursWeekly} onChange={e => setBackupForm(f => ({ ...f, authorizedHoursWeekly: e.target.value }))} />
                  </div>
                  <div className="wq-form-field"><label>Begin Date *</label>
                    <input type="date" value={backupForm.beginDate} onChange={e => setBackupForm(f => ({ ...f, beginDate: e.target.value }))} />
                  </div>
                  <div className="wq-form-field"><label>Program Type</label>
                    <select value={backupForm.programType} onChange={e => setBackupForm(f => ({ ...f, programType: e.target.value }))}>
                      <option value="IHSS">IHSS</option>
                      <option value="WPCS">WPCS</option>
                    </select>
                  </div>
                  <div className="wq-form-field"><label>Primary Provider Name</label>
                    <input value={backupForm.primaryProviderName} onChange={e => setBackupForm(f => ({ ...f, primaryProviderName: e.target.value }))} placeholder="Provider covering for..." />
                  </div>
                  <div className="wq-form-field"><label>Case Number</label>
                    <input value={backupForm.caseNumber} onChange={e => setBackupForm(f => ({ ...f, caseNumber: e.target.value }))} />
                  </div>
                  <div className="wq-form-field"><label>Recipient Name</label>
                    <input value={backupForm.recipientName} onChange={e => setBackupForm(f => ({ ...f, recipientName: e.target.value }))} />
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-primary" disabled={saving} onClick={handleSaveBackupHours}>{saving ? 'Saving...' : 'Save'}</button>
                  <button className="wq-btn wq-btn-outline" onClick={() => setShowBackupForm(false)}>Cancel</button>
                </div>
              </div>
            )}
            {backupHours.length === 0 ? <p className="wq-empty">No backup provider hours recorded.</p> : (
              <table className="wq-table">
                <thead><tr><th>Primary Provider</th><th>Case</th><th>Recipient</th><th>Weekly Hrs</th><th>Program</th><th>Begin</th><th>End</th><th>Status</th></tr></thead>
                <tbody>
                  {backupHours.map((b, i) => (
                    <tr key={b.id || i}>
                      <td>{b.primaryProviderName || '—'}</td>
                      <td>{b.caseNumber || '—'}</td>
                      <td>{b.recipientName || '—'}</td>
                      <td>{b.authorizedHoursWeekly ?? '—'}</td>
                      <td>{b.programType || '—'}</td>
                      <td>{fmt(b.beginDate)}</td>
                      <td>{fmt(b.endDate)}</td>
                      <td><span style={badgeStyle(b.status?.toLowerCase())}>{b.status || '—'}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── MONTHLY PAID HOURS ── */}
      {activeTab === 'monthlyhours' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Monthly Paid Hours Summary</h4>
            <button className="wq-btn wq-btn-outline" onClick={() => providersApi.getMonthlyPaidHours(id).then(d => setMonthlyHours(d)).catch(() => {})}>Refresh</button>
          </div>
          <div className="wq-panel-body">
            {!monthlyHours ? (
              <p className="wq-empty">Click Refresh to load monthly paid hours summary.</p>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
                <div>
                  <h5 style={{ marginBottom: '0.75rem', color: '#153554' }}>Hours Summary</h5>
                  <div className="wq-detail-grid">
                    {[
                      ['Provider Name', monthlyHours.providerName],
                      ['Provider Number', monthlyHours.providerNumber],
                      ['Authorized Weekly Hours', monthlyHours.totalAuthorizedWeeklyHours],
                      ['Estimated Monthly Hours', monthlyHours.estimatedMonthlyHours],
                      ['Active Assignments', monthlyHours.activeAssignmentCount],
                    ].map(([l, v]) => (
                      <div key={l} className="wq-detail-row">
                        <span className="wq-detail-label">{l}:</span>
                        <span className="wq-detail-value">{v ?? '—'}</span>
                      </div>
                    ))}
                  </div>
                </div>
                <div>
                  <h5 style={{ marginBottom: '0.75rem', color: '#153554' }}>Sick Leave Balance</h5>
                  <div className="wq-detail-grid">
                    {[
                      ['Accrued Hours', monthlyHours.sickLeaveAccruedHours],
                      ['Remaining Hours', monthlyHours.sickLeaveRemainingHours],
                    ].map(([l, v]) => (
                      <div key={l} className="wq-detail-row">
                        <span className="wq-detail-label">{l}:</span>
                        <span className="wq-detail-value" style={{ fontWeight: l === 'Remaining Hours' ? 600 : 'normal' }}>{v ?? '—'}</span>
                      </div>
                    ))}
                  </div>
                  <p style={{ fontSize: '0.8rem', color: '#718096', marginTop: '1rem' }}>
                    Estimated monthly hours = authorized weekly hours × 4.33. Actual hours are tracked via approved timesheets.
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── NOTES ── */}
      {activeTab === 'qualification' && (() => {
        const check = (val, label) => (
          <div key={label} style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderBottom: '1px solid #f0f0f0' }}>
            <span style={{ fontSize: 16 }}>{val ? '✅' : '❌'}</span>
            <span style={{ flex: 1, fontSize: 13, color: '#2d3748' }}>{label}</span>
          </div>
        );
        const fmtMins = (m) => {
          if (!m) return '0:00';
          return `${Math.floor(m/60)}:${String(m%60).padStart(2,'0')}`;
        };
        const handleRecordTraining = async () => {
          setSaving(true); setActionError(''); setActionSuccess('');
          try {
            await providersApi.recordProviderTraining(id, {
              ...trainingForm,
              hoursCompletedMinutes: trainingForm.hoursCompletedMinutes ? Number(trainingForm.hoursCompletedMinutes) : null
            });
            setActionSuccess('Training record saved.');
            setShowTrainingForm(false);
            setTrainingForm({ trainingType: 'ANNUAL_REFRESHER', completionDate: '', hoursCompletedMinutes: '', certificateNumber: '', notes: '' });
            providersApi.getQualificationSummary(id).then(d => setQualSummary(d)).catch(() => {});
            providersApi.getProviderTraining(id).then(d => setTrainingRecords(Array.isArray(d) ? d : [])).catch(() => {});
          } catch (err) {
            setActionError(err?.response?.data?.error || 'Failed to save training record.');
          } finally {
            setSaving(false);
          }
        };
        const qs = qualSummary;
        return (
          <div className="wq-panel">
            <div className="wq-panel-header">
              <h4>Provider Qualification — DSD Section 23</h4>
              <button className="wq-btn wq-btn-primary" onClick={() => setShowTrainingForm(true)}>+ Record Training</button>
            </div>
            <div className="wq-panel-body">
              {!qs ? (
                <p className="wq-empty">Loading qualification summary…</p>
              ) : (
                <>
                  {/* Overall status banner */}
                  <div style={{
                    padding: '10px 14px', borderRadius: 6, marginBottom: 16,
                    background: qs.qualificationMet ? '#c6f6d5' : '#fed7d7',
                    color: qs.qualificationMet ? '#276749' : '#9b2335',
                    fontWeight: 700, fontSize: 14
                  }}>
                    {qs.qualificationMet ? '✅ All qualification requirements met' : '❌ Qualification requirements not fully met'}
                    {qs.ineligibleReason && <span style={{ marginLeft: 12, fontWeight: 400, fontSize: 13 }}>Reason: {qs.ineligibleReason}</span>}
                  </div>

                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
                    {/* Enrollment Requirements */}
                    <div>
                      <div style={{ fontWeight: 700, fontSize: 13, color: '#4a5568', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Enrollment Requirements</div>
                      {check(qs.soc426Completed, `SOC 426 Completed${qs.soc426Date ? ' — ' + fmt(qs.soc426Date) : ''}`)}
                      {check(qs.orientationCompleted, `Provider Orientation${qs.orientationDate ? ' — ' + fmt(qs.orientationDate) : ''}`)}
                      {check(qs.backgroundCheckCompleted, `Background Check${qs.backgroundCheckDate ? ' — ' + fmt(qs.backgroundCheckDate) : ''}${qs.backgroundCheckStatus ? ' (' + qs.backgroundCheckStatus + ')' : ''}`)}
                      {check(qs.providerAgreementSigned, 'Provider Agreement Signed')}
                    </div>

                    {/* PA & CORI */}
                    <div>
                      <div style={{ fontWeight: 700, fontSize: 13, color: '#4a5568', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>PA Registration & CORI</div>
                      {check(qs.paRegistered, 'PA Registered')}
                      {check(qs.paTrainingCompleted, 'PA Training Completed')}
                      {check(qs.paFingerprintingCompleted, 'PA Fingerprinting Completed')}
                      <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '6px 0', borderBottom: '1px solid #f0f0f0' }}>
                        <span style={{ fontSize: 16 }}>
                          {qs.coriStatus === 'CLEAR' || qs.coriStatus === 'TIER_2_WAIVED' ? '✅' : '❌'}
                        </span>
                        <span style={{ flex: 1, fontSize: 13, color: '#2d3748' }}>
                          CORI Status: <span style={{ fontWeight: 600 }}>{qs.coriStatus}</span>
                          {qs.activeCoriCount > 0 && <span style={{ marginLeft: 6, color: '#718096' }}>({qs.activeCoriCount} active record{qs.activeCoriCount > 1 ? 's' : ''})</span>}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Annual Training */}
                  <div style={{ marginTop: 16 }}>
                    <div style={{ fontWeight: 700, fontSize: 13, color: '#4a5568', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                      Annual Training — FY {qs.currentFiscalYear}
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 16, padding: '10px 14px', background: '#f7fafc', borderRadius: 6, border: '1px solid #e2e8f0' }}>
                      <div>
                        <span style={{ fontSize: 12, color: '#718096' }}>Completed</span>
                        <div style={{ fontWeight: 700, fontSize: 20, color: '#2d3748' }}>{fmtMins(qs.trainingCompletedMinutes)}</div>
                      </div>
                      <div style={{ color: '#a0aec0', fontSize: 20 }}>/</div>
                      <div>
                        <span style={{ fontSize: 12, color: '#718096' }}>Required</span>
                        <div style={{ fontWeight: 700, fontSize: 20, color: '#2d3748' }}>{fmtMins(qs.trainingRequiredMinutes)}</div>
                      </div>
                      <div style={{ marginLeft: 16 }}>
                        <span style={{
                          background: qs.trainingStatus === 'COMPLETE' ? '#c6f6d5' : qs.trainingStatus === 'INCOMPLETE' ? '#feebc8' : '#e2e8f0',
                          color: qs.trainingStatus === 'COMPLETE' ? '#276749' : qs.trainingStatus === 'INCOMPLETE' ? '#c05621' : '#4a5568',
                          padding: '3px 10px', borderRadius: 12, fontSize: 13, fontWeight: 700
                        }}>{qs.trainingStatus}</span>
                      </div>
                    </div>
                  </div>
                </>
              )}

              {/* Training History */}
              <div style={{ marginTop: 20 }}>
                <div style={{ fontWeight: 700, fontSize: 13, color: '#4a5568', marginBottom: 8, textTransform: 'uppercase', letterSpacing: '0.05em' }}>Training History</div>
                {trainingRecords.length === 0 ? (
                  <p className="wq-empty">No training records.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr>
                        <th>Type</th><th>Fiscal Year</th><th>Completion Date</th>
                        <th>Completed (hrs)</th><th>Required (hrs)</th><th>Certificate #</th><th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {trainingRecords.map(t => (
                        <tr key={t.id}>
                          <td>{t.trainingType?.replace(/_/g, ' ')}</td>
                          <td>{t.fiscalYear || '—'}</td>
                          <td>{fmt(t.completionDate)}</td>
                          <td>{fmtMins(t.hoursCompletedMinutes)}</td>
                          <td>{t.hoursRequiredMinutes ? fmtMins(t.hoursRequiredMinutes) : '—'}</td>
                          <td>{t.certificateNumber || '—'}</td>
                          <td><span style={badgeStyle(t.status?.toLowerCase())}>{t.status}</span></td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>

            {/* Add Training Modal */}
            {showTrainingForm && (
              <div className="wq-modal-overlay">
                <div className="wq-modal">
                  <div className="wq-modal-header">
                    <h3>Record Training Completion</h3>
                    <button className="wq-modal-close" onClick={() => setShowTrainingForm(false)}>✕</button>
                  </div>
                  <div className="wq-modal-body">
                    <div className="wq-form-grid">
                      <div className="wq-form-group">
                        <label className="wq-label">Training Type *</label>
                        <select className="wq-input" value={trainingForm.trainingType}
                          onChange={e => setTrainingForm(f => ({ ...f, trainingType: e.target.value }))}>
                          <option value="INITIAL_ORIENTATION">Initial Orientation</option>
                          <option value="ANNUAL_REFRESHER">Annual Refresher</option>
                          <option value="VIOLATION_REMEDIATION">Violation Remediation</option>
                        </select>
                      </div>
                      <div className="wq-form-group">
                        <label className="wq-label">Completion Date *</label>
                        <input type="date" className="wq-input" value={trainingForm.completionDate}
                          onChange={e => setTrainingForm(f => ({ ...f, completionDate: e.target.value }))} />
                      </div>
                      <div className="wq-form-group">
                        <label className="wq-label">Hours Completed (minutes)</label>
                        <input type="number" className="wq-input" min="0" value={trainingForm.hoursCompletedMinutes}
                          onChange={e => setTrainingForm(f => ({ ...f, hoursCompletedMinutes: e.target.value }))}
                          placeholder="e.g. 1440 = 24:00 hrs" />
                      </div>
                      <div className="wq-form-group">
                        <label className="wq-label">Certificate Number</label>
                        <input className="wq-input" value={trainingForm.certificateNumber}
                          onChange={e => setTrainingForm(f => ({ ...f, certificateNumber: e.target.value }))} />
                      </div>
                    </div>
                    <div className="wq-form-group" style={{ marginTop: 8 }}>
                      <label className="wq-label">Notes</label>
                      <textarea className="wq-input" rows={2} value={trainingForm.notes}
                        onChange={e => setTrainingForm(f => ({ ...f, notes: e.target.value }))} />
                    </div>
                  </div>
                  <div className="wq-modal-footer">
                    <button className="wq-btn wq-btn-secondary" onClick={() => setShowTrainingForm(false)}>Cancel</button>
                    <button className="wq-btn wq-btn-primary" onClick={handleRecordTraining} disabled={saving || !trainingForm.completionDate}>
                      {saving ? 'Saving…' : 'Save'}
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        );
      })()}

      {activeTab === 'notes' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Notes ({notes.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAddNote(true)}>Add Note</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {notes.length === 0 ? <p className="wq-empty">No notes for this provider.</p> : (
              <table className="wq-table">
                <thead><tr><th>Date</th><th>Priority</th><th>Sensitivity</th><th>Text</th><th>Created By</th></tr></thead>
                <tbody>
                  {notes.map((n, i) => (
                    <tr key={n.id || i}>
                      <td>{n.createdAt ? new Date(n.createdAt).toLocaleString() : '—'}</td>
                      <td>{n.priority || '—'}</td>
                      <td>{n.sensitivity || '—'}</td>
                      <td style={{ maxWidth: '350px', whiteSpace: 'pre-wrap' }}>{n.text || n.content || n.noteText || '—'}</td>
                      <td>{n.createdBy || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* ── Sick Leave Tab (DSD Section 23.9) ── */}
      {activeTab === 'sickleave' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Sick Leave Balance</h4></div>
          <div className="wq-panel-body">
            {sickLeaveAccrual ? (
              <div className="wq-grid wq-grid-2">
                <div><strong>Accrual Year:</strong> {sickLeaveAccrual.accrual?.accrualYear || new Date().getFullYear()}</div>
                <div><strong>Hours Accrued:</strong> {sickLeaveAccrual.accrual ? `${Math.floor(sickLeaveAccrual.accrual.hoursAccrued/60)}:${String(sickLeaveAccrual.accrual.hoursAccrued%60).padStart(2,'0')}` : '—'}</div>
                <div><strong>Hours Used:</strong> {sickLeaveAccrual.accrual ? `${Math.floor(sickLeaveAccrual.accrual.hoursUsed/60)}:${String(sickLeaveAccrual.accrual.hoursUsed%60).padStart(2,'0')}` : '—'}</div>
                <div><strong>Hours Available:</strong> <span style={{fontWeight:700, color:'#276749'}}>{sickLeaveAccrual.hoursAvailableFormatted || '—'}</span></div>
                <div><strong>Last Accrual Date:</strong> {fmt(sickLeaveAccrual.accrual?.lastAccrualDate)}</div>
                <div><strong>Eligibility Date:</strong> {fmt(sickLeaveAccrual.accrual?.eligibilityDate)}</div>
              </div>
            ) : <p className="wq-empty">No sick leave accrual record for current year.</p>}
          </div>
          <div className="wq-panel-header" style={{marginTop:'1rem'}}><h4>Sick Leave Claims ({sickLeaveClaims.length})</h4></div>
          <div className="wq-panel-body" style={{padding:0}}>
            {sickLeaveClaims.length === 0 ? <p className="wq-empty">No sick leave claims.</p> : (
              <table className="wq-table"><thead><tr><th>Claim #</th><th>Pay Period</th><th>Hours</th><th>Status</th><th>Entered</th></tr></thead>
                <tbody>{sickLeaveClaims.map((c,i) => (
                  <tr key={c.id||i}><td>{c.claimNumber}</td><td>{fmt(c.payPeriodBeginDate)}</td>
                    <td>{c.claimedHours ? `${Math.floor(c.claimedHours/60)}:${String(c.claimedHours%60).padStart(2,'0')}` : '—'}</td>
                    <td><span style={badgeStyle(c.status)}>{c.status}</span></td><td>{fmt(c.claimEnteredDate)}</td></tr>
                ))}</tbody></table>
            )}
          </div>
        </div>
      )}

      {/* ── Waivers Tab (DSD Section 23.3) ── */}
      {activeTab === 'waivers' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Recipient Waivers ({waivers.length})</h4></div>
          <div className="wq-panel-body" style={{padding:0}}>
            {waivers.length === 0 ? <p className="wq-empty">No recipient waivers for this provider.</p> : (
              <table className="wq-table"><thead><tr><th>Recipient</th><th>Case #</th><th>Conviction</th><th>Status</th><th>Effective</th><th>County Decision</th></tr></thead>
                <tbody>{waivers.map((w,i) => (
                  <tr key={w.id||i}><td>{w.recipientName || '—'}</td><td>{w.caseNumber || '—'}</td>
                    <td>{w.convictionType || '—'}</td><td><span style={badgeStyle(w.status)}>{w.status}</span></td>
                    <td>{fmt(w.effectiveDate)}</td><td>{w.countyDecision || '—'}</td></tr>
                ))}</tbody></table>
            )}
          </div>
        </div>
      )}

      {/* ── Notification Preferences Tab (DSD Section 23.10) ── */}
      {activeTab === 'notifications' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Notification Preferences</h4></div>
          <div className="wq-panel-body">
            {notifPrefs && !notifPrefs.message ? (
              <div className="wq-grid wq-grid-2">
                <div><strong>Preferred Contact:</strong> {notifPrefs.preferredContactMethod || '—'}</div>
                <div><strong>Timesheet Method:</strong> {notifPrefs.timesheetMethod || '—'}</div>
                <div><strong>E-Timesheet:</strong> <span style={badgeStyle(notifPrefs.eTimesheetIndicator ? 'yes' : 'no')}>{notifPrefs.eTimesheetIndicator ? 'YES' : 'NO'}</span></div>
                <div><strong>Cell Verified:</strong> <span style={badgeStyle(notifPrefs.cellPhoneVerified ? 'yes' : 'no')}>{notifPrefs.cellPhoneVerified ? 'YES' : 'NO'}</span></div>
                <div><strong>Email Notifications:</strong> {notifPrefs.emailNotificationsEnabled ? 'Enabled' : 'Disabled'}</div>
                <div><strong>SMS Notifications:</strong> {notifPrefs.smsNotificationsEnabled ? 'Enabled' : 'Disabled'}</div>
                <div><strong>Timesheet Reminders:</strong> {notifPrefs.timesheetReminders ? 'Yes' : 'No'}</div>
                <div><strong>Payment Confirmations:</strong> {notifPrefs.paymentConfirmations ? 'Yes' : 'No'}</div>
              </div>
            ) : <p className="wq-empty">No notification preferences configured. Use the provider portal to set preferences.</p>}
            <div style={{marginTop:'1rem', display:'flex', gap:'0.5rem'}}>
              <button className="wq-btn wq-btn-secondary" onClick={() => providersApi.verifyCellPhone(id).then(() => { setActionSuccess('Cell phone verified'); setActiveTab('notifications'); })}>Verify Cell Phone</button>
              <button className="wq-btn wq-btn-secondary" onClick={() => providersApi.stopETimesheet(id).then(() => { setActionSuccess('E-Timesheet stopped; switched to paper'); setActiveTab('notifications'); })}>Stop E-Timesheet</button>
            </div>
          </div>
        </div>
      )}

      {/* Modals */}
      {showAddNote && (
        <AddNoteModal entityType="provider" entityId={id} onClose={() => setShowAddNote(false)} onSaved={() => { setShowAddNote(false); setActiveTab('notes'); }} />
      )}
      {showAssign && (
        <AssignProviderModal providerId={id} onClose={() => setShowAssign(false)} onAssigned={() => { setShowAssign(false); setActiveTab('assignments'); }} />
      )}
    </div>
  );
};
