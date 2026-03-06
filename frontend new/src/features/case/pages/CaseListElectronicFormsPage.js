import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Forms Correspondence', route: '/case/list-forms-correspondence' },
    { label: 'create Electronic Form', route: '/case/create-electronic-form' },
    { label: 'edit Electronic Forms', route: '/case/edit-electronic-forms' },
    { label: 'save Electronic Forms', route: '/case/save-electronic-forms' },
    { label: 'send To E S P Electronic Forms', route: '/case/send-to-esp-electronic-forms' },
    { label: 'approve Electronic Forms', route: '/case/approve-electronic-forms' },
    { label: 'reject Electronic Forms', route: '/case/reject-electronic-forms' },
    { label: 'delete Electronic Forms', route: '/case/delete-electronic-forms' },
    { label: 'list Electronic Forms History', route: '/case/list-electronic-forms-history' }
  ];

export function CaseListElectronicFormsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listElectronicForms"}
      title={"Electronic Forms:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Form Name"} value={record && record['formName']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Sent to"} value={record && record['sentTo']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Created By"} value={record && record['createdBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Forms/Correspondence')}>Forms/Correspondence</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/edit-electronic-forms')}>Electronic Forms</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Resend to CSP')}>Resend to CSP</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Resend to ESP')}>Resend to ESP</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send To CSP')}>Send To CSP</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send To ESP')}>Send To ESP</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Accept</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, { status: 'denied' }).then(() => { alert('Denied successfully'); navigate(-1); }).catch(err => alert('Action failed: ' + err.message)); }}>Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { casesApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/list-electronic-forms-history')}>History</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListElectronicFormsPage;
