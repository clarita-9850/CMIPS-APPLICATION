/**
 * WorkQueueDetailPage - View Work Queue details + subscriptions tab
 * Matches legacy CMIPS "View Work Queue: 19 Timesheet Errors"
 * Two tabs: View Work Queue | Work Queues Subscriptions
 */

import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import http, { httpClient } from '../api/httpClient';
import './WorkQueues.css';

export const WorkQueueDetailPage = () => {
  const { queueId } = useParams();
  const [queue, setQueue] = useState(null);
  const [subscribers, setSubscribers] = useState([]);
  const [activeTab, setActiveTab] = useState('details');
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [newUser, setNewUser] = useState('');

  const fetchQueue = useCallback(() => {
    Promise.all([
      http.get(`/work-queues/${queueId}`).catch(() => ({ data: null })),
      http.get(`/work-queues/${queueId}/subscribers`).catch(() => ({ data: [] }))
    ]).then(([qRes, sRes]) => {
      setQueue(qRes.data);
      const sData = sRes.data;
      setSubscribers(Array.isArray(sData) ? sData : []);
    }).finally(() => setLoading(false));
  }, [queueId]);

  useEffect(() => { fetchQueue(); }, [fetchQueue]);

  const handleRemoveSubscriber = (username) => {
    if (!window.confirm('Remove this subscriber?')) return;
    httpClient(`/work-queues/${queueId}/subscribe`, {
      method: 'DELETE',
      body: { username }
    })
      .then(() => fetchQueue())
      .catch(() => alert('Failed to remove subscriber'));
  };

  const handleAddSubscriber = () => {
    if (!newUser.trim()) return;
    http.post(`/work-queues/${queueId}/subscribe`, { username: newUser.trim() })
      .then(() => {
        setNewUser('');
        setShowAddModal(false);
        fetchQueue();
      })
      .catch(() => alert('Failed to add subscriber'));
  };

  if (loading) {
    return <div className="wq-page"><p>Loading...</p></div>;
  }

  const queueName = queue?.displayName || queue?.name || `Queue ${queueId}`;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>View Work Queue: {queueName}</h2>
      </div>

      {/* Tabs */}
      <div className="wq-tabs">
        <button
          className={`wq-tab ${activeTab === 'details' ? 'active' : ''}`}
          onClick={() => setActiveTab('details')}
        >
          View Work Queue
        </button>
        <button
          className={`wq-tab ${activeTab === 'subscriptions' ? 'active' : ''}`}
          onClick={() => setActiveTab('subscriptions')}
        >
          Work Queues Subscriptions
        </button>
      </div>

      {activeTab === 'details' && (
        <>
          {/* Details panel */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Details</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-row" style={{ justifyContent: 'center' }}>
                <span className="wq-detail-label">Name:</span>
                <span className="wq-detail-value">{queueName}</span>
              </div>
            </div>
          </div>

          {/* Subscription panel */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Subscription</h4></div>
            <div className="wq-panel-body">
              <div className="wq-detail-grid">
                <div className="wq-detail-row">
                  <span className="wq-detail-label">User Subscription Allowed:</span>
                  <span className="wq-detail-value">{queue?.subscriptionAllowed !== false ? 'Yes' : 'No'}</span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Administrator:</span>
                  <span className="wq-detail-value" style={{ color: '#2b6cb0' }}>
                    {queue?.administrator || '—'}
                  </span>
                </div>
                <div className="wq-detail-row">
                  <span className="wq-detail-label">Sensitivity:</span>
                  <span className="wq-detail-value">{queue?.sensitivityLevel ?? 1}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Comments panel */}
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Comments</h4></div>
            <div className="wq-panel-body">
              <p>{queue?.description || 'No comments.'}</p>
            </div>
          </div>
        </>
      )}

      {activeTab === 'subscriptions' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>User Subscriber</h4></div>
          <div className="wq-panel-body">
            <div style={{ marginBottom: '0.75rem' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => setShowAddModal(true)}>
                New...
              </button>
            </div>

            {subscribers.length === 0 ? (
              <p className="wq-empty">No subscribers.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Action</th>
                    <th>Name</th>
                    <th>Subscription Date</th>
                  </tr>
                </thead>
                <tbody>
                  {subscribers.map((s, i) => (
                    <tr key={s.id || i}>
                      <td>
                        <button
                          className="action-link"
                          style={{ color: '#c53030' }}
                          onClick={() => handleRemoveSubscriber(s.username)}
                        >
                          Remove...
                        </button>
                      </td>
                      <td>{s.username || '—'}</td>
                      <td>{s.subscribedAt || s.createdAt || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {/* Add Subscription Modal */}
      {showAddModal && (
        <div className="wq-modal-overlay" onClick={() => setShowAddModal(false)}>
          <div className="wq-modal" onClick={e => e.stopPropagation()}>
            <div className="wq-modal-header">
              <h3>Add Work Queue Subscription: {queueName}</h3>
              <button className="wq-modal-close" onClick={() => setShowAddModal(false)}>×</button>
            </div>
            <div className="wq-modal-body">
              <div className="wq-modal-required">* required field</div>
              <div className="wq-panel">
                <div className="wq-panel-header"><h4>Details</h4></div>
                <div className="wq-panel-body">
                  <div className="wq-detail-row">
                    <span className="wq-detail-label">User: *</span>
                    <input
                      type="text"
                      value={newUser}
                      onChange={e => setNewUser(e.target.value)}
                      style={{ padding: '0.375rem 0.5rem', border: '1px solid #aab', borderRadius: '3px', minWidth: '200px' }}
                      placeholder="Enter username"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div className="wq-modal-footer">
              <button className="wq-btn wq-btn-primary" onClick={handleAddSubscriber}>Save</button>
              <button className="wq-btn wq-btn-primary" onClick={() => { handleAddSubscriber(); setNewUser(''); }}>Save &amp; New</button>
              <button className="wq-btn wq-btn-outline" onClick={() => setShowAddModal(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
