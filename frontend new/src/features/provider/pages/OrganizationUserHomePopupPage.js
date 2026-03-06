/**
 * OrganizationUserHomePopupPage – generated from Organization_userHomePopup
 * Route: /provider/user-home-popup
 * Source UIM: CMIPSFE/ReferenceApp/Organization/User/Organization_userHomePopup.uim
 * Domain: provider
 * Status: Stubbed
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../../pages/uim/UimPage.css';

export const OrganizationUserHomePopupPage = () => {
  const navigate = useNavigate();

  return (
    <div className="uim-page">
      <div className="container">

        <h1 className="page-title">user Home Popup</h1>

        {/* Placeholder banner */}
        <div className="uim-info-banner" style={{background:'#fff3cd',border:'1px solid #ffc107',borderRadius:'4px',padding:'0.75rem 1rem',marginBottom:'1.5rem'}}>
          <strong>Placeholder</strong> – generated from <code>Organization_userHomePopup.uim</code>.
          Backend integration pending.
        </div>

        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Label.ContactDetails</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <div className="uim-action-bar">
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] ActionControl.Label.TaskRedirection')}>ActionControl.Label.TaskRedirection</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] ActionControl.Label.Close')}>ActionControl.Label.Close</button>
        </div>
        <div className="uim-action-bar" style={{marginTop:'1rem'}}>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/provider/task-redirection')}>task Redirection</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/list-case')}>list Case</button>
        </div>

        {/* Back button */}
        <div style={{marginTop:'1.5rem'}}>
          <button className="uim-btn uim-btn-secondary" onClick={() => window.history.back()}>
            &larr; Back
          </button>
        </div>

      </div>
    </div>
  );
};

export default OrganizationUserHomePopupPage;
