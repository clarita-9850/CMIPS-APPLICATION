import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'resolve Case Or Provider', route: '/misc/pa-search-resolve-case-or-provider' }
  ];

export function PublicAuthorityWorkspacePage() {
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PublicAuthority_workspace"}
      title={"Provider Management - My Workspace:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Provider Search"}>
        <div className="uim-form-grid">
          <UimField label={"Task"} value={record && record['task']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Due Date"} value={record && record['dueDate']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
        </div>
      </UimSection>
      <UimSection title={"My Tasks"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
      </div>
    </UimPageLayout>
  );
}

export default PublicAuthorityWorkspacePage;
