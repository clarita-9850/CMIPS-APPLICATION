import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Work Queue', route: '/task-management/view-work-queue' },
    { label: 'add Work Queue Subscription', route: '/task-management/add-work-queue-subscription' },
    { label: 'remove Subscription To Work Queue', route: '/task-management/remove-subscription-to-work-queue' },
    { label: 'user Home', route: '/organization/user-home' }
  ];

export function WorkAllocationListAllWorkQueueSubscriptionsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const tasksApi = getDomainApi('task-management');
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"WorkAllocation_listAllWorkQueueSubscriptions"}
      title={"Work Queue Subscriptions:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Subscription Date"} value={record && record['subscriptionDate']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Subscription Date"} value={record && record['subscriptionDate']} />
        </div>
      </UimSection>
      <UimSection title={"User Subscriber"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Organization Object Subscriber"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/view-work-queue')}>View Work Queue</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Work Queues Subscriptions')}>Work Queues Subscriptions</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { tasksApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Remove</button>
      </div>
    </UimPageLayout>
  );
}

export default WorkAllocationListAllWorkQueueSubscriptionsPage;
