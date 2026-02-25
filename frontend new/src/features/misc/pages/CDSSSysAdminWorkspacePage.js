import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'ihss User Search In Page', route: '/organization/ihss-user-search-in-page' },
    { label: 'view User Inbox', route: '/task-management/view-user-inbox' }
  ];

export function CDSSSysAdminWorkspacePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"CDSSSysAdmin_workspace"}
      title={"My Workspace:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"My Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"Task"} value={record && record['task']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Priority"} value={record && record['priority']} />
        </div>
      </UimSection>
      <UimSection title={"My Tasks - Due"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a User')}>Find a User</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/view-user-inbox')}>Inbox</button>
      </div>
    </UimPageLayout>
  );
}

export default CDSSSysAdminWorkspacePage;
