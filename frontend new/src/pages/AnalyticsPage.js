import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import * as analyticsApi from '../api/analyticsApi';
import './WorkQueues.css';

export const AnalyticsPage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();
  const [metrics, setMetrics] = useState(null);
  const [demographics, setDemographics] = useState({ gender: [], ethnicity: [], age: [] });
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview');
  const [adhocParams, setAdhocParams] = useState({ county: '', dateFrom: '', dateTo: '' });
  const [adhocData, setAdhocData] = useState(null);
  const [adhocLoading, setAdhocLoading] = useState(false);

  useEffect(() => {
    setBreadcrumbs([{ label: 'Analytics' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  useEffect(() => {
    const load = async () => {
      try {
        const [m, g, e, a] = await Promise.all([
          analyticsApi.getRealtimeMetrics().catch(() => null),
          analyticsApi.getGenderDemographics().catch(() => []),
          analyticsApi.getEthnicityDemographics().catch(() => []),
          analyticsApi.getAgeDemographics().catch(() => [])
        ]);
        setMetrics(m);
        setDemographics({
          gender: Array.isArray(g) ? g : [],
          ethnicity: Array.isArray(e) ? e : [],
          age: Array.isArray(a) ? a : []
        });
      } catch (err) {
        console.warn('[Analytics] Error:', err?.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const handleAdhocQuery = async () => {
    setAdhocLoading(true);
    try {
      const params = {};
      if (adhocParams.county) params.county = adhocParams.county;
      if (adhocParams.dateFrom) params.dateFrom = adhocParams.dateFrom;
      if (adhocParams.dateTo) params.dateTo = adhocParams.dateTo;
      const data = await analyticsApi.getAdhocData(params);
      setAdhocData(data);
    } catch (err) {
      console.warn('[Analytics] Adhoc error:', err?.message);
    } finally {
      setAdhocLoading(false);
    }
  };

  const renderStat = (label, value) => (
    <div className="workspace-stat-card">
      <div className="stat-number">{value ?? '\u2014'}</div>
      <div className="stat-label">{label}</div>
    </div>
  );

  const renderDemoTable = (title, data) => (
    <div className="wq-panel">
      <div className="wq-panel-header"><h4>{title}</h4></div>
      <div className="wq-panel-body" style={{ padding: 0 }}>
        {data.length === 0 ? (
          <p style={{ padding: '1rem', color: '#888' }}>No data available.</p>
        ) : (
          <table className="wq-table">
            <thead><tr><th>Category</th><th>Count</th><th>Percentage</th></tr></thead>
            <tbody>
              {data.map((d, i) => (
                <tr key={i}>
                  <td>{d.category || d.label || d.name || '\u2014'}</td>
                  <td>{d.count ?? d.value ?? '\u2014'}</td>
                  <td>{d.percentage != null ? `${d.percentage}%` : '\u2014'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );

  if (loading) return <div className="wq-page"><p>Loading analytics...</p></div>;

  return (
    <div className="wq-page">
      <div className="wq-page-header"><h2>Analytics & Reports</h2></div>

      {/* Real-time Metrics */}
      {metrics && (
        <div className="workspace-stats-row">
          {renderStat('Active Cases', metrics.activeCases)}
          {renderStat('Active Providers', metrics.activeProviders)}
          {renderStat('Active Recipients', metrics.activeRecipients)}
          {renderStat('Pending Tasks', metrics.pendingTasks)}
          {renderStat('Open Applications', metrics.openApplications)}
        </div>
      )}

      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>Overview</button>
        <button className={`wq-tab ${activeTab === 'demographics' ? 'active' : ''}`} onClick={() => setActiveTab('demographics')}>Demographics</button>
        <button className={`wq-tab ${activeTab === 'adhoc' ? 'active' : ''}`} onClick={() => setActiveTab('adhoc')}>Ad-Hoc Query</button>
      </div>

      {activeTab === 'overview' && metrics && (
        <div className="wq-panel">
          <div className="wq-panel-header"><h4>System Metrics</h4></div>
          <div className="wq-panel-body">
            <pre style={{ fontSize: '0.85rem', overflow: 'auto', background: '#f7fafc', padding: '1rem', borderRadius: '4px', maxHeight: '400px' }}>
              {JSON.stringify(metrics, null, 2)}
            </pre>
          </div>
        </div>
      )}

      {activeTab === 'demographics' && (
        <div className="workspace-columns">
          {renderDemoTable('Gender Distribution', demographics.gender)}
          {renderDemoTable('Ethnicity Distribution', demographics.ethnicity)}
          {renderDemoTable('Age Distribution', demographics.age)}
        </div>
      )}

      {activeTab === 'adhoc' && (
        <>
          <div className="wq-panel">
            <div className="wq-panel-header"><h4>Ad-Hoc Data Query</h4></div>
            <div className="wq-panel-body">
              <div className="wq-search-grid">
                <div className="wq-form-field"><label>County</label><input type="text" value={adhocParams.county} onChange={e => setAdhocParams(p => ({ ...p, county: e.target.value }))} /></div>
                <div className="wq-form-field"><label>Date From</label><input type="date" value={adhocParams.dateFrom} onChange={e => setAdhocParams(p => ({ ...p, dateFrom: e.target.value }))} /></div>
                <div className="wq-form-field"><label>Date To</label><input type="date" value={adhocParams.dateTo} onChange={e => setAdhocParams(p => ({ ...p, dateTo: e.target.value }))} /></div>
              </div>
              <div className="wq-search-actions">
                <button className="wq-btn wq-btn-primary" onClick={handleAdhocQuery} disabled={adhocLoading}>
                  {adhocLoading ? 'Running...' : 'Run Query'}
                </button>
              </div>
            </div>
          </div>
          {adhocData && (
            <div className="wq-panel">
              <div className="wq-panel-header"><h4>Query Results</h4></div>
              <div className="wq-panel-body">
                <pre style={{ fontSize: '0.85rem', overflow: 'auto', background: '#f7fafc', padding: '1rem', borderRadius: '4px', maxHeight: '400px' }}>
                  {JSON.stringify(adhocData, null, 2)}
                </pre>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};
