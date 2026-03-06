/**
 * PersonLiveInProvSelfCertificationSearchPage
 * Conversion of Person_liveInProvSelfCertificationSearch.uim
 * Route: /person/live-in-prov-self-certification-search
 * "IRS Live-In Provider Self-Certification" shortcut from My Shortcuts.
 */

import React, { useState } from 'react';
import './UimPage.css';

export const PersonLiveInProvSelfCertificationSearchPage = () => {
  const [form, setForm] = useState({ providerNumber: '', caseNumber: '' });

  const handleChange = (e) => {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }));
  };

  const handleContinue = (e) => {
    e.preventDefault();
    // TODO: call LiveInProviderCertification.validateLiveInProvCert via BFF API
    // On success, navigate to Person_liveInProvSelfCertificationEntry with caseParticipantRoleID
    console.log('[TODO] Validate live-in provider self-certification:', form);
    alert(`[Stub] Would validate Provider #${form.providerNumber} + Case #${form.caseNumber} and navigate to the certification entry page.`);
  };

  const handleReset = () => {
    setForm({ providerNumber: '', caseNumber: '' });
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">
          IRS Live-In Provider Self-Certification
        </h1>

        <form onSubmit={handleContinue}>
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Field Details</h2>
            <div className="uim-cluster-body">
              <div className="uim-form-grid cols-1">
                <div className="uim-field" style={{ maxWidth: '350px' }}>
                  <label htmlFor="providerNumber">Provider Number</label>
                  <input
                    id="providerNumber"
                    name="providerNumber"
                    value={form.providerNumber}
                    onChange={handleChange}
                    style={{ width: '20%', minWidth: '160px' }}
                    placeholder="e.g. PR-10001"
                  />
                </div>
                <div className="uim-field" style={{ maxWidth: '350px' }}>
                  <label htmlFor="caseNumber">Case Number</label>
                  <input
                    id="caseNumber"
                    name="caseNumber"
                    value={form.caseNumber}
                    onChange={handleChange}
                    style={{ width: '20%', minWidth: '160px' }}
                    placeholder="e.g. CASE-55001"
                  />
                </div>
              </div>
            </div>
          </section>

          <div className="uim-action-bar center">
            <button type="submit" className="uim-btn uim-btn-primary">
              Continue
              <span className="uim-todo-badge">Stub</span>
            </button>
            <button type="button" className="uim-btn uim-btn-secondary" onClick={handleReset}>
              Reset
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
