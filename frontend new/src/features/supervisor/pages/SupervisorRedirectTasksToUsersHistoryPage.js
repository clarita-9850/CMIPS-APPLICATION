import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'user Workspace', route: '/supervisor/user-workspace' }
  ];

export function SupervisorRedirectTasksToUsersHistoryPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_redirectTasksToUsersHistory"}
      title={"Redirection History"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Redirected To"} value={record && record['redirectedTo']} />
          <UimField label={"Redirect Type"} value={record && record['redirectType']} />
          <UimField label={"Start"} value={record && record['start']} />
          <UimField label={"End"} value={record && record['end']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Redirected To"} value={record && record['redirectedTo']} />
          <UimField label={"Redirect Type"} value={record && record['redirectType']} />
          <UimField label={"Start"} value={record && record['start']} />
          <UimField label={"End"} value={record && record['end']} />
        </div>
      </UimSection>
      <UimSection title={"Active and Pending Redirections"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Expired Redirections"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorRedirectTasksToUsersHistoryPage;
