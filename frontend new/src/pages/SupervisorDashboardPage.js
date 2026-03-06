import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as workspaceApi from '../api/workspaceApi';
import './WorkQueues.css';
import './WorkspaceContent.css';

export const SupervisorDashboardPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [stats, setStats] = useState({ taskCount: 0, caseCount: 0, approvalCount: 0 });
  const [team, setTeam] = useState([]);
  const [approvals, setApprovals] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Supervisor Dashboard' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const [statsData, teamData, approvalsData] = await Promise.all([
          workspaceApi.fetchDashboardStats(username),
          workspaceApi.fetchTeamWorkloads(username),
          workspaceApi.fetchPendingApprovals(username)
        ]);
        setStats(statsData);
        setTeam(teamData.teamMembers || []);
        setApprovals(approvalsData.timesheets || []);
      } catch (err) {
        console.warn('[SupervisorDashboard] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    if (username) load();
    else setLoading(false);
  }, [username]);

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header"><h2>Supervisor Dashboard</h2></div>

      {/* Stats */}
      <div className="workspace-stats-row">
        <div className="workspace-stat-card" onClick={() => navigate('/supervisor/tasks')}>
          <div className="stat-number">{team.length}</div>
          <div className="stat-label">Team Members</div>
        </div>
        <div className="workspace-stat-card" onClick={() => navigate('/cases')}>
          <div className="stat-number">{stats.caseCount}</div>
          <div className="stat-label">Open Cases</div>
        </div>
        <div className="workspace-stat-card workspace-stat-supervisor" onClick={() => navigate('/supervisor/approvals')}>
          <div className="stat-number">{stats.approvalCount}</div>
          <div className="stat-label">Pending Approvals</div>
        </div>
        <div className="workspace-stat-card" onClick={() => navigate('/tasks/assigned')}>
          <div className="stat-number">{stats.taskCount}</div>
          <div className="stat-label">Overdue Tasks</div>
        </div>
      </div>

      <div className="workspace-columns">
        {/* Team Workloads */}
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Team Workloads</h4>
            <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }} onClick={() => navigate('/supervisor/tasks')}>View All</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {team.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No team data available.</p>
            ) : (
              <table className="wq-table">
                <thead><tr><th>Worker</th><th>Open Tasks</th></tr></thead>
                <tbody>
                  {team.slice(0, 10).map(m => (
                    <tr key={m.username}><td>{m.username}</td><td><span className="wq-badge">{m.openTasks}</span></td></tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* Pending Approvals */}
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Pending Approvals</h4>
            <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '0.25rem 0.5rem' }} onClick={() => navigate('/supervisor/approvals')}>View All</button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {approvals.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No pending approvals.</p>
            ) : (
              <table className="wq-table">
                <thead><tr><th>ID</th><th>Employee</th><th>Status</th><th>Hours</th></tr></thead>
                <tbody>
                  {approvals.slice(0, 5).map(a => (
                    <tr key={a.id}>
                      <td>{a.id}</td>
                      <td>{a.employeeName}</td>
                      <td><span className={`wq-badge wq-badge-${(a.status || '').toLowerCase()}`}>{a.status}</span></td>
                      <td>{a.totalHours}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
