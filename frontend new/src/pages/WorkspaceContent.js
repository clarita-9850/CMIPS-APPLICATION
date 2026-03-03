import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
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
    status: t.status || '',
    taskTypeCode: t.taskTypeCode || '',
    caseNumber: t.caseNumber || '',
    isOverdue: t.dueDate ? new Date(t.dueDate) < new Date() && t.status !== 'CLOSED' : false,
  };
}

const SHORTCUTS = [
  { label: 'New Referral',                            route: '/persons/search/referral',                        icon: '🏠' },
  { label: 'New Application',                         route: '/persons/search/application',                     icon: '📋' },
  { label: 'Find a Person',                           route: '/recipients',                                     icon: '👤' },
  { label: 'Find a State Hearing Case',               route: '/cases/state-hearing',                            icon: '⚖️' },
  { label: 'Register a Provider',                     route: '/providers/register',                             icon: '👩‍⚕️' },
  { label: 'Merge Duplicate SSN',                     route: '/recipients/merge',                               icon: '🔗' },
  { label: 'Enter Warrant Replacements',              route: '/payments/warrant-replacements',                  icon: '💰' },
  { label: 'Reissue Large Font Timesheet',            route: '/payments/bvi-timesheet',                         icon: '🕐' },
  { label: 'IRS Live-In Provider Self-Certification', route: '/providers/live-in',                              icon: '🏡' },
  { label: 'Sick Leave Claim Manual Entry',           route: '/payments/sick-leave',                            icon: '🏥' },
];

const badgeClass = (val) => {
  if (!val) return '';
  return `wq-badge wq-badge-${val.toLowerCase().replace(/\s/g, '_')}`;
};

export const WorkspaceContent = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [loading, setLoading] = useState(true);
  const [tasks, setTasks] = useState([]);

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
      setTasks(allTasks.slice(0, 15).map(mapBackendTask));
    } catch (err) {
      console.warn('[WorkspaceContent] Dashboard load error:', err?.message);
    } finally {
      setLoading(false);
    }
  }, [username]);

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
      <h1 className="wq-page-title">
        My Workspace: Welcome to CMIPS
      </h1>

      {/* My Tasks heading */}
      <h2 style={{ color: '#153554', fontSize: '1.25rem', fontWeight: 600, margin: '1rem 0 0.75rem' }}>
        My Tasks ({tasks.length})
      </h2>

      {/* Two-column layout: Tasks | Shortcuts */}
      <div className="workspace-main-columns">

        {/* ── Left: My Tasks ── */}
        <section className="wq-cluster">
          <div className="wq-cluster-body" style={{ padding: 0 }}>
            {tasks.length === 0 ? (
              <p style={{ padding: '1.5rem', color: '#888', fontSize: '0.875rem', textAlign: 'center', fontStyle: 'italic' }}>
                No tasks assigned.
              </p>
            ) : (
              <table className="wq-table-uim">
                <thead>
                  <tr>
                    <th>Task</th>
                    <th>Subject</th>
                    <th>Status</th>
                    <th>Due Date</th>
                    <th>Priority</th>
                  </tr>
                </thead>
                <tbody>
                  {tasks.map(t => (
                    <tr key={t.id} className="wq-clickable-row" onClick={() => navigate(`/tasks/${t.id}`)}>
                      <td>
                        <button
                          className="wq-link"
                          onClick={(e) => { e.stopPropagation(); navigate(`/tasks/${t.id}`); }}
                        >
                          {t.id}
                        </button>
                      </td>
                      <td>{t.subject}</td>
                      <td>
                        <span className={badgeClass(t.status)}>{t.status || '\u2014'}</span>
                      </td>
                      <td style={t.isOverdue ? { color: '#c53030', fontWeight: 600 } : {}}>
                        {t.dueDate || '\u2014'}
                        {t.isOverdue && <span style={{ marginLeft: '0.35rem', fontSize: '0.7rem' }}>OVERDUE</span>}
                      </td>
                      <td>
                        <span className={badgeClass(t.priority)}>{t.priority}</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </section>

        {/* ── Right: My Shortcuts ── */}
        <section className="wq-cluster">
          <h2 className="wq-cluster-title">My Shortcuts</h2>
          <div className="wq-cluster-body" style={{ padding: '0.75rem' }}>
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
        </section>

      </div>

    </div>
  );
};
