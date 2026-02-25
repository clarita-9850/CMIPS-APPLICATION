import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as workspaceApi from '../api/workspaceApi';
import http from '../api/httpClient';
import './WorkQueues.css';
import './WorkspaceContent.css';

function mapBackendTask(t) {
  return {
    id: String(t.id || t.taskID || ''),
    subject: t.title || t.subject || '',
    dueDate: t.dueDate ? new Date(t.dueDate).toLocaleString('en-US', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit'
    }) : '',
    priority: t.priority || 'Normal',
    status: t.status || ''
  };
}

const SHORTCUTS = [
  { label: 'New Referral',                            route: '/person/duplicate-referral-check',                icon: '🏠' },
  { label: 'New Application',                         route: '/person/duplicate-registration-check',            icon: '📋' },
  { label: 'Find a Person',                           route: '/person/search',                                  icon: '👤' },
  { label: 'Find a State Hearing Case',               route: '/case/search-state-hearing',                      icon: '⚖️' },
  { label: 'Register a Provider',                     route: '/person/duplicate-provider-check',                icon: '👩‍⚕️' },
  { label: 'Merge Duplicate SSN',                     route: '/person/merge-duplicate-ssn',                     icon: '🔗' },
  { label: 'Enter Warrant Replacements',              route: '/payment/list-warrant-replacements',              icon: '💰' },
  { label: 'Reissue Large Font Timesheet',            route: '/case/reissue-bvi-timesheet',                     icon: '🕐' },
  { label: 'IRS Live-In Provider Self-Certification', route: '/person/live-in-prov-self-certification-search',  icon: '🏡' },
  { label: 'Sick Leave Claim Manual Entry',           route: '/person/enter-sick-leave-claim-manual-entry',     icon: '🏥' },
];

export const WorkspaceContent = () => {
  const { user, roles } = useAuth();
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const isSupervisor = roles.some(r => r.includes('SUPERVISOR'));

  const [loading, setLoading] = useState(true);
  const [tasks, setTasks] = useState([]);
  const [approvals, setApprovals] = useState([]);

  useEffect(() => {
    setBreadcrumbs([]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const loadDashboard = useCallback(async () => {
    if (!username) { setLoading(false); return; }
    try {
      const tasksRes = await http
        .get(`/tasks?username=${encodeURIComponent(username)}&includeSubscribedQueues=true`)
        .catch(() => ({ data: [] }));

      const allTasks = Array.isArray(tasksRes.data)
        ? tasksRes.data
        : (tasksRes.data?.content || []);
      setTasks(allTasks.slice(0, 10).map(mapBackendTask));

      if (isSupervisor) {
        const approvalsData = await workspaceApi.fetchPendingApprovals(username).catch(() => ({}));
        setApprovals(approvalsData.timesheets || []);
      }
    } catch (err) {
      console.warn('[WorkspaceContent] Dashboard load error:', err?.message);
    } finally {
      setLoading(false);
    }
  }, [username, isSupervisor]);

  useEffect(() => { loadDashboard(); }, [loadDashboard]);

  if (loading) {
    return (
      <div className="wq-page">
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div className="spinner-border" style={{ color: '#153554' }} />
          <p style={{ marginTop: '1rem', color: '#666' }}>Loading workspace...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="wq-page">

      {/* Page heading */}
      <h2 style={{ fontSize: '1.5rem', fontWeight: 700, color: '#153554', marginBottom: '1.5rem', borderBottom: '2px solid #153554', paddingBottom: '0.5rem' }}>
        My Workspace: Welcome to CMIPS
      </h2>

      {/* Two-column layout: Tasks | Shortcuts */}
      <div className="workspace-main-columns">

        {/* ── Left: My Tasks ── */}
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>My Tasks</h4>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {tasks.length === 0 ? (
              <p style={{ padding: '1rem 1.25rem', color: '#888', fontSize: '0.875rem' }}>No tasks assigned.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Task</th>
                    <th>Subject</th>
                    <th>Due Date</th>
                    <th>Priority</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map(t => (
                    <tr key={t.id}>
                      <td>
                        <button
                          onClick={() => navigate(`/tasks/${t.id}`)}
                          style={{ background: 'none', border: 'none', color: '#153554', cursor: 'pointer', padding: 0, fontWeight: 600, textDecoration: 'underline' }}
                        >
                          {t.id}
                        </button>
                      </td>
                      <td>{t.subject}</td>
                      <td>{t.dueDate}</td>
                      <td>
                        <span className={`wq-badge wq-badge-${t.priority.toLowerCase()}`}>{t.priority}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* ── Right: My Shortcuts ── */}
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>My Shortcuts</h4>
          </div>
          <div className="wq-panel-body" style={{ padding: '0.75rem' }}>
            <div className="workspace-shortcuts-list">
              {SHORTCUTS.map(({ label, route, icon }) => (
                <button
                  key={label}
                  className="workspace-shortcut-btn"
                  onClick={() => navigate(route)}
                >
                  <span style={{ marginRight: '0.6rem' }}>{icon}</span>
                  {label}
                </button>
              ))}
            </div>
          </div>
        </div>

      </div>

      {/* Supervisor: Pending Approvals */}
      {isSupervisor && approvals.length > 0 && (
        <div className="wq-panel" style={{ marginTop: '1rem' }}>
          <div className="wq-panel-header">
            <h4>Pending Approvals</h4>
            <button
              className="wq-btn wq-btn-outline"
              style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }}
              onClick={() => navigate('/supervisor/approvals')}
            >
              View All
            </button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            <table className="wq-table">
              <thead>
                <tr><th>ID</th><th>Type</th><th>Employee</th><th>Status</th><th>Hours</th><th>Period</th></tr>
              </thead>
              <tbody>
                {approvals.slice(0, 5).map(a => (
                  <tr key={a.id}>
                    <td>{a.id}</td>
                    <td>{a.type}</td>
                    <td>{a.employeeName}</td>
                    <td><span className={`wq-badge wq-badge-${(a.status || '').toLowerCase()}`}>{a.status}</span></td>
                    <td>{a.totalHours}</td>
                    <td>{a.payPeriodStart} – {a.payPeriodEnd}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

    </div>
  );
};
