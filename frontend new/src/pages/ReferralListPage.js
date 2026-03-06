import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import * as referralsApi from '../api/referralsApi';
import './WorkQueues.css';

const STATUS_COLORS = {
  OPEN:         { bg: '#bee3f8', color: '#2b6cb0' },
  PENDING:      { bg: '#fefcbf', color: '#744210' },
  IN_PROGRESS:  { bg: '#feebc8', color: '#c05621' },
  CONVERTED:    { bg: '#c6f6d5', color: '#276749' },
  CLOSED:       { bg: '#e2e8f0', color: '#4a5568' },
};

const PRIORITY_COLORS = {
  URGENT:   { bg: '#fed7d7', color: '#c53030' },
  HIGH:     { bg: '#feebc8', color: '#c05621' },
  NORMAL:   { bg: '#bee3f8', color: '#2b6cb0' },
  LOW:      { bg: '#e2e8f0', color: '#4a5568' },
};

const Badge = ({ value, map }) => {
  const style = map[value] || { bg: '#e2e8f0', color: '#4a5568' };
  return (
    <span style={{
      background: style.bg, color: style.color,
      padding: '2px 8px', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600
    }}>{value || '—'}</span>
  );
};

export const ReferralListPage = () => {
  const navigate = useNavigate();

  const [referrals, setReferrals] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Filter state
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [countyCode, setCountyCode] = useState('');
  const [source, setSource] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const doSearch = () => {
    setLoading(true);
    setError('');
    const params = {};
    if (status)     params.status = status;
    if (priority)   params.priority = priority;
    if (countyCode) params.countyCode = countyCode;
    if (source)     params.source = source;
    if (startDate)  params.startDate = startDate;
    if (endDate)    params.endDate = endDate;

    referralsApi.searchReferrals(params)
      .then(data => setReferrals(Array.isArray(data) ? data : []))
      .catch(() => setError('Failed to load referrals.'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    doSearch();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleClear = () => {
    setStatus(''); setPriority(''); setCountyCode('');
    setSource(''); setStartDate(''); setEndDate('');
    setReferrals([]);
    doSearch();
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Referral Management</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => navigate('/persons/referral/new')}>
          New Referral
        </button>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search / Filter</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>Status</label>
              <select value={status} onChange={e => setStatus(e.target.value)}>
                <option value="">All</option>
                <option value="OPEN">Open</option>
                <option value="PENDING">Pending</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="CONVERTED">Converted</option>
                <option value="CLOSED">Closed</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Priority</label>
              <select value={priority} onChange={e => setPriority(e.target.value)}>
                <option value="">All</option>
                <option value="URGENT">Urgent</option>
                <option value="HIGH">High</option>
                <option value="NORMAL">Normal</option>
                <option value="LOW">Low</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>County</label>
              <input type="text" value={countyCode} onChange={e => setCountyCode(e.target.value)}
                placeholder="e.g. Sacramento" />
            </div>
            <div className="wq-form-field">
              <label>Source</label>
              <select value={source} onChange={e => setSource(e.target.value)}>
                <option value="">All</option>
                <option value="SELF">Self</option>
                <option value="FAMILY">Family</option>
                <option value="HOSPITAL">Hospital</option>
                <option value="SOCIAL_WORKER">Social Worker</option>
                <option value="PHONE">Phone</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="wq-form-field">
              <label>Referral Date From</label>
              <input type="date" value={startDate} onChange={e => setStartDate(e.target.value)} />
            </div>
            <div className="wq-form-field">
              <label>Referral Date To</label>
              <input type="date" value={endDate} onChange={e => setEndDate(e.target.value)} />
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={doSearch} disabled={loading}>
              {loading ? 'Searching…' : 'Search'}
            </button>
            <button className="wq-btn wq-btn-secondary" onClick={handleClear}>Clear</button>
          </div>
        </div>
      </div>

      {error && <div className="wq-alert wq-alert-error">{error}</div>}

      <div className="wq-panel">
        <div className="wq-panel-header">
          <h4>Results {referrals.length > 0 && <span style={{ fontWeight: 400, fontSize: '0.9rem' }}>({referrals.length} records)</span>}</h4>
        </div>
        <div className="wq-panel-body">
          {loading ? (
            <div className="wq-loading">Loading…</div>
          ) : referrals.length === 0 ? (
            <div className="wq-empty-state">No referrals found. Adjust filters and search again.</div>
          ) : (
            <div className="wq-table-wrapper">
              <table className="wq-table">
                <thead>
                  <tr>
                    <th>Referral ID</th>
                    <th>Recipient Name</th>
                    <th>County</th>
                    <th>Referral Date</th>
                    <th>Status</th>
                    <th>Priority</th>
                    <th>Assigned Worker</th>
                    <th>Follow-Up Date</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {referrals.map(r => (
                    <tr key={r.id} className="wq-table-row-hover">
                      <td><code style={{ fontSize: '0.8rem' }}>{r.id}</code></td>
                      <td>{r.potentialRecipientName || '—'}</td>
                      <td>{r.countyName || r.countyCode || '—'}</td>
                      <td>{r.referralDate || '—'}</td>
                      <td><Badge value={r.status} map={STATUS_COLORS} /></td>
                      <td><Badge value={r.priority} map={PRIORITY_COLORS} /></td>
                      <td>{r.assignedWorkerName || '—'}</td>
                      <td>{r.followUpDate || '—'}</td>
                      <td>
                        <button className="wq-btn wq-btn-sm wq-btn-secondary"
                          onClick={() => navigate(`/referrals/${r.id}/edit`)}>
                          Edit
                        </button>
                        {r.recipientId && (
                          <button className="wq-btn wq-btn-sm wq-btn-secondary"
                            style={{ marginLeft: '4px' }}
                            onClick={() => navigate(`/recipients/${r.recipientId}`)}>
                            Person
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
