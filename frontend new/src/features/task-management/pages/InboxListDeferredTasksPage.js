import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'Restart Task', route: '/task-management/restart-task' },
    { label: 'task Home', route: '/task-management/task-home' }
  ];

export function InboxListDeferredTasksPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_listDeferredTasks"}
      title={"Deferred Tasks:\\\\"}
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
          <UimField label={"Deadline"} value={record && record['deadline']} />
          <UimField label={"Restart"} value={record && record['restart']} />
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Deadline"} value={record && record['deadline']} />
          <UimField label={"Restart"} value={record && record['restart']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/restart-task')}>Restart</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/restart-task')}>Restart</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxListDeferredTasksPage;
