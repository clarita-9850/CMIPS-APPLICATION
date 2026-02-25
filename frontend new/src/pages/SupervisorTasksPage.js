import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const SupervisorTasksPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [workers, setWorkers] = useState([]);
  const [selectedWorker, setSelectedWorker] = useState(null);
  const [workerTasks, setWorkerTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Supervisor', path: '/supervisor' }, { label: 'Team Tasks' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const loadTeam = async () => {
      try {
        const res = await http.get('/tasks');
        const allTasks = Array.isArray(res.data) ? res.data : (res.data?.content || []);
        // Group by assignedTo
        const workerMap = {};
        allTasks.forEach(t => {
          const w = t.assignedTo || 'Unassigned';
          if (!workerMap[w]) workerMap[w] = { username: w, tasks: [], openCount: 0 };
          workerMap[w].tasks.push(t);
          if (t.status !== 'CLOSED' && t.status !== 'COMPLETED') workerMap[w].openCount++;
        });
        setWorkers(Object.values(workerMap).sort((a, b) => b.openCount - a.openCount));
      } catch (err) {
        console.warn('[SupervisorTasks] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    loadTeam();
  }, []);

  const handleWorkerClick = (worker) => {
    setSelectedWorker(worker.username);
    setWorkerTasks(worker.tasks);
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Team Tasks</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/supervisor')}>Back to Dashboard</button>
      </div>

      {loading ? (
        <p style={{ color: '#888' }}>Loading team data...</p>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '300px 1fr', gap: '1rem' }}>
          {/* Workers list */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Team Members</h4></div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              <table className="wq-table">
                <thead><tr><th>Worker</th><th>Open</th></tr></thead>
                <tbody>
                  {workers.map(w => (
                    <tr key={w.username}
                      className="wq-clickable-row"
                      style={{ background: selectedWorker === w.username ? '#eef3f8' : undefined }}
                      onClick={() => handleWorkerClick(w)}>
                      <td>{w.username}</td>
                      <td><span className="wq-badge">{w.openCount}</span></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Worker's tasks */}
          <div className="wq-panel">
            <div className="wq-panel-header">
              <h4>{selectedWorker ? `Tasks for ${selectedWorker}` : 'Select a team member'}</h4>
            </div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {!selectedWorker ? (
                <p style={{ padding: '1rem', color: '#888' }}>Click a team member to see their tasks.</p>
              ) : workerTasks.length === 0 ? (
                <p style={{ padding: '1rem', color: '#888' }}>No tasks for this worker.</p>
              ) : (
                <table className="wq-table">
                  <thead>
                    <tr><th>ID</th><th>Subject</th><th>Due</th><th>Priority</th><th>Status</th></tr>
                  </thead>
                  <tbody>
                    {workerTasks.map(t => (
                      <tr key={t.id} className="wq-clickable-row" onClick={() => navigate(`/tasks/${t.id}`)}>
                        <td>{t.id}</td>
                        <td>{t.title || t.subject}</td>
                        <td>{t.dueDate ? new Date(t.dueDate).toLocaleDateString() : ''}</td>
                        <td><span className={`wq-badge wq-badge-${(t.priority || '').toLowerCase()}`}>{t.priority}</span></td>
                        <td><span className={`wq-badge wq-badge-${(t.status || '').toLowerCase()}`}>{t.status}</span></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
