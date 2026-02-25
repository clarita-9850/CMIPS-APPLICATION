/**
 * WorkQueueTasksPage - Tasks in a specific work queue
 * Matches legacy CMIPS "Work Queue Tasks: 19 Payments Pending Approval"
 * Search by District Office, bulk reserve/forward, task table with checkboxes
 */

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const WorkQueueTasksPage = () => {
  const { queueId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';
  const [tasks, setTasks] = useState([]);
  const [queueName, setQueueName] = useState('');
  const [loading, setLoading] = useState(true);
  const [districtOffice, setDistrictOffice] = useState('');
  const [selected, setSelected] = useState(new Set());

  const fetchTasks = useCallback((search = '') => {
    setLoading(true);
    const params = search ? `?districtOffice=${encodeURIComponent(search)}` : '';
    Promise.all([
      http.get(`/work-queues/${queueId}`).catch(() => ({ data: {} })),
      http.get(`/work-queues/${queueId}/tasks${params}`).catch(() => ({ data: [] }))
    ]).then(([qRes, tRes]) => {
      setQueueName(qRes.data?.displayName || qRes.data?.name || `Queue ${queueId}`);
      const tData = tRes.data;
      setTasks(Array.isArray(tData) ? tData : (tData?.content || []));
    }).finally(() => setLoading(false));
  }, [queueId]);

  useEffect(() => { fetchTasks(); }, [fetchTasks]);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchTasks(districtOffice);
  };

  const handleReset = () => {
    setDistrictOffice('');
    fetchTasks();
  };

  const toggleSelect = (taskId) => {
    setSelected(prev => {
      const next = new Set(prev);
      if (next.has(taskId)) next.delete(taskId); else next.add(taskId);
      return next;
    });
  };

  const toggleSelectAll = () => {
    if (selected.size === tasks.length) {
      setSelected(new Set());
    } else {
      setSelected(new Set(tasks.map(t => t.id || t.taskId)));
    }
  };

  const handleReserve = (taskId) => {
    http.post(`/tasks/${taskId}/reserve`, { username })
      .then(() => fetchTasks(districtOffice))
      .catch(() => alert('Failed to reserve task'));
  };

  const handleBulkReserve = (count) => {
    const ids = count ? tasks.slice(0, count).map(t => t.id || t.taskId) : Array.from(selected);
    if (ids.length === 0) return;
    Promise.all(ids.map(id => http.post(`/tasks/${id}/reserve`, { username }).catch(() => null)))
      .then(() => fetchTasks(districtOffice));
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Work Queue Tasks: {queueName}</h2>
      </div>

      {/* Search Criteria */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search Criteria</h4></div>
        <div className="wq-panel-body">
          <form onSubmit={handleSearch} className="wq-search-form" style={{ justifyContent: 'flex-end' }}>
            <div className="wq-form-field">
              <label>District Office:</label>
              <input
                type="text"
                value={districtOffice}
                onChange={e => setDistrictOffice(e.target.value)}
              />
            </div>
            <div className="wq-form-actions">
              <button type="submit" className="wq-btn wq-btn-primary">Search</button>
              <button type="button" className="wq-btn wq-btn-outline" onClick={handleReset}>Reset</button>
            </div>
          </form>
        </div>
      </div>

      {/* Bulk action buttons */}
      <div className="wq-btn-group">
        <button className="wq-btn wq-btn-outline" onClick={() => handleBulkReserve(5)}>
          Reserve Next 5 Tasks...
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => handleBulkReserve(20)}>
          Reserve Next 20 Tasks...
        </button>
        <button className="wq-btn wq-btn-outline" onClick={() => handleBulkReserve(0)} disabled={selected.size === 0}>
          Reserve Selected Tasks...
        </button>
        <button className="wq-btn wq-btn-outline" disabled={selected.size === 0}>
          Forward Selected Tasks...
        </button>
      </div>

      {/* Tasks table */}
      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Total Tasks: {tasks.length}</h4>
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? (
            <p style={{ padding: '1rem' }}>Loading tasks...</p>
          ) : tasks.length === 0 ? (
            <p className="wq-empty">No tasks in this queue.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>
                    <input type="checkbox" checked={selected.size === tasks.length && tasks.length > 0} onChange={toggleSelectAll} />
                  </th>
                  <th>Action</th>
                  <th>Task ID</th>
                  <th>District Office</th>
                  <th>Subject</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Deadline</th>
                </tr>
              </thead>
              <tbody>
                {tasks.map((t, i) => {
                  const tid = t.id || t.taskId;
                  return (
                    <tr key={tid || i}>
                      <td><input type="checkbox" checked={selected.has(tid)} onChange={() => toggleSelect(tid)} /></td>
                      <td>
                        <button className="action-link" onClick={() => handleReserve(tid)}>
                          Reserve...
                        </button>
                      </td>
                      <td>
                        <button className="action-link" onClick={() => navigate(`/tasks/${tid}`)}>
                          {tid}
                        </button>
                      </td>
                      <td>{t.districtOffice || t.district || '—'}</td>
                      <td>{t.subject || t.title || '—'}</td>
                      <td>{t.priority || 'Medium'}</td>
                      <td><span className={`wq-badge wq-badge-${(t.status || 'open').toLowerCase()}`}>{t.status || 'Open'}</span></td>
                      <td>{t.deadline || t.dueDate || '—'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
};
