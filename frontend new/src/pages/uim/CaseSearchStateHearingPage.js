/**
 * CaseSearchStateHearingPage – conversion of Case_searchStateHearing.uim
 * Route: /case/search-state-hearing
 * "Find a State Hearing Case" shortcut from My Shortcuts.
 */

import React, { useState } from 'react';
import './UimPage.css';
import { mockStateHearingResults, mockCountyOptions } from './mockData';

export const CaseSearchStateHearingPage = () => {
  const [form, setForm] = useState({
    stateHearingStatus: '',
    county: '',
    fromDate: '',
    toDate: '',
  });
  const [searched, setSearched] = useState(false);

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setSearched(true);
    // TODO: call StateHearing.searchStateHearing via BFF API
    console.log('[TODO] Search state hearing:', form);
  };

  const handleReset = () => {
    setForm({ stateHearingStatus: '', county: '', fromDate: '', toDate: '' });
    setSearched(false);
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">
          Find a State Hearing Case
        </h1>

        <form onSubmit={handleSearch}>
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Search Criteria</h2>
            <div className="uim-cluster-body">
              <section className="uim-cluster" style={{ border: 'none', marginBottom: 0 }}>
                <h3 className="uim-cluster-title" style={{ fontSize: '0.9rem', background: 'var(--cdss-accent)' }}>General</h3>
                <div className="uim-cluster-body">
                  <div className="uim-form-grid">
                    <div className="uim-field">
                      <label>State Hearing Status</label>
                      <select name="stateHearingStatus" value={form.stateHearingStatus} onChange={handleChange}>
                        <option value="">-- Select --</option>
                        <option value="Scheduled">Scheduled</option>
                        <option value="Pending">Pending</option>
                        <option value="Completed">Completed</option>
                        <option value="Withdrawn">Withdrawn</option>
                      </select>
                    </div>
                    <div className="uim-field">
                      <label>County</label>
                      <select name="county" value={form.county} onChange={handleChange}>
                        {mockCountyOptions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                      </select>
                    </div>
                  </div>
                </div>
              </section>

              <section className="uim-cluster" style={{ border: 'none', marginBottom: 0, marginTop: '0.5rem' }}>
                <h3 className="uim-cluster-title" style={{ fontSize: '0.9rem', background: 'var(--cdss-accent)' }}>Request Date</h3>
                <div className="uim-cluster-body">
                  <div className="uim-form-grid">
                    <div className="uim-field">
                      <label>From</label>
                      <input type="date" name="fromDate" value={form.fromDate} onChange={handleChange} />
                    </div>
                    <div className="uim-field">
                      <label>To</label>
                      <input type="date" name="toDate" value={form.toDate} onChange={handleChange} />
                    </div>
                  </div>
                </div>
              </section>
            </div>
          </section>

          <div className="uim-action-bar center">
            <button type="submit" className="uim-btn uim-btn-primary">Search</button>
            <button type="button" className="uim-btn uim-btn-secondary" onClick={handleReset}>Reset</button>
          </div>
        </form>

        {searched && (
          <section className="uim-cluster" style={{ marginTop: '1.5rem' }}>
            <h2 className="uim-cluster-title">State Hearing Cases</h2>
            <div className="uim-cluster-body">
              <div className="uim-table-wrapper">
                <table className="uim-table">
                  <thead>
                    <tr>
                      <th>Case Number</th>
                      <th>Recipient Name</th>
                      <th>State Hearing Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {mockStateHearingResults.map((row) => (
                      <tr key={row.caseID}>
                        <td className="link-cell">
                          <button type="button" onClick={() => console.log('[TODO] Navigate to IHSSCase_home, caseID:', row.caseID)}>
                            {row.caseNumber}
                          </button>
                        </td>
                        <td>{row.recipientName}</td>
                        <td className="link-cell">
                          <button type="button" onClick={() => console.log('[TODO] Navigate to Case_viewStateHearing, appealID:', row.appealID)}>
                            {row.stateHearingStatus}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </section>
        )}
      </div>
    </div>
  );
};
