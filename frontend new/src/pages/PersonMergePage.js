import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const PersonMergePage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();
  const [searchSSN, setSearchSSN] = useState('');
  const [matches, setMatches] = useState([]);
  const [selected, setSelected] = useState([]);
  const [loading, setLoading] = useState(false);
  const [merging, setMerging] = useState(false);
  const [error, setError] = useState('');

  React.useEffect(() => {
    setBreadcrumbs([{ label: 'Recipients', path: '/recipients' }, { label: 'Merge Duplicate SSN' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = async () => {
    if (!searchSSN.trim()) { setError('Enter an SSN to search.'); return; }
    setError('');
    setLoading(true);
    try {
      const res = await http.post('/person/search', { ssn: searchSSN });
      const results = res.data?.results || [];
      setMatches(results);
      setSelected([]);
      if (results.length < 2) setError('Need at least 2 matching records to merge.');
    } catch (err) {
      setError('Search failed: ' + (err?.message || 'Unknown error'));
      setMatches([]);
    } finally {
      setLoading(false);
    }
  };

  const toggleSelect = (id) => {
    setSelected(prev => prev.includes(id) ? prev.filter(x => x !== id) : prev.length < 2 ? [...prev, id] : prev);
  };

  const handleMerge = async () => {
    if (selected.length !== 2) { setError('Select exactly 2 records to merge.'); return; }
    setMerging(true);
    setError('');
    try {
      await http.post('/persons/merge', { primaryId: selected[0], secondaryId: selected[1] });
      navigate(`/recipients/${selected[0]}`);
    } catch (err) {
      setError('Merge failed: ' + (err?.response?.data?.message || err?.message || 'Unknown error'));
    } finally {
      setMerging(false);
    }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Merge Duplicate SSN</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/recipients')}>Back</button>
      </div>

      {error && (
        <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>
          {error}
        </div>
      )}

      <div className="wq-panel">
        <div className="wq-panel-header"><h4>Search by SSN</h4></div>
        <div className="wq-panel-body">
          <div className="wq-search-grid">
            <div className="wq-form-field">
              <label>SSN</label>
              <input type="text" value={searchSSN} onChange={e => setSearchSSN(e.target.value)} placeholder="###-##-####" />
            </div>
          </div>
          <div className="wq-search-actions">
            <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
              {loading ? 'Searching...' : 'Search'}
            </button>
          </div>
        </div>
      </div>

      {matches.length > 0 && (
        <div className="wq-panel">
          <div className="wq-panel-header">
            <h4>Matching Records ({matches.length})</h4>
            <button className="wq-btn wq-btn-primary" onClick={handleMerge} disabled={selected.length !== 2 || merging}>
              {merging ? 'Merging...' : `Merge Selected (${selected.length}/2)`}
            </button>
          </div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            <table className="wq-table">
              <thead>
                <tr><th>Select</th><th>ID</th><th>Name</th><th>DOB</th><th>SSN</th><th>County</th></tr>
              </thead>
              <tbody>
                {matches.map(p => {
                  const pid = p.personId || p.id;
                  return (
                    <tr key={pid}>
                      <td><input type="checkbox" checked={selected.includes(pid)} onChange={() => toggleSelect(pid)} /></td>
                      <td>{pid}</td>
                      <td>{p.firstName} {p.lastName}</td>
                      <td>{p.dateOfBirth || 'N/A'}</td>
                      <td>{p.maskedSsn || '***-**-****'}</td>
                      <td>{p.countyCode || 'N/A'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};
