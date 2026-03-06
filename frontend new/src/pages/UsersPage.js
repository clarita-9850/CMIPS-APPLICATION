import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as orgApi from '../api/organizationApi';
import './WorkQueues.css';

export const UsersPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [error, setError] = useState('');
  const [createForm, setCreateForm] = useState({ username: '', email: '', password: '', firstName: '', lastName: '', role: '' });
  const [roles, setRoles] = useState([]);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Administration', path: '/admin' }, { label: 'Users' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const [u, r] = await Promise.all([
          orgApi.getUsers().catch(() => []),
          orgApi.getRoles().catch(() => [])
        ]);
        setUsers(Array.isArray(u) ? u : []);
        setRoles(Array.isArray(r) ? r : []);
      } catch (err) {
        console.warn('[Users] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const filtered = users.filter(u => {
    if (!searchTerm) return true;
    const s = searchTerm.toLowerCase();
    return (u.username || '').toLowerCase().includes(s)
      || (u.email || '').toLowerCase().includes(s)
      || (u.firstName || '').toLowerCase().includes(s)
      || (u.lastName || '').toLowerCase().includes(s);
  });

  const handleCreate = async () => {
    if (!createForm.username || !createForm.email || !createForm.password) {
      setError('Username, email, and password are required.'); return;
    }
    setError('');
    try {
      await orgApi.createUser(createForm);
      setShowCreate(false);
      setCreateForm({ username: '', email: '', password: '', firstName: '', lastName: '', role: '' });
      const u = await orgApi.getUsers();
      setUsers(Array.isArray(u) ? u : []);
    } catch (err) {
      setError('Create failed: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    }
  };

  const handleDelete = async (userId, username) => {
    if (!window.confirm(`Delete user "${username}"? This cannot be undone.`)) return;
    try {
      await orgApi.deleteUser(userId);
      setUsers(prev => prev.filter(u => u.id !== userId));
    } catch (err) {
      setError('Delete failed: ' + (err?.message || 'Unknown error'));
    }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>User Management</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="wq-btn wq-btn-primary" onClick={() => setShowCreate(!showCreate)}>
            {showCreate ? 'Cancel' : 'Create User'}
          </button>
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/admin')}>Back to Admin</button>
        </div>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>
      )}

      {showCreate && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Create New User</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field"><label>Username *</label><input type="text" value={createForm.username} onChange={e => setCreateForm(p => ({ ...p, username: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Email *</label><input type="email" value={createForm.email} onChange={e => setCreateForm(p => ({ ...p, email: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Password *</label><input type="password" value={createForm.password} onChange={e => setCreateForm(p => ({ ...p, password: e.target.value }))} /></div>
              <div className="wq-form-field"><label>First Name</label><input type="text" value={createForm.firstName} onChange={e => setCreateForm(p => ({ ...p, firstName: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Last Name</label><input type="text" value={createForm.lastName} onChange={e => setCreateForm(p => ({ ...p, lastName: e.target.value }))} /></div>
              <div className="wq-form-field">
                <label>Initial Role</label>
                <select value={createForm.role} onChange={e => setCreateForm(p => ({ ...p, role: e.target.value }))}>
                  <option value="">-- None --</option>
                  {roles.map(r => <option key={r.id || r.name} value={r.name}>{r.name}</option>)}
                </select>
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleCreate}>Create User</button>
            </div>
          </div>
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Users ({filtered.length})</h4>
          <input type="text" value={searchTerm} onChange={e => setSearchTerm(e.target.value)}
            placeholder="Search users..." style={{ padding: '0.3rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.875rem' }} />
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? (
            <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
          ) : filtered.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No users found.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr><th>Username</th><th>Name</th><th>Email</th><th>Enabled</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {filtered.map(u => (
                  <tr key={u.id} className="wq-clickable-row" onClick={() => navigate(`/admin/users/${u.id}`)}>
                    <td>{u.username}</td>
                    <td>{[u.firstName, u.lastName].filter(Boolean).join(' ') || '\u2014'}</td>
                    <td>{u.email || '\u2014'}</td>
                    <td><span className={`wq-badge wq-badge-${u.enabled ? 'active' : 'inactive'}`}>{u.enabled ? 'Yes' : 'No'}</span></td>
                    <td>
                      <button className="wq-btn" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', color: '#c53030', border: '1px solid #c53030' }}
                        onClick={(e) => { e.stopPropagation(); handleDelete(u.id, u.username); }}>Delete</button>
                    </td>
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
