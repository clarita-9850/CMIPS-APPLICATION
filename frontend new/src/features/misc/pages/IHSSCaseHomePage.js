/**
 * IHSSCaseHomePage – generated from IHSSCase_home
 * Route: /case/ihss-case-home
 * Source UIM: CMIPSFE/Case/Home/IHSSCase_home.uim
 * Domain: misc
 * Status: Stubbed
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../../../pages/uim/UimPage.css';

export const IHSSCaseHomePage = () => {
  const navigate = useNavigate();

  return (
    <div className="uim-page">
      <div className="container">

        <h1 className="page-title">I H S S Case_home</h1>

        {/* Placeholder banner */}
        <div className="uim-info-banner" style={{background:'#fff3cd',border:'1px solid #ffc107',borderRadius:'4px',padding:'0.75rem 1rem',marginBottom:'1.5rem'}}>
          <strong>Placeholder</strong> – generated from <code>IHSSCase_home.uim</code>.
          Backend integration pending.
        </div>

        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Title.Manage</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Title.Details</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Title.CountySpeicalComments</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Title.Comments</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <section className="uim-cluster">
          <h2 className="uim-cluster-title">Cluster.Label.Contact</h2>
          <div className="uim-cluster-body">
            <div className="uim-form-grid">
              <div className="uim-field"><label>Field 1</label><input type="text" placeholder="—" readOnly /></div>
              <div className="uim-field"><label>Field 2</label><input type="text" placeholder="—" readOnly /></div>
            </div>
          </div>
        </section>
        <section className="uim-cluster">
          <h2 className="uim-cluster-title">List.Title.RecentChanges</h2>
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
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] ActionControl.Label.Edit')}>ActionControl.Label.Edit</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] Field.Label.Withdraw')}>Field.Label.Withdraw</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] Field.Label.Leave')}>Field.Label.Leave</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] Field.Label.Terminate')}>Field.Label.Terminate</button>
          <button className="uim-btn uim-btn-secondary" onClick={() => console.log('[TODO] Field.Label.Reactivate')}>Field.Label.Reactivate</button>
        </div>
        <div className="uim-action-bar" style={{marginTop:'1rem'}}>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/withdrawal-case')}>withdrawal Case</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/leave-case')}>leave Case</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/terminate-case')}>terminate Case</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/reactivate-case')}>reactivate Case</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/rescind-case')}>rescind Case</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/rescind-case-with-c-i-n')}>rescind Case With C I N</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/home-page')}>home Page</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/provider/user-home-popup')}>user Home Popup</button>
          <button className="uim-btn uim-btn-primary" onClick={() => navigate('/provider/change-user')}>change User</button>
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

export default IHSSCaseHomePage;
