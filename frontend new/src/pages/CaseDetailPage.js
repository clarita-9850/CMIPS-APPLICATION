import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import * as eligibilityApi from '../api/eligibilityApi';
import * as caseAttachmentsApi from '../api/caseAttachmentsApi';
import * as homeVisitsApi from '../api/homeVisitsApi';
import * as flexibleHoursApi from '../api/flexibleHoursApi';
import * as formsApi from '../api/formsApi';
import { AddNoteModal } from './modals/AddNoteModal';
import { AddContactModal } from './modals/AddContactModal';
import { AssignCaseModal } from './modals/AssignCaseModal';
import { DenyCaseModal } from './modals/DenyCaseModal';
import { TransferCaseModal } from './modals/TransferCaseModal';
import './WorkQueues.css';

export const CaseDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';
  const { setBreadcrumbs } = useBreadcrumbs();

  const [caseData, setCaseData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [notes, setNotes] = useState([]);
  const [contacts, setContacts] = useState([]);
  const [assessments, setAssessments] = useState([]);
  const [servicePlans, setServicePlans] = useState([]);
  const [statusHistory, setStatusHistory] = useState([]);
  const [actionError, setActionError] = useState('');
  const [infoMessage, setInfoMessage] = useState('');

  // New tab data states
  const [workweekAgreements, setWorkweekAgreements] = useState([]);
  const [workweekHistory, setWorkweekHistory] = useState([]);
  const [overtimeAgreements, setOvertimeAgreements] = useState([]);
  const [wpcsHours, setWpcsHours] = useState([]);
  const [workplaceHours, setWorkplaceHours] = useState([]);
  const [attachments, setAttachments] = useState([]);
  const [electronicForms, setElectronicForms] = useState([]);
  const [espRegistrations, setEspRegistrations] = useState([]);
  const [homeVisits, setHomeVisits] = useState([]);
  const [flexibleHours, setFlexibleHours] = useState([]);
  const [contractorInvoices, setContractorInvoices] = useState([]);
  const [mediCalSoc, setMediCalSoc] = useState(null);
  const [agreementsSubTab, setAgreementsSubTab] = useState('workweek');
  const [hoursSubTab, setHoursSubTab] = useState('flexible');
  const [formsSubTab, setFormsSubTab] = useState('electronic');

  // New modal states
  const [showWorkweekModal, setShowWorkweekModal] = useState(false);
  const [showOvertimeModal, setShowOvertimeModal] = useState(false);
  const [showWpcsModal, setShowWpcsModal] = useState(false);
  const [showWorkplaceModal, setShowWorkplaceModal] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [showFormRequestModal, setShowFormRequestModal] = useState(false);
  const [showVisitModal, setShowVisitModal] = useState(false);
  const [showFlexModal, setShowFlexModal] = useState(false);
  const [showContractorModal, setShowContractorModal] = useState(false);
  const [showReassessModal, setShowReassessModal] = useState(false);
  const [showInactivateAgreementConfirm, setShowInactivateAgreementConfirm] = useState(null);
  const [inactivateReason, setInactivateReason] = useState('');
  const [modalError, setModalError] = useState('');

  // Form state for modals
  const [modalForm, setModalForm] = useState({});

  // Modal states
  const [showAddNote, setShowAddNote] = useState(false);
  const [showAddContact, setShowAddContact] = useState(false);
  const [showAssign, setShowAssign] = useState(false);
  const [showDeny, setShowDeny] = useState(false);
  const [showTransfer, setShowTransfer] = useState(false);

  const loadCase = useCallback(() => {
    if (!id) { setLoading(false); return; }
    casesApi.getCaseById(id)
      .then(data => setCaseData(data))
      .catch(() => setCaseData(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { loadCase(); }, [loadCase]);

  // EM OS 186: Show informational message after case creation (SAWS referral notice)
  useEffect(() => {
    const msg = sessionStorage.getItem('caseInfoMessage');
    if (msg) {
      setInfoMessage(msg);
      sessionStorage.removeItem('caseInfoMessage');
    }
  }, []);

  useEffect(() => {
    if (caseData) {
      setBreadcrumbs([{ label: 'Cases', path: '/cases' }, { label: `Case ${caseData.caseNumber || id}` }]);
    }
    return () => setBreadcrumbs([]);
  }, [caseData, id, setBreadcrumbs]);

  useEffect(() => {
    if (!id) return;
    if (activeTab === 'notes') {
      casesApi.getCaseNotes(id)
        .then(d => setNotes(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setNotes([]));
    } else if (activeTab === 'contacts') {
      casesApi.getCaseContacts(id)
        .then(d => setContacts(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setContacts([]));
    } else if (activeTab === 'history') {
      casesApi.getCaseStatusHistory(id)
        .then(d => setStatusHistory(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setStatusHistory([]));
    } else if (activeTab === 'eligibility') {
      Promise.all([
        eligibilityApi.getAssessments(id).catch(() => []),
        eligibilityApi.getServicePlans(id).catch(() => [])
      ]).then(([a, sp]) => {
        setAssessments(Array.isArray(a) ? a : (a?.content || a?.items || []));
        setServicePlans(Array.isArray(sp) ? sp : (sp?.content || sp?.items || []));
      });
    } else if (activeTab === 'agreements') {
      casesApi.getWorkweekAgreements(id).then(d => setWorkweekAgreements(Array.isArray(d) ? d : [])).catch(() => setWorkweekAgreements([]));
      casesApi.getOvertimeAgreements(id).then(d => setOvertimeAgreements(Array.isArray(d) ? d : [])).catch(() => setOvertimeAgreements([]));
    } else if (activeTab === 'hours') {
      casesApi.getWpcsHours(id).then(d => setWpcsHours(Array.isArray(d) ? d : [])).catch(() => setWpcsHours([]));
      casesApi.getWorkplaceHours(id).then(d => setWorkplaceHours(Array.isArray(d) ? d : [])).catch(() => setWorkplaceHours([]));
      flexibleHoursApi.getFlexibleHours(id).then(d => setFlexibleHours(Array.isArray(d) ? d : [])).catch(() => setFlexibleHours([]));
    } else if (activeTab === 'attachments') {
      caseAttachmentsApi.listAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : [])).catch(() => setAttachments([]));
    } else if (activeTab === 'forms') {
      formsApi.getForms(id).then(d => setElectronicForms(Array.isArray(d) ? d : [])).catch(() => setElectronicForms([]));
      formsApi.getEspRegistrations(id).then(d => setEspRegistrations(Array.isArray(d) ? d : [])).catch(() => setEspRegistrations([]));
    } else if (activeTab === 'visits') {
      homeVisitsApi.getHomeVisits(id).then(d => setHomeVisits(Array.isArray(d) ? d : [])).catch(() => setHomeVisits([]));
    } else if (activeTab === 'contractor') {
      formsApi.getContractorInvoices(id).then(d => setContractorInvoices(Array.isArray(d) ? d : [])).catch(() => setContractorInvoices([]));
    } else if (activeTab === 'medicalSoc') {
      casesApi.getMediCalSoc(id).then(d => setMediCalSoc(d)).catch(() => setMediCalSoc(null));
    }
  }, [id, activeTab]);

  const handleAction = (actionFn, successMsg) => {
    setActionError('');
    actionFn()
      .then(() => { loadCase(); })
      .catch(err => setActionError(err?.response?.data?.message || err.message || 'Action failed'));
  };

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '\u2014';

  if (loading) return <div className="wq-page"><p>Loading case...</p></div>;
  if (!caseData) return <div className="wq-page"><p>Case not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Back to Cases</button></div>;

  const c = caseData;
  const status = c.status || 'PENDING';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Case: {c.caseNumber || c.id}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/cases')}>Back to Cases</button>
      </div>

      {actionError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {actionError}
        </div>
      )}

      {infoMessage && (
        <div style={{ background: '#ebf8ff', border: '1px solid #63b3ed', borderLeft: '4px solid #3182ce', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#2b6cb0', fontSize: '0.875rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>{infoMessage}</span>
          <button onClick={() => setInfoMessage('')} style={{ background: 'none', border: 'none', color: '#2b6cb0', cursor: 'pointer', fontSize: '1rem', fontWeight: 'bold' }}>&times;</button>
        </div>
      )}

      {/* Action Bar */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Actions</h4></div>
        <div className="wq-manage-bar">
          {/* Approve/Deny — available for PENDING cases */}
          {(status === 'PENDING') && (
            <>
              <button className="wq-manage-action" onClick={() => handleAction(() => casesApi.approveCase(id))}>
                <span className="action-icon">&#10003;</span> Approve
              </button>
              <button className="wq-manage-action" onClick={() => setShowDeny(true)}>
                <span className="action-icon">&#10005;</span> Deny
              </button>
            </>
          )}

          {/* Assign — available for most active statuses */}
          {['PENDING', 'ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE', 'IN_PROGRESS', 'ACTIVE'].includes(status) && (
            <button className="wq-manage-action" onClick={() => setShowAssign(true)}>
              <span className="action-icon">&#8594;</span> Assign
            </button>
          )}

          {/* Withdraw — DSD 3.1: available for PENDING cases */}
          {(status === 'PENDING') && (
            <button className="wq-manage-action" onClick={() => navigate(`/case/withdrawal-case?caseId=${id}`)}>
              <span className="action-icon">&#8617;</span> Withdraw
            </button>
          )}

          {/* Leave — DSD 3.2: available for ELIGIBLE/PRESUMPTIVE_ELIGIBLE */}
          {['ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE', 'ACTIVE'].includes(status) && (
            <button className="wq-manage-action" onClick={() => navigate(`/case/leave-case?caseId=${id}`)}>
              <span className="action-icon">&#9208;</span> Place on Leave
            </button>
          )}

          {/* Terminate — DSD 3.3: available for ELIGIBLE/PRESUMPTIVE_ELIGIBLE/LEAVE */}
          {['ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE', 'LEAVE', 'ACTIVE'].includes(status) && (
            <button className="wq-manage-action" onClick={() => navigate(`/case/terminate-case?caseId=${id}`)}>
              <span className="action-icon">&#9632;</span> Terminate
            </button>
          )}

          {/* Rescind — DSD 3.4: available for TERMINATED/DENIED */}
          {['TERMINATED', 'DENIED'].includes(status) && (
            <button className="wq-manage-action" onClick={() => navigate(`/case/rescind-case?caseId=${id}`)}>
              <span className="action-icon">&#8634;</span> Rescind
            </button>
          )}

          {/* Reactivate (New Application) — DSD 3.6: available for TERMINATED/DENIED/WITHDRAWN */}
          {/* TR25: Hidden if terminated for CC514 (non-compliance with Medi-Cal) within 90 days */}
          {['TERMINATED', 'DENIED', 'WITHDRAWN'].includes(status) && c.reactivationAllowed !== false && (
            <button className="wq-manage-action" onClick={() => navigate(`/case/reactivate-case?caseId=${id}`)}>
              <span className="action-icon">&#43;</span> New Application
            </button>
          )}
          {status === 'TERMINATED' && c.reactivationAllowed === false && (
            <div style={{ background: '#fff3cd', border: '1px solid #ffc107', borderLeft: '4px solid #856404',
              borderRadius: '4px', padding: '0.5rem 1rem', color: '#856404', fontSize: '0.85rem' }}>
              TR25: This case was terminated for non-compliance with Medi-Cal within the past 90 days.
              A new application cannot be created until the 90-day period has elapsed.
            </div>
          )}

          {/* Transfer — available for active cases */}
          {['PENDING', 'ELIGIBLE', 'PRESUMPTIVE_ELIGIBLE', 'IN_PROGRESS', 'ACTIVE'].includes(status) && (
            <button className="wq-manage-action" onClick={() => setShowTransfer(true)}>
              <span className="action-icon">&#8644;</span> Transfer
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Overview</button>
        <button className={`wq-tab ${activeTab === 'eligibility' ? 'active' : ''}`} onClick={() => setActiveTab('eligibility')}>Eligibility</button>
        <button className={`wq-tab ${activeTab === 'agreements' ? 'active' : ''}`} onClick={() => setActiveTab('agreements')}>Agreements</button>
        <button className={`wq-tab ${activeTab === 'hours' ? 'active' : ''}`} onClick={() => setActiveTab('hours')}>Hours</button>
        <button className={`wq-tab ${activeTab === 'attachments' ? 'active' : ''}`} onClick={() => setActiveTab('attachments')}>Attachments</button>
        <button className={`wq-tab ${activeTab === 'forms' ? 'active' : ''}`} onClick={() => setActiveTab('forms')}>Forms</button>
        <button className={`wq-tab ${activeTab === 'visits' ? 'active' : ''}`} onClick={() => setActiveTab('visits')}>Visits</button>
        <button className={`wq-tab ${activeTab === 'contractor' ? 'active' : ''}`} onClick={() => setActiveTab('contractor')}>Contractor</button>
        <button className={`wq-tab ${activeTab === 'medicalSoc' ? 'active' : ''}`} onClick={() => setActiveTab('medicalSoc')}>Medi-Cal</button>
        <button className={`wq-tab ${activeTab === 'notes' ? 'active' : ''}`} onClick={() => setActiveTab('notes')}>Notes</button>
        <button className={`wq-tab ${activeTab === 'contacts' ? 'active' : ''}`} onClick={() => setActiveTab('contacts')}>Contacts</button>
        <button className={`wq-tab ${activeTab === 'history' ? 'active' : ''}`} onClick={() => setActiveTab('history')}>History</button>
      </div>

      {/* Overview Tab */}
      {activeTab === 'overview' && (
        <>
          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Case Details</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Case Number:</span>
                    <span className="wq-detail-value">{c.caseNumber || c.id}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Status:</span>
                    <span className="wq-detail-value"><span className={`wq-badge wq-badge-${status.toLowerCase()}`}>{status}</span></span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">County:</span>
                    <span className="wq-detail-value">{c.countyCode || c.county || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Case Owner:</span>
                    <span className="wq-detail-value">{c.caseOwnerId || c.assignedTo || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">CIN:</span>
                    <span className="wq-detail-value">{c.cin || c.clientIndexNumber || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">IHSS Referral Date:</span>
                    <span className="wq-detail-value">{c.ihssReferralDate ? new Date(c.ihssReferralDate).toLocaleDateString() : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">District Office:</span>
                    <span className="wq-detail-value">{c.districtOffice || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Interpreter Available:</span>
                    <span className="wq-detail-value">{c.interpreterAvailable === true ? 'Yes' : c.interpreterAvailable === false ? 'No' : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Medi-Cal Status:</span>
                    <span className="wq-detail-value">{c.mediCalStatus || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Medi-Cal Aid Code:</span>
                    <span className="wq-detail-value">{c.mediCalAidCode || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Medi-Cal Elig. Referral Date:</span>
                    <span className="wq-detail-value">{c.mediCalEligibilityReferralDate ? new Date(c.mediCalEligibilityReferralDate).toLocaleDateString() : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Companion Case:</span>
                    <span className="wq-detail-value">{c.companionCase || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">State Hearing:</span>
                    <span className="wq-detail-value">{c.stateHearing || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Household Members:</span>
                    <span className="wq-detail-value">{c.numberOfHouseholdMembers ?? '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Mail Designee:</span>
                    <span className="wq-detail-value">{c.mailDesignee || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Created:</span>
                    <span className="wq-detail-value">{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Last Modified:</span>
                    <span className="wq-detail-value">{c.updatedAt ? new Date(c.updatedAt).toLocaleDateString() : '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Recipient Information</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Recipient Name:</span>
                    <span className="wq-detail-value">
                      {c.recipientId ? (
                        <button className="action-link" onClick={() => navigate(`/recipients/${c.recipientId}`)}>
                          {c.recipientName || c.clientName || c.recipientId}
                        </button>
                      ) : (c.recipientName || c.clientName || '\u2014')}
                    </span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Date of Birth:</span>
                    <span className="wq-detail-value">{c.recipientDob ? new Date(c.recipientDob).toLocaleDateString() : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">SSN:</span>
                    <span className="wq-detail-value">{maskSsn(c.recipientSsn)}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Address:</span>
                    <span className="wq-detail-value">{c.recipientAddress || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Phone:</span>
                    <span className="wq-detail-value">{c.recipientPhone || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Language:</span>
                    <span className="wq-detail-value">{c.spokenLanguage || c.language || '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Provider Information</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Provider:</span>
                    <span className="wq-detail-value">
                      {c.providerId ? (
                        <button className="action-link" onClick={() => navigate(`/providers/${c.providerId}`)}>
                          {c.providerName || c.providerId}
                        </button>
                      ) : (c.providerName || '\u2014')}
                    </span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Provider Number:</span>
                    <span className="wq-detail-value">{c.providerNumber || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Provider Status:</span>
                    <span className="wq-detail-value">{c.providerStatus || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Assigned Hours:</span>
                    <span className="wq-detail-value">{c.assignedHours ?? '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Assessment</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Auth Status:</span>
                    <span className="wq-detail-value">{c.authorizationStatus || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Authorized Hours:</span>
                    <span className="wq-detail-value">{c.authorizedHours ?? '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Next Reassessment:</span>
                    <span className="wq-detail-value">{c.nextReassessmentDate ? new Date(c.nextReassessmentDate).toLocaleDateString() : '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Eligibility Tab */}
      {activeTab === 'eligibility' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header">
              <h4>Assessments ({assessments.length})</h4>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                eligibilityApi.createAssessment(id, { assessmentType: 'INITIAL', requestedBy: username })
                  .then(() => setActiveTab('eligibility'))
                  .catch(err => setActionError(err?.message || 'Failed to create assessment'));
              }}>New Assessment</button>
            </div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {assessments.length === 0 ? (
                <p className="wq-empty">No assessments for this case.</p>
              ) : (
                <table className="wq-table">
                  <thead>
                    <tr><th>ID</th><th>Type</th><th>Status</th><th>Date</th><th>Authorized Hours</th></tr>
                  </thead>
                  <tbody>
                    {assessments.map(a => (
                      <tr key={a.id}>
                        <td>{a.id}</td>
                        <td>{a.assessmentType || a.type || '\u2014'}</td>
                        <td><span className={`wq-badge wq-badge-${(a.status || '').toLowerCase()}`}>{a.status || '\u2014'}</span></td>
                        <td>{a.assessmentDate ? new Date(a.assessmentDate).toLocaleDateString() : '\u2014'}</td>
                        <td>{a.authorizedHours ?? '\u2014'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="wq-panel">
            <div className="wq-panel-header">
              <h4>Service Plans ({servicePlans.length})</h4>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                eligibilityApi.createServicePlan(id, { createdBy: username })
                  .then(() => setActiveTab('eligibility'))
                  .catch(err => setActionError(err?.message || 'Failed to create service plan'));
              }}>New Service Plan</button>
            </div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {servicePlans.length === 0 ? (
                <p className="wq-empty">No service plans for this case.</p>
              ) : (
                <table className="wq-table">
                  <thead>
                    <tr><th>ID</th><th>Status</th><th>Start Date</th><th>End Date</th><th>Created By</th></tr>
                  </thead>
                  <tbody>
                    {servicePlans.map(sp => (
                      <tr key={sp.id}>
                        <td>{sp.id}</td>
                        <td><span className={`wq-badge wq-badge-${(sp.status || '').toLowerCase()}`}>{sp.status || '\u2014'}</span></td>
                        <td>{sp.startDate ? new Date(sp.startDate).toLocaleDateString() : '\u2014'}</td>
                        <td>{sp.endDate ? new Date(sp.endDate).toLocaleDateString() : '\u2014'}</td>
                        <td>{sp.createdBy || '\u2014'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="wq-panel">
            <div className="wq-panel-header">
              <h4>Schedule Reassessment</h4>
            </div>
            <div className="wq-panel-body">
              <button className="wq-btn wq-btn-outline" onClick={() => {
                const date = prompt('Enter reassessment date (YYYY-MM-DD):');
                if (date) {
                  eligibilityApi.scheduleReassessment(id, { scheduledDate: date, scheduledBy: username })
                    .then(() => alert('Reassessment scheduled.'))
                    .catch(err => setActionError(err?.message || 'Failed to schedule'));
                }
              }}>Schedule Reassessment</button>
            </div>
          </div>
        </>
      )}

      {/* Agreements Tab */}
      {activeTab === 'agreements' && (
        <>
          <div className="wq-tabs" style={{ marginBottom: '1rem' }}>
            <button className={`wq-tab ${agreementsSubTab === 'workweek' ? 'active' : ''}`} onClick={() => setAgreementsSubTab('workweek')}>Workweek</button>
            <button className={`wq-tab ${agreementsSubTab === 'overtime' ? 'active' : ''}`} onClick={() => setAgreementsSubTab('overtime')}>Overtime</button>
          </div>

          {/* Workweek Agreements */}
          {agreementsSubTab === 'workweek' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Workweek Agreements ({workweekAgreements.length})</h4>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button className="wq-btn wq-btn-outline" onClick={() => {
                    casesApi.getWorkweekAgreementHistory(id).then(d => setWorkweekHistory(Array.isArray(d) ? d : [])).catch(() => setWorkweekHistory([]));
                    setAgreementsSubTab('workweek-history');
                  }}>History</button>
                  <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowWorkweekModal(true); }}>Add Workweek Agreement</button>
                </div>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {workweekAgreements.length === 0 ? (
                  <p className="wq-empty">No active workweek agreements.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Provider Name</th><th>Provider Type</th><th>Begin Date</th><th>End Date</th><th>Weekly Hours</th><th>Back-Up</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {workweekAgreements.map(a => (
                        <tr key={a.id}>
                          <td>{a.providerName || a.providerNumber || '\u2014'}</td>
                          <td>{a.providerType || '\u2014'}</td>
                          <td>{a.beginDate ? new Date(a.beginDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{a.endDate ? new Date(a.endDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{a.agreedHoursWeekly != null ? `${Math.floor(a.agreedHoursWeekly / 60)}:${String(a.agreedHoursWeekly % 60).padStart(2, '0')}` : '\u2014'}</td>
                          <td>{a.backUpProvider ? 'Yes' : 'No'}</td>
                          <td><span className={`wq-badge wq-badge-${(a.status || 'active').toLowerCase()}`}>{a.status || 'ACTIVE'}</span></td>
                          <td>
                            <button className="wq-btn wq-btn-sm" onClick={() => { setModalForm({ ...a }); setModalError(''); setShowWorkweekModal(true); }}>Edit</button>
                            {' '}
                            <button className="wq-btn wq-btn-sm wq-btn-danger" onClick={() => { setShowInactivateAgreementConfirm({ type: 'workweek', id: a.id }); setInactivateReason(''); setModalError(''); }}>Inactivate</button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* Workweek History */}
          {agreementsSubTab === 'workweek-history' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Workweek Agreement History</h4>
                <button className="wq-btn wq-btn-outline" onClick={() => setAgreementsSubTab('workweek')}>Back</button>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {workweekHistory.length === 0 ? (
                  <p className="wq-empty">No inactive workweek agreements.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Provider</th><th>Begin Date</th><th>End Date</th><th>Weekly Hours</th><th>Inactivated Date</th><th>Reason</th></tr>
                    </thead>
                    <tbody>
                      {workweekHistory.map(a => (
                        <tr key={a.id}>
                          <td>{a.providerName || a.providerNumber || '\u2014'}</td>
                          <td>{a.beginDate ? new Date(a.beginDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{a.endDate ? new Date(a.endDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{a.agreedHoursWeekly != null ? `${Math.floor(a.agreedHoursWeekly / 60)}:${String(a.agreedHoursWeekly % 60).padStart(2, '0')}` : '\u2014'}</td>
                          <td>{a.inactivatedDate ? new Date(a.inactivatedDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{a.inactivationReason || '\u2014'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* Overtime Agreements */}
          {agreementsSubTab === 'overtime' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Overtime Agreements ({overtimeAgreements.length})</h4>
                <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowOvertimeModal(true); }}>Add Overtime Agreement</button>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {overtimeAgreements.length === 0 ? (
                  <p className="wq-empty">No overtime agreements.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Provider Number</th><th>Agreement Type</th><th>Date Received</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {overtimeAgreements.map(a => (
                        <tr key={a.id}>
                          <td>{a.providerNumber || '\u2014'}</td>
                          <td>{a.agreementType || '\u2014'}</td>
                          <td>{a.dateReceived ? new Date(a.dateReceived).toLocaleDateString() : '\u2014'}</td>
                          <td><span className={`wq-badge wq-badge-${(a.status || 'active').toLowerCase()}`}>{a.status || 'ACTIVE'}</span></td>
                          <td>
                            {a.status === 'ACTIVE' && (
                              <button className="wq-btn wq-btn-sm wq-btn-danger" onClick={() => {
                                if (window.confirm('Inactivate this overtime agreement?')) {
                                  casesApi.inactivateOvertimeAgreement(a.id).then(() => {
                                    casesApi.getOvertimeAgreements(id).then(d => setOvertimeAgreements(Array.isArray(d) ? d : []));
                                  }).catch(err => setActionError(err?.response?.data?.message || 'Failed to inactivate'));
                                }
                              }}>Inactivate</button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {/* Hours Tab */}
      {activeTab === 'hours' && (
        <>
          <div className="wq-tabs" style={{ marginBottom: '1rem' }}>
            <button className={`wq-tab ${hoursSubTab === 'flexible' ? 'active' : ''}`} onClick={() => setHoursSubTab('flexible')}>Flexible</button>
            <button className={`wq-tab ${hoursSubTab === 'wpcs' ? 'active' : ''}`} onClick={() => setHoursSubTab('wpcs')}>WPCS</button>
            <button className={`wq-tab ${hoursSubTab === 'workplace' ? 'active' : ''}`} onClick={() => setHoursSubTab('workplace')}>Workplace</button>
          </div>

          {/* Flexible Hours */}
          {hoursSubTab === 'flexible' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Flexible Hours ({flexibleHours.length})</h4>
                <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowFlexModal(true); }}>Request Flexible Hours</button>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {flexibleHours.length === 0 ? (
                  <p className="wq-empty">No flexible hours requests.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Service Month</th><th>Frequency</th><th>Program</th><th>Requested</th><th>Approved</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {flexibleHours.map(f => {
                        const flexStatusColor = { PENDING: '#fefcbf', APPROVED: '#c6f6d5', DENIED: '#fed7d7', CANCELLED: '#e2e8f0' };
                        return (
                          <tr key={f.id}>
                            <td>{f.serviceMonth ? new Date(f.serviceMonth).toLocaleDateString('en-US', { year: 'numeric', month: 'short' }) : '\u2014'}</td>
                            <td>{f.frequency || '\u2014'}</td>
                            <td>{f.program || '\u2014'}</td>
                            <td>{f.hoursRequested != null ? `${Math.floor(f.hoursRequested / 60)}:${String(f.hoursRequested % 60).padStart(2, '0')}` : '\u2014'}</td>
                            <td>{f.approvedHours != null ? `${Math.floor(f.approvedHours / 60)}:${String(f.approvedHours % 60).padStart(2, '0')}` : '\u2014'}</td>
                            <td><span style={{ padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', background: flexStatusColor[f.status] || '#e2e8f0' }}>{f.status || '\u2014'}</span></td>
                            <td>
                              {f.status === 'PENDING' && (
                                <>
                                  <button className="wq-btn wq-btn-sm" style={{ background: '#c6f6d5', marginRight: '4px' }} onClick={() => {
                                    flexibleHoursApi.approveFlexibleHours(f.id, { approvedBy: username, approvedMinutes: f.hoursRequested }).then(() => {
                                      flexibleHoursApi.getFlexibleHours(id).then(d => setFlexibleHours(Array.isArray(d) ? d : []));
                                    }).catch(err => setActionError(err?.response?.data?.message || 'Failed to approve'));
                                  }}>Approve</button>
                                  <button className="wq-btn wq-btn-sm" style={{ background: '#fed7d7', marginRight: '4px' }} onClick={() => {
                                    flexibleHoursApi.denyFlexibleHours(f.id).then(() => {
                                      flexibleHoursApi.getFlexibleHours(id).then(d => setFlexibleHours(Array.isArray(d) ? d : []));
                                    }).catch(err => setActionError(err?.response?.data?.message || 'Failed to deny'));
                                  }}>Deny</button>
                                  <button className="wq-btn wq-btn-sm wq-btn-outline" onClick={() => {
                                    flexibleHoursApi.cancelFlexibleHours(f.id).then(() => {
                                      flexibleHoursApi.getFlexibleHours(id).then(d => setFlexibleHours(Array.isArray(d) ? d : []));
                                    }).catch(err => setActionError(err?.response?.data?.message || 'Failed to cancel'));
                                  }}>Cancel</button>
                                </>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* WPCS Hours */}
          {hoursSubTab === 'wpcs' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>WPCS Hours ({wpcsHours.length})</h4>
                <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowWpcsModal(true); }}>Add WPCS Hours</button>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {wpcsHours.length === 0 ? (
                  <p className="wq-empty">No WPCS hours records.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Begin Date</th><th>End Date</th><th>Authorized Hours</th><th>Funding Source</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {wpcsHours.map(w => (
                        <tr key={w.id}>
                          <td>{w.beginDate ? new Date(w.beginDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{w.endDate ? new Date(w.endDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{w.authorizedHours != null ? `${Math.floor(w.authorizedHours / 60)}:${String(w.authorizedHours % 60).padStart(2, '0')}` : '\u2014'}</td>
                          <td>{w.fundingSource || '\u2014'}</td>
                          <td><span className={`wq-badge wq-badge-${(w.status || 'active').toLowerCase()}`}>{w.status || 'ACTIVE'}</span></td>
                          <td>
                            {w.status === 'ACTIVE' && (
                              <button className="wq-btn wq-btn-sm wq-btn-danger" onClick={() => {
                                if (window.confirm('Inactivate these WPCS hours?')) {
                                  casesApi.inactivateWpcsHours(w.id).then(() => {
                                    casesApi.getWpcsHours(id).then(d => setWpcsHours(Array.isArray(d) ? d : []));
                                  }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                                }
                              }}>Inactivate</button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* Workplace Hours */}
          {hoursSubTab === 'workplace' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Workplace Hours ({workplaceHours.length})</h4>
                <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowWorkplaceModal(true); }}>Add Workplace Hours</button>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {workplaceHours.length === 0 ? (
                  <p className="wq-empty">No workplace hours records.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Begin Date</th><th>End Date</th><th>Workplace Hours</th><th>Notes</th><th>Status</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {workplaceHours.map(w => (
                        <tr key={w.id}>
                          <td>{w.beginDate ? new Date(w.beginDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{w.endDate ? new Date(w.endDate).toLocaleDateString() : '\u2014'}</td>
                          <td>{w.workplaceHours != null ? `${Math.floor(w.workplaceHours / 60)}:${String(w.workplaceHours % 60).padStart(2, '0')}` : '\u2014'}</td>
                          <td>{w.notes || '\u2014'}</td>
                          <td><span className={`wq-badge wq-badge-${(w.status || 'active').toLowerCase()}`}>{w.status || 'ACTIVE'}</span></td>
                          <td>
                            {w.status === 'ACTIVE' && (
                              <button className="wq-btn wq-btn-sm wq-btn-danger" onClick={() => {
                                if (window.confirm('Inactivate these workplace hours?')) {
                                  casesApi.inactivateWorkplaceHours(w.id).then(() => {
                                    casesApi.getWorkplaceHours(id).then(d => setWorkplaceHours(Array.isArray(d) ? d : []));
                                  }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                                }
                              }}>Inactivate</button>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {/* Attachments Tab */}
      {activeTab === 'attachments' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Case Attachments ({attachments.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowUploadModal(true); }}>Upload Document</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {attachments.length === 0 ? (
              <p className="wq-empty">No attachments for this case.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Document Type</th><th>Description</th><th>File Name</th><th>Upload Date</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {attachments.map(a => (
                    <tr key={a.id}>
                      <td>{a.documentType || '\u2014'}</td>
                      <td>{a.description || '\u2014'}</td>
                      <td>{a.originalFileName || '\u2014'}</td>
                      <td>{a.uploadDate ? new Date(a.uploadDate).toLocaleDateString() : '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(a.status || 'active').toLowerCase()}`}>{a.status || 'ACTIVE'}</span></td>
                      <td>
                        <button className="wq-btn wq-btn-sm wq-btn-outline" style={{ marginRight: '4px' }} onClick={() => {
                          caseAttachmentsApi.downloadAttachment(a.id).then(data => {
                            alert('Download initiated: ' + (data?.url || a.originalFileName));
                          }).catch(() => setActionError('Failed to download'));
                        }}>Download</button>
                        {a.status === 'ACTIVE' && (
                          <button className="wq-btn wq-btn-sm" onClick={() => {
                            caseAttachmentsApi.archiveAttachment(a.id).then(() => {
                              caseAttachmentsApi.listAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : []));
                            }).catch(err => setActionError(err?.response?.data?.message || 'Failed to archive'));
                          }}>Archive</button>
                        )}
                        {(a.status === 'ARCHIVED' || a.status === 'RESTORED') && (
                          <button className="wq-btn wq-btn-sm" onClick={() => {
                            caseAttachmentsApi.restoreAttachment(a.id).then(() => {
                              caseAttachmentsApi.listAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : []));
                            }).catch(err => setActionError(err?.response?.data?.message || 'Failed to restore'));
                          }}>Restore</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Forms Tab */}
      {activeTab === 'forms' && (
        <>
          <div className="wq-tabs" style={{ marginBottom: '1rem' }}>
            <button className={`wq-tab ${formsSubTab === 'electronic' ? 'active' : ''}`} onClick={() => setFormsSubTab('electronic')}>Electronic Forms</button>
            <button className={`wq-tab ${formsSubTab === 'etimesheet' ? 'active' : ''}`} onClick={() => setFormsSubTab('etimesheet')}>E-Timesheet</button>
          </div>

          {/* Electronic Forms */}
          {formsSubTab === 'electronic' && (
            <div className="wq-panel">
              <div className="wq-panel-header">
                <h4>Electronic Forms ({electronicForms.length})</h4>
                <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowFormRequestModal(true); }}>Request Form</button>
              </div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {electronicForms.length === 0 ? (
                  <p className="wq-empty">No form requests.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Form Type</th><th>Language</th><th>Format</th><th>Print Method</th><th>Status</th><th>Request Date</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {electronicForms.map(f => {
                        const formStatusColor = { PENDING: '#fefcbf', PRINTED: '#c6f6d5', NOT_MAILED: '#fbd38d', INACTIVATED: '#e2e8f0', SUPPRESSED: '#fed7d7' };
                        return (
                          <tr key={f.id}>
                            <td>{f.formType || '\u2014'}</td>
                            <td>{f.language || '\u2014'}</td>
                            <td>{f.bviFormat || '\u2014'}</td>
                            <td>{f.printMethod || '\u2014'}</td>
                            <td><span style={{ padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', background: formStatusColor[f.status] || '#e2e8f0' }}>{f.status || '\u2014'}</span></td>
                            <td>{f.requestDate ? new Date(f.requestDate).toLocaleDateString() : '\u2014'}</td>
                            <td>
                              {f.status === 'PRINTED' && (
                                <button className="wq-btn wq-btn-sm wq-btn-outline" style={{ marginRight: '4px' }} onClick={() => {
                                  formsApi.downloadForm(f.id).then(() => alert('Download initiated')).catch(() => setActionError('Failed to download'));
                                }}>Download</button>
                              )}
                              {!['INACTIVATED', 'SUPPRESSED'].includes(f.status) && (
                                <button className="wq-btn wq-btn-sm" onClick={() => {
                                  formsApi.inactivateForm(f.id).then(() => {
                                    formsApi.getForms(id).then(d => setElectronicForms(Array.isArray(d) ? d : []));
                                  }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                                }}>Inactivate</button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}

          {/* E-Timesheet (ESP Registrations) */}
          {formsSubTab === 'etimesheet' && (
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>E-Timesheet Accounts ({espRegistrations.length})</h4></div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {espRegistrations.length === 0 ? (
                  <p className="wq-empty">No ESP accounts for this case.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Provider Name</th><th>Account Status</th><th>Enrolled Date</th><th>Inactivated Date</th><th>Actions</th></tr>
                    </thead>
                    <tbody>
                      {espRegistrations.map(e => (
                        <tr key={e.id}>
                          <td>{e.providerName || e.email || e.id}</td>
                          <td><span className={`wq-badge wq-badge-${(e.status || 'active').toLowerCase()}`}>{e.status || 'ACTIVE'}</span></td>
                          <td>{e.enrolledDate ? new Date(e.enrolledDate).toLocaleDateString() : (e.createdAt ? new Date(e.createdAt).toLocaleDateString() : '\u2014')}</td>
                          <td>{e.inactivatedDate ? new Date(e.inactivatedDate).toLocaleDateString() : '\u2014'}</td>
                          <td>
                            {e.status !== 'CANCELLED' && (
                              <button className="wq-btn wq-btn-sm wq-btn-danger" style={{ marginRight: '4px' }} onClick={() => {
                                const reason = window.prompt('Enter inactivation reason:');
                                if (reason) {
                                  formsApi.inactivateEsp(e.id, { reason, inactivatedBy: username }).then(() => {
                                    formsApi.getEspRegistrations(id).then(d => setEspRegistrations(Array.isArray(d) ? d : []));
                                  }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                                }
                              }}>Inactivate</button>
                            )}
                            {e.status === 'CANCELLED' && (
                              <>
                                <button className="wq-btn wq-btn-sm" style={{ marginRight: '4px' }} onClick={() => {
                                  formsApi.reactivateEsp(e.id, { reactivatedBy: username }).then(() => {
                                    formsApi.getEspRegistrations(id).then(d => setEspRegistrations(Array.isArray(d) ? d : []));
                                  }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                                }}>Reactivate</button>
                                <button className="wq-btn wq-btn-sm wq-btn-outline" onClick={() => {
                                  formsApi.downloadSoc2321(e.id).then(() => alert('SOC 2321 download initiated')).catch(() => setActionError('Failed'));
                                }}>SOC 2321</button>
                              </>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {/* Visits Tab */}
      {activeTab === 'visits' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Unannounced Home Visits ({homeVisits.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowVisitModal(true); }}>Record Visit</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {homeVisits.length === 0 ? (
              <p className="wq-empty">No home visits recorded.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Visit Date</th><th>Time</th><th>Visit Type</th><th>Outcome</th><th>Follow-Up Required</th><th>Created By</th></tr>
                </thead>
                <tbody>
                  {homeVisits.map(v => (
                    <tr key={v.id}>
                      <td>{v.visitDate ? new Date(v.visitDate).toLocaleDateString() : '\u2014'}</td>
                      <td>{v.visitTime || '\u2014'}</td>
                      <td>{v.visitType || '\u2014'}</td>
                      <td><span style={{ padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', background: v.outcome === 'SUCCESSFUL' ? '#c6f6d5' : '#fed7d7' }}>{v.outcome || '\u2014'}</span></td>
                      <td>{v.followUpRequired === true ? 'Yes' : v.followUpRequired === false ? 'No' : '\u2014'}</td>
                      <td>{v.createdBy || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Contractor Tab */}
      {activeTab === 'contractor' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>County Contractor Invoices ({contractorInvoices.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => { setModalForm({}); setModalError(''); setShowContractorModal(true); }}>Add Invoice</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {contractorInvoices.length === 0 ? (
              <p className="wq-empty">No contractor invoices.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Contractor</th><th>Invoice Date</th><th>Invoice #</th><th>Amount</th><th>Service Period</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {contractorInvoices.map(inv => (
                    <tr key={inv.id}>
                      <td>{inv.contractorName || '\u2014'}</td>
                      <td>{inv.invoiceDate ? new Date(inv.invoiceDate).toLocaleDateString() : '\u2014'}</td>
                      <td>{inv.invoiceNumber || '\u2014'}</td>
                      <td>{inv.invoiceAmount != null ? `$${parseFloat(inv.invoiceAmount).toFixed(2)}` : '\u2014'}</td>
                      <td>{[inv.servicePeriodFrom && new Date(inv.servicePeriodFrom).toLocaleDateString(), inv.servicePeriodTo && new Date(inv.servicePeriodTo).toLocaleDateString()].filter(Boolean).join(' - ') || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(inv.status || 'pending').toLowerCase()}`}>{inv.status || 'PENDING'}</span></td>
                      <td>
                        {inv.status === 'PENDING' && (
                          <>
                            <button className="wq-btn wq-btn-sm" style={{ background: '#c6f6d5', marginRight: '4px' }} onClick={() => {
                              formsApi.authorizeInvoice(inv.id).then(() => {
                                formsApi.getContractorInvoices(id).then(d => setContractorInvoices(Array.isArray(d) ? d : []));
                              }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                            }}>Authorize</button>
                            <button className="wq-btn wq-btn-sm wq-btn-danger" onClick={() => {
                              const reason = window.prompt('Rejection reason:');
                              if (reason) {
                                formsApi.rejectInvoice(inv.id, { rejectionReason: reason }).then(() => {
                                  formsApi.getContractorInvoices(id).then(d => setContractorInvoices(Array.isArray(d) ? d : []));
                                }).catch(err => setActionError(err?.response?.data?.message || 'Failed'));
                              }
                            }}>Reject</button>
                          </>
                        )}
                        {inv.status === 'AUTHORIZED' && (
                          <button className="wq-btn wq-btn-sm wq-btn-outline" onClick={() => alert('SOC 432 generation initiated for invoice #' + inv.invoiceNumber)}>SOC 432</button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Medi-Cal Tab */}
      {activeTab === 'medicalSoc' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Medi-Cal Share of Cost</h4>
            <button className="wq-btn wq-btn-outline" onClick={() => {
              casesApi.getMediCalEligibility(id).then(d => alert(JSON.stringify(d, null, 2))).catch(() => setActionError('Failed to fetch Medi-Cal eligibility'));
            }}>SAWS Lookup</button>
          </div>
          <div className="wq-panel-body">
            {!mediCalSoc ? (
              <p className="wq-empty">No Medi-Cal SOC data.</p>
            ) : (
              <div className="wq-detail-grid">
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Share of Cost Amount:</span>
                  <span className="wq-detail-value">{mediCalSoc.shareOfCostAmount != null ? `$${mediCalSoc.shareOfCostAmount.toFixed(2)}` : '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Countable Income:</span>
                  <span className="wq-detail-value">{mediCalSoc.countableIncome != null ? `$${mediCalSoc.countableIncome.toFixed(2)}` : '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Net Income:</span>
                  <span className="wq-detail-value">{mediCalSoc.netIncome != null ? `$${mediCalSoc.netIncome.toFixed(2)}` : '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Reassessment Due Date:</span>
                  <span className="wq-detail-value" style={{ color: mediCalSoc.reassessmentDueDate && new Date(mediCalSoc.reassessmentDueDate) < new Date() ? '#c53030' : 'inherit' }}>
                    {mediCalSoc.reassessmentDueDate ? new Date(mediCalSoc.reassessmentDueDate).toLocaleDateString() : '\u2014'}
                    {mediCalSoc.reassessmentDueDate && new Date(mediCalSoc.reassessmentDueDate) < new Date() && (
                      <span className="wq-badge wq-badge-terminated" style={{ marginLeft: '0.5rem' }}>OVERDUE</span>
                    )}
                  </span>
                </div>
              </div>
            )}
            <div style={{ marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => setShowReassessModal(true)}>Schedule Reassessment</button>
            </div>
          </div>
        </div>
      )}

      {/* Notes Tab */}
      {activeTab === 'notes' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Case Notes ({notes.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAddNote(true)}>Add Note</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {notes.length === 0 ? (
              <p className="wq-empty">No notes for this case.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Date</th><th>Entered By</th><th>Priority</th><th>Sensitivity</th><th>Text</th></tr>
                </thead>
                <tbody>
                  {notes.map((n, i) => (
                    <tr key={n.id || i}>
                      <td>{n.createdAt ? new Date(n.createdAt).toLocaleString() : '\u2014'}</td>
                      <td>{n.createdBy || '\u2014'}</td>
                      <td>{n.priority || '\u2014'}</td>
                      <td>{n.sensitivity || '\u2014'}</td>
                      <td style={{ maxWidth: '400px', whiteSpace: 'pre-wrap' }}>{n.text || n.content || n.noteText || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Contacts Tab */}
      {activeTab === 'contacts' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Case Contacts ({contacts.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAddContact(true)}>Add Contact</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {contacts.length === 0 ? (
              <p className="wq-empty">No contacts for this case.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Name</th><th>Relationship</th><th>Phone</th><th>Email</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {contacts.map((ct, i) => (
                    <tr key={ct.id || i}>
                      <td>{ct.name || [ct.lastName, ct.firstName].filter(Boolean).join(', ') || '\u2014'}</td>
                      <td>{ct.relationship || '\u2014'}</td>
                      <td>{ct.phone || ct.phoneNumber || '\u2014'}</td>
                      <td>{ct.email || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(ct.status || 'active').toLowerCase()}`}>{ct.status || 'Active'}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* History Tab */}
      {activeTab === 'history' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Case Summary</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-grid">
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Created:</span>
                  <span className="wq-detail-value">{c.createdAt ? new Date(c.createdAt).toLocaleString() : '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Created By:</span>
                  <span className="wq-detail-value">{c.createdBy || '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Last Modified:</span>
                  <span className="wq-detail-value">{c.updatedAt ? new Date(c.updatedAt).toLocaleString() : '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Current Status:</span>
                  <span className="wq-detail-value"><span className={`wq-badge wq-badge-${status.toLowerCase()}`}>{status}</span></span>
                </div>
              </div>
            </div>
          </div>

          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Status Change History ({statusHistory.length})</h4></div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {statusHistory.length === 0 ? (
                <p className="wq-empty">No status changes recorded.</p>
              ) : (
                <table className="wq-table">
                  <thead>
                    <tr><th>Date</th><th>Action</th><th>From</th><th>To</th><th>Reason</th><th>Changed By</th><th>Notes</th></tr>
                  </thead>
                  <tbody>
                    {statusHistory.map((h, i) => (
                      <tr key={h.id || i}>
                        <td>{h.changedAt ? new Date(h.changedAt).toLocaleString() : '\u2014'}</td>
                        <td>{h.action || '\u2014'}</td>
                        <td>{h.previousStatus || '\u2014'}</td>
                        <td>{h.newStatus || '\u2014'}</td>
                        <td>{h.reasonDescription || h.reasonCode || '\u2014'}</td>
                        <td>{h.changedBy || '\u2014'}</td>
                        <td style={{ maxWidth: '200px', whiteSpace: 'pre-wrap' }}>{h.notes || '\u2014'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </>
      )}

      {/* Modals */}
      {showAddNote && (
        <AddNoteModal
          entityType="case"
          entityId={id}
          onClose={() => setShowAddNote(false)}
          onSaved={() => { setShowAddNote(false); setActiveTab('notes'); }}
        />
      )}
      {showAddContact && (
        <AddContactModal
          caseId={id}
          onClose={() => setShowAddContact(false)}
          onSaved={() => { setShowAddContact(false); setActiveTab('contacts'); }}
        />
      )}
      {showAssign && (
        <AssignCaseModal
          caseId={id}
          onClose={() => setShowAssign(false)}
          onAssigned={() => { setShowAssign(false); loadCase(); }}
        />
      )}
      {showDeny && (
        <DenyCaseModal
          caseId={id}
          onClose={() => setShowDeny(false)}
          onDenied={() => { setShowDeny(false); loadCase(); }}
        />
      )}
      {showTransfer && (
        <TransferCaseModal
          caseId={id}
          username={username}
          onClose={() => setShowTransfer(false)}
          onTransferred={() => { setShowTransfer(false); loadCase(); }}
        />
      )}

      {/* Inactivate Workweek Agreement Modal */}
      {showInactivateAgreementConfirm && (
        <div className="wq-modal-overlay" onClick={() => setShowInactivateAgreementConfirm(null)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '400px' }}>
            <div className="wq-modal-header"><h3>Inactivate Agreement</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <label className="wq-label">Inactivation Reason *</label>
              <textarea className="wq-input" rows={3} value={inactivateReason} onChange={e => setInactivateReason(e.target.value)} placeholder="Required" style={{ width: '100%' }} />
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowInactivateAgreementConfirm(null)}>Cancel</button>
              <button className="wq-btn wq-btn-danger" onClick={() => {
                if (!inactivateReason.trim()) { setModalError('Inactivation reason is required.'); return; }
                casesApi.inactivateWorkweekAgreement(showInactivateAgreementConfirm.id, inactivateReason).then(() => {
                  setShowInactivateAgreementConfirm(null);
                  casesApi.getWorkweekAgreements(id).then(d => setWorkweekAgreements(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to inactivate'));
              }}>Confirm Inactivate</button>
            </div>
          </div>
        </div>
      )}

      {/* Add Workweek Agreement Modal */}
      {showWorkweekModal && (
        <div className="wq-modal-overlay" onClick={() => setShowWorkweekModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '550px' }}>
            <div className="wq-modal-header"><h3>{modalForm.id ? 'Edit' : 'Add'} Workweek Agreement</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Provider Number *</label>
                  <input className="wq-input" value={modalForm.providerNumber || ''} onChange={e => setModalForm(p => ({ ...p, providerNumber: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Provider Type</label>
                  <select className="wq-input" value={modalForm.providerType || ''} onChange={e => setModalForm(p => ({ ...p, providerType: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="IHSS">IHSS</option>
                    <option value="WPCS">WPCS</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">Begin Date *</label>
                  <input type="date" className="wq-input" value={modalForm.beginDate || ''} onChange={e => setModalForm(p => ({ ...p, beginDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">End Date</label>
                  <input type="date" className="wq-input" value={modalForm.endDate || '9999-12-31'} onChange={e => setModalForm(p => ({ ...p, endDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Weekly Hours (HH:MM) *</label>
                  <input className="wq-input" placeholder="e.g. 40:00" value={modalForm.weeklyHoursInput || ''} onChange={e => setModalForm(p => ({ ...p, weeklyHoursInput: e.target.value }))} />
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', paddingTop: '1.5rem' }}>
                  <input type="checkbox" id="backupProvider" checked={!!modalForm.backUpProvider} onChange={e => setModalForm(p => ({ ...p, backUpProvider: e.target.checked }))} />
                  <label htmlFor="backupProvider" className="wq-label" style={{ margin: 0 }}>Back-Up Provider</label>
                </div>
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <label className="wq-label">Daily Hours (SUN - SAT, HH:MM each)</label>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '0.5rem' }}>
                  {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                    <div key={day}>
                      <label className="wq-label" style={{ fontSize: '0.75rem' }}>{day}</label>
                      <input className="wq-input" style={{ padding: '4px' }} placeholder="0:00" value={modalForm[`hours${day}`] || ''} onChange={e => setModalForm(p => ({ ...p, [`hours${day}`]: e.target.value }))} />
                    </div>
                  ))}
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowWorkweekModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                const [hh, mm] = (modalForm.weeklyHoursInput || '0:00').split(':').map(Number);
                const agreedHoursWeekly = (hh || 0) * 60 + (mm || 0);
                const payload = { ...modalForm, agreedHoursWeekly, createdBy: username };
                const action = modalForm.id ? casesApi.updateWorkweekAgreement(modalForm.id, payload) : casesApi.createWorkweekAgreement(id, payload);
                action.then(() => {
                  setShowWorkweekModal(false);
                  casesApi.getWorkweekAgreements(id).then(d => setWorkweekAgreements(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Add Overtime Agreement Modal */}
      {showOvertimeModal && (
        <div className="wq-modal-overlay" onClick={() => setShowOvertimeModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <div className="wq-modal-header"><h3>Add Overtime Agreement</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Provider Number *</label>
                  <input className="wq-input" value={modalForm.providerNumber || ''} onChange={e => setModalForm(p => ({ ...p, providerNumber: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Agreement Type *</label>
                  <select className="wq-input" value={modalForm.agreementType || ''} onChange={e => setModalForm(p => ({ ...p, agreementType: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="OVERTIME">Overtime</option>
                    <option value="OVERTIME_EXEMPTION">Overtime Exemption</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">Date Received * (on/after 11/01/2014, not future)</label>
                  <input type="date" className="wq-input" value={modalForm.dateReceived || ''} onChange={e => setModalForm(p => ({ ...p, dateReceived: e.target.value }))} />
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowOvertimeModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                casesApi.createOvertimeAgreement(id, { ...modalForm, createdBy: username }).then(() => {
                  setShowOvertimeModal(false);
                  casesApi.getOvertimeAgreements(id).then(d => setOvertimeAgreements(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Add WPCS Hours Modal */}
      {showWpcsModal && (
        <div className="wq-modal-overlay" onClick={() => setShowWpcsModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <div className="wq-modal-header"><h3>Add WPCS Hours</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Begin Date *</label>
                  <input type="date" className="wq-input" value={modalForm.beginDate || ''} onChange={e => setModalForm(p => ({ ...p, beginDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">End Date *</label>
                  <input type="date" className="wq-input" value={modalForm.endDate || ''} onChange={e => setModalForm(p => ({ ...p, endDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Authorized Hours (HH:MM) *</label>
                  <input className="wq-input" placeholder="e.g. 20:00" value={modalForm.hoursInput || ''} onChange={e => setModalForm(p => ({ ...p, hoursInput: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Funding Source *</label>
                  <select className="wq-input" value={modalForm.fundingSource || ''} onChange={e => setModalForm(p => ({ ...p, fundingSource: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="IFO">IFO</option>
                    <option value="PCSP">PCSP</option>
                    <option value="CFCO">CFCO</option>
                  </select>
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowWpcsModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                const [hh, mm] = (modalForm.hoursInput || '0:00').split(':').map(Number);
                casesApi.createWpcsHours(id, { ...modalForm, authorizedHours: (hh || 0) * 60 + (mm || 0), createdBy: username }).then(() => {
                  setShowWpcsModal(false);
                  casesApi.getWpcsHours(id).then(d => setWpcsHours(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Add Workplace Hours Modal */}
      {showWorkplaceModal && (
        <div className="wq-modal-overlay" onClick={() => setShowWorkplaceModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <div className="wq-modal-header"><h3>Add Workplace Hours</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Begin Date *</label>
                  <input type="date" className="wq-input" value={modalForm.beginDate || ''} onChange={e => setModalForm(p => ({ ...p, beginDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">End Date *</label>
                  <input type="date" className="wq-input" value={modalForm.endDate || ''} onChange={e => setModalForm(p => ({ ...p, endDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Workplace Hours (HH:MM) *</label>
                  <input className="wq-input" placeholder="e.g. 10:00" value={modalForm.hoursInput || ''} onChange={e => setModalForm(p => ({ ...p, hoursInput: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Notes</label>
                  <input className="wq-input" value={modalForm.notes || ''} onChange={e => setModalForm(p => ({ ...p, notes: e.target.value }))} />
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowWorkplaceModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                const [hh, mm] = (modalForm.hoursInput || '0:00').split(':').map(Number);
                casesApi.createWorkplaceHours(id, { ...modalForm, workplaceHours: (hh || 0) * 60 + (mm || 0), createdBy: username }).then(() => {
                  setShowWorkplaceModal(false);
                  casesApi.getWorkplaceHours(id).then(d => setWorkplaceHours(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Upload Attachment Modal */}
      {showUploadModal && (
        <div className="wq-modal-overlay" onClick={() => setShowUploadModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <div className="wq-modal-header"><h3>Upload Document</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Document Type *</label>
                  <select className="wq-input" value={modalForm.documentType || ''} onChange={e => setModalForm(p => ({ ...p, documentType: e.target.value }))}>
                    <option value="">Select...</option>
                    {['SOC_426', 'SOC_426A', 'SOC_846', 'SOC_2305', 'SOC_2303', 'SOC_2313', 'OTHER'].map(t => (
                      <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="wq-label">Description</label>
                  <input className="wq-input" value={modalForm.description || ''} onChange={e => setModalForm(p => ({ ...p, description: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">File * (PDF, DOC, DOCX, TIF, TIFF, GIF, JPG, JPEG — max 5MB)</label>
                  <input type="file" accept=".pdf,.doc,.docx,.tif,.tiff,.gif,.jpg,.jpeg" onChange={e => setModalForm(p => ({ ...p, file: e.target.files[0] }))} />
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowUploadModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                if (!modalForm.file || !modalForm.documentType) { setModalError('Document type and file are required.'); return; }
                const fd = new FormData();
                fd.append('file', modalForm.file);
                fd.append('documentType', modalForm.documentType);
                if (modalForm.description) fd.append('description', modalForm.description);
                fd.append('uploadedBy', username);
                caseAttachmentsApi.uploadAttachment(id, fd).then(() => {
                  setShowUploadModal(false);
                  caseAttachmentsApi.listAttachments(id).then(d => setAttachments(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Upload failed'));
              }}>Upload</button>
            </div>
          </div>
        </div>
      )}

      {/* Request Form Modal */}
      {showFormRequestModal && (
        <div className="wq-modal-overlay" onClick={() => setShowFormRequestModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <div className="wq-modal-header"><h3>Request Form</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Form Type *</label>
                  <select className="wq-input" value={modalForm.formType || ''} onChange={e => setModalForm(p => ({ ...p, formType: e.target.value }))}>
                    <option value="">Select...</option>
                    {['SOC_295', 'SOC_295A', 'SOC_873', 'SOC_2303', 'SOC_2305', 'SOC_2313', 'SOC_426', 'SOC_846', 'SOC_2321', 'IH34', 'OTHER'].map(f => (
                      <option key={f} value={f}>{f.replace(/_/g, ' ')}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="wq-label">Language</label>
                  <select className="wq-input" value={modalForm.language || 'ENGLISH'} onChange={e => setModalForm(p => ({ ...p, language: e.target.value }))}>
                    <option value="ENGLISH">English</option>
                    <option value="SPANISH">Spanish</option>
                    <option value="CHINESE">Chinese</option>
                    <option value="ARMENIAN">Armenian</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">BVI Format</label>
                  <select className="wq-input" value={modalForm.bviFormat || 'STANDARD'} onChange={e => setModalForm(p => ({ ...p, bviFormat: e.target.value }))}>
                    <option value="STANDARD">Standard</option>
                    <option value="LARGE_FONT">Large Font</option>
                    <option value="BRAILLE">Braille</option>
                    <option value="AUDIO_CD">Audio CD</option>
                    <option value="DATA_CD">Data CD</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">Print Method</label>
                  <select className="wq-input" value={modalForm.printMethod || 'PRINT_NOW'} onChange={e => setModalForm(p => ({ ...p, printMethod: e.target.value }))}>
                    <option value="PRINT_NOW">Print Now</option>
                    <option value="NIGHTLY_BATCH">Nightly Batch</option>
                  </select>
                </div>
                {modalForm.formType === 'SOC_873' && (
                  <div>
                    <label className="wq-label">Paramedical Text (SOC 873)</label>
                    <textarea className="wq-input" rows={3} value={modalForm.notes || ''} onChange={e => setModalForm(p => ({ ...p, notes: e.target.value }))} style={{ width: '100%' }} />
                  </div>
                )}
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowFormRequestModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                formsApi.requestForm(id, { ...modalForm, language: modalForm.language || 'ENGLISH', bviFormat: modalForm.bviFormat || 'STANDARD', printMethod: modalForm.printMethod || 'PRINT_NOW', createdBy: username }).then(() => {
                  setShowFormRequestModal(false);
                  formsApi.getForms(id).then(d => setElectronicForms(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to request form'));
              }}>Submit</button>
            </div>
          </div>
        </div>
      )}

      {/* Record Visit Modal */}
      {showVisitModal && (
        <div className="wq-modal-overlay" onClick={() => setShowVisitModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <div className="wq-modal-header"><h3>Record Unannounced Home Visit</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Visit Date *</label>
                  <input type="date" className="wq-input" value={modalForm.visitDate || ''} onChange={e => setModalForm(p => ({ ...p, visitDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Visit Time (HH:MM)</label>
                  <input className="wq-input" placeholder="e.g. 10:30" value={modalForm.visitTime || ''} onChange={e => setModalForm(p => ({ ...p, visitTime: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Visit Type *</label>
                  <select className="wq-input" value={modalForm.visitType || ''} onChange={e => setModalForm(p => ({ ...p, visitType: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="INITIAL">Initial</option>
                    <option value="FOLLOWUP">Follow-Up</option>
                    <option value="FINAL_PHONE">Final Phone Call</option>
                    <option value="FINAL_HOME">Final Home Visit</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">Outcome *</label>
                  <select className="wq-input" value={modalForm.outcome || ''} onChange={e => setModalForm(p => ({ ...p, outcome: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="SUCCESSFUL">Successful</option>
                    <option value="UNSUCCESSFUL">Unsuccessful</option>
                  </select>
                </div>
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <label className="wq-label">Reason for Visit *</label>
                <input className="wq-input" value={modalForm.reasonForVisit || ''} onChange={e => setModalForm(p => ({ ...p, reasonForVisit: e.target.value }))} style={{ width: '100%' }} />
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <label className="wq-label">Findings</label>
                <textarea className="wq-input" rows={2} value={modalForm.findings || ''} onChange={e => setModalForm(p => ({ ...p, findings: e.target.value }))} style={{ width: '100%' }} />
              </div>
              <div style={{ marginTop: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <input type="checkbox" id="followUpRequired" checked={!!modalForm.followUpRequired} onChange={e => setModalForm(p => ({ ...p, followUpRequired: e.target.checked }))} />
                <label htmlFor="followUpRequired" className="wq-label" style={{ margin: 0 }}>Follow-Up Required</label>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowVisitModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                homeVisitsApi.createHomeVisit(id, { ...modalForm, createdBy: username }).then(() => {
                  setShowVisitModal(false);
                  homeVisitsApi.getHomeVisits(id).then(d => setHomeVisits(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save visit'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Request Flexible Hours Modal */}
      {showFlexModal && (
        <div className="wq-modal-overlay" onClick={() => setShowFlexModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '450px' }}>
            <div className="wq-modal-header"><h3>Request Flexible Hours</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div>
                  <label className="wq-label">Frequency *</label>
                  <select className="wq-input" value={modalForm.frequency || ''} onChange={e => setModalForm(p => ({ ...p, frequency: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="ONE_TIME">One-Time</option>
                    <option value="ONGOING">On-Going</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">Program *</label>
                  <select className="wq-input" value={modalForm.program || ''} onChange={e => setModalForm(p => ({ ...p, program: e.target.value }))}>
                    <option value="">Select...</option>
                    <option value="IHSS">IHSS</option>
                    <option value="WPCS">WPCS</option>
                  </select>
                </div>
                <div>
                  <label className="wq-label">Service Month *</label>
                  <input type="month" className="wq-input" value={modalForm.serviceMonth || ''} onChange={e => setModalForm(p => ({ ...p, serviceMonth: e.target.value + '-01' }))} />
                </div>
                <div>
                  <label className="wq-label">Hours Requested (HH:MM) * (max 80/month)</label>
                  <input className="wq-input" placeholder="e.g. 10:00" value={modalForm.hoursInput || ''} onChange={e => setModalForm(p => ({ ...p, hoursInput: e.target.value }))} />
                </div>
                {modalForm.frequency === 'ONGOING' && (
                  <div>
                    <label className="wq-label">End Date (On-Going)</label>
                    <input type="date" className="wq-input" value={modalForm.endDate || ''} onChange={e => setModalForm(p => ({ ...p, endDate: e.target.value }))} />
                  </div>
                )}
              </div>
              <div style={{ marginTop: '0.75rem' }}>
                <label className="wq-label">Reason *</label>
                <textarea className="wq-input" rows={2} value={modalForm.reason || ''} onChange={e => setModalForm(p => ({ ...p, reason: e.target.value }))} style={{ width: '100%' }} />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowFlexModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                const [hh, mm] = (modalForm.hoursInput || '0:00').split(':').map(Number);
                flexibleHoursApi.createFlexibleHours(id, { ...modalForm, hoursRequested: (hh || 0) * 60 + (mm || 0), createdBy: username }).then(() => {
                  setShowFlexModal(false);
                  flexibleHoursApi.getFlexibleHours(id).then(d => setFlexibleHours(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save'));
              }}>Submit</button>
            </div>
          </div>
        </div>
      )}

      {/* Add Contractor Invoice Modal */}
      {showContractorModal && (
        <div className="wq-modal-overlay" onClick={() => setShowContractorModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '500px' }}>
            <div className="wq-modal-header"><h3>Add Contractor Invoice</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div style={{ gridColumn: '1 / -1' }}>
                  <label className="wq-label">Contractor Name *</label>
                  <input className="wq-input" value={modalForm.contractorName || ''} onChange={e => setModalForm(p => ({ ...p, contractorName: e.target.value }))} style={{ width: '100%' }} />
                </div>
                <div>
                  <label className="wq-label">Invoice Date *</label>
                  <input type="date" className="wq-input" value={modalForm.invoiceDate || ''} onChange={e => setModalForm(p => ({ ...p, invoiceDate: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Invoice Number *</label>
                  <input className="wq-input" value={modalForm.invoiceNumber || ''} onChange={e => setModalForm(p => ({ ...p, invoiceNumber: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Service Period From *</label>
                  <input type="date" className="wq-input" value={modalForm.servicePeriodFrom || ''} onChange={e => setModalForm(p => ({ ...p, servicePeriodFrom: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Service Period To *</label>
                  <input type="date" className="wq-input" value={modalForm.servicePeriodTo || ''} onChange={e => setModalForm(p => ({ ...p, servicePeriodTo: e.target.value }))} />
                </div>
                <div>
                  <label className="wq-label">Amount ($) *</label>
                  <input type="number" step="0.01" className="wq-input" value={modalForm.invoiceAmount || ''} onChange={e => setModalForm(p => ({ ...p, invoiceAmount: e.target.value }))} />
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowContractorModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                formsApi.createContractorInvoice(id, { ...modalForm, createdBy: username }).then(() => {
                  setShowContractorModal(false);
                  formsApi.getContractorInvoices(id).then(d => setContractorInvoices(Array.isArray(d) ? d : []));
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to save'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}

      {/* Schedule Reassessment Modal */}
      {showReassessModal && (
        <div className="wq-modal-overlay" onClick={() => setShowReassessModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()} style={{ maxWidth: '380px' }}>
            <div className="wq-modal-header"><h3>Schedule Reassessment</h3></div>
            <div className="wq-modal-body">
              {modalError && <p style={{ color: '#c53030', marginBottom: '0.5rem' }}>{modalError}</p>}
              <label className="wq-label">Reassessment Due Date *</label>
              <input type="date" className="wq-input" value={modalForm.dueDate || ''} onChange={e => setModalForm(p => ({ ...p, dueDate: e.target.value }))} style={{ width: '100%' }} />
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowReassessModal(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={() => {
                casesApi.scheduleReassessment(id, { dueDate: modalForm.dueDate, updatedBy: username }).then(() => {
                  setShowReassessModal(false);
                  casesApi.getMediCalSoc(id).then(d => setMediCalSoc(d)).catch(() => {});
                }).catch(err => setModalError(err?.response?.data?.message || 'Failed to schedule'));
              }}>Save</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
