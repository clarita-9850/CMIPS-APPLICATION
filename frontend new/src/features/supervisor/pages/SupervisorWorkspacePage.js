import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'user Workspace', route: '/supervisor/user-workspace' },
    { label: 'cases Home', route: '/supervisor/cases-home' },
    { label: 'list Users', route: '/supervisor/list-users' },
    { label: 'work Queue', route: '/supervisor/work-queue' },
    { label: 'resolve Application Home', route: '/misc/activity-resolve-application-home' }
  ];

export function SupervisorWorkspacePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_workspace"}
      title={"My Workspace:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"My Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Position"} value={record && record['position']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"Subject"} value={record && record['subject']} />
        </div>
      </UimSection>
      <UimSection title={"Users Reporting To Supervisor"}>
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
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Cases')}>View Cases</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Users')}>View Users</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/work-queue')}>View Work Queues</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorWorkspacePage;
