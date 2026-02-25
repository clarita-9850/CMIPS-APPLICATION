import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as orgApi from '../api/organizationApi';
import './WorkQueues.css';

export const OrganizationPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [activeTab, setActiveTab] = useState('groups');
  const [groups, setGroups] = useState([]);
  const [roles, setRoles] = useState([]);
  const [resources, setResources] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [newGroupName, setNewGroupName] = useState('');
  const [newRoleName, setNewRoleName] = useState('');
  const [newRoleDesc, setNewRoleDesc] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Administration' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const [g, r, res] = await Promise.all([
          orgApi.getGroups().catch(() => []),
          orgApi.getRoles().catch(() => []),
          orgApi.getResources().catch(() => [])
        ]);
        setGroups(Array.isArray(g) ? g : []);
        setRoles(Array.isArray(r) ? r : []);
        setResources(Array.isArray(res) ? res : []);
      } catch (err) {
        console.warn('[Organization] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleCreateGroup = async () => {
    if (!newGroupName.trim()) return;
    setError('');
    try {
      await orgApi.createGroup(newGroupName.trim());
      setNewGroupName('');
      const g = await orgApi.getGroups();
      setGroups(Array.isArray(g) ? g : []);
    } catch (err) {
      setError('Failed to create group: ' + (err?.message || 'Unknown error'));
    }
  };

  const handleDeleteGroup = async (groupId) => {
    if (!window.confirm('Delete this group?')) return;
    try {
      await orgApi.deleteGroup(groupId);
      setGroups(prev => prev.filter(g => g.id !== groupId));
    } catch (err) {
      setError('Delete failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const handleCreateRole = async () => {
    if (!newRoleName.trim()) return;
    setError('');
    try {
      await orgApi.createRole(newRoleName.trim(), newRoleDesc.trim());
      setNewRoleName('');
      setNewRoleDesc('');
      const r = await orgApi.getRoles();
      setRoles(Array.isArray(r) ? r : []);
    } catch (err) {
      setError('Failed to create role: ' + (err?.message || 'Unknown error'));
    }
  };

  if (loading) return <div className="wq-page"><p>Loading...</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Administration</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => navigate('/admin/users')}>Manage Users</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>
      )}

      {/* Stats */}
      <div className="workspace-stats-row">
        <div className="workspace-stat-card" onClick={() => setActiveTab('groups')}>
          <div className="stat-number">{groups.length}</div>
          <div className="stat-label">Groups</div>
        </div>
        <div className="workspace-stat-card" onClick={() => setActiveTab('roles')}>
          <div className="stat-number">{roles.length}</div>
          <div className="stat-label">Roles</div>
        </div>
        <div className="workspace-stat-card" onClick={() => setActiveTab('resources')}>
          <div className="stat-number">{resources.length}</div>
          <div className="stat-label">Resources</div>
        </div>
        <div className="workspace-stat-card" onClick={() => navigate('/admin/users')}>
          <div className="stat-number">&rarr;</div>
          <div className="stat-label">Users</div>
        </div>
      </div>

      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'groups' ? 'active' : ''}`} onClick={() => setActiveTab('groups')}>Groups</button>
        <button className={`wq-tab ${activeTab === 'roles' ? 'active' : ''}`} onClick={() => setActiveTab('roles')}>Roles</button>
        <button className={`wq-tab ${activeTab === 'resources' ? 'active' : ''}`} onClick={() => setActiveTab('resources')}>Resources</button>
      </div>

      {activeTab === 'groups' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Organization Groups ({groups.length})</h4>
          </div>
          <div className="wq-panel-body">
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
              <input type="text" value={newGroupName} onChange={e => setNewGroupName(e.target.value)}
                placeholder="New group name" style={{ flex: 1, padding: '0.4rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
              <button className="wq-btn wq-btn-primary" onClick={handleCreateGroup}>Create Group</button>
            </div>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {groups.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No groups found.</p>
            ) : (
              <table className="wq-table">
                <thead><tr><th>Group Name</th><th>ID</th><th>Actions</th></tr></thead>
                <tbody>
                  {groups.map(g => (
                    <tr key={g.id}>
                      <td>{g.name}</td>
                      <td style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: '#888' }}>{g.id}</td>
                      <td>
                        <button className="wq-btn" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', color: '#c53030', border: '1px solid #c53030' }}
                          onClick={() => handleDeleteGroup(g.id)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {activeTab === 'roles' && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Keycloak Roles ({roles.length})</h4>
          </div>
          <div className="wq-panel-body">
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '1rem' }}>
              <input type="text" value={newRoleName} onChange={e => setNewRoleName(e.target.value)}
                placeholder="Role name" style={{ flex: 1, padding: '0.4rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
              <input type="text" value={newRoleDesc} onChange={e => setNewRoleDesc(e.target.value)}
                placeholder="Description" style={{ flex: 1, padding: '0.4rem', border: '1px solid #cbd5e0', borderRadius: '4px' }} />
              <button className="wq-btn wq-btn-primary" onClick={handleCreateRole}>Create Role</button>
            </div>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {roles.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No roles found.</p>
            ) : (
              <table className="wq-table">
                <thead><tr><th>Role Name</th><th>Description</th></tr></thead>
                <tbody>
                  {roles.map(r => (
                    <tr key={r.id || r.name}>
                      <td>{r.name}</td>
                      <td>{r.description || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {activeTab === 'resources' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Authorization Resources ({resources.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {resources.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No resources found.</p>
            ) : (
              <table className="wq-table">
                <thead><tr><th>Resource Name</th><th>Type</th><th>URI</th></tr></thead>
                <tbody>
                  {resources.map(r => (
                    <tr key={r._id || r.name}>
                      <td>{r.name}</td>
                      <td>{r.type || '\u2014'}</td>
                      <td style={{ fontSize: '0.8rem', color: '#888' }}>{(r.uris || []).join(', ') || '\u2014'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
