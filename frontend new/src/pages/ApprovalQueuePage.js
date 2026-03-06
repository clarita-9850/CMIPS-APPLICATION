import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as workspaceApi from '../api/workspaceApi';
import http from '../api/httpClient';
import './WorkQueues.css';

export const ApprovalQueuePage = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [activeTab, setActiveTab] = useState('timesheets');
  const [timesheets, setTimesheets] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Supervisor', path: '/supervisor' }, { label: 'Approval Queue' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const data = await workspaceApi.fetchPendingApprovals(username);
        setTimesheets(data.timesheets || []);
      } catch (err) {
        console.warn('[ApprovalQueue] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    if (username) load();
  }, [username]);

  const handleApprove = async (id) => {
    try {
      await http.post(`/timesheets/${id}/approve`);
      setTimesheets(prev => prev.filter(t => t.id !== id));
    } catch (err) {
      alert('Approve failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const handleReject = async (id) => {
    try {
      await http.post(`/timesheets/${id}/reject`);
      setTimesheets(prev => prev.filter(t => t.id !== id));
    } catch (err) {
      alert('Reject failed: ' + (err?.message || 'Unknown error'));
    }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Approval Queue</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/supervisor')}>Back to Dashboard</button>
      </div>

      <div className="wq-tabs">
        {['timesheets', 'cases', 'notes'].map(tab => (
          <button key={tab} className={`wq-tab ${activeTab === tab ? 'active' : ''}`}
            onClick={() => setActiveTab(tab)}>
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {activeTab === 'timesheets' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Pending Timesheets ({timesheets.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? (
              <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
            ) : timesheets.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No pending timesheets.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>ID</th><th>Employee</th><th>Period</th><th>Hours</th><th>Status</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {timesheets.map(t => (
                    <tr key={t.id}>
                      <td>{t.id}</td>
                      <td>{t.employeeName}</td>
                      <td>{t.payPeriodStart} - {t.payPeriodEnd}</td>
                      <td>{t.totalHours}</td>
                      <td><span className={`wq-badge wq-badge-${(t.status || '').toLowerCase()}`}>{t.status}</span></td>
                      <td>
                        <button className="wq-btn wq-btn-primary" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', marginRight: '0.25rem' }} onClick={() => handleApprove(t.id)}>Approve</button>
                        <button className="wq-btn" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem', color: '#c53030', border: '1px solid #c53030' }} onClick={() => handleReject(t.id)}>Reject</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      )}

      {activeTab === 'cases' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Pending Cases</h4></div>
          <div className="wq-panel-body"><p style={{ color: '#888' }}>No pending case approvals.</p></div>
        </div>
      )}

      {activeTab === 'notes' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Pending Notes</h4></div>
          <div className="wq-panel-body"><p style={{ color: '#888' }}>No pending note approvals.</p></div>
        </div>
      )}
    </div>
  );
};
