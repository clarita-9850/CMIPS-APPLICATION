import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import http from '../api/httpClient';
import './WorkQueues.css';

export const LiveInProviderPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [activeTab, setActiveTab] = useState('search');
  const [providerId, setProviderId] = useState('');
  const [providerName, setProviderName] = useState('');
  const [countyCode, setCountyCode] = useState('');
  const [results, setResults] = useState([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Self-certification form
  const [certForm, setCertForm] = useState({
    providerId: '', recipientName: '', caseNumber: '', certificationDate: '', hoursPerWeek: ''
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Providers', path: '/providers' }, { label: 'Live-In Provider Certification' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const handleSearch = () => {
    setLoading(true);
    setSearched(true);
    const params = {};
    if (providerId) params.providerId = providerId;
    if (providerName) params.name = providerName;
    if (countyCode) params.countyCode = countyCode;
    const qs = new URLSearchParams(params).toString();
    http.get(`/providers/search?${qs}`)
      .then(res => {
        const d = res?.data;
        setResults(Array.isArray(d) ? d : (d?.content || d?.providers || []));
      })
      .catch(() => setResults([]))
      .finally(() => setLoading(false));
  };

  const handleReset = () => {
    setProviderId('');
    setProviderName('');
    setCountyCode('');
    setResults([]);
    setSearched(false);
  };

  const handleSubmitCertification = async () => {
    if (!certForm.providerId || !certForm.caseNumber) {
      setError('Provider ID and Case Number are required.');
      return;
    }
    setError('');
    setSuccess('');
    setSubmitting(true);
    try {
      await http.post('/providers/live-in-certification', {
        providerId: certForm.providerId,
        recipientName: certForm.recipientName,
        caseNumber: certForm.caseNumber,
        certificationDate: certForm.certificationDate || new Date().toISOString().split('T')[0],
        hoursPerWeek: certForm.hoursPerWeek ? parseFloat(certForm.hoursPerWeek) : null
      });
      setSuccess('Live-in self-certification submitted successfully.');
      setCertForm({ providerId: '', recipientName: '', caseNumber: '', certificationDate: '', hoursPerWeek: '' });
    } catch (err) {
      setError('Submission failed: ' + (err?.message || 'Unknown error'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Live-In Provider Self-Certification</h2>
        <button className="wq-btn wq-btn-outline" onClick={() => navigate('/providers')}>Back to Providers</button>
      </div>

      {error && <div style={{ background: '#fff5f5', border: '1px solid #fc8181', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#c53030', fontSize: '0.875rem' }}>{error}</div>}
      {success && <div style={{ background: '#f0fff4', border: '1px solid #68d391', padding: '0.5rem 1rem', borderRadius: '4px', marginBottom: '1rem', color: '#276749', fontSize: '0.875rem' }}>{success}</div>}

      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'search' ? 'active' : ''}`} onClick={() => setActiveTab('search')}>Search Providers</button>
        <button className={`wq-tab ${activeTab === 'certify' ? 'active' : ''}`} onClick={() => setActiveTab('certify')}>New Certification</button>
      </div>

      {activeTab === 'search' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Search Live-In Providers</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field">
                  <label>Provider ID</label>
                  <input type="text" value={providerId} onChange={e => setProviderId(e.target.value)} placeholder="Enter provider ID" />
                </div>
                <div className="wq-form-field">
                  <label>Provider Name</label>
                  <input type="text" value={providerName} onChange={e => setProviderName(e.target.value)} placeholder="Last name, First name" />
                </div>
                <div className="wq-form-field">
                  <label>County Code</label>
                  <input type="text" value={countyCode} onChange={e => setCountyCode(e.target.value)} placeholder="e.g. 19" />
                </div>
              </div>
              <div className="wq-search-actions">
                <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={loading}>
                  {loading ? 'Searching...' : 'Search'}
                </button>
                <button className="wq-btn wq-btn-outline" onClick={handleReset}>Reset</button>
              </div>
            </div>
          </div>

          {searched && (
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Results ({results.length})</h4></div>
              <div className="wq-panel-body" style={{ padding: 0 }}>
                {loading ? (
                  <p style={{ padding: '1rem', color: '#888' }}>Searching...</p>
                ) : results.length === 0 ? (
                  <p style={{ padding: '1rem', color: '#888' }}>No providers found.</p>
                ) : (
                  <table className="wq-table">
                    <thead>
                      <tr><th>Provider ID</th><th>Name</th><th>County</th><th>Status</th><th>Enrollment Date</th></tr>
                    </thead>
                    <tbody>
                      {results.map((p, i) => (
                        <tr key={i} className="wq-clickable-row" onClick={() => navigate(`/providers/${p.id || p.providerId}`)}>
                          <td>{p.providerId || p.id || '\u2014'}</td>
                          <td>{[p.firstName, p.lastName].filter(Boolean).join(' ') || p.name || '\u2014'}</td>
                          <td>{p.countyCode || '\u2014'}</td>
                          <td><span className={`wq-badge wq-badge-${(p.status || '').toLowerCase()}`}>{p.status || '\u2014'}</span></td>
                          <td>{p.enrollmentDate || p.createdAt || '\u2014'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </div>
          )}
        </>
      )}

      {activeTab === 'certify' && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>Submit Live-In Self-Certification</h4></div>
          <div className="wq-panel-body">
            <div className="wq-search-grid">
              <div className="wq-form-field">
                <label>Provider ID *</label>
                <input type="text" value={certForm.providerId} onChange={e => setCertForm(p => ({ ...p, providerId: e.target.value }))} placeholder="Enter provider ID" />
              </div>
              <div className="wq-form-field">
                <label>Recipient Name</label>
                <input type="text" value={certForm.recipientName} onChange={e => setCertForm(p => ({ ...p, recipientName: e.target.value }))} placeholder="Recipient name" />
              </div>
              <div className="wq-form-field">
                <label>Case Number *</label>
                <input type="text" value={certForm.caseNumber} onChange={e => setCertForm(p => ({ ...p, caseNumber: e.target.value }))} placeholder="Case number" />
              </div>
              <div className="wq-form-field">
                <label>Certification Date</label>
                <input type="date" value={certForm.certificationDate} onChange={e => setCertForm(p => ({ ...p, certificationDate: e.target.value }))} />
              </div>
              <div className="wq-form-field">
                <label>Hours Per Week</label>
                <input type="number" value={certForm.hoursPerWeek} onChange={e => setCertForm(p => ({ ...p, hoursPerWeek: e.target.value }))} placeholder="e.g. 40" />
              </div>
            </div>
            <div className="wq-search-actions">
              <button className="wq-btn wq-btn-primary" onClick={handleSubmitCertification} disabled={submitting}>
                {submitting ? 'Submitting...' : 'Submit Certification'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
