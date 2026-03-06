import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'work Queue Workspace', route: '/supervisor/work-queue-workspace' }
  ];

export function SupervisorUnSubscribedWorkQueuesPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_unSubscribedWorkQueues"}
      title={"Un Subscribed Work Queues"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Unsubscribed Work Queues"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Tasks In Work Queue"} value={record && record['tasksInWorkQueue']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorUnSubscribedWorkQueuesPage;
