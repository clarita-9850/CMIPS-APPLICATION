import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const WorkQueueAdminPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [queues, setQueues] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [newQueue, setNewQueue] = useState({ name: '', description: '', category: '' });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Admin', path: '/admin' }, { label: 'Work Queue Admin' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await http.get('/work-queues');
        const data = Array.isArray(res.data) ? res.data : (res.data?.content || []);
        setQueues(data);
      } catch (err) {
        console.warn('[WorkQueueAdmin] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleCreate = async () => {
    if (!newQueue.name.trim()) return;
    setSaving(true);
    try {
      const res = await http.post('/work-queues', newQueue);
      setQueues(prev => [...prev, res.data]);
      setShowCreate(false);
      setNewQueue({ name: '', description: '', category: '' });
    } catch (err) {
      console.warn('[WorkQueueAdmin] Create error:', err?.message);
    } finally {
      setSaving(false);
    }
  };

  const handleDeactivate = async (queueId) => {
    if (!window.confirm('Deactivate this work queue?')) return;
    try {
      await http.delete(`/work-queues/${queueId}`);
      setQueues(prev => prev.filter(q => (q.id || q.queueId) !== queueId));
    } catch (err) {
      console.warn('[WorkQueueAdmin] Deactivate error:', err?.message);
    }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Work Queue Administration</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => setShowCreate(true)}>Create Queue</button>
      </div>

      {/* Create Form */}
      {showCreate && (
        <div className="wq-panel" style={{ marginBottom: '1rem' }}>
          <div className="wq-panel-header"><h4>Create New Queue</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field">
                <label>Queue Name *</label>
                <input type="text" value={newQueue.name} onChange={e => setNewQueue(p => ({ ...p, name: e.target.value }))} />
              </div>
              <div className="wq-form-field">
                <label>Category</label>
                <input type="text" value={newQueue.category} onChange={e => setNewQueue(p => ({ ...p, category: e.target.value }))} />
              </div>
              <div className="wq-form-field" style={{ gridColumn: '1 / -1' }}>
                <label>Description</label>
                <textarea value={newQueue.description} onChange={e => setNewQueue(p => ({ ...p, description: e.target.value }))} rows={2} style={{ width: '100%' }} />
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleCreate} disabled={saving}>
                {saving ? 'Creating...' : 'Create'}
              </button>
              <button className="wq-btn wq-btn-outline" onClick={() => setShowCreate(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Queues Table */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Work Queues ({queues.length})</h4></div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? (
            <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
          ) : queues.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No work queues found.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr><th>ID</th><th>Name</th><th>Category</th><th>Tasks</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {queues.map(q => {
                  const id = q.id || q.queueId;
                  return (
                    <tr key={id}>
                      <td className="wq-clickable-row" onClick={() => navigate(`/work-queues/${id}`)}>{id}</td>
                      <td className="wq-clickable-row" onClick={() => navigate(`/work-queues/${id}`)}>{q.name || q.queueName}</td>
                      <td>{q.category || 'General'}</td>
                      <td>{q.taskCount || q.pendingTaskCount || 0}</td>
                      <td>
                        <button className="wq-btn wq-btn-outline" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', marginRight: '0.25rem' }}
                          onClick={() => navigate(`/work-queues/${id}`)}>View</button>
                        <button className="wq-btn" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', color: '#c53030', border: '1px solid #c53030' }}
                          onClick={() => handleDeactivate(id)}>Deactivate</button>
                      </td>
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
