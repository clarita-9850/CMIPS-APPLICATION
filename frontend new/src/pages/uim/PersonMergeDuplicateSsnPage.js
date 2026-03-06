/**
 * PersonMergeDuplicateSsnPage – conversion of Person_mergeDuplicateSSN.uim
 * Route: /person/merge-duplicate-ssn
 * "Merge Duplicate SSN" shortcut from My Shortcuts.
 *
 * Two-step flow:
 *  Step 1 (Verify): show SSN/Master fields + up to 5 duplicate CIN entries
 *  Step 2 (Save):   show read-only review, submit = stub save
 */

import React, { useState } from 'react';
import './UimPage.css';
import { mockMergeDuplicateSSN } from './mockData';

const MAX_DUPS = 5;

export const PersonMergeDuplicateSsnPage = () => {
  const [isConfirm, setIsConfirm] = useState(false);
  const [fields, setFields] = useState({
    ssn: mockMergeDuplicateSSN.ssn,
    masterRecord: mockMergeDuplicateSSN.masterRecord,
    makeMaster: mockMergeDuplicateSSN.makeMaster,
    duplicates: mockMergeDuplicateSSN.duplicates.map((d) => d.rec),
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFields((f) => ({ ...f, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleDupChange = (idx, val) => {
    setFields((f) => {
      const dups = [...f.duplicates];
      dups[idx] = val;
      return { ...f, duplicates: dups };
    });
  };

  const handleVerify = (e) => {
    e.preventDefault();
    // TODO: call IHSSPerson.mergeDupSSN (ACTION_ID=Verify) via BFF API
    console.log('[TODO] Verify merge duplicate SSN:', fields);
    setIsConfirm(true);
  };

  const handleSave = (e) => {
    e.preventDefault();
    // TODO: call IHSSPerson.mergeDupSSN (ACTION_ID=Save) → navigate to Person_resolveMergeSSN
    console.log('[TODO] Save merge duplicate SSN:', fields);
    alert('[Stub] Merge saved. Would navigate to Person_resolveMergeSSN.');
    setIsConfirm(false);
  };

  return (
    <div className="uim-page">
      <div className="container">
        <h1 className="page-title">
          Merge Duplicate SSN
        </h1>

        {isConfirm && (
          <div className="uim-info-banner" role="status">
            Please review the entries below and click <strong>Save</strong> to confirm the merge.
          </div>
        )}

        <form onSubmit={isConfirm ? handleSave : handleVerify}>
          {/* SSN / Master record */}
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Name / SSN</h2>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                <div className="uim-field">
                  <label>SSN</label>
                  <input
                    name="ssn"
                    value={fields.ssn}
                    onChange={handleChange}
                    readOnly={isConfirm}
                  />
                </div>
                <div className="uim-field">
                  <label>Master Record (CIN)</label>
                  <input
                    name="masterRecord"
                    value={fields.masterRecord}
                    onChange={handleChange}
                    readOnly={isConfirm}
                  />
                </div>
                <div className="uim-field">
                  <div className="uim-checkbox-row" style={{ paddingTop: '0.25rem' }}>
                    <input
                      type="checkbox"
                      name="makeMaster"
                      id="makeMaster"
                      checked={fields.makeMaster}
                      onChange={handleChange}
                      disabled={isConfirm}
                    />
                    <label htmlFor="makeMaster">Make Master</label>
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* Duplicate records */}
          <section className="uim-cluster">
            <h2 className="uim-cluster-title">Duplicate Records</h2>
            <div className="uim-cluster-body">
              <div className="uim-form-grid">
                {[...Array(MAX_DUPS)].map((_, i) => (
                  <div className="uim-field" key={i}>
                    <label>Duplicate Record {i + 1}</label>
                    <input
                      value={fields.duplicates[i] || ''}
                      onChange={(e) => handleDupChange(i, e.target.value)}
                      readOnly={isConfirm}
                      placeholder="CIN or empty"
                    />
                  </div>
                ))}
              </div>
            </div>
          </section>

          <div className="uim-action-bar center">
            {!isConfirm && (
              <button type="submit" className="uim-btn uim-btn-primary">
                Verify
                <span className="uim-todo-badge">Stub</span>
              </button>
            )}
            {isConfirm && (
              <button type="submit" className="uim-btn uim-btn-primary">
                Save
                <span className="uim-todo-badge">Stub</span>
              </button>
            )}
            {isConfirm && (
              <button type="button" className="uim-btn uim-btn-secondary" onClick={() => setIsConfirm(false)}>
                Back
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};
