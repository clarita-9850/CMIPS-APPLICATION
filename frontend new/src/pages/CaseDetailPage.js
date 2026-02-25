import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as casesApi from '../api/casesApi';
import * as eligibilityApi from '../api/eligibilityApi';
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
          {['TERMINATED', 'DENIED', 'WITHDRAWN'].includes(status) && (
            <button className="wq-manage-action" onClick={() => navigate(`/case/reactivate-case?caseId=${id}`)}>
              <span className="action-icon">&#43;</span> New Application
            </button>
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
    </div>
  );
};
