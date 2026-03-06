import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Approved Electronic Forms', route: '/person/list-approved-electronic-forms' },
    { label: 'upload Documents', route: '/person/upload-documents' },
    { label: 'list Uploaded Documents History', route: '/person/list-uploaded-documents-history' },
    { label: 'modify Document Description', route: '/person/modify-document-description' },
    { label: 'archive Uploaded Document', route: '/person/archive-uploaded-document' }
  ];

export function PersonListUploadedDocumentsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listUploadedDocuments"}
      title={"Attachments:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Form Name"} value={record && record['formName']} />
          <UimField label={"Uploaded By"} value={record && record['uploadedBy']} />
          <UimField label={"Uploaded Date"} value={record && record['uploadedDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Attachments')}>Attachments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Completed Electronic Forms')}>Completed Electronic Forms</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Upload File')}>Upload File</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Attachments History')}>Attachments History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Download')}>Download</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/archive-uploaded-document')}>Archive</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListUploadedDocumentsPage;
