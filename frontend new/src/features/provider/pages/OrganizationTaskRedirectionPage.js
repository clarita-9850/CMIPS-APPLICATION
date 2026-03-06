/**
 * OrganizationTaskRedirectionPage – generated from Organization_taskRedirection
 * Route: /provider/task-redirection
 * Source UIM: CMIPSFE/ReferenceApp/Organization/User/Organization_taskRedirection.uim
 * Domain: provider
 * Status: Stubbed
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../../pages/uim/UimPage.css';

export const OrganizationTaskRedirectionPage = () => {
  const navigate = useNavigate();

  return (
    <div className="uim-page">
      <div className="container">

        <h1 className="page-title">task Redirection</h1>

        {/* Placeholder banner */}
        <div className="uim-info-banner" style={{background:'#fff3cd',border:'1px solid #ffc107',borderRadius:'4px',padding:'0.75rem 1rem',marginBottom:'1.5rem'}}>
          <strong>Placeholder</strong> – generated from <code>Organization_taskRedirection.uim</code>.
          Backend integration pending.
        </div>

        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Title.NewTaskRedirection</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <section className="uim-cluster">
          <h2 className="uim-cluster-title">List.Title.ActiveAndPendingRedirections</h2>
          <div className="uim-cluster-body">
            <div className="uim-table-wrapper" style={{overflowX:'auto'}}>
              <table className="uim-table">
                <thead><tr><th>#</th><th>ID</th><th>Description</th><th>Status</th><th>Actions</th></tr></thead>
                <tbody>
                  <tr><td>1</td><td>—</td><td>Sample record</td><td>Active</td><td><button className="uim-btn uim-btn-link">View</button></td></tr>
                </tbody>
              </table>
            </div>
          </div>
        </section>
        <div className="uim-action-bar">
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] ActionControl.Label.Clear')}>ActionControl.Label.Clear</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] ActionControl.SaveButton.label')}>ActionControl.SaveButton.label</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] ActionControl.CancelButton.label')}>ActionControl.CancelButton.label</button>
        </div>
        <div className="uim-action-bar" style={{marginTop:'1rem'}}>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/provider/user-home')}>user Home</button>
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

export default OrganizationTaskRedirectionPage;
