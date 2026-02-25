import React, { useState, useEffect } from 'react';
import { useBreadcrumbs } from '../lib/BreadcrumbContext';
import './WorkQueues.css';

const WAGE_RATE_DATA = {
  publicAuthority: [
    { county: 'Alameda', hourlyRate: 19.00, overtimeRate: 28.50, effectiveDate: '2024-01-01' },
    { county: 'Contra Costa', hourlyRate: 18.50, overtimeRate: 27.75, effectiveDate: '2024-01-01' },
    { county: 'Fresno', hourlyRate: 16.50, overtimeRate: 24.75, effectiveDate: '2024-01-01' },
    { county: 'Kern', hourlyRate: 16.00, overtimeRate: 24.00, effectiveDate: '2024-01-01' },
    { county: 'Los Angeles', hourlyRate: 17.50, overtimeRate: 26.25, effectiveDate: '2024-01-01' },
    { county: 'Orange', hourlyRate: 17.00, overtimeRate: 25.50, effectiveDate: '2024-01-01' },
    { county: 'Riverside', hourlyRate: 16.50, overtimeRate: 24.75, effectiveDate: '2024-01-01' },
    { county: 'Sacramento', hourlyRate: 17.00, overtimeRate: 25.50, effectiveDate: '2024-01-01' },
    { county: 'San Bernardino', hourlyRate: 16.50, overtimeRate: 24.75, effectiveDate: '2024-01-01' },
    { county: 'San Diego', hourlyRate: 17.50, overtimeRate: 26.25, effectiveDate: '2024-01-01' },
    { county: 'San Francisco', hourlyRate: 20.00, overtimeRate: 30.00, effectiveDate: '2024-01-01' },
    { county: 'Santa Clara', hourlyRate: 19.50, overtimeRate: 29.25, effectiveDate: '2024-01-01' },
    { county: 'Ventura', hourlyRate: 17.00, overtimeRate: 25.50, effectiveDate: '2024-01-01' }
  ],
  countyContractor: [
    { county: 'Alameda', hourlyRate: 22.00, effectiveDate: '2024-01-01', contractType: 'Standard' },
    { county: 'Los Angeles', hourlyRate: 21.50, effectiveDate: '2024-01-01', contractType: 'Standard' },
    { county: 'San Francisco', hourlyRate: 24.00, effectiveDate: '2024-01-01', contractType: 'Standard' },
    { county: 'Santa Clara', hourlyRate: 23.00, effectiveDate: '2024-01-01', contractType: 'Standard' },
    { county: 'San Diego', hourlyRate: 21.00, effectiveDate: '2024-01-01', contractType: 'Standard' }
  ]
};

export const WageRatePage = () => {
  const { setBreadcrumbs } = useBreadcrumbs();
  const [activeTab, setActiveTab] = useState('publicAuthority');
  const [filterCounty, setFilterCounty] = useState('');

  useEffect(() => {
    setBreadcrumbs([{ label: 'Wage Rate' }]);
    return () => setBreadcrumbs([]);
  }, [setBreadcrumbs]);

  const currentData = activeTab === 'publicAuthority' ? WAGE_RATE_DATA.publicAuthority : WAGE_RATE_DATA.countyContractor;
  const filteredData = filterCounty
    ? currentData.filter(r => r.county.toLowerCase().includes(filterCounty.toLowerCase()))
    : currentData;

  const formatCurrency = (val) => val != null ? `$${val.toFixed(2)}` : '\u2014';

  return (
    <div className="wq-page">
      <div className="wq-page-header">
        <h2>Wage Rate Schedule</h2>
      </div>

      <div className="workspace-stats-row">
        <div className="workspace-stat-card">
          <div className="stat-number">{WAGE_RATE_DATA.publicAuthority.length}</div>
          <div className="stat-label">Public Authority Counties</div>
        </div>
        <div className="workspace-stat-card">
          <div className="stat-number">{WAGE_RATE_DATA.countyContractor.length}</div>
          <div className="stat-label">County Contractor Rates</div>
        </div>
        <div className="workspace-stat-card">
          <div className="stat-number">$16.00</div>
          <div className="stat-label">Min Hourly Rate</div>
        </div>
        <div className="workspace-stat-card">
          <div className="stat-number">$24.00</div>
          <div className="stat-label">Max Hourly Rate</div>
        </div>
      </div>

      <div className="wq-tabs">
        <button className={`wq-tab ${activeTab === 'publicAuthority' ? 'active' : ''}`} onClick={() => setActiveTab('publicAuthority')}>Public Authority Wage Rates</button>
        <button className={`wq-tab ${activeTab === 'countyContractor' ? 'active' : ''}`} onClick={() => setActiveTab('countyContractor')}>County Contractor Rates</button>
      </div>

      <div className="wq-panel">
        <div className="wq-panel-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h4>{activeTab === 'publicAuthority' ? 'Public Authority' : 'County Contractor'} Wage Rates ({filteredData.length})</h4>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <input type="text" value={filterCounty} onChange={e => setFilterCounty(e.target.value)}
              placeholder="Filter by county..."
              style={{ padding: '0.3rem 0.5rem', border: '1px solid #cbd5e0', borderRadius: '4px', fontSize: '0.85rem' }} />
          </div>
        </div>
        <div className="wq-panel-body" style={{ padding: 0 }}>
          {filteredData.length === 0 ? (
            <p style={{ padding: '1rem', color: '#888' }}>No matching wage rates found.</p>
          ) : (
            <table className="wq-table">
              <thead>
                <tr>
                  <th>County</th>
                  <th>Hourly Rate</th>
                  {activeTab === 'publicAuthority' && <th>Overtime Rate</th>}
                  {activeTab === 'countyContractor' && <th>Contract Type</th>}
                  <th>Effective Date</th>
                </tr>
              </thead>
              <tbody>
                {filteredData.map((r, i) => (
                  <tr key={i}>
                    <td style={{ fontWeight: 500 }}>{r.county}</td>
                    <td>{formatCurrency(r.hourlyRate)}</td>
                    {activeTab === 'publicAuthority' && <td>{formatCurrency(r.overtimeRate)}</td>}
                    {activeTab === 'countyContractor' && <td>{r.contractType || '\u2014'}</td>}
                    <td>{r.effectiveDate || '\u2014'}</td>
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
