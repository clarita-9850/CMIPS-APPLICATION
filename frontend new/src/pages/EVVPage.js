import React, { useState, useEffect } from 'react';
import { useAuth } from '../auth/AuthContext';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as evvApi from '../api/evvApi';
import './WorkQueues.css';

export const EVVPage = () => {
  const { user } = useAuth();
  const { setBreadcrumbs } = useBreadcrumbs();
  const username = user?.username || user?.preferred_username || '';

  const [activeTab, setActiveTab] = useState('records');
  const [records, setRecords] = useState([]);
  const [activeCheckin, setActiveCheckin] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [checkInForm, setCheckInForm] = useState({ recipientId: '', serviceType: '', location: '' });

  useEffect(() => {
    setBreadcrumbs([{ label: 'EVV' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const [recs, active] = await Promise.all([
          evvApi.getMyRecords().catch(() => []),
          evvApi.getActiveCheckin().catch(() => null)
        ]);
        setRecords(Array.isArray(recs) ? recs : (recs?.content || recs?.items || []));
        setActiveCheckin(active);
      } catch (err) {
        console.warn('[EVV] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleCheckIn = async () => {
    if (!checkInForm.recipientId) { setError('Recipient ID is required.'); return; }
    setError(''); setSuccess('');
    try {
      const result = await evvApi.checkIn({
        providerId: username,
        recipientId: checkInForm.recipientId,
        serviceType: checkInForm.serviceType || 'PERSONAL_CARE',
        location: checkInForm.location
      });
      setActiveCheckin(result);
      setSuccess('Check-in recorded successfully.');
      setCheckInForm({ recipientId: '', serviceType: '', location: '' });
    } catch (err) {
      setError('Check-in failed: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    }
  };

  const handleCheckOut = async () => {
    if (!activeCheckin?.id && !activeCheckin?.evvId) { setError('No active check-in.'); return; }
    setError(''); setSuccess('');
    try {
      await evvApi.checkOut(activeCheckin.id || activeCheckin.evvId, {});
      setActiveCheckin(null);
      setSuccess('Check-out recorded successfully.');
      const recs = await evvApi.getMyRecords().catch(() => []);
      setRecords(Array.isArray(recs) ? recs : []);
    } catch (err) {
      setError('Check-out failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const formatDateTime = (d) => d ? new Date(d).toLocaleString() : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Electronic Visit Verification (EVV)</h2>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>
      )}
      {success && (
        <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>{success}</div>
      )}

      {/* Active Check-In Status */}
      {activeCheckin && (
        <div className="wq-panel" style={{ borderLeft: '4px solid #38a169' }}>
          <div className="wq-panel-header">
            <h4>Active Check-In</h4>
            <button className="wq-btn wq-btn-primary" onClick={handleCheckOut}>Check Out</button>
          </div>
          <div className="wq-panel-body">
            <div className="wq-detail-grid">
              <div className="wq-detail-row"><span className="wq-detail-label">Recipient:</span><span className="wq-detail-value">{activeCheckin.recipientId || '\u2014'}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Check-In Time:</span><span className="wq-detail-value">{formatDateTime(activeCheckin.checkInTime)}</span></div>
              <div className="wq-detail-row"><span className="wq-detail-label">Service Type:</span><span className="wq-detail-value">{activeCheckin.serviceType || '\u2014'}</span></div>
            </div>
          </div>
        </div>
      )}

      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'records' ? 'active' : ''}`} onClick={() => setActiveTab('records')}>My Records</button>
        <button className={`wq-tab ${activeTab === 'checkin' ? 'active' : ''}`} onClick={() => setActiveTab('checkin')}>New Check-In</button>
      </div>

      {activeTab === 'checkin' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>New Check-In</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field"><label>Recipient ID *</label><input type="text" value={checkInForm.recipientId} onChange={e => setCheckInForm(p => ({ ...p, recipientId: e.target.value }))} /></div>
              <div className="wq-form-field">
                <label>Service Type</label>
                <select value={checkInForm.serviceType} onChange={e => setCheckInForm(p => ({ ...p, serviceType: e.target.value }))}>
                  <option value="">-- Select --</option>
                  <option value="PERSONAL_CARE">Personal Care</option>
                  <option value="DOMESTIC">Domestic</option>
                  <option value="PARAMEDICAL">Paramedical</option>
                  <option value="ACCOMPANIMENT">Accompaniment</option>
                  <option value="PROTECTIVE_SUPERVISION">Protective Supervision</option>
                </select>
              </div>
              <div className="wq-form-field"><label>Location</label><input type="text" value={checkInForm.location} onChange={e => setCheckInForm(p => ({ ...p, location: e.target.value }))} placeholder="GPS or address" /></div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleCheckIn} disabled={!!activeCheckin}>
                {activeCheckin ? 'Already Checked In' : 'Check In'}
              </button>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'records' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>EVV Records ({records.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? (
              <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
            ) : records.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No EVV records found.</p>
            ) : (
              <table className="wq-table">
                <thead>
                  <tr><th>ID</th><th>Recipient</th><th>Check-In</th><th>Check-Out</th><th>Service Type</th><th>Hours</th><th>Status</th></tr>
                </thead>
                <tbody>
                  {records.map(r => (
                    <tr key={r.id || r.evvId}>
                      <td>{r.id || r.evvId}</td>
                      <td>{r.recipientId || '\u2014'}</td>
                      <td>{formatDateTime(r.checkInTime)}</td>
                      <td>{formatDateTime(r.checkOutTime)}</td>
                      <td>{r.serviceType || '\u2014'}</td>
                      <td>{r.totalHours ?? '\u2014'}</td>
                      <td><span className={`wq-badge wq-badge-${(r.status || '').toLowerCase()}`}>{r.status || '\u2014'}</span></td>
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
