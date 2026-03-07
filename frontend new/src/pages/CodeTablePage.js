import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

const base = '/timesheet-code-tables';

export const CodeTablePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [types, setTypes] = useState([]);
  const [selectedType, setSelectedType] = useState('');
  const [entries, setEntries] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showInactive, setShowInactive] = useState(false);
  const [addModal, setAddModal] = useState(false);
  const [addForm, setAddForm] = useState({ code: '', description: '', displayOrder: 0, active: true, metadata: '' });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    setBreadcrumbs([
      { label: 'Payments', path: '/payments/timesheets' },
      { label: 'Code Tables' }
    ]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    http.get(`${base}/types`).then(r => setTypes(r.data)).catch(() => {});
  }, []);

  useEffect(() => {
    if (!selectedType) { setEntries([]); return; }
    setLoading(true);
    http.get(`${base}/${selectedType}?activeOnly=${!showInactive}`)
      .then(r => setEntries(r.data))
      .catch(() => setEntries([]))
      .finally(() => setLoading(false));
  }, [selectedType, showInactive]);

  const handleAdd = async () => {
    setError('');
    if (!addForm.code.trim() || !addForm.description.trim()) {
      setError('Code and Description are required.'); return;
    }
    try {
      await http.post(base, { tableType: selectedType, ...addForm });
      setAddModal(false);
      setAddForm({ code: '', description: '', displayOrder: 0, active: true, metadata: '' });
      setSuccess('Entry added.');
      // Reload
      const r = await http.get(`${base}/${selectedType}?activeOnly=${!showInactive}`);
      setEntries(r.data);
    } catch (err) {
      setError('Failed: ' + (err?.response?.data?.error || err.message));
    }
  };

  // Group types into categories for display
  const typeCategories = {
    'Timesheet': types.filter(t => t.startsWith('TIMESHEET_')),
    'Travel Claim': types.filter(t => t.startsWith('TRAVEL_')),
    'Sick Leave': types.filter(t => t.startsWith('SICK_')),
    'BVI': types.filter(t => t.startsWith('BVI_')),
    'TTS': types.filter(t => t.startsWith('TTS_')),
    'Other': types.filter(t =>
      !t.startsWith('TIMESHEET_') && !t.startsWith('TRAVEL_') && !t.startsWith('SICK_') &&
      !t.startsWith('BVI_') && !t.startsWith('TTS_'))
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Timesheet Code Tables</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/payments/timesheets')}>Back</button>
      </div>

      {success && (
        <div style={{ padding: '0.5rem', background: '#f0fdf4', border: '1px solid #86efac', borderRadius: '4px', color: '#16a34a', marginBottom: '0.75rem', fontSize: '0.85rem', display: 'flex', justifyContent: 'space-between' }}>
          <span>{success}</span>
          <button onClick={() => setSuccess('')} style={{ background: 'none', border: 'none', cursor: 'pointer' }}>&times;</button>
        </div>
      )}

      <div style={{ display: 'flex', gap: '1rem' }}>
        {/* Left: Table Type Selector */}
        <div style={{ width: '280px', flexShrink: 0 }}>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Table Types ({types.length})</h4></div>
            <div className="wq-panel-body" style={{ padding: 0, maxHeight: '600px', overflowY: 'auto' }}>
              {Object.entries(typeCategories).filter(([, items]) => items.length > 0).map(([cat, items]) => (
                <div key={cat}>
                  <div style={{ padding: '0.4rem 0.75rem', background: '#f3f4f6', fontSize: '0.7rem', fontWeight: 700, color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    {cat}
                  </div>
                  {items.map(t => (
                    <div key={t}
                      onClick={() => setSelectedType(t)}
                      style={{
                        padding: '0.5rem 0.75rem', cursor: 'pointer', fontSize: '0.8rem',
                        background: selectedType === t ? '#dbeafe' : 'transparent',
                        color: selectedType === t ? '#1e40af' : '#374151',
                        fontWeight: selectedType === t ? 600 : 400,
                        borderBottom: '1px solid #f3f4f6'
                      }}>
                      {t.replace(/_/g, ' ')}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Right: Entries Table */}
        <div style={{ flex: 1 }}>
          <div className="wq-panel">
            <div className="wq-panel-header">
              <h4>{selectedType ? selectedType.replace(/_/g, ' ') : 'Select a Table Type'} ({entries.length})</h4>
              <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                <label style={{ fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                  <input type="checkbox" checked={showInactive} onChange={e => setShowInactive(e.target.checked)} />
                  Show Inactive
                </label>
                {selectedType && (
                  <button className="wq-btn wq-btn-primary" style={{ padding: '0.2rem 0.6rem', fontSize: '0.8rem' }}
                    onClick={() => setAddModal(true)}>Add Entry</button>
                )}
              </div>
            </div>
            <div className="wq-panel-body" style={{ padding: 0 }}>
              {!selectedType ? (
                <div style={{ padding: '3rem', textAlign: 'center', color: '#9ca3af' }}>Select a table type from the left panel</div>
              ) : loading ? (
                <div style={{ padding: '2rem', textAlign: 'center' }}>Loading...</div>
              ) : (
                <table className="wq-table" style={{ fontSize: '0.8rem' }}>
                  <thead>
                    <tr>
                      <th>Order</th>
                      <th>Code</th>
                      <th>Description</th>
                      <th>Active</th>
                      <th>Effective</th>
                      <th>Expiration</th>
                      <th>Parent</th>
                      <th>Metadata</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.length === 0 ? (
                      <tr><td colSpan={8} style={{ textAlign: 'center', padding: '2rem', color: '#9ca3af' }}>No entries</td></tr>
                    ) : entries.map(e => (
                      <tr key={e.id}>
                        <td>{e.displayOrder}</td>
                        <td style={{ fontWeight: 600, fontFamily: 'monospace' }}>{e.code}</td>
                        <td>{e.description}</td>
                        <td>
                          <span style={{
                            padding: '1px 6px', borderRadius: '8px', fontSize: '0.7rem', fontWeight: 600,
                            background: e.active ? '#d1fae5' : '#fee2e2',
                            color: e.active ? '#065f46' : '#991b1b'
                          }}>
                            {e.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td style={{ fontSize: '0.75rem' }}>{e.effectiveDate || '\u2014'}</td>
                        <td style={{ fontSize: '0.75rem' }}>{e.expirationDate || '\u2014'}</td>
                        <td style={{ fontSize: '0.75rem' }}>{e.parentCode || '\u2014'}</td>
                        <td style={{ fontSize: '0.75rem', maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis' }}>{e.metadata || '\u2014'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Add Entry Modal */}
      {addModal && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: '8px', padding: '1.5rem', width: '420px', boxShadow: '0 8px 30px rgba(0,0,0,0.2)' }}>
            <h4 style={{ marginBottom: '1rem' }}>Add Entry to {selectedType.replace(/_/g, ' ')}</h4>
            {error && <div style={{ padding: '0.5rem', background: '#fef2f2', border: '1px solid #fca5a5', borderRadius: '4px', color: '#dc2626', marginBottom: '0.75rem', fontSize: '0.85rem' }}>{error}</div>}
            <div className="wq-search-grid">
              <div className="wq-form-field"><label>Code *</label><input type="text" value={addForm.code} onChange={e => setAddForm(f => ({ ...f, code: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Description *</label><input type="text" value={addForm.description} onChange={e => setAddForm(f => ({ ...f, description: e.target.value }))} /></div>
              <div className="wq-form-field"><label>Display Order</label><input type="number" value={addForm.displayOrder} onChange={e => setAddForm(f => ({ ...f, displayOrder: parseInt(e.target.value) || 0 }))} /></div>
              <div className="wq-form-field"><label>Metadata</label><input type="text" value={addForm.metadata} onChange={e => setAddForm(f => ({ ...f, metadata: e.target.value }))} placeholder="Optional JSON" /></div>
            </div>
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button className="wq-btn wq-btn-outline" onClick={() => { setAddModal(false); setError(''); }}>Cancel</button>
              <button className="wq-btn wq-btn-primary" onClick={handleAdd}>Add</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
