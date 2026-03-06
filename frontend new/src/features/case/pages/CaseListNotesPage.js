import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Note', route: '/case/create-note' },
    { label: 'print Notes', route: '/case/print-notes' },
    { label: 'view Note', route: '/case/view-note' },
    { label: 'modify Note From List', route: '/case/modify-note-from-list' },
    { label: 'user Home Popup', route: '/organization/user-home-popup' }
  ];

export function CaseListNotesPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listNotes"}
      title={"Case Notes:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
          <UimField label={"Text"} value={record && record['text']} />
          <UimField label={"Sensitivity"} value={record && record['sensitivity']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print Case Notes Form')}>Print Case Notes Form</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-note')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListNotesPage;
