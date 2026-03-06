import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const TasksBeforeDeadlinePage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [daysAhead, setDaysAhead] = useState(7);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Tasks', path: '/tasks/assigned' }, { label: 'Before Deadline' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = useCallback(async () => {
    setLoading(true);
    try {
      const dueBefore = new Date();
      dueBefore.setDate(dueBefore.getDate() + daysAhead);
      const res = await http.get(`/tasks?username=${encodeURIComponent(username)}&dueBefore=${dueBefore.toISOString().split('T')[0]}`);
      const data = Array.isArray(res.data) ? res.data : (res.data?.content || res.data?.items || []);
      setTasks(data.filter(t => t.status !== 'CLOSED' && t.status !== 'COMPLETED'));
    } catch (err) {
      console.warn('[TasksBeforeDeadline] Error:', err?.message);
      setTasks([]);
    } finally {
      setLoading(false);
    }
  }, [username, daysAhead]);

  useEffect(() => { if (username) handleSearch(); }, [handleSearch, username]);

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Tasks Before Deadline</h2>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Filter</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Days Ahead</label>
              <select value={daysAhead} onChange={e => setDaysAhead(Number(e.target.value))}>
                <option value={1}>1 day</option>
                <option value={3}>3 days</option>
                <option value={7}>7 days</option>
                <option value={14}>14 days</option>
                <option value={30}>30 days</option>
              </select>
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
              {loading ? 'Searching...' : 'Search'}
            </button>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Results ({tasks.length})</h4>
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {tasks.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No tasks found within the selected deadline window.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Subject</th>
                  <th>Due Date</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Assigned To</th>
                </tr>
              </thead>
              <tbody>
                {tasks.map(t => (
                  <tr key={t.id} className="wq-clickable-row" onClick={() => navigate(`/tasks/${t.id}`)}>
                    <td>{t.id}</td>
                    <td>{t.title || t.subject}</td>
                    <td>{t.dueDate ? new Date(t.dueDate).toLocaleDateString() : ''}</td>
                    <td><span className={`wq-badge wq-badge-${(t.priority || '').toLowerCase()}`}>{t.priority}</span></td>
                    <td><span className={`wq-badge wq-badge-${(t.status || '').toLowerCase()}`}>{t.status}</span></td>
                    <td>{t.assignedTo}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
