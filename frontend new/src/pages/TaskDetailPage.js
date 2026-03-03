/**
 * TaskDetailPage — CMIPS 3.0 Task Home
 *
 * Matches the legacy CMIPS "Task Home" UIM screen design:
 *  - Page title: "Task: {subject}"
 *  - Tab navigation: View Task | Task History | Assignment List
 *  - Manage toolbar (4-column icon grid)
 *  - Subject cluster
 *  - Details cluster (2-col form grid)
 *  - Primary Actions / Supporting Information side-by-side
 *  - History timeline & assignment table
 */

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import http from '../api/httpClient';
import { ForwardTaskModal } from './modals/ForwardTaskModal';
import { DeferTaskModal } from './modals/DeferTaskModal';
import { ReallocateTaskModal } from './modals/ReallocateTaskModal';
import './WorkQueues.css';

/* ── Manage button (inline icon + label) ── */
const ManageButton = ({ icon, label, disabled = false, onClick }) => (
  <button
    className="wq-manage-btn"
    onClick={onClick}
    disabled={disabled}
    type="button"
    title={label}
  >
    <span className="btn-icon">{icon}</span>{label}
  </button>
);

/* ── Helpers ── */
const fmtDate = (v) => {
  if (!v) return '\u2014';
  try {
    return new Date(v).toLocaleString('en-US', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit',
    });
  } catch { return v; }
};

const badgeClass = (val) => {
  if (!val) return '';
  return `wq-badge wq-badge-${val.toLowerCase().replace(/\s/g, '_')}`;
};

