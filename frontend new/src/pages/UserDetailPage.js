import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as orgApi from '../api/organizationApi';
import './WorkQueues.css';

export const UserDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [user, setUser] = useState(null);
  const [userRoles, setUserRoles] = useState([]);
  const [userGroups, setUserGroups] = useState([]);
  const [allRoles, setAllRoles] = useState([]);
  const [allGroups, setAllGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedRole, setSelectedRole] = useState('');
  const [selectedGroup, setSelectedGroup] = useState('');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Administration', path: '/admin' },
      { label: 'Users', path: '/admin/users' },
      { label: user?.username || 'User Detail' }
    ]);
    return () => setBreadcrumbs([]);
  }, [user, setBreadcrumbs]);

  const loadData = useCallback(async () => {
    try {
      const [users, roles, groups, uRoles, uGroups] = await Promise.all([
        orgApi.getUsers().catch(() => []),
        orgApi.getRoles().catch(() => []),
        orgApi.getGroups().catch(() => []),
        orgApi.getUserRoles(id).catch(() => []),
        orgApi.getUserGroups(id).catch(() => [])
      ]);
      const allUsers = Array.isArray(users) ? users : [];
      const found = allUsers.find(u => u.id === id);
      setUser(found || null);
      setAllRoles(Array.isArray(roles) ? roles : []);
      setAllGroups(Array.isArray(groups) ? groups : []);
      setUserRoles(Array.isArray(uRoles) ? uRoles : []);
      setUserGroups(Array.isArray(uGroups) ? uGroups : []);
    } catch (err) {
      console.warn('[UserDetail] Error:', err?.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { loadData(); }, [loadData]);

  const handleAssignRole = async () => {
    if (!selectedRole) return;
    setError('');
    try {
      await orgApi.assignRole(id, selectedRole);
      setSelectedRole('');
      const r = await orgApi.getUserRoles(id);
      setUserRoles(Array.isArray(r) ? r : []);
    } catch (err) {
      setError('Failed to assign role: ' + (err?.message || 'Unknown error'));
    }
  };

  const handleAddToGroup = async () => {
    if (!selectedGroup) return;
    setError('');
    try {
      await orgApi.addUserToGroup(id, selectedGroup);
      setSelectedGroup('');
      const g = await orgApi.getUserGroups(id);
      setUserGroups(Array.isArray(g) ? g : []);
    } catch (err) {
      setError('Failed to add to group: ' + (err?.message || 'Unknown error'));
    }
  };

  const handleRemoveFromGroup = async (groupId) => {
    setError('');
    try {
      await orgApi.removeUserFromGroup(id, groupId);
      setUserGroups(prev => prev.filter(g => g.id !== groupId));
    } catch (err) {
      setError('Failed to remove from group: ' + (err?.message || 'Unknown error'));
    }
  };

  if (loading) return <div className="wq-page"><p>Loading user...</p></div>;
  if (!user) return <div className="wq-page"><p>User not found.</p><button className="wq-btn wq-btn-outline" onClick={() => navigate('/admin/users')}>Back</button></div>;

  const assignedRoleNames = userRoles.map(r => r.name);
  const availableRoles = allRoles.filter(r => !assignedRoleNames.includes(r.name));
  const assignedGroupIds = userGroups.map(g => g.id);
  const availableGroups = allGroups.filter(g => !assignedGroupIds.includes(g.id));

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>User: {user.username}</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/admin/users')}>Back to Users</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>
      )}

      <div className="wq-task-columns">
        {/* User Info */}
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>User Information</h4></div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">User ID:</span><span className="wq-detail-value" style={{ fontFamily: 'monospace', fontSize: '0.8rem' }}>{user.id}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Username:</span><span className="wq-detail-value">{user.username}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">First Name:</span><span className="wq-detail-value">{user.firstName || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Last Name:</span><span className="wq-detail-value">{user.lastName || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Email:</span><span className="wq-detail-value">{user.email || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Enabled:</span><span className="wq-detail-value"><span className={`wq-badge wq-badge-${user.enabled ? 'active' : 'inactive'}`}>{user.enabled ? 'Yes' : 'No'}</span></span></div>
            </div>
          </div>
        </div>

        {/* Roles */}
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Assigned Roles ({userRoles.length})</h4></div>
          <div className="wq-panel-body">
            <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem' }}>
              <select value={selectedRole} onChange={e => setSelectedRole(e.target.value)}
                style={{ flex: 1, padding: '0.4rem', border: '1px solid #cbd5e0', borderRadius: '4px' }}>
                <option value="">-- Select role --</option>
                {availableRoles.map(r => <option key={r.id || r.name} value={r.name}>{r.name}</option>)}
              </select>
              <button className="wq-btn wq-btn-primary" onClick={handleAssignRole} disabled={!selectedRole}>Assign</button>
            </div>
            {userRoles.length === 0 ? (
              <p style={{ color: '#888', fontSize: '0.875rem' }}>No roles assigned.</p>
            ) : (
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.3rem' }}>
                {userRoles.map(r => (
                  <span key={r.id || r.name} className="wq-badge" style={{ padding: '0.3rem 0.5rem' }}>{r.name}</span>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Groups */}
      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Group Membership ({userGroups.length})</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', gap: '0.5rem', marginBottom: '0.75rem' }}>
            <select value={selectedGroup} onChange={e => setSelectedGroup(e.target.value)}
              style={{ flex: 1, padding: '0.4rem', border: '1px solid #cbd5e0', borderRadius: '4px' }}>
              <option value="">-- Select group --</option>
              {availableGroups.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
            </select>
            <button className="wq-btn wq-btn-primary" onClick={handleAddToGroup} disabled={!selectedGroup}>Add to Group</button>
          </div>
          {userGroups.length === 0 ? (
            <p style={{ color: '#888', fontSize: '0.875rem' }}>Not a member of any groups.</p>
          ) : (
            <table className="wq-table">
              <thead><tr><th>Group Name</th><th>Actions</th></tr></thead>
              <tbody>
                {userGroups.map(g => (
                  <tr key={g.id}>
                    <td>{g.name}</td>
                    <td>
                      <button className="wq-btn" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', color: '#c53030', border: '1px solid #c53030' }}
                        onClick={() => handleRemoveFromGroup(g.id)}>Remove</button>
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
