import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const DirectDepositPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [batches, setBatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Payments', path: '/payments/timesheets' }, { label: 'Direct Deposit' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await http.get('/payments/direct-deposit/batches');
        const list = Array.isArray(res.data) ? res.data : (res.data?.content || []);
        setBatches(list);
      } catch (err) {
        console.warn('[DirectDeposit] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleCreateBatch = async () => {
    setError('');
    try {
      await http.post('/payments/direct-deposit/batches', { batchType: 'REGULAR' });
      const res = await http.get('/payments/direct-deposit/batches');
      setBatches(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Failed to create batch: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    }
  };

  const handleProcessBatch = async (batchId) => {
    setError('');
    try {
      await http.post(`/payments/direct-deposit/batches/${batchId}/process`);
      const res = await http.get('/payments/direct-deposit/batches');
      setBatches(Array.isArray(res.data) ? res.data : (res.data?.content || []));
    } catch (err) {
      setError('Processing failed: ' + (err?.message || 'Unknown error'));
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleString() : '\u2014';
  const formatAmount = (a) => a != null ? `$${Number(a).toFixed(2)}` : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Direct Deposit</h2>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="wq-btn wq-btn-primary" onClick={handleCreateBatch}>Create New Batch</button>
          <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back to Payments</button>
        </div>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Direct Deposit Batches</h4></div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {loading ? (
            <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
          ) : batches.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No direct deposit batches found.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr><th>Batch ID</th><th>Type</th><th>Records</th><th>Total Amount</th><th>Status</th><th>Created</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {batches.map(b => (
                  <tr key={b.id || b.batchId}>
                    <td>{b.id || b.batchId}</td>
                    <td>{b.batchType || '\u2014'}</td>
                    <td>{b.recordCount ?? '\u2014'}</td>
                    <td>{formatAmount(b.totalAmount)}</td>
                    <td><span className={`wq-badge wq-badge-${(b.status || '').toLowerCase()}`}>{b.status}</span></td>
                    <td>{formatDate(b.createdAt)}</td>
                    <td>
                      {b.status === 'PENDING' && (
                        <button className="wq-btn wq-btn-primary" style={{ fontSize: '0.75rem', padding: '0.2rem 0.5rem' }}
                          onClick={() => handleProcessBatch(b.id || b.batchId)}>
                          Process
                        </button>
                      )}
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
