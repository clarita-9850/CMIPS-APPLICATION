import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'reserve Next Work Queue Task', route: '/task-management/reserve-next-work-queue-task' },
    { label: 'task Search', route: '/task-management/task-search' },
    { label: 'create Task', route: '/task-management/create-task' },
    { label: 'list Reserved Due On Or Before Date', route: '/task-management/list-reserved-due-on-or-before-date' }
  ];

export function InboxViewUserInboxPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_viewUserInbox"}
      title={"Inbox:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"My Tasks"}>
        <div className="uim-form-grid">
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/reserve-next-work-queue-task')}>Reserve Next Work Queue Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find Task')}>Find Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/create-task')}>Create Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Tasks Before Deadline')}>View Tasks Before Deadline</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxViewUserInboxPage;
