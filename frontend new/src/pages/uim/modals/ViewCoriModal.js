import React from 'react';
import { UimField } from '../../../shared/components/UimField';
import { formatDate } from '../../../lib/providerConstants';
import '../../../shared/components/UimPage.css';

export const ViewCoriModal = ({ record, onClose, onInactivate }) => {
  if (!record) return null;

  return (
    <div className="uim-modal-overlay" onClick={onClose}>
      <div className="uim-cluster" onClick={e => e.stopPropagation()} style={{ maxWidth: '600px' }}>
        <h3 className="uim-cluster-title">View Provider CORI</h3>
        <div className="uim-cluster-body">
          <div className="uim-form-grid">
            <UimField label="CORI Date" value={formatDate(record.coriDate || record.checkDate || record.createdAt)} />
            <UimField label="CORI End Date" value={formatDate(record.coriEndDate)} />
            <UimField label="Conviction or Release Date" value={formatDate(record.convictionReleaseDate)} />
            <UimField label="Tier" value={record.tier || '\u2014'} />
            <UimField label="Status" value={record.status || '\u2014'} />
            <UimField label="County" value={record.countyCode || '\u2014'} />
          </div>

          <h4 style={{ color: 'var(--cdss-blue)', marginTop: '1rem', marginBottom: '0.5rem', fontWeight: 600, fontSize: '0.95rem' }}>
            General Exception
          </h4>
          <div className="uim-form-grid">
            <UimField label="GE Begin Date" value={formatDate(record.geBeginDate)} />
            <UimField label="GE End Date" value={formatDate(record.geEndDate)} />
            <UimField label="GE Notes" value={record.geNotes || '\u2014'} />
          </div>

          <div className="uim-form-grid" style={{ marginTop: '0.75rem' }}>
            <UimField label="Created By" value={record.createdBy || '\u2014'} />
            <UimField label="Updated By" value={record.updatedBy || '\u2014'} />
          </div>
        </div>
        <div className="uim-action-bar" style={{ padding: '0 1.25rem 1rem' }}>
          <button className="uim-btn uim-btn-secondary" onClick={onClose}>Close</button>
          {record.status !== 'INACTIVE' && onInactivate && (
            <button className="uim-btn uim-btn-danger" onClick={onInactivate}>Inactivate</button>
          )}
        </div>
      </div>
    </div>
  );
};
