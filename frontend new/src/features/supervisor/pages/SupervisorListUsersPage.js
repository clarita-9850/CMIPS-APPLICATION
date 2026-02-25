import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view User Details From List', route: '/organization/view-user-details-from-list' },
    { label: 'user Workspace', route: '/supervisor/user-workspace' }
  ];

export function SupervisorListUsersPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_listUsers"}
      title={"List Users"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Last Logon"} value={record && record['lastLogon']} />
          <UimField label={"Leave"} value={record && record['leave']} />
          <UimField label={"Exempt"} value={record && record['exempt']} />
          <UimField label={"Active Cases"} value={record && record['activeCases']} />
          <UimField label={"Reserved Tasks"} value={record && record['reservedTasks']} />
          <UimField label={"Assigned Tasks"} value={record && record['assignedTasks']} />
          <UimField label={"Approval Type"} value={record && record['approvalType']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Last Logon"} value={record && record['lastLogon']} />
          <UimField label={"Leave"} value={record && record['leave']} />
          <UimField label={"Exempt"} value={record && record['exempt']} />
          <UimField label={"Active Cases"} value={record && record['activeCases']} />
          <UimField label={"Reserved Tasks"} value={record && record['reservedTasks']} />
          <UimField label={"Assigned Tasks"} value={record && record['assignedTasks']} />
          <UimField label={"Approval Type"} value={record && record['approvalType']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Previous')}>Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Previous')}>Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorListUsersPage;
