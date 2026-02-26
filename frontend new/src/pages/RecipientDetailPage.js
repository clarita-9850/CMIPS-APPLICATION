import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as recipientsApi from '../api/recipientsApi';
import * as notesApi from '../api/notesApi';
import { AddNoteModal } from './modals/AddNoteModal';
import './WorkQueues.css';

// ── PersonType badge ─────────────────────────────────────────────────────────
const PERSON_TYPE_STYLES = {
  OPEN_REFERRAL:   { background: '#bee3f8', color: '#2b6cb0' },
  CLOSED_REFERRAL: { background: '#e2e8f0', color: '#4a5568' },
  APPLICANT:       { background: '#feebc8', color: '#c05621' },
  RECIPIENT:       { background: '#c6f6d5', color: '#276749' },
};
const PersonTypeBadge = ({ type }) => {
  if (!type) return null;
  const style = PERSON_TYPE_STYLES[type] || { background: '#e2e8f0', color: '#4a5568' };
  const label = type.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  return (
    <span style={{ ...style, padding: '0.2rem 0.75rem', borderRadius: '12px', fontSize: '0.8rem', fontWeight: 700, marginLeft: '0.75rem', whiteSpace: 'nowrap' }}>
      {label}
    </span>
  );
};

// ── CloseReferralModal ────────────────────────────────────────────────────────
const CLOSE_REASONS = [
  'Moved / Unable to Locate',
  'Unable to Contact',
  'Declined Services',
  'No Longer Eligible',
  'Duplicate Record',
  'Services Completed',
  'Other',
];
const CloseReferralModal = ({ onConfirm, onCancel, saving }) => {
  const [reason, setReason] = useState('');
  const [notes, setNotes]   = useState('');
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
      <div style={{ background: '#fff', borderRadius: '8px', width: '460px', maxWidth: '95vw', boxShadow: '0 20px 60px rgba(0,0,0,0.3)' }}>
        <div style={{ background: '#153554', color: '#fff', padding: '1rem 1.5rem', borderRadius: '8px 8px 0 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, fontSize: '1rem' }}>Close Referral</h3>
          <button onClick={onCancel} style={{ background: 'none', border: 'none', color: '#fff', fontSize: '1.4rem', cursor: 'pointer', lineHeight: 1 }}>×</button>
        </div>
        <div style={{ padding: '1.25rem' }}>
          <div style={{ marginBottom: '0.75rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#4a5568', marginBottom: '0.25rem' }}>Reason *</label>
            <select value={reason} onChange={e => setReason(e.target.value)}
              style={{ width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem' }}>
              <option value="">-- Select Reason --</option>
              {CLOSE_REASONS.map(r => <option key={r} value={r}>{r}</option>)}
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#4a5568', marginBottom: '0.25rem' }}>Notes</label>
            <textarea value={notes} onChange={e => setNotes(e.target.value)} rows={3}
              style={{ width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem', boxSizing: 'border-box', resize: 'vertical' }} />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
            <button onClick={onCancel} style={{ background: '#fff', color: '#153554', border: '1px solid #153554', padding: '0.4rem 1rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem' }}>Cancel</button>
            <button onClick={() => onConfirm({ reason, notes })} disabled={!reason || saving}
              style={{ background: '#c53030', color: '#fff', border: 'none', padding: '0.4rem 1rem', borderRadius: '4px', cursor: reason && !saving ? 'pointer' : 'not-allowed', fontSize: '0.875rem', opacity: !reason ? 0.6 : 1 }}>
              {saving ? 'Closing...' : 'Close Referral'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

// ── ReopenReferralModal ───────────────────────────────────────────────────────
const ReopenReferralModal = ({ onConfirm, onCancel, saving }) => {
  const [referralDate, setReferralDate] = useState(new Date().toISOString().split('T')[0]);
  const [reason, setReason]             = useState('');
  return (
    <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
      <div style={{ background: '#fff', borderRadius: '8px', width: '420px', maxWidth: '95vw', boxShadow: '0 20px 60px rgba(0,0,0,0.3)' }}>
        <div style={{ background: '#153554', color: '#fff', padding: '1rem 1.5rem', borderRadius: '8px 8px 0 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h3 style={{ margin: 0, fontSize: '1rem' }}>Re-open Referral</h3>
          <button onClick={onCancel} style={{ background: 'none', border: 'none', color: '#fff', fontSize: '1.4rem', cursor: 'pointer', lineHeight: 1 }}>×</button>
        </div>
        <div style={{ padding: '1.25rem' }}>
          <div style={{ marginBottom: '0.75rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#4a5568', marginBottom: '0.25rem' }}>New Referral Date *</label>
            <input type="date" value={referralDate} onChange={e => setReferralDate(e.target.value)}
              style={{ width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem', boxSizing: 'border-box' }} />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label style={{ display: 'block', fontSize: '0.8rem', fontWeight: 600, color: '#4a5568', marginBottom: '0.25rem' }}>Reason</label>
            <textarea value={reason} onChange={e => setReason(e.target.value)} rows={3}
              placeholder="Explain why this referral is being re-opened..."
              style={{ width: '100%', padding: '0.4rem 0.6rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem', boxSizing: 'border-box', resize: 'vertical' }} />
          </div>
          <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
            <button onClick={onCancel} style={{ background: '#fff', color: '#153554', border: '1px solid #153554', padding: '0.4rem 1rem', borderRadius: '4px', cursor: 'pointer', fontSize: '0.875rem' }}>Cancel</button>
            <button onClick={() => onConfirm({ referralDate, reason })} disabled={!referralDate || saving}
              style={{ background: '#153554', color: '#fff', border: 'none', padding: '0.4rem 1rem', borderRadius: '4px', cursor: referralDate && !saving ? 'pointer' : 'not-allowed', fontSize: '0.875rem' }}>
              {saving ? 'Re-opening...' : 'Re-open Referral'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export const RecipientDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [person, setPerson] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [cases, setCases] = useState([]);
  const [notes, setNotes] = useState([]);
  const [referrals, setReferrals] = useState([]);
  const [showAddNote, setShowAddNote] = useState(false);
  // PersonType lifecycle modals
  const [showCloseReferral,  setShowCloseReferral]  = useState(false);
  const [showReopenReferral, setShowReopenReferral] = useState(false);
  const [modalSaving,        setModalSaving]        = useState(false);
  const [actionError,        setActionError]        = useState('');

  const loadPerson = useCallback(() => {
    if (!id) { setLoading(false); return; }
    recipientsApi.getRecipientById(id)
      .then(data => setPerson(data))
      .catch(() => setPerson(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { loadPerson(); }, [loadPerson]);

  useEffect(() => {
    if (!id) return;
    if (activeTab === 'cases') {
      recipientsApi.getCompanionCases(id)
        .then(d => setCases(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setCases([]));
    } else if (activeTab === 'notes') {
      notesApi.getPersonNotes(id)
        .then(d => setNotes(Array.isArray(d) ? d : (d?.content || d?.items || [])))
        .catch(() => setNotes([]));
    } else if (activeTab === 'referrals') {
      recipientsApi.getOpenReferrals()
        .then(d => {
          const all = Array.isArray(d) ? d : (d?.content || d?.items || []);
          setReferrals(all.filter(r => String(r.recipientId) === String(id) || String(r.personId) === String(id)));
        })
        .catch(() => setReferrals([]));
    }
  }, [id, activeTab]);

  // ── PersonType lifecycle handlers ─────────────────────────────────────────
  const handleCloseReferral = async ({ reason, notes: closeNotes }) => {
    setModalSaving(true);
    setActionError('');
    try {
      await recipientsApi.updatePersonType(id, { personType: 'CLOSED_REFERRAL', reason, notes: closeNotes });
      setShowCloseReferral(false);
      loadPerson();
    } catch (err) {
      setActionError(err?.response?.data?.message || 'Failed to close referral.');
    } finally {
      setModalSaving(false);
    }
  };

  const handleReopenReferral = async ({ referralDate, reason }) => {
    setModalSaving(true);
    setActionError('');
    try {
      await recipientsApi.updatePersonType(id, { personType: 'OPEN_REFERRAL', referralDate, reason });
      setShowReopenReferral(false);
      loadPerson();
    } catch (err) {
      setActionError(err?.response?.data?.message || 'Failed to re-open referral.');
    } finally {
      setModalSaving(false);
    }
  };

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '\u2014';

  if (loading) return <div className="wq-page"><p>Loading person...</p></div>;
  if (!person) return <div className="wq-page"><p>Person not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Back to Search</button></div>;

  const p = person;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2 style={{ display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: '0.25rem' }}>
          {[p.lastName, p.firstName].filter(Boolean).join(', ') || p.name || 'Person Detail'}
          <PersonTypeBadge type={p.personType} />
        </h2>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          {/* Edit always available */}
          <button className="wq-btn wq-btn-outline" onClick={() => navigate(`/recipients/${id}/edit`)}>Edit</button>

          {/* OPEN_REFERRAL: Start Application + Close Referral */}
          {p.personType === 'OPEN_REFERRAL' && (
            <>
              <button className="wq-btn wq-btn-primary"
                onClick={() => navigate(`/applications/new?recipientId=${id}&source=existing`)}>
                Start Application
              </button>
              <button className="wq-btn wq-btn-outline"
                style={{ color: '#c53030', borderColor: '#fc8181' }}
                onClick={() => { setActionError(''); setShowCloseReferral(true); }}>
                Close Referral
              </button>
            </>
          )}

          {/* CLOSED_REFERRAL: Re-open Referral + Start Application (BR-8) */}
          {p.personType === 'CLOSED_REFERRAL' && (
            <>
              <button className="wq-btn wq-btn-primary"
                onClick={() => { setActionError(''); setShowReopenReferral(true); }}>
                Re-open Referral
              </button>
              <button className="wq-btn wq-btn-outline"
                onClick={() => navigate(`/applications/new?recipientId=${id}&source=existing`)}>
                Start Application
              </button>
            </>
          )}

          {/* APPLICANT: Continue Application */}
          {p.personType === 'APPLICANT' && (
            <button className="wq-btn wq-btn-primary"
              onClick={() => navigate(`/applications/new?recipientId=${id}&source=existing`)}>
              Continue Application
            </button>
          )}

          {/* RECIPIENT: View Case */}
          {p.personType === 'RECIPIENT' && (
            <button className="wq-btn wq-btn-primary" onClick={() => navigate('/cases')}>
              View Cases
            </button>
          )}

          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Back</button>
        </div>
      </div>

      {/* Action error banner */}
      {actionError && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', borderLeft: '4px solid #c53030', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {actionError}
        </div>
      )}

      {/* Tabs */}
      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Overview</button>
        <button className={`wq-tab ${activeTab === 'cases' ? 'active' : ''}`} onClick={() => setActiveTab('cases')}>Cases</button>
        <button className={`wq-tab ${activeTab === 'notes' ? 'active' : ''}`} onClick={() => setActiveTab('notes')}>Notes</button>
        <button className={`wq-tab ${activeTab === 'referrals' ? 'active' : ''}`} onClick={() => setActiveTab('referrals')}>Referrals</button>
      </div>

      {/* Overview Tab */}
      {activeTab === 'overview' && (
        <>
          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Name</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Title:</span>
                    <span className="wq-detail-value">{p.title || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">First Name:</span>
                    <span className="wq-detail-value">{p.firstName || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Middle Name:</span>
                    <span className="wq-detail-value">{p.middleName || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Last Name:</span>
                    <span className="wq-detail-value">{p.lastName || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Suffix:</span>
                    <span className="wq-detail-value">{p.suffix || '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>

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
                    <span className="wq-detail-label">Ethnicity:</span>
                    <span className="wq-detail-value">{p.ethnicity || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">SSN:</span>
                    <span className="wq-detail-value">{maskSsn(p.ssn)}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">CIN:</span>
                    <span className="wq-detail-value">{p.cin || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Taxpayer ID:</span>
                    <span className="wq-detail-value">{p.taxpayerId || '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Contact</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Address:</span>
                    <span className="wq-detail-value">{p.residenceAddress || p.address || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">City:</span>
                    <span className="wq-detail-value">{p.city || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">County:</span>
                    <span className="wq-detail-value">{p.countyCode || '\u2014'}</span>
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

            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Language &amp; Status</h4></div>
              <div className="wq-panel-body">
                <div className="wq-detail-grid">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Spoken Language:</span>
                    <span className="wq-detail-value">{p.spokenLanguage || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Written Language:</span>
                    <span className="wq-detail-value">{p.writtenLanguage || '\u2014'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Interpreter:</span>
                    <span className="wq-detail-value">{p.interpreterNeeded ? 'Yes' : 'No'}</span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Person Type:</span>
                    <span className="wq-detail-value">
                      {p.personType
                        ? <PersonTypeBadge type={p.personType} />
                        : '\u2014'}
                    </span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Status:</span>
                    <span className="wq-detail-value">
                      <span className={`wq-badge wq-badge-${(p.status || '').toLowerCase()}`}>{p.status || '\u2014'}</span>
                    </span>
                  </div>
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Referral Source:</span>
                    <span className="wq-detail-value">{p.referralSource || '\u2014'}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </>
      )}

      {/* Cases Tab */}
      {activeTab === 'cases' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Associated Cases ({cases.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {cases.length === 0 ? (
              <p className="wq-empty">No cases found for this person.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Case Number</th><th>Status</th><th>Case Owner</th><th>County</th><th>Auth Hours</th><th>Created</th></tr>
                </thead>
                <tbody>
                  {cases.map((c, i) => (
                    <tr key={c.id || i} className="wq-clickable-row" onClick={() => navigate(`/cases/${c.id}`)}>
                      <td><button className="action-link">{c.caseNumber || c.id}</button></td>
                      <td><span className={`wq-badge wq-badge-${(c.status || '').toLowerCase()}`}>{c.status || '\u2014'}</span></td>
                      <td>{c.caseOwnerId || c.assignedTo || '\u2014'}</td>
                      <td>{c.countyCode || '\u2014'}</td>
                      <td>{c.authorizedHours ?? '\u2014'}</td>
                      <td>{c.createdAt ? new Date(c.createdAt).toLocaleDateString() : '\u2014'}</td>
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
              <p className="wq-empty">No notes for this person.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Date</th><th>Category</th><th>Subject</th><th>Content</th><th>Created By</th></tr>
                </thead>
                <tbody>
                  {notes.map((n, i) => (
                    <tr key={n.id || i}>
                      <td>{n.createdAt ? new Date(n.createdAt).toLocaleString() : '\u2014'}</td>
                      <td>{n.category || n.priority || '\u2014'}</td>
                      <td>{n.subject || '\u2014'}</td>
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

      {/* Referrals Tab */}
      {activeTab === 'referrals' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Referrals ({referrals.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {referrals.length === 0 ? (
              <p className="wq-empty">No referrals found for this person.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>Referral ID</th><th>Status</th><th>Source</th><th>Created</th><th>Assigned Worker</th></tr>
                </thead>
                <tbody>
                  {referrals.map((r, i) => (
                    <tr key={r.id || i}>
                      <td>{r.id}</td>
                      <td><span className={`wq-badge wq-badge-${(r.status || '').toLowerCase()}`}>{r.status || '\u2014'}</span></td>
                      <td>{r.referralSource || r.source || '\u2014'}</td>
                      <td>{r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '\u2014'}</td>
                      <td>{r.assignedWorker || r.assignedTo || '\u2014'}</td>
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
          entityType="recipient"
          entityId={id}
          onClose={() => setShowAddNote(false)}
          onSaved={() => { setShowAddNote(false); setActiveTab('notes'); }}
        />
      )}
      {showCloseReferral && (
        <CloseReferralModal
          onConfirm={handleCloseReferral}
          onCancel={() => setShowCloseReferral(false)}
          saving={modalSaving}
        />
      )}
      {showReopenReferral && (
        <ReopenReferralModal
          onConfirm={handleReopenReferral}
          onCancel={() => setShowReopenReferral(false)}
          saving={modalSaving}
        />
      )}
    </div>
  );
};
