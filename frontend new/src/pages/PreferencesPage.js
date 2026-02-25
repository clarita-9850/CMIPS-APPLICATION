import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import { useAuth } from '../auth/AuthContext';
import './WorkQueues.css';

export const PreferencesPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();
  const { user } = useAuth();

  const [preferences, setPreferences] = useState({
    defaultLandingPage: '/workspace',
    itemsPerPage: '20',
    dateFormat: 'MM/DD/YYYY',
    enableNotifications: true,
    enableEmailAlerts: false,
    defaultCounty: '',
    theme: 'light'
  });
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Preferences' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const stored = localStorage.getItem('cmips_preferences');
    if (stored) {
      try {
        setPreferences(prev => ({ ...prev, ...JSON.parse(stored) }));
      } catch { /* ignore */ }
    }
  }, []);

  const handleSave = () => {
    localStorage.setItem('cmips_preferences', JSON.stringify(preferences));
    setSaved(true);
    setTimeout(() => setSaved(false), 3000);
  };

  const handleChange = (key, value) => {
    setPreferences(prev => ({ ...prev, [key]: value }));
    setSaved(false);
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>User Preferences</h2>
      </div>

      {saved && <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>Preferences saved successfully.</div>}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Account Information</h4></div>
        <div className="wq-panel-body">
          <div className="wq-detail-grid">
            <div className="wq-detail-field"><label>Username</label><span>{user?.username || user?.preferred_username || '\u2014'}</span></div>
            <div className="wq-detail-field"><label>Name</label><span>{user?.name || [user?.given_name, user?.family_name].filter(Boolean).join(' ') || '\u2014'}</span></div>
            <div className="wq-detail-field"><label>Email</label><span>{user?.email || '\u2014'}</span></div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Display Settings</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Default Landing Page</label>
              <select value={preferences.defaultLandingPage} onChange={e => handleChange('defaultLandingPage', e.target.value)}>
                <option value="/workspace">My Workspace</option>
                <option value="/cases">Cases</option>
                <option value="/inbox">Inbox</option>
                <option value="/tasks/assigned">Assigned Tasks</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Items Per Page</label>
              <select value={preferences.itemsPerPage} onChange={e => handleChange('itemsPerPage', e.target.value)}>
                <option value="10">10</option>
                <option value="20">20</option>
                <option value="50">50</option>
                <option value="100">100</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Date Format</label>
              <select value={preferences.dateFormat} onChange={e => handleChange('dateFormat', e.target.value)}>
                <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                <option value="YYYY-MM-DD">YYYY-MM-DD</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Default County</label>
              <input type="text" value={preferences.defaultCounty} onChange={e => handleChange('defaultCounty', e.target.value)} placeholder="e.g. Los Angeles" />
            </div>
          </div>
        </div>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Notification Settings</h4></div>
        <div className="wq-panel-body">
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
              <input type="checkbox" checked={preferences.enableNotifications} onChange={e => handleChange('enableNotifications', e.target.checked)} />
              Enable in-app notifications
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
              <input type="checkbox" checked={preferences.enableEmailAlerts} onChange={e => handleChange('enableEmailAlerts', e.target.checked)} />
              Enable email alerts for task assignments
            </label>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
        <button className="wq-btn wq-btn-primary" onClick={handleSave}>Save Preferences</button>
      </div>
    </div>
  );
};
