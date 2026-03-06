import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as waiversApi from '../api/waiversApi';
import './WorkQueues.css';

export const WaiversPage = () => {
  const navigate = useNavigate();
  const { setBreadcrumbs } = useBreadcrumbs();

  const [activeTab, setActiveTab] = useState('active');
  const [waivers, setWaivers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchParams, setSearchParams] = useState({ recipientId: '', providerId: '', countyCode: '' });
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Waivers' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        let data;
        switch (activeTab) {
          case 'active': data = await waiversApi.getActiveWaivers(); break;
          case 'pending': data = await waiversApi.getPendingWaivers(); break;
          case 'county-review': data = await waiversApi.getPendingCountyReview(); break;
          case 'expiring': data = await waiversApi.getExpiringWaivers(); break;
          default: data = [];
        }
        const list = Array.isArray(data) ? data : (data?.content || data?.items || []);
        setWaivers(list);
      } catch (err) {
        console.warn('[Waivers] Error:', err?.message);
        setWaivers([]);
      } finally {
        setLoading(false);
      }
    };
    if (activeTab !== 'search') load();
  }, [activeTab]);

  const handleSearch = async () => {
    setSearching(true);
    try {
      const params = {};
      if (searchParams.recipientId) params.recipientId = searchParams.recipientId;
      if (searchParams.providerId) params.providerId = searchParams.providerId;
      if (searchParams.countyCode) params.countyCode = searchParams.countyCode;
      const data = await waiversApi.searchWaivers(params);
      const list = Array.isArray(data) ? data : (data?.content || data?.items || []);
      setSearchResults(list);
    } catch (err) {
      console.warn('[Waivers] Search error:', err?.message);
      setSearchResults([]);
    } finally {
      setSearching(false);
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '\u2014';

  const renderTable = (list) => (
    <table className="wq-table">
      <thead>
        <tr><th>ID</th><th>Recipient</th><th>Provider</th><th>County</th><th>Status</th><th>Effective Date</th><th>Expiry Date</th></tr>
      </thead>
      <tbody>
        {list.map(w => (
          <tr key={w.id || w.waiverId} className="wq-clickable-row" onClick={() => navigate(`/waivers/${w.id || w.waiverId}`)}>
            <td>{w.id || w.waiverId}</td>
            <td>{w.recipientName || w.recipientId || '\u2014'}</td>
            <td>{w.providerName || w.providerId || '\u2014'}</td>
            <td>{w.countyCode || '\u2014'}</td>
            <td><span className={`wq-badge wq-badge-${(w.status || '').toLowerCase().replace(/_/g, '-')}`}>{w.status || '\u2014'}</span></td>
            <td>{formatDate(w.effectiveDate)}</td>
            <td>{formatDate(w.expirationDate)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Recipient Waivers</h2>
        <button className="wq-btn wq-btn-primary" onClick={() => navigate('/waivers/new')}>Initiate Waiver</button>
      </div>

      <div className="wq-tabs">
        {['active', 'pending', 'county-review', 'expiring', 'search'].map(tab => (
          <button key={tab} className={`wq-tab ${activeTab === tab ? 'active' : ''}`} onClick={() => setActiveTab(tab)}>
            {tab === 'county-review' ? 'County Review' : tab.charAt(0).toUpperCase() + tab.slice(1)}
          </button>
        ))}
      </div>

      {activeTab === 'search' ? (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Search Waivers</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field"><label>Recipient ID</label><input type="text" value={searchParams.recipientId} onChange={e => setSearchParams(p => ({ ...p, recipientId: e.target.value }))} /></div>
                <div className="wq-form-field"><label>Provider ID</label><input type="text" value={searchParams.providerId} onChange={e => setSearchParams(p => ({ ...p, providerId: e.target.value }))} /></div>
                <div className="wq-form-field"><label>County Code</label><input type="text" value={searchParams.countyCode} onChange={e => setSearchParams(p => ({ ...p, countyCode: e.target.value }))} /></div>
              </div>
              <div className="wq-search-actions">
                <button className="wq-btn wq-btn-primary" onClick={handleSearch} disabled={searching}>{searching ? 'Searching...' : 'Search'}</button>
              </div>
            </div>
          </div>
          {searchResults.length > 0 && (
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Results ({searchResults.length})</h4></div>
              <div className="wq-panel-body" style={{ padding: 0 }}>{renderTable(searchResults)}</div>
            </div>
          )}
        </>
      ) : (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>{activeTab.charAt(0).toUpperCase() + activeTab.slice(1)} Waivers ({waivers.length})</h4></div>
          <div className="wq-panel-body" style={{ padding: 0 }}>
            {loading ? (
              <p style={{ padding: '1rem', color: '#888' }}>Loading...</p>
            ) : waivers.length === 0 ? (
              <p style={{ padding: '1rem', color: '#888' }}>No waivers found.</p>
            ) : renderTable(waivers)}
          </div>
        </div>
      )}
    </div>
  );
};
