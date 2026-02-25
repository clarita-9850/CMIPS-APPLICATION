import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'delete Alert', route: '/task-management/delete-alert' },
    { label: 'list Alerts', route: '/task-management/list-alerts' }
  ];

export function InboxViewAlertPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const tasksApi = getDomainApi('task-management');
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_viewAlert"}
      title={"View Notification:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
          <UimField label={"Category"} value={record && record['category']} />
        </div>
      </UimSection>
      <UimSection title={"Content"}>
        <div className="uim-form-grid">
          <UimField label={"Action Link"} value={record && record['actionLink']} />
          <UimField label={"Action Link"} value={record && record['actionLink']} />
          <UimField label={"Content"} value={record && record['content']} />
        </div>
      </UimSection>
      <UimSection title={"Related Pages"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Related Pages"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { tasksApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxViewAlertPage;
