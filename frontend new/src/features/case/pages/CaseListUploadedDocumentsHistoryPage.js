import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'undelete Uploaded Document', route: '/case/undelete-uploaded-document' },
    { label: 'restore Uploaded Document', route: '/case/restore-uploaded-document' },
    { label: 'delete Uploaded Document', route: '/case/delete-uploaded-document' },
    { label: 'list Uploaded Documents', route: '/case/list-uploaded-documents' }
  ];

export function CaseListUploadedDocumentsHistoryPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listUploadedDocumentsHistory"}
      title={"Attachments History:"}
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
          <UimField label={"Last Action By"} value={record && record['lastActionBy']} />
          <UimField label={"Last Action Date"} value={record && record['lastActionDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Download')}>Download</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/undelete-uploaded-document')}>Undelete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/restore-uploaded-document')}>Restore</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { casesApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListUploadedDocumentsHistoryPage;
