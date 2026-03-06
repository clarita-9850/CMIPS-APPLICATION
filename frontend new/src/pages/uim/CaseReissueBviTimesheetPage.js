/**
 * CaseReissueBviTimesheetPage – conversion of Case_reissueBVITimesheet.uim
 * Route: /case/reissue-bvi-timesheet
 * "Reissue Large Font Timesheet" shortcut from My Shortcuts.
 */

import React, { useState } from 'react';
import './UimPage.css';

export const CaseReissueBviTimesheetPage = () => {
  const [form, setForm] = useState({
    caseNumber: '',
    timesheetNumber: '',
    providerNumber: '',
  });
  const [successMsg, setSuccessMsg] = useState('');

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
    setSuccessMsg('');
  };

  const handleReissue = (e) => {
    e.preventDefault();
    // TODO: call IssueTimesheet.reissueBVITimesheet via BFF API
    console.log('[TODO] Reissue BVI Timesheet:', form);
    setSuccessMsg('Timesheet reissued successfully. (Stub – no backend connected)');
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">
          Reissue Large Font (BVI) Timesheet
        </h1>

        {successMsg && (
          <div className="uim-info-banner" role="status">
            {successMsg}
          </div>
        )}

        <form onSubmit={handleReissue}>
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Details</h2>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field">
                  <label htmlFor="caseNumber">Case Number</label>
                  <input
                    id="caseNumber"
                    name="caseNumber"
                    value={form.caseNumber}
                    onChange={handleChange}
                    placeholder="e.g. CASE-55001"
                  />
                </div>
                <div className="uim-field">
                  <label htmlFor="timesheetNumber">Timesheet Number</label>
                  <input
                    id="timesheetNumber"
                    name="timesheetNumber"
                    value={form.timesheetNumber}
                    onChange={handleChange}
                    placeholder="e.g. TS-20001"
                  />
                </div>
                <div className="uim-field">
                  <label htmlFor="providerNumber">Provider Number</label>
                  <input
                    id="providerNumber"
                    name="providerNumber"
                    value={form.providerNumber}
                    onChange={handleChange}
                    placeholder="e.g. PR-10001"
                  />
                </div>
              </div>
            </div>
          </section>

          <div className="uim-action-bar center">
            <button type="submit" className="uim-btn uim-btn-primary">
              Reissue
              <span className="uim-todo-badge">Stub</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
