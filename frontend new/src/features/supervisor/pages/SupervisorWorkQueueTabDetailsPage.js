import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'user Workspace', route: '/supervisor/user-workspace' }
  ];

export function SupervisorWorkQueueTabDetailsPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_workQueueTabDetails"}
      title={"\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"User Subscription Allowed"} value={record && record['userSubscriptionAllowed']} />
          <UimField label={"Owner"} value={record && record['owner']} />
          <UimField label={"Sensitivity"} value={record && record['sensitivity']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorWorkQueueTabDetailsPage;
