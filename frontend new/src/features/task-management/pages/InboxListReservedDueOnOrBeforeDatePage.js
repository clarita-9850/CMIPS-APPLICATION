import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' }
  ];

export function InboxListReservedDueOnOrBeforeDatePage() {
  const tasksApi = getDomainApi('task-management');
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_listReservedDueOnOrBeforeDate"}
      title={"Reserved Tasks Due On Date Or Before"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Deadline Date"} value={record && record['deadlineDate']} />
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Deadline"} value={record && record['deadline']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { tasksApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxListReservedDueOnOrBeforeDatePage;
