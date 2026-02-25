import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list All Work Queue Subscriptions', route: '/task-management/list-all-work-queue-subscriptions' },
    { label: 'user Home', route: '/organization/user-home' }
  ];

export function WorkAllocationViewWorkQueuePage() {
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"WorkAllocation_viewWorkQueue"}
      title={"Un-reserve Task:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"User Subscription Allowed"} value={record && record['userSubscriptionAllowed']} />
        </div>
      </UimSection>
      <UimSection title={"Subscription"}>
        <div className="uim-form-grid">
          <UimField label={"Sensitivity"} value={record && record['sensitivity']} />
          <UimField label={"Administrator"} value={record && record['administrator']} />
        </div>
      </UimSection>
      <UimSection title={"Comment"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Work Queue')}>View Work Queue</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Work Queues Subscriptions')}>Work Queues Subscriptions</button>
      </div>
    </UimPageLayout>
  );
}

export default WorkAllocationViewWorkQueuePage;
