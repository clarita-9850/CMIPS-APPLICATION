import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import * as recipientsApi from '../api/recipientsApi';
import * as notesApi from '../api/notesApi';
import { AddNoteModal } from './modals/AddNoteModal';
import './WorkQueues.css';

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

  const maskSsn = (val) => val ? '***-**-' + val.slice(-4) : '\u2014';

  if (loading) return <div className="wq-page"><p>Loading person...</p></div>;
  if (!person) return <div className="wq-page"><p>Person not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Back to Search</button></div>;

  const p = person;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>{[p.lastName, p.firstName].filter(Boolean).join(', ') || p.name || 'Person Detail'}</h2>
        <div>
          <button className="wq-btn wq-btn-primary" style={{ marginRight: '0.5rem' }} onClick={() => navigate(`/recipients/${id}/edit`)}>Edit</button>
          <button className="wq-btn wq-btn-outline" style={{ marginRight: '0.5rem' }} onClick={() => navigate('/recipients/new')}>Create Referral</button>
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Back</button>
        </div>
      </div>

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
                    <span className="wq-detail-value">{p.personType || '\u2014'}</span>
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
    </div>
  );
};
