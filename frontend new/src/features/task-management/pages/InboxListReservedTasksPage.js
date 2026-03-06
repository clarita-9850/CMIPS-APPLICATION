import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' }
  ];

export function InboxListReservedTasksPage() {
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_listReservedTasks"}
      title={"Reserved Tasks:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Reserved"} value={record && record['reserved']} />
          <UimField label={"Deadline"} value={record && record['deadline']} />
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Reserved"} value={record && record['reserved']} />
          <UimField label={"Deadline"} value={record && record['deadline']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxListReservedTasksPage;
