/**
 * PaymentListWarrantReplacementsPage – conversion of Payment_listWarrantReplacements.uim
 * Route: /payment/list-warrant-replacements
 * "Enter Warrant Replacements" shortcut from My Shortcuts.
 */

import React, { useState } from 'react';
import './UimPage.css';
import { mockWarrantReplacements } from './mockData';

export const PaymentListWarrantReplacementsPage = () => {
  const [form, setForm] = useState({
    replacementEntryDate: '',
    replacementDate: '',
    warrantNumber: '',
  });
  const [searched, setSearched] = useState(false);

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setSearched(true);
    // TODO: call PaymentView.searchWarrantReplacement via BFF API
    console.log('[TODO] Search warrant replacements:', form);
  };

  const handleReset = () => {
    setForm({ replacementEntryDate: '', replacementDate: '', warrantNumber: '' });
    setSearched(false);
  };

  const displayRows = searched ? mockWarrantReplacements : mockWarrantReplacements;

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">
          Warrant Replacements
        </h1>

        <form onSubmit={handleSearch}>
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Search Criteria</h2>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field">
                  <label>Replacement Entry Date</label>
                  <input type="date" name="replacementEntryDate" value={form.replacementEntryDate} onChange={handleChange} />
                </div>
                <div className="uim-field">
                  <label>Replacement Date</label>
                  <input type="date" name="replacementDate" value={form.replacementDate} onChange={handleChange} />
                </div>
                <div className="uim-field">
                  <label>Warrant Number</label>
                  <input name="warrantNumber" value={form.warrantNumber} onChange={handleChange} style={{ width: '35%' }} />
                </div>
              </div>
              <div className="uim-action-bar center" style={{ marginTop: '1rem' }}>
                <button type="submit" className="uim-btn uim-btn-primary">Search</button>
                <button type="button" className="uim-btn uim-btn-secondary" onClick={handleReset}>Reset</button>
              </div>
            </div>
          </section>
        </form>

        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Warrant Replacements</h2>
          <div className="uim-cluster-body">
            <div className="uim-action-bar left" style={{ marginBottom: '0.5rem' }}>
              <button
                className="uim-btn uim-btn-primary"
                onClick={() => console.log('[TODO] Navigate to Payment_enterWarrantReplacement')}
              >
                New
                <span className="uim-todo-badge">Stub</span>
              </button>
            </div>
            <div className="uim-table-wrapper">
              <table className="uim-table">
                <thead>
                  <tr>
                    <th>Entry Date</th>
                    <th>Replacement Date</th>
                    <th>Warrant Number</th>
                    <th>Issue Date</th>
                    <th>Net Amount</th>
                    <th>County</th>
                    <th>Case Number</th>
                    <th>Recipient Name</th>
                    <th>Payee Number</th>
                    <th>Payee Name</th>
                  </tr>
                </thead>
                <tbody>
                  {displayRows.length === 0 ? (
                    <tr className="empty-row"><td colSpan={10}>No warrant replacements found.</td></tr>
                  ) : (
                    displayRows.map((row, i) => (
                      <tr key={i}>
                        <td>{row.replacementEntryDate}</td>
                        <td>{row.replacementDate}</td>
                        <td>{row.scoWarrantNumber}</td>
                        <td>{row.issueDate}</td>
                        <td>{row.amount}</td>
                        <td>{row.countyCode}</td>
                        <td>{row.caseNumber}</td>
                        <td>{row.recipientFullName}</td>
                        <td>{row.payeeNumber}</td>
                        <td>{row.payeeFullName}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
            <div className="uim-pagination">
              <button className="uim-btn uim-btn-secondary" disabled>← Previous</button>
              <button className="uim-btn uim-btn-secondary" disabled>Next →</button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
};