export const TaskDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';

  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('ViewTask');
  const [history, setHistory] = useState([]);
  const [assignments, setAssignments] = useState([]);
  const [showForward, setShowForward] = useState(false);
  const [showDefer, setShowDefer] = useState(false);
  const [showReallocate, setShowReallocate] = useState(false);
  const [showCloseConfirm, setShowCloseConfirm] = useState(false);
  const [closeComment, setCloseComment] = useState('');

  /* ── Load task ── */
  const loadTask = useCallback(() => {
    if (!id) { setLoading(false); return; }
    http.get(`/tasks/${id}`)
      .then(res => setTask(res?.data))
      .catch(() => setTask(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { loadTask(); }, [loadTask]);

  /* ── Load history / assignments on tab switch ── */
  useEffect(() => {
    if (!id) return;
    if (activeTab === 'TaskHistory') {
      http.get(`/tasks/${id}/history`)
        .then(res => {
          const d = res?.data;
          setHistory(Array.isArray(d) ? d : (d?.content || []));
        })
        .catch(() => setHistory([]));
    } else if (activeTab === 'AssignmentList') {
      http.get(`/tasks/${id}/assignments`)
        .then(res => {
          const d = res?.data;
          setAssignments(Array.isArray(d) ? d : (d?.content || []));
        })
        .catch(() => setAssignments([]));
    }
  }, [id, activeTab]);

  /* ── Actions ── */
  const handleAction = (action) => {
    const body = { username };
    if (action === 'add-comment') {
      const comment = window.prompt('Enter comment:');
      if (!comment) return;
      body.comment = comment;
    }
    http.post(`/tasks/${id}/${action}`, body)
      .then(() => loadTask())
      .catch((err) => alert(`Failed to ${action}: ${err?.data?.message || err.message || 'Unknown error'}`));
  };

  const handleClose = () => {
    if (task?.requiredActionForClosure && !closeComment.trim()) {
      return;
    }
    const body = { username, comment: closeComment || 'Task closed' };
    http.post(`/tasks/${id}/close`, body)
      .then(() => { setShowCloseConfirm(false); setCloseComment(''); loadTask(); })
      .catch((err) => alert(`Failed to close: ${err?.data?.message || err.message || 'Unknown error'}`));
  };

  /* ── Loading / error states ── */
  if (loading) return (
    <div className="wq-page">
      <div style={{ textAlign: 'center', padding: '3rem' }}>
        <div className="spinner-border" style={{ color: '#153554' }} />
        <p style={{ marginTop: '1rem', color: '#666' }}>Loading task...</p>
      </div>
    </div>
  );
  if (!task) return (
    <div className="wq-page">
      <p style={{ padding: '2rem', color: '#888' }}>Task not found.</p>
    </div>
  );

  const taskStatus = task.status || 'OPEN';
  const rawId = task.id || task.taskId || id;
  const tid = String(rawId).startsWith('T-') ? rawId : `T-${rawId}`;
  const taskTitle = task.subject || task.title || 'Task Details';

  return (
    <div className="wq-page">

      {/* ═══ Page Title ═══ */}
      <h1 className="wq-page-title">Task: {taskTitle}</h1>

      {/* ═══ Tab Navigation ═══ */}
      <div className="wq-tabs" style={{ marginTop: '1rem' }}>
        {['ViewTask', 'TaskHistory', 'AssignmentList'].map(tab => (
          <button
            key={tab}
            className={`wq-tab ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab === 'ViewTask' ? 'View Task' : tab === 'TaskHistory' ? 'Task History' : 'Assignment List'}
          </button>
        ))}
      </div>

      {/* ═══════════ VIEW TASK TAB ═══════════ */}
      {activeTab === 'ViewTask' && (
        <>
          {/* ── Manage Toolbar ── */}
          <section className="wq-cluster">
            <h2 className="wq-cluster-title">Manage</h2>
            <div className="wq-cluster-body">
              <div className="wq-manage-toolbar">
                <ManageButton icon="💬" label="Add Comment" onClick={() => handleAction('add-comment')} />
                <ManageButton icon="✅" label="Close Task" onClick={() => {
                  if (task.requiredActionForClosure) setShowCloseConfirm(true);
                  else handleAction('close');
                }} />
                <ManageButton icon="🔒" label="Reserve" onClick={() => handleAction('reserve')}
                  disabled={taskStatus === 'RESERVED' || taskStatus === 'CLOSED'} />
                <ManageButton icon="🔓" label="Un-reserve" onClick={() => handleAction('unreserve')}
                  disabled={taskStatus !== 'RESERVED'} />
                <ManageButton icon="➡️" label="Forward" onClick={() => setShowForward(true)}
                  disabled={taskStatus === 'CLOSED'} />
                <ManageButton icon="🔄" label="Reallocate" onClick={() => setShowReallocate(true)}
                  disabled={taskStatus === 'CLOSED'} />
                <ManageButton icon="⏸️" label="Defer" onClick={() => setShowDefer(true)}
                  disabled={taskStatus === 'CLOSED' || taskStatus === 'DEFERRED'} />
                <ManageButton icon="▶️" label="Restart" onClick={() => handleAction('restart')}
                  disabled={taskStatus !== 'DEFERRED'} />
              </div>
            </div>
          </section>

          {/* ── Subject ── */}
          <section className="wq-cluster">
            <h2 className="wq-cluster-title">Subject</h2>
            <div className="wq-cluster-body">
              <p style={{ margin: 0, fontSize: '0.95rem', fontWeight: 500 }}>
                {task.subject || task.title || 'No subject'}
              </p>
            </div>
          </section>

          {/* ── Details ── */}
          <section className="wq-cluster">
            <h2 className="wq-cluster-title">Details</h2>
            <div className="wq-cluster-body">
              <div className="wq-form-grid">
                <div className="wq-field">
                  <span className="wq-field-label">Task ID</span>
                  <span className="wq-field-value">{tid}</span>
                </div>
                <div className="wq-field">
                  <span className="wq-field-label">Priority</span>
                  <span className="wq-field-value">
                    <span className={badgeClass(task.priority)}>{task.priority || 'Medium'}</span>
                  </span>
                </div>
                <div className="wq-field">
                  <span className="wq-field-label">Reserved By</span>
                  <span className="wq-field-value">
                    {task.reservedBy ? (
                      <button className="wq-link" type="button" onClick={() => {}}>{task.reservedBy}</button>
                    ) : '\u2014'}
                  </span>
                </div>
                <div className="wq-field">
                  <span className="wq-field-label">Time Worked</span>
                  <span className="wq-field-value" style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    {task.timeWorked || '\u2014'}
                    <button className="wq-btn wq-btn-outline" style={{ padding: '0.2rem 0.6rem', fontSize: '0.75rem' }}
                      type="button" onClick={() => {
                        const val = window.prompt('Enter time worked (e.g. 2h 30m):');
                        if (val) console.log('[TODO] Update time worked:', val);
                      }}>Change</button>
                  </span>
                </div>
                <div className="wq-field">
                  <span className="wq-field-label">Status</span>
                  <span className="wq-field-value">
                    <span className={badgeClass(taskStatus)}>{taskStatus}</span>
                  </span>
                </div>
                <div className="wq-field">
                  <span className="wq-field-label">Deadline</span>
                  <span className="wq-field-value">{fmtDate(task.dueDate || task.deadline)}</span>
                </div>
                <div className="wq-field">
                  <span className="wq-field-label">Assigned</span>
                  <span className="wq-field-value">{fmtDate(task.createdAt)}</span>
                </div>
                {task.requiredActionForClosure && (
                  <div className="wq-field" style={{ gridColumn: '1 / -1' }}>
                    <span className="wq-field-label">Required Action for Closure</span>
                    <span className="wq-field-value" style={{ color: '#c53030', fontWeight: 500 }}>
                      {task.requiredActionForClosure}
                    </span>
                  </div>
                )}
              </div>
            </div>
          </section>

          {/* ── Primary Actions + Supporting Information ── */}
          <div className="wq-task-columns">
            <section className="wq-cluster">
              <h2 className="wq-cluster-title">Primary Actions</h2>
              <div className="wq-cluster-body">
                {task.actionLink ? (
                  <ul style={{ margin: 0, padding: '0 0 0 1.25rem' }}>
                    <li style={{ marginBottom: '0.4rem' }}>
                      <button className="wq-link" type="button"
                        onClick={() => navigate(task.actionLink)}>
                        {task.title || 'Open Action'}
                      </button>
                    </li>
                  </ul>
                ) : (
                  <p style={{ margin: 0, color: '#888', fontStyle: 'italic', fontSize: '0.875rem' }}>
                    No primary action defined.
                  </p>
                )}
              </div>
            </section>

            <section className="wq-cluster">
              <h2 className="wq-cluster-title">Supporting Information</h2>
              <div className="wq-cluster-body">
                {(task.caseNumber || task.caseParticipant || task.description) ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {task.caseNumber && (
                      <div className="wq-field" style={{ flexDirection: 'row', gap: '0.75rem', alignItems: 'baseline' }}>
                        <span className="wq-field-label" style={{ minWidth: 120 }}>Case Number</span>
                        <button className="wq-link" type="button"
                          onClick={() => navigate(`/cases?id=${task.caseNumber}`)}>
                          {task.caseNumber}
                        </button>
                      </div>
                    )}
                    {task.caseParticipant && (
                      <div className="wq-field" style={{ flexDirection: 'row', gap: '0.75rem', alignItems: 'baseline' }}>
                        <span className="wq-field-label" style={{ minWidth: 120 }}>Case Participant</span>
                        <span className="wq-field-value">{task.caseParticipant}</span>
                      </div>
                    )}
                    {task.county && (
                      <div className="wq-field" style={{ flexDirection: 'row', gap: '0.75rem', alignItems: 'baseline' }}>
                        <span className="wq-field-label" style={{ minWidth: 120 }}>County</span>
                        <span className="wq-field-value">{task.county}</span>
                      </div>
                    )}
                    {task.description && (
                      <div className="wq-field" style={{ flexDirection: 'row', gap: '0.75rem', alignItems: 'baseline' }}>
                        <span className="wq-field-label" style={{ minWidth: 120 }}>Description</span>
                        <span className="wq-field-value">{task.description}</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <p style={{ margin: 0, color: '#888', fontStyle: 'italic', fontSize: '0.875rem' }}>
                    No supporting information.
                  </p>
                )}
              </div>
            </section>
          </div>
        </>
      )}

      {/* ═══════════ TASK HISTORY TAB ═══════════ */}
      {activeTab === 'TaskHistory' && (
        <section className="wq-cluster">
          <h2 className="wq-cluster-title">Task History</h2>
          <div className="wq-cluster-body" style={{ padding: history.length > 0 ? '1rem 1.25rem' : 0 }}>
            {history.length === 0 ? (
              <p className="wq-empty">No history records.</p>
            ) : (
              <div className="wq-timeline">
                {history.map((h, i) => (
                  <div className="wq-timeline-item" key={h.id || i}>
                    <div className="wq-timeline-date">{fmtDate(h.performedAt)}</div>
                    <div className="wq-timeline-action">
                      {h.action || '\u2014'}
                      {h.previousStatus && h.newStatus && (
                        <span className="wq-status-transition">
                          {' '}<span className={badgeClass(h.previousStatus)}>{h.previousStatus}</span>
                          {' \u2192 '}
                          <span className={badgeClass(h.newStatus)}>{h.newStatus}</span>
                        </span>
                      )}
                    </div>
                    {h.performedBy && <div className="wq-timeline-by">by {h.performedBy}</div>}
                    {(h.comments || h.details) && (
                      <div className="wq-timeline-notes">{h.comments || h.details}</div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>
      )}

      {/* ═══════════ ASSIGNMENT LIST TAB ═══════════ */}
      {activeTab === 'AssignmentList' && (
        <section className="wq-cluster">
          <h2 className="wq-cluster-title">Assignment List</h2>
          <div className="wq-cluster-body" style={{ padding: 0 }}>
            {assignments.length === 0 ? (
              <p className="wq-empty">No assignment records.</p>
            ) : (
              <table className="wq-table-uim">
                <thead>
                  <tr>
                    <th>Assigned To</th>
                    <th>Assigned Date</th>
                    <th>Action</th>
                    <th>Comments</th>
                  </tr>
                </thead>
                <tbody>
                  {assignments.map((a, i) => (
                    <tr key={a.id || i}>
                      <td>{a.performedBy || '\u2014'}</td>
                      <td>{fmtDate(a.performedAt)}</td>
                      <td>
                        <span className={badgeClass(a.action)}>{a.action || '\u2014'}</span>
                      </td>
                      <td>{a.comments || a.details || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>
      )}

      {/* ═══ Close Task Confirmation (when requiredActionForClosure) ═══ */}
      {showCloseConfirm && (
        <div className="wq-modal-overlay" onClick={() => setShowCloseConfirm(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()}>
            <div className="wq-modal-header">
              <h3>Close Task</h3>
              <button className="wq-modal-close" onClick={() => setShowCloseConfirm(false)}>&times;</button>
            </div>
            <div className="wq-modal-body">
              {task.requiredActionForClosure && (
                <div className="wq-close-modal-required">
                  <strong>Required Action:</strong>
                  <span>{task.requiredActionForClosure}</span>
                </div>
              )}
              <div className="wq-form-field">
                <label>Comments {task.requiredActionForClosure && <span style={{ color: '#c53030' }}>*</span>}</label>
                <textarea
                  value={closeComment}
                  onChange={e => setCloseComment(e.target.value)}
                  placeholder="Describe the action taken..."
                  rows={4}
                />
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-outline" onClick={() => setShowCloseConfirm(false)}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleClose}
                disabled={task.requiredActionForClosure && !closeComment.trim()}>
                Close Task
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ═══ Action Modals ═══ */}
      {showForward && (
        <ForwardTaskModal
          taskId={id} username={username}
          onClose={() => setShowForward(false)}
          onForwarded={() => { setShowForward(false); loadTask(); }}
        />
      )}
      {showDefer && (
        <DeferTaskModal
          taskId={id} username={username}
          onClose={() => setShowDefer(false)}
          onDeferred={() => { setShowDefer(false); loadTask(); }}
        />
      )}
      {showReallocate && (
        <ReallocateTaskModal
          taskId={id} username={username}
          onClose={() => setShowReallocate(false)}
          onReallocated={() => { setShowReallocate(false); loadTask(); }}
        />
      )}
    </div>
  );
};
