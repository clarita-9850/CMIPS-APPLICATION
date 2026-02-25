import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import * as providersApi from '../api/providersApi';
import * as notesApi from '../api/notesApi';
import { AddNoteModal } from './modals/AddNoteModal';
import { AssignProviderModal } from './modals/AssignProviderModal';
import './WorkQueues.css';

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
  const [notes, setNotes] = useState([]);
  const [actionError, setActionError] = useState('');
  const [showAddNote, setShowAddNote] = useState(false);
  const [showAssign, setShowAssign] = useState(false);

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
    } else if (activeTab === 'violations') {
      providersApi.getProviderViolations(id)
        .then(d => setViolations(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setViolations([]));
    } else if (activeTab === 'notes') {
      notesApi.getPersonNotes(id)
        .then(d => setNotes(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setNotes([]));
    }
  }, [id, activeTab]);

  const handleAction = (actionFn) => {
    setActionError('');
    actionFn()
      .then(() => loadProvider())
      .catch(err => setActionError(err?.response?.data?.message || err.message || 'Action failed'));
  };

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '\u2014';

  if (loading) return <div className="wq-page"><p>Loading provider...</p></div>;
  if (!provider) return <div className="wq-page"><p>Provider not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Back to Providers</button></div>;

  const p = provider;
  const status = p.status || 'PENDING';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Provider: {p.providerNumber || [p.lastName, p.firstName].filter(Boolean).join(', ') || p.id}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Back to Providers</button>
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
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.approveEnrollment(id))}>
            <span className="action-icon">&#10003;</span> Approve Enrollment
          </button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.setIneligible(id, { reason: 'Manual review', setBy: username }))}>
            <span className="action-icon">&#10005;</span> Set Ineligible
          </button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.reinstateProvider(id))}>
            <span className="action-icon">&#8634;</span> Reinstate
          </button>
          <button className="wq-manage-action" onClick={() => handleAction(() => providersApi.reEnrollProvider(id))}>
            <span className="action-icon">&#8635;</span> Re-Enroll
          </button>
        </div>
      </div>

      {/* Tabs */}
      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Overview</button>
        <button className={`wq-tab ${activeTab === 'assignments' ? 'active' : ''}`} onClick={() => setActiveTab('assignments')}>Assignments</button>
        <button className={`wq-tab ${activeTab === 'cori' ? 'active' : ''}`} onClick={() => setActiveTab('cori')}>CORI</button>
        <button className={`wq-tab ${activeTab === 'violations' ? 'active' : ''}`} onClick={() => setActiveTab('violations')}>Violations</button>
        <button className={`wq-tab ${activeTab === 'notes' ? 'active' : ''}`} onClick={() => setActiveTab('notes')}>Notes</button>
      </div>

      {/* Overview Tab */}
      {activeTab === 'overview' && (
        <>
          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Provider Information</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Provider Number:</span>
                    <span className="wq-detail-value">{p.providerNumber || p.id}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Name:</span>
                    <span className="wq-detail-value">{[p.firstName, p.middleName, p.lastName].filter(Boolean).join(' ') || p.name || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Status:</span>
                    <span className="wq-detail-value"><span className={`wq-badge wq-badge-${status.toLowerCase()}`}>{status}</span></span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">County:</span>
                    <span className="wq-detail-value">{p.countyCode || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Enrollment Date:</span>
                    <span className="wq-detail-value">{p.enrollmentDate ? new Date(p.enrollmentDate).toLocaleDateString() : '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Contact</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Address:</span>
                    <span className="wq-detail-value">{p.address || p.residenceAddress || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">City:</span>
                    <span className="wq-detail-value">{p.city || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Zip:</span>
                    <span className="wq-detail-value">{p.zipCode || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Phone:</span>
                    <span className="wq-detail-value">{p.phone || p.phoneNumber || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Email:</span>
                    <span className="wq-detail-value">{p.email || '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Demographics</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Date of Birth:</span>
                    <span className="wq-detail-value">{p.dateOfBirth ? new Date(p.dateOfBirth).toLocaleDateString() : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Gender:</span>
                    <span className="wq-detail-value">{p.gender || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">SSN:</span>
                    <span className="wq-detail-value">{maskSsn(p.ssn)}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Ethnicity:</span>
                    <span className="wq-detail-value">{p.ethnicity || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Language:</span>
                    <span className="wq-detail-value">{p.spokenLanguage || p.language || '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Employment</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Provider Type:</span>
                    <span className="wq-detail-value">{p.providerType || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Pay Rate:</span>
                    <span className="wq-detail-value">{p.payRate ? `$${p.payRate}` : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Hourly Rate:</span>
                    <span className="wq-detail-value">{p.hourlyRate ? `$${p.hourlyRate}` : '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Sick Leave:</span>
                    <span className="wq-detail-value">{p.sickLeaveAccrual ?? '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Assignments Tab */}
      {activeTab === 'assignments' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Case Assignments ({assignments.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAssign(true)}>Assign to Case</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {assignments.length === 0 ? (
              <p className="wq-empty">No case assignments.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Case Number</th><th>Recipient</th><th>Begin Date</th><th>End Date</th><th>Hours</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {assignments.map((a, i) => (
                    <tr key={a.id || i} className="wq-clickable-row" onClick={() => a.caseId && navigate(`/cases/${a.caseId}`)}>
                      <td><button className="action-link">{a.caseNumber || a.caseId || '\u2014'}</button></td>
                      <td>{a.recipientName || '\u2014'}</td>
                      <td>{a.beginDate ? new Date(a.beginDate).toLocaleDateString() : '\u2014'}</td>
                      <td>{a.endDate ? new Date(a.endDate).toLocaleDateString() : '\u2014'}</td>
                      <td>{a.assignedHours ?? '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(a.status || '').toLowerCase()}`}>{a.status || '\u2014'}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* CORI Tab */}
      {activeTab === 'cori' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>CORI Records ({cori.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {cori.length === 0 ? (
              <p className="wq-empty">No CORI records.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Date</th><th>Status</th><th>Result</th><th>Exceptions</th></tr>
                </thead>
                <tbody>
                  {cori.map((c, i) => (
                    <tr key={c.id || i}>
                      <td>{c.checkDate ? new Date(c.checkDate).toLocaleDateString() : (c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '\u2014')}</td>
                      <td><span className={`wq-badge wq-badge-${(c.status || '').toLowerCase()}`}>{c.status || '\u2014'}</span></td>
                      <td>{c.result || '\u2014'}</td>
                      <td>{c.exceptions || c.notes || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Violations Tab */}
      {activeTab === 'violations' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Violations ({violations.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {violations.length === 0 ? (
              <p className="wq-empty">No violations recorded.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Date</th><th>Type</th><th>Status</th><th>Review Status</th></tr>
                </thead>
                <tbody>
                  {violations.map((v, i) => (
                    <tr key={v.id || i}>
                      <td>{v.violationDate ? new Date(v.violationDate).toLocaleDateString() : (v.createdAt ? new Date(v.createdAt).toLocaleDateString() : '\u2014')}</td>
                      <td>{v.violationType || v.type || '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(v.status || '').toLowerCase()}`}>{v.status || '\u2014'}</span></td>
                      <td>{v.reviewStatus || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Notes Tab */}
      {activeTab === 'notes' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Notes ({notes.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={() => setShowAddNote(true)}>Add Note</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {notes.length === 0 ? (
              <p className="wq-empty">No notes for this provider.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Date</th><th>Priority</th><th>Sensitivity</th><th>Text</th><th>Created By</th></tr>
                </thead>
                <tbody>
                  {notes.map((n, i) => (
                    <tr key={n.id || i}>
                      <td>{n.createdAt ? new Date(n.createdAt).toLocaleString() : '\u2014'}</td>
                      <td>{n.priority || '\u2014'}</td>
                      <td>{n.sensitivity || '\u2014'}</td>
                      <td style={{ maxWidth: '350px', whiteSpace: 'pre-wrap' }}>{n.text || n.content || n.noteText || '\u2014'}</td>
                      <td>{n.createdBy || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Modals */}
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
    </div>
  );
};
