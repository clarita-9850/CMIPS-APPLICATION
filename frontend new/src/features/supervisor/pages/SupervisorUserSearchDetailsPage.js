import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'user Workspace', route: '/supervisor/user-workspace' },
    { label: 'org Unit Workspace', route: '/supervisor/org-unit-workspace' },
    { label: 'view Position Details', route: '/organization/view-position-details' }
  ];

export function SupervisorUserSearchDetailsPage() {
  const supervisorApi = getDomainApi('supervisor');
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_userSearchDetails"}
      title={"User Search"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Job"} value={record && record['job']} />
          <UimField label={"User Name"} value={record && record['userName']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Position"} value={record && record['position']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorUserSearchDetailsPage;
