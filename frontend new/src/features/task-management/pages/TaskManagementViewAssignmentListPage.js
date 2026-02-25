import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'view History Text', route: '/task-management/view-history-text' },
    { label: 'resolve User', route: '/organization/resolve-user' },
    { label: 'view User Details', route: '/organization/view-user-details' },
    { label: 'list Unreserved Work Queue Tasks', route: '/task-management/list-unreserved-work-queue-tasks' }
  ];

export function TaskManagementViewAssignmentListPage() {
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"TaskManagement_viewAssignmentList"}
      title={"Task Assignment List :"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"User Name"} value={record && record['userName']} />
          <UimField label={"Work Queue Name"} value={record && record['workQueueName']} />
        </div>
      </UimSection>
      <UimSection title={"Users"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Work Queues"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Task')}>View Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Task History')}>Task History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Assignment List')}>Assignment List</button>
      </div>
    </UimPageLayout>
  );
}

export default TaskManagementViewAssignmentListPage;
