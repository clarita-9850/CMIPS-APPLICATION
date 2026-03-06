/**
 * TaskDetailPage - Full Task Home view
 * Matches legacy CMIPS "Task Home" screen with:
 * - Manage action bar (Add Comment, Reserve, Forward, Defer, Close, Un-Reserve, Reallocate, Restart)
 * - Subject section
 * - Details section (Task ID, Status, Priority, Deadline, Reserved By, etc.)
 * - Primary Action / Supporting Information two-column layout
 * - Tabs: View Task | Task History | Assignment List
 */

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import http from '../api/httpClient';
import { ForwardTaskModal } from './modals/ForwardTaskModal';
import { DeferTaskModal } from './modals/DeferTaskModal';
import { ReallocateTaskModal } from './modals/ReallocateTaskModal';
import './WorkQueues.css';

export const TaskDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';
  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('view');
  const [history, setHistory] = useState([]);
  const [assignments, setAssignments] = useState([]);

  // Modal states
  const [showForward, setShowForward] = useState(false);
  const [showDefer, setShowDefer] = useState(false);
  const [showReallocate, setShowReallocate] = useState(false);

  const loadTask = useCallback(() => {
    if (!id) { setLoading(false); return; }
    http.get(`/tasks/${id}`)
      .then(res => setTask(res?.data))
      .catch(() => setTask(null))
      .finally(() => setLoading(false));
  }, [id]);

  useEffect(() => { loadTask(); }, [loadTask]);

  // Load history / assignments on tab switch
  useEffect(() => {
    if (!id) return;
    if (activeTab === 'history') {
      http.get(`/tasks/${id}/history`)
        .then(res => {
          const d = res?.data;
          setHistory(Array.isArray(d) ? d : (d?.content || []));
        })
        .catch(() => setHistory([]));
    } else if (activeTab === 'assignments') {
      http.get(`/tasks/${id}/assignments`)
        .then(res => {
          const d = res?.data;
          setAssignments(Array.isArray(d) ? d : (d?.content || []));
        })
        .catch(() => setAssignments([]));
    }
  }, [id, activeTab]);

  const handleAction = (action) => {
    const body = { username };
    if (action === 'add-comment') {
      const comment = window.prompt('Enter comment:');
      if (!comment) return;
      body.comment = comment;
    }
    http.post(`/tasks/${id}/${action}`, body)
      .then(() => loadTask())
      .catch((err) => alert(`Failed to ${action} task: ${err?.data?.message || err.message || 'Unknown error'}`));
  };

  if (loading) return <div className="wq-page"><p>Loading task...</p></div>;
  if (!task) return <div className="wq-page"><p>Task not found.</p></div>;

  const status = task.status || 'OPEN';
  const tid = task.id || task.taskId || id;

  return (
    <div className="wq-page">
      {/* Page Header */}
      <div className="wq-page-header">
        <h2>Task Home</h2>
        <span className="help-icon" title="Help">?</span>
      </div>

      {/* Manage Action Bar */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Manage</h4></div>
        <div className="wq-manage-bar">
          <button className="wq-manage-action" onClick={() => handleAction('add-comment')}>
            <span className="action-icon">+</span> Add Comment
          </button>
          <button className="wq-manage-action" onClick={() => handleAction('reserve')}>
            <span className="action-icon">&#9654;</span> Reserve
          </button>
          <button className="wq-manage-action" onClick={() => setShowForward(true)}>
            <span className="action-icon">&#8594;</span> Forward
          </button>
          <button className="wq-manage-action" onClick={() => setShowDefer(true)}>
            <span className="action-icon">&#8987;</span> Defer
          </button>
          <button className="wq-manage-action" onClick={() => handleAction('close')}>
            <span className="action-icon">&#10005;</span> Close
          </button>
          <button className="wq-manage-action" onClick={() => handleAction('unreserve')}>
            <span className="action-icon">&#9664;</span> Un-Reserve
          </button>
          <button className="wq-manage-action" onClick={() => setShowReallocate(true)}>
            <span className="action-icon">&#8635;</span> Reallocate
          </button>
          <button className="wq-manage-action" onClick={() => handleAction('restart')}>
            <span className="action-icon">&#8634;</span> Restart
          </button>
        </div>
      </div>

      {/* Subject */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Subject</h4></div>
        <div className="wq-panel-body">
          <p style={{ margin: 0, fontSize: '0.9rem' }}>{task.subject || task.title || 'No subject'}</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'view' ? 'active' : ''}`} onClick={() => setActiveTab('view')}>
          View Task
        </button>
        <button className={`wq-tab ${activeTab === 'history' ? 'active' : ''}`} onClick={() => setActiveTab('history')}>
          Task History
        </button>
        <button className={`wq-tab ${activeTab === 'assignments' ? 'active' : ''}`} onClick={() => setActiveTab('assignments')}>
          Assignment List
        </button>
      </div>

      {/* View Task Tab */}
      {activeTab === 'view' && (
        <>
          {/* Details Panel */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Details</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-grid">
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Task ID:</span>
                  <span className="wq-detail-value">{tid}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Status:</span>
                  <span className="wq-detail-value">
                    <span className={`wq-badge wq-badge-${status.toLowerCase()}`}>{status}</span>
                  </span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Priority:</span>
                  <span className="wq-detail-value">{task.priority || 'Medium'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Deadline:</span>
                  <span className="wq-detail-value">{task.deadline || task.dueDate || '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Reserved By:</span>
                  <span className="wq-detail-value">{task.reservedBy || '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Last Assigned:</span>
                  <span className="wq-detail-value">{task.assignedTo || task.lastAssigned || '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Created:</span>
                  <span className="wq-detail-value">{task.createdAt || task.assignedDate || '\u2014'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Time Worked:</span>
                  <span className="wq-detail-value">{task.timeWorked || '\u2014'}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Two-column: Primary Action / Supporting Information */}
          <div className="wq-task-columns">
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Primary Action</h4></div>
              <div className="wq-panel-body">
                {task.primaryAction ? (
                  <p style={{ margin: 0, fontSize: '0.875rem' }}>{task.primaryAction}</p>
                ) : (
                  <p style={{ margin: 0, color: '#888', fontStyle: 'italic', fontSize: '0.875rem' }}>
                    No primary action defined.
                  </p>
                )}
              </div>
            </div>
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Supporting Information</h4></div>
              <div className="wq-panel-body">
                {task.caseNumber && (
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Case Number:</span>
                    <span className="wq-detail-value">
                      <button className="action-link" onClick={() => navigate(`/cases?id=${task.caseNumber}`)}>
                        {task.caseNumber}
                      </button>
                    </span>
                  </div>
                )}
                {task.caseParticipant && (
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Case Participant:</span>
                    <span className="wq-detail-value">{task.caseParticipant}</span>
                  </div>
                )}
                {task.comments && (
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">Comments:</span>
                    <span className="wq-detail-value">{task.comments}</span>
                  </div>
                )}
                {!task.caseNumber && !task.caseParticipant && !task.comments && (
                  <p style={{ margin: 0, color: '#888', fontStyle: 'italic', fontSize: '0.875rem' }}>
                    No supporting information.
                  </p>
                )}
              </div>
            </div>
          </div>
        </>
      )}

      {/* Task History Tab */}
      {activeTab === 'history' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Task History</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {history.length === 0 ? (
              <p className="wq-empty">No history records.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Action</th>
                    <th>By</th>
                    <th>Comments</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((h, i) => (
                    <tr key={h.id || i}>
                      <td>{h.performedAt ? new Date(h.performedAt).toLocaleString() : '\u2014'}</td>
                      <td>{h.action || '\u2014'}</td>
                      <td>{h.performedBy || '\u2014'}</td>
                      <td>{h.comments || h.details || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Assignment List Tab */}
      {activeTab === 'assignments' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Assignment List</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {assignments.length === 0 ? (
              <p className="wq-empty">No assignment records.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Assigned To</th>
                    <th>Assigned Date</th>
                    <th>Status</th>
                    <th>Comments</th>
                  </tr>
                </thead>
                <tbody>
                  {assignments.map((a, i) => (
                    <tr key={a.id || i}>
                      <td>{a.performedBy || '\u2014'}</td>
                      <td>{a.performedAt ? new Date(a.performedAt).toLocaleString() : '\u2014'}</td>
                      <td>{a.action || '\u2014'}</td>
                      <td>{a.comments || a.details || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Modals */}
      {showForward && (
        <ForwardTaskModal
          taskId={id}
          username={username}
          onClose={() => setShowForward(false)}
          onForwarded={() => { setShowForward(false); loadTask(); }}
        />
      )}
      {showDefer && (
        <DeferTaskModal
          taskId={id}
          username={username}
          onClose={() => setShowDefer(false)}
          onDeferred={() => { setShowDefer(false); loadTask(); }}
        />
      )}
      {showReallocate && (
        <ReallocateTaskModal
          taskId={id}
          username={username}
          onClose={() => setShowReallocate(false)}
          onReallocated={() => { setShowReallocate(false); loadTask(); }}
        />
      )}
    </div>
  );
};
