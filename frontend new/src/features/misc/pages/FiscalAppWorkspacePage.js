import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'search', route: '/person/search' },
    { label: 'search State Hearing', route: '/case/search-state-hearing' },
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function FiscalAppWorkspacePage() {
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"FiscalApp_workspace"}
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
        </div>
      </UimSection>
      <UimSection title={"Case Search"}>
        <div className="uim-form-grid">
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a Person')}>Find a Person</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a State Hearing Case')}>Find a State Hearing Case</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
      </div>
    </UimPageLayout>
  );
}

export default FiscalAppWorkspacePage;
