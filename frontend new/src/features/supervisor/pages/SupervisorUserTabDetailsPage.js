import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view User Details', route: '/organization/view-user-details' }
  ];

export function SupervisorUserTabDetailsPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_userTabDetails"}
      title={"\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Phone Number"} value={record && record['phoneNumber']} />
          <UimField label={"Available For Task Redirection"} value={record && record['availableForTaskRedirection']} />
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
          <UimField label={"Tasks Redirected to"} value={record && record['tasksRedirectedTo']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorUserTabDetailsPage;
