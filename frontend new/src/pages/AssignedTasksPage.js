/**
 * AssignedTasksPage - Tasks assigned to the current user
 * Matches legacy CMIPS "Assigned Tasks" tab
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const AssignedTasksPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username;
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!username) { setLoading(false); return; }
    http.get(`/tasks?assignedTo=${encodeURIComponent(username)}&status=OPEN`)
      .then(res => {
        const d = res?.data;
        setTasks(Array.isArray(d) ? d : (d?.content || []));
      })
      .catch(() => setTasks([]))
      .finally(() => setLoading(false));
  }, [username]);

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Assigned Tasks</h2>
      </div>

      {loading ? (
        <p>Loading...</p>
      ) : tasks.length === 0 ? (
        <p className="wq-empty">No assigned tasks.</p>
      ) : (
        <table className="wq-table">
          <thead>
            <tr>
              <th>Action</th>
              <th>Task ID</th>
              <th>Subject</th>
              <th>Priority</th>
              <th>Assigned</th>
              <th>Deadline</th>
            </tr>
          </thead>
          <tbody>
            {tasks.map((t, i) => {
              const tid = t.id || t.taskId;
              return (
                <tr key={tid || i}>
                  <td>
                    <button className="action-link" onClick={() => navigate(`/tasks/${tid}`)}>View</button>
                  </td>
                  <td>
                    <button className="action-link" onClick={() => navigate(`/tasks/${tid}`)}>{tid}</button>
                  </td>
                  <td>{t.subject || t.title || '—'}</td>
                  <td>{t.priority || 'Medium'}</td>
                  <td>{t.assignedDate || t.createdAt || '—'}</td>
                  <td>{t.deadline || t.dueDate || '—'}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
};
