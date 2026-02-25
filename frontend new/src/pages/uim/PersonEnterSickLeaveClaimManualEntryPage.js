/**
 * PersonEnterSickLeaveClaimManualEntryPage
 * Route: /person/enter-sick-leave-claim-manual-entry
 * "Sick Leave Claim Manual Entry" shortcut from My Shortcuts.
 */

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './UimPage.css';

export const PersonEnterSickLeaveClaimManualEntryPage = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    providerNumber: '',
    payPeriodStartDate: '',
    recipientNumber: '',
  });

  const handleChange = (e) => {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleContinue = (e) => {
    e.preventDefault();
    console.log('[TODO] Validate sick leave claim manual entry:', form);
    navigate('/person/create-sick-leave-claim-manual-entry', { state: { ...form } });
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">Sick Leave Claim Manual Entry</h1>

        <form onSubmit={handleContinue}>
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Field Details</h2>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field">
                  <label htmlFor="providerNumber">Provider Number</label>
                  <input
                    id="providerNumber"
                    name="providerNumber"
                    value={form.providerNumber}
                    onChange={handleChange}
                    style={{ maxWidth: '220px' }}
                    placeholder="e.g. PR-10001"
                  />
                </div>
                <div className="uim-field">
                  <label htmlFor="payPeriodStartDate">Pay Period Begin Date</label>
                  <input
                    type="date"
                    id="payPeriodStartDate"
                    name="payPeriodStartDate"
                    value={form.payPeriodStartDate}
                    onChange={handleChange}
                    style={{ maxWidth: '220px' }}
                  />
                </div>
                <div className="uim-field">
                  <label htmlFor="recipientNumber">Recipient Case Number</label>
                  <input
                    id="recipientNumber"
                    name="recipientNumber"
                    value={form.recipientNumber}
                    onChange={handleChange}
                    style={{ maxWidth: '220px' }}
                    placeholder="e.g. CASE-55001"
                  />
                </div>
              </div>
            </div>
          </section>

          <div className="uim-action-bar center">
            <button type="submit" className="uim-btn uim-btn-primary">Continue</button>
            <button type="button" className="uim-btn uim-btn-secondary" onClick={() => navigate('/workspace')}>Cancel</button>
          </div>
        </form>
      </div>
    </div>
  );
};
