/**
 * ReserveNextTaskModal - Reserve next task from a work queue
 * Matches legacy CMIPS "Reserve Next Task From Work Queue" modal
 * Fields: Work Queue dropdown, Reserve / Reserve Next 5 / Reserve Next 20
 */

import React, { useState, useEffect } from 'react';
import { useAuth } from '../../auth/AuthContext';
import http from '../../api/httpClient';
import '../WorkQueues.css';

export const ReserveNextTaskModal = ({ onClose, onReserve }) => {
  const { user } = useAuth();
  const username = user?.username || user?.preferred_username || 'unknown';
  const [queues, setQueues] = useState([]);
  const [selectedQueue, setSelectedQueue] = useState('');
  const [loading, setLoading] = useState(true);
  const [reserving, setReserving] = useState(false);

  useEffect(() => {
    http.get('/work-queues')
      .then(res => {
        const d = res?.data;
        const list = Array.isArray(d) ? d : (d?.content || []);
        setQueues(list);
        if (list.length > 0) setSelectedQueue(list[0].id || list[0].queueId || '');
      })
      .catch(() => setQueues([]))
      .finally(() => setLoading(false));
  }, []);

  const handleReserve = (count) => {
    if (!selectedQueue) { alert('Please select a work queue'); return; }
    setReserving(true);
    http.post(`/work-queues/${selectedQueue}/reserve`, { username, count })
      .then(() => { if (onReserve) onReserve(); })
      .catch(() => alert('Failed to reserve task(s)'))
      .finally(() => setReserving(false));
  };

  return (
    <div className="wq-modal-overlay" onClick={onClose}>
      <div className="wq-modal" style={{ minWidth: '400px' }} onClick={e => e.stopPropagation()}>
        <div className="wq-modal-header">
          <h3>Reserve Next Task From Work Queue</h3>
          <button className="wq-modal-close" onClick={onClose}>&times;</button>
        </div>

        <div className="wq-modal-body">
          {loading ? (
            <p>Loading work queues...</p>
          ) : queues.length === 0 ? (
            <p>No work queues available.</p>
          ) : (
            <div className="wq-form-field" style={{ width: '100%' }}>
              <label>Work Queue</label>
              <select
                value={selectedQueue}
                onChange={e => setSelectedQueue(e.target.value)}
                style={{ width: '100%' }}
              >
                {queues.map(q => {
                  const qid = q.id || q.queueId;
                  return (
                    <option key={qid} value={qid}>
                      {q.displayName || q.name || qid}
                    </option>
                  );
                })}
              </select>
            </div>
          )}
        </div>

        <div className="wq-modal-footer">
          <button
            className="wq-btn wq-btn-primary"
            onClick={() => handleReserve(1)}
            disabled={reserving || !selectedQueue}
          >
            Reserve
          </button>
          <button
            className="wq-btn wq-btn-outline"
            onClick={() => handleReserve(5)}
            disabled={reserving || !selectedQueue}
          >
            Reserve Next 5
          </button>
          <button
            className="wq-btn wq-btn-outline"
            onClick={() => handleReserve(20)}
            disabled={reserving || !selectedQueue}
          >
            Reserve Next 20
          </button>
          <button className="wq-btn wq-btn-outline" onClick={onClose}>Cancel</button>
        </div>
      </div>
    </div>
  );
};
