/**
 * InboxPage - Main task hub with shortcuts
 * Matches legacy CMIPS "Inbox" screen
 * Shortcuts: Reserve Next Work Queue Task, Find Task, Create Task, View Tasks Before Deadline
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import http from '../api/httpClient';
import { CreateTaskModal } from './modals/CreateTaskModal';
import { ReserveNextTaskModal } from './modals/ReserveNextTaskModal';
import './WorkQueues.css';

export const InboxPage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [showCreateTask, setShowCreateTask] = useState(false);
  const [showReserveNext, setShowReserveNext] = useState(false);

  const displayName = user?.name || user?.username || '';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Inbox: {displayName}</h2>
      </div>

      {/* Shortcuts panel */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Shortcuts</h4></div>
        <div className="wq-panel-body">
          <div className="wq-shortcuts-grid">
            {/* Card 1 */}
            <div className="wq-shortcut-card">
              <button className="shortcut-link" onClick={() => setShowReserveNext(true)}>
                <span>📋</span> Reserve Next Work Queue Task...
              </button>
            </div>
            {/* Card 2 */}
            <div className="wq-shortcut-card">
              <button className="shortcut-link" onClick={() => navigate('/tasks/search')}>
                <span>🔍</span> Find Task
              </button>
              <button className="shortcut-link" onClick={() => setShowCreateTask(true)}>
                <span>📝</span> Create Task...
              </button>
            </div>
            {/* Card 3 */}
            <div className="wq-shortcut-card">
              <button className="shortcut-link" onClick={() => navigate('/tasks/before-deadline')}>
                <span>⏰</span> View Tasks Before Deadline...
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Create Task Modal */}
      {showCreateTask && (
        <CreateTaskModal
          onClose={() => setShowCreateTask(false)}
          onSave={(taskData) => {
            const username = user?.username || user?.preferred_username || 'unknown';
            const { reserveToMe, ...createData } = taskData;
            // Set status and createdBy
            createData.status = 'OPEN';
            createData.createdBy = username;
            if (!createData.assignedTo) createData.assignedTo = username;

            http.post('/tasks', createData)
              .then((res) => {
                const newTask = res?.data;
                if (reserveToMe && newTask?.id) {
                  return http.post(`/tasks/${newTask.id}/reserve`, { username });
                }
              })
              .then(() => { setShowCreateTask(false); navigate('/tasks/reserved'); })
              .catch(() => alert('Failed to create task'));
          }}
        />
      )}

      {/* Reserve Next Task Modal */}
      {showReserveNext && (
        <ReserveNextTaskModal
          onClose={() => setShowReserveNext(false)}
          onReserve={() => { setShowReserveNext(false); navigate('/tasks/reserved'); }}
        />
      )}
    </div>
  );
};
