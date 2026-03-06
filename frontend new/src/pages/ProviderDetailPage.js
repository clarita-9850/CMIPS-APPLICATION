import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as providersApi from '../api/providersApi';
import * as notesApi from '../api/notesApi';
import { UimPageLayout } from '../shared/components/UimPageLayout';
import { UimSection } from '../shared/components/UimSection';
import { UimField } from '../shared/components/UimField';
import { UimTable } from '../shared/components/UimTable';
import { AddNoteModal } from './modals/AddNoteModal';
import { AssignProviderModal } from './modals/AssignProviderModal';
import { ApproveEnrollmentModal } from './uim/modals/ApproveEnrollmentModal';
import { RejectEnrollmentModal } from './uim/modals/RejectEnrollmentModal';
import { SetIneligibleModal } from './uim/modals/SetIneligibleModal';
import { ReinstateProviderModal } from './uim/modals/ReinstateProviderModal';
import { ReEnrollProviderModal } from './uim/modals/ReEnrollProviderModal';
import { CreateCoriModal } from './uim/modals/CreateCoriModal';
import { ViewCoriModal } from './uim/modals/ViewCoriModal';
import { ModifyCoriModal } from './uim/modals/ModifyCoriModal';
import { InactivateCoriModal } from './uim/modals/InactivateCoriModal';
import {
  SSN_VERIFICATION_STATUS, BACKGROUND_CHECK_STATUS,
  getCountyLabel, getIneligibleReasonLabel, formatDate,
} from '../lib/providerConstants';
import '../shared/components/UimPage.css';

