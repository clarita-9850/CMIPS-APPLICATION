import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'admin User Search', route: '/organization/admin-user-search' },
    { label: 'view User Inbox', route: '/task-management/view-user-inbox' },
    { label: 'modify User Password', route: '/organization/modify-user-password' },
    { label: 'resolve Application Home', route: '/misc/activity-resolve-application-home' }
  ];

export function AdminWorkspacePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Admin_workspace"}
      title={"Home"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"My Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Assessment Due Date"} value={record && record['assessmentDueDate']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"Subject"} value={record && record['subject']} />
        </div>
      </UimSection>
      <UimSection title={"My Tasks"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"My Calendar"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a User')}>Find a User</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/view-user-inbox')}>Inbox</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Change Password')}>Change Password</button>
      </div>
    </UimPageLayout>
  );
}

export default AdminWorkspacePage;