export const ProviderDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const userRoles = user?.roles || user?.realm_access?.roles || [];
  const isSupervisor = userRoles.some(r =>
    ['SUPERVISOR', 'CASEMANAGEMENTSUPERVISORROLE', 'INTAKESUPERVISORROLE', 'ELIGIBILITYSUPERVISORROLE'].includes(r)
  );
  const isCDSS = userRoles.some(r => ['CDSSProgramMgmt', 'CDSSModify', 'ADMIN'].includes(r));
  const canModify = userRoles.some(r => ['ProviderManagementModify', 'ADMIN', 'SUPERVISOR'].includes(r)) || true; // default allow for now

  const [provider, setProvider] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('details');
  const [assignments, setAssignments] = useState([]);
  const [cori, setCori] = useState([]);
  const [notes, setNotes] = useState([]);
  const [actionError, setActionError] = useState('');
  const [verifying, setVerifying] = useState(false);
  const [verificationResults, setVerificationResults] = useState(null);

  // Modal states
  const [showAddNote, setShowAddNote] = useState(false);
  const [showAssign, setShowAssign] = useState(false);
  const [showApprove, setShowApprove] = useState(false);
  const [showReject, setShowReject] = useState(false);
  const [showSetIneligible, setShowSetIneligible] = useState(false);
  const [showReinstate, setShowReinstate] = useState(false);
  const [showReEnroll, setShowReEnroll] = useState(false);
  const [showCreateCori, setShowCreateCori] = useState(false);
  const [viewCoriRecord, setViewCoriRecord] = useState(null);
  const [editCoriRecord, setEditCoriRecord] = useState(null);
  const [inactivateCoriRecord, setInactivateCoriRecord] = useState(null);

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
      providersApi.getProviderAssignments(id)
        .then(d => setAssignments(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setAssignments([]));
    } else if (activeTab === 'cori') {
      providersApi.getProviderCori(id)
        .then(d => setCori(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setCori([]));
    } else if (activeTab === 'notes') {
      notesApi.getPersonNotes(id)
        .then(d => setNotes(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setNotes([]));
    }
  }, [id, activeTab]);

  const refreshAndClose = (closeFn) => () => {
    closeFn();
    loadProvider();
    // Refresh tab data if on cori tab
    if (activeTab === 'cori') {
      providersApi.getProviderCori(id)
        .then(d => setCori(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setCori([]));
    }
  };

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '\u2014';

  const handleRunVerifications = async () => {
    setVerifying(true);
    setActionError('');
    setVerificationResults(null);
    try {
      const results = await providersApi.runAllVerifications(id);
      setVerificationResults(results);
      // Reload provider to pick up updated status
      loadProvider();
    } catch (err) {
      setActionError(err?.response?.data?.message || err.message || 'Verification failed');
    } finally {
      setVerifying(false);
    }
  };

  const handleCompleteEnrollment = async () => {
    setActionError('');
    try {
      await providersApi.updateEnrollmentRequirements(id, {
        soc426Completed: true,
        orientationCompleted: true,
        soc846Completed: true,
        providerAgreementSigned: true,
        overtimeAgreementSigned: true,
        backgroundCheckCompleted: true,
      });
      loadProvider();
    } catch (err) {
      setActionError(err?.response?.data?.message || err.message || 'Failed to update requirements');
    }
  };

  if (loading) return <div className="uim-page"><div className="container"><p>Loading provider...</p></div></div>;
  if (!provider) return (
    <div className="uim-page"><div className="container">
      <p>Provider not found.</p>
      <button className="uim-btn uim-btn-secondary" onClick={() => navigate('/providers')}>Back to Providers</button>
    </div></div>
  );

  const p = provider;
  const eligible = p.eligible || 'PENDING';
  const providerName = [p.firstName, p.middleName, p.lastName].filter(Boolean).join(' ') || p.providerNumber || p.id;

  // Badge helpers
  const renderSsnBadge = (status) => {
    const info = SSN_VERIFICATION_STATUS[status];
    if (!info) return status || '\u2014';
    return <span className={`uim-badge ${info.cssClass}`}>{info.label}</span>;
  };

  const renderBgCheckBadge = (status) => {
    const info = BACKGROUND_CHECK_STATUS[status];
    if (!info) return status || '\u2014';
    return <span className={`uim-badge ${info.cssClass}`}>{info.label}</span>;
  };

  const renderMediCalBadge = (suspended) => {
    if (suspended) return <span className="uim-badge uim-badge-red">Yes</span>;
    return <span className="uim-badge uim-badge-green">No</span>;
  };

  const tabs = ['Provider Details', 'CORI', 'Assignments', 'Notes'];
  const tabKeys = ['details', 'cori', 'assignments', 'notes'];

  return (
    <UimPageLayout title="View Provider Details" hidePlaceholderBanner={true}>
      {actionError && (
        <div className="uim-error-banner">{actionError}</div>
      )}

      {/* Action Bar */}
      <div className="uim-action-bar" style={{ marginBottom: '1rem' }}>
        {canModify && (
          <button className="uim-btn uim-btn-primary" onClick={() => navigate(`/providers/${id}/modify-enrollment`)}>
            Edit Enrollment
          </button>
        )}
        {isSupervisor && eligible === 'PENDING_REINSTATEMENT' && (
          <button className="uim-btn uim-btn-primary" onClick={() => setShowApprove(true)}>Approve</button>
        )}
        {isSupervisor && eligible === 'PENDING_REINSTATEMENT' && (
          <button className="uim-btn uim-btn-secondary" onClick={() => setShowReject(true)}>Reject</button>
        )}
        {eligible !== 'NO' && (
          <button className="uim-btn uim-btn-danger" onClick={() => setShowSetIneligible(true)}>Set Ineligible</button>
        )}
        {eligible === 'NO' && (
          <button className="uim-btn uim-btn-secondary" onClick={() => setShowReinstate(true)}>Reinstate</button>
        )}
        {eligible === 'NO' && (
          <button className="uim-btn uim-btn-secondary" onClick={() => setShowReEnroll(true)}>Re-Enroll</button>
        )}
        <button className="uim-btn uim-btn-secondary" onClick={() => navigate(`/providers/${id}/enrollment-history`)}>History</button>
      </div>

      {/* Tab Navigation */}
      <div className="uim-tab-nav">
        {tabs.map((tab, i) => (
          <button
            key={tabKeys[i]}
            className={`tab-item ${activeTab === tabKeys[i] ? 'active' : ''}`}
            onClick={() => setActiveTab(tabKeys[i])}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Provider Details Tab */}
      {activeTab === 'details' && (
        <>
          <UimSection title="Enrollment - Details">
            <div className="uim-form-grid">
              <UimField label="Eligible" value={eligible} />
              <UimField label="Ineligible Reason" value={getIneligibleReasonLabel(p.ineligibleReason)} />
              <UimField label="SOC 426" value={p.soc426Completed ? 'Yes' : 'No'} />
              <UimField label="DOJ Background Check" value={p.backgroundCheckCompleted ? 'Yes' : 'No'} />
              <UimField label="SOC 846 Overtime Agreement" value={p.overtimeAgreementSigned ? 'Yes' : 'No'} />
              <UimField label="SOC 846 Provider Agreement" value={p.providerAgreementSigned ? 'Yes' : 'No'} />
              <UimField label="Provider Orientation" value={p.orientationCompleted ? 'Yes' : 'No'} />
              <UimField label="Provider Orientation Date" value={formatDate(p.orientationDate)} />
              <UimField label="Effective Date" value={formatDate(p.effectiveDate)} />
              <UimField label="Enrollment County" value={getCountyLabel(p.countyCode)} />
              <UimField label="Provider Enrollment Begin Date" value={formatDate(p.enrollmentBeginDate)} />
              <UimField label="Number of Active Cases" value={p.activeCaseCount ?? '\u2014'} />
              <UimField label="Updated By" value={p.updatedBy || '\u2014'} />
              <UimField label="Provider Enrollment Due Date" value={formatDate(p.enrollmentDueDate)} />
              <UimField label="DOJ Counties" value={p.dojCountyCode || '\u2014'} />
              <UimField label="Last Updated" value={formatDate(p.updatedAt)} />
            </div>
          </UimSection>

          <UimSection title="Enrollment - Appeals">
            <div className="uim-form-grid">
              <UimField label="Appeal Status Date" value={formatDate(p.appealDate)} />
              <UimField label="Appeal Status" value={p.appealStatus || '\u2014'} />
              <UimField label="Admin Hearing Date" value={formatDate(p.adminHearingDate)} />
            </div>
          </UimSection>

          <UimSection title="County Use">
            <div className="uim-form-grid">
              <UimField label="County Use 1" value={p.countyUse1 || '\u2014'} />
              <UimField label="County Use 2" value={p.countyUse2 || '\u2014'} />
              <UimField label="County Use 3" value={p.countyUse3 || '\u2014'} />
              <UimField label="County Use 4" value={p.countyUse4 || '\u2014'} />
            </div>
          </UimSection>

          <UimSection title="General">
            <div className="uim-form-grid">
              <UimField label="Provider Number" value={p.providerNumber || p.id} />
              <UimField label="Initial Hire Date" value={formatDate(p.initialHireDate)} />
              <div className="uim-field">
                <label>SSN Verification</label>
                <span className="uim-field-value">{renderSsnBadge(p.ssnVerificationStatus)}</span>
              </div>
              <div className="uim-field">
                <label>Background Check Status</label>
                <span className="uim-field-value">{renderBgCheckBadge(p.backgroundCheckStatus)}</span>
              </div>
            </div>
          </UimSection>

          {/* Verification Actions — visible when provider is PENDING */}
          {eligible === 'PENDING' && (
            <UimSection title="Enrollment Verification">
              <div style={{ padding: '0.5rem 0' }}>
                <p style={{ margin: '0 0 0.75rem', color: '#4a5568' }}>
                  Run external verifications to check SSN (SSA), background (DOJ), and Medi-Cal suspended list.
                  Once all verifications pass and enrollment requirements are met, the provider will be automatically set to Eligible.
                </p>
                <div className="uim-action-bar" style={{ gap: '0.75rem' }}>
                  <button
                    className="uim-btn uim-btn-primary"
                    onClick={handleRunVerifications}
                    disabled={verifying}
                  >
                    {verifying ? 'Running Verifications...' : 'Run All Verifications'}
                  </button>
                  <button
                    className="uim-btn uim-btn-secondary"
                    onClick={handleCompleteEnrollment}
                    title="Mark all enrollment forms as completed (SOC 426, Orientation, SOC 846, Agreements)"
                  >
                    Mark All Requirements Complete
                  </button>
                </div>
                {verificationResults && (
                  <div style={{ marginTop: '1rem', padding: '1rem', background: '#f7fafc', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
                    <h4 style={{ margin: '0 0 0.75rem', color: '#153554' }}>Verification Results</h4>
                    <div className="uim-form-grid">
                      <div className="uim-field">
                        <label>SSN Verification (SSA)</label>
                        <span className="uim-field-value">
                          {verificationResults.ssaVerification?.passed
                            ? <span className="uim-badge uim-badge-green">Verified</span>
                            : <span className="uim-badge uim-badge-red">{verificationResults.ssaVerification?.status || 'Failed'}</span>}
                        </span>
                      </div>
                      <div className="uim-field">
                        <label>DOJ Background Check</label>
                        <span className="uim-field-value">
                          {verificationResults.dojBackgroundCheck?.passed
                            ? <span className="uim-badge uim-badge-green">Clear - No Record</span>
                            : <span className="uim-badge uim-badge-red">
                                {verificationResults.dojBackgroundCheck?.coriTier || 'Failed'}
                                {verificationResults.dojBackgroundCheck?.waiverAvailable && ' (Waiver Available)'}
                              </span>}
                        </span>
                      </div>
                      <div className="uim-field">
                        <label>Medi-Cal Suspended List</label>
                        <span className="uim-field-value">
                          {verificationResults.mediCalCheck?.passed
                            ? <span className="uim-badge uim-badge-green">Not on List</span>
                            : <span className="uim-badge uim-badge-red">On Suspended List</span>}
                        </span>
                      </div>
                      <div className="uim-field">
                        <label>Overall Result</label>
                        <span className="uim-field-value">
                          {verificationResults.overallResult?.providerEligible
                            ? <span className="uim-badge uim-badge-green">ELIGIBLE</span>
                            : verificationResults.overallResult?.allVerificationsPassed
                              ? <span className="uim-badge uim-badge-yellow">Verifications Passed - Complete Requirements</span>
                              : <span className="uim-badge uim-badge-red">Not Eligible - {verificationResults.overallResult?.updatedEligibleStatus}</span>}
                        </span>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </UimSection>
          )}

          <UimSection title="Medi-Cal">
            <div className="uim-form-grid">
              <div className="uim-field">
                <label>Suspended/Ineligible</label>
                <span className="uim-field-value">{renderMediCalBadge(p.mediCalSuspended)}</span>
              </div>
              <UimField label="Begin Date" value={formatDate(p.mediCalBeginDate)} />
              <UimField label="End Date" value={formatDate(p.mediCalEndDate)} />
            </div>
            {p.mediCalSuspended && (
              <div className="uim-warning-text" style={{ marginTop: '0.5rem' }}>
                This provider is currently Medi-Cal Suspended/Ineligible. Enrollment may be affected.
              </div>
            )}
          </UimSection>

          <UimSection title="Contact">
            <div className="uim-form-grid">
              <UimField label="Residence Address" value={p.streetAddress || p.address || p.residenceAddress || '\u2014'} />
              <UimField label="City" value={p.city || '\u2014'} />
              <UimField label="State" value={p.state || 'CA'} />
              <UimField label="Zip" value={p.zipCode || '\u2014'} />
              <UimField label="Mailing Address" value={p.mailingStreetAddress || p.streetAddress || '\u2014'} />
              <UimField label="Phone" value={p.phone || p.phoneNumber || '\u2014'} />
              <UimField label="Email" value={p.email || '\u2014'} />
            </div>
          </UimSection>

          <UimSection title="Demographics">
            <div className="uim-form-grid">
              <UimField label="Date of Birth" value={formatDate(p.dateOfBirth)} />
              <UimField label="Gender" value={p.gender || '\u2014'} />
              <UimField label="SSN" value={maskSsn(p.ssn)} />
              <UimField label="Spoken Language" value={p.spokenLanguage || '\u2014'} />
              <UimField label="Written Language" value={p.writtenLanguage || '\u2014'} />
            </div>
          </UimSection>
        </>
      )}

      {/* CORI Tab */}
      {activeTab === 'cori' && (
        <UimSection title="Provider CORI Details">
          <div className="uim-action-bar" style={{ marginBottom: '0.75rem' }}>
            <button className="uim-btn uim-btn-primary" onClick={() => setShowCreateCori(true)}>New</button>
          </div>
          <UimTable
            columns={[
              { key: 'coriDate', label: 'CORI Date' },
              { key: 'convictionReleaseDate', label: 'Conviction/Release Date' },
              { key: 'tier', label: 'Tier' },
              { key: 'geBeginDate', label: 'GE Begin Date' },
              { key: 'geEndDate', label: 'GE End Date' },
              { key: 'updatedBy', label: 'Updated By' },
              { key: 'countyCode', label: 'County' },
              { key: 'createdAt', label: 'History Created' },
            ]}
            data={cori.map(c => ({
              ...c,
              coriDate: formatDate(c.coriDate || c.checkDate || c.createdAt),
              convictionReleaseDate: formatDate(c.convictionReleaseDate),
              geBeginDate: formatDate(c.geBeginDate),
              geEndDate: formatDate(c.geEndDate),
              createdAt: formatDate(c.createdAt),
            }))}
            actions={[
              { label: 'View', onClick: (row) => setViewCoriRecord(row) },
              ...(isCDSS ? [{ label: 'Edit', onClick: (row) => setEditCoriRecord(row) }] : []),
              { label: 'Inactivate', onClick: (row) => setInactivateCoriRecord(row), visible: (row) => row.status !== 'INACTIVE' },
            ]}
          />
        </UimSection>
      )}

      {/* Assignments Tab */}
      {activeTab === 'assignments' && (
        <UimSection title="Cases">
          <div className="uim-action-bar" style={{ marginBottom: '0.75rem' }}>
            <button className="uim-btn uim-btn-primary" onClick={() => setShowAssign(true)}>Assign to Case</button>
          </div>
          <UimTable
            columns={[
              { key: 'caseName', label: 'Case Name' },
              { key: 'caseOwner', label: 'Case Owner' },
              { key: 'countyCode', label: 'County' },
              { key: 'caseStatus', label: 'Case Status' },
              { key: 'authHours', label: 'Auth Hours' },
              { key: 'providerStatus', label: 'Provider Status' },
              { key: 'assignedHours', label: 'Assigned Hours' },
            ]}
            data={assignments.map(a => ({
              ...a,
              caseName: a.caseNumber || a.caseId || '\u2014',
              caseOwner: a.caseOwner || '\u2014',
              caseStatus: a.status || a.caseStatus || '\u2014',
              providerStatus: a.providerStatus || '\u2014',
              authHours: a.authorizedHours ?? a.authHours ?? '\u2014',
              assignedHours: a.assignedHours ?? '\u2014',
            }))}
            onRowAction={(row) => row.caseId && navigate(`/cases/${row.caseId}`)}
            actionLabel="View Case"
          />
        </UimSection>
      )}

      {/* Notes Tab */}
      {activeTab === 'notes' && (
        <UimSection title="Notes">
          <div className="uim-action-bar" style={{ marginBottom: '0.75rem' }}>
            <button className="uim-btn uim-btn-primary" onClick={() => setShowAddNote(true)}>Add Note</button>
          </div>
          <UimTable
            columns={[
              { key: 'date', label: 'Date' },
              { key: 'priority', label: 'Priority' },
              { key: 'noteText', label: 'Text' },
              { key: 'createdBy', label: 'Created By' },
            ]}
            data={notes.map(n => ({
              ...n,
              date: formatDate(n.createdAt),
              noteText: n.text || n.content || n.noteText || '\u2014',
              createdBy: n.createdBy || '\u2014',
              priority: n.priority || '\u2014',
            }))}
          />
        </UimSection>
      )}

      {/* Modals */}
      {showApprove && (
        <ApproveEnrollmentModal
          providerId={id}
          providerName={providerName}
          onClose={() => setShowApprove(false)}
          onSuccess={refreshAndClose(() => setShowApprove(false))}
        />
      )}
      {showReject && (
        <RejectEnrollmentModal
          providerId={id}
          providerName={providerName}
          onClose={() => setShowReject(false)}
          onSuccess={refreshAndClose(() => setShowReject(false))}
        />
      )}
      {showSetIneligible && (
        <SetIneligibleModal
          providerId={id}
          providerName={providerName}
          onClose={() => setShowSetIneligible(false)}
          onSuccess={refreshAndClose(() => setShowSetIneligible(false))}
        />
      )}
      {showReinstate && (
        <ReinstateProviderModal
          providerId={id}
          providerName={providerName}
          provider={p}
          onClose={() => setShowReinstate(false)}
          onSuccess={refreshAndClose(() => setShowReinstate(false))}
        />
      )}
      {showReEnroll && (
        <ReEnrollProviderModal
          providerId={id}
          providerName={providerName}
          onClose={() => setShowReEnroll(false)}
          onSuccess={refreshAndClose(() => setShowReEnroll(false))}
        />
      )}
      {showCreateCori && (
        <CreateCoriModal
          providerId={id}
          onClose={() => setShowCreateCori(false)}
          onSuccess={refreshAndClose(() => setShowCreateCori(false))}
        />
      )}
      {viewCoriRecord && (
        <ViewCoriModal
          record={viewCoriRecord}
          onClose={() => setViewCoriRecord(null)}
          onInactivate={() => { setInactivateCoriRecord(viewCoriRecord); setViewCoriRecord(null); }}
        />
      )}
      {editCoriRecord && (
        <ModifyCoriModal
          record={editCoriRecord}
          onClose={() => setEditCoriRecord(null)}
          onSuccess={refreshAndClose(() => setEditCoriRecord(null))}
        />
      )}
      {inactivateCoriRecord && (
        <InactivateCoriModal
          record={inactivateCoriRecord}
          onClose={() => setInactivateCoriRecord(null)}
          onSuccess={refreshAndClose(() => setInactivateCoriRecord(null))}
        />
      )}
      {showAddNote && (
        <AddNoteModal
          entityType="provider"
          entityId={id}
          onClose={() => setShowAddNote(false)}
          onSaved={() => { setShowAddNote(false); setActiveTab('notes'); }}
        />
      )}
      {showAssign && (
        <AssignProviderModal
          providerId={id}
          onClose={() => setShowAssign(false)}
          onAssigned={() => { setShowAssign(false); setActiveTab('assignments'); }}
        />
      )}
    </UimPageLayout>
  );
};
