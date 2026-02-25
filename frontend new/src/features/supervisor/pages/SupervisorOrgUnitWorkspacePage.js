import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'user Workspace', route: '/supervisor/user-workspace' }
  ];

export function SupervisorOrgUnitWorkspacePage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_orgUnitWorkspace"}
      title={"Home"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Members"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Position"} value={record && record['position']} />
          <UimField label={"Email"} value={record && record['email']} />
          <UimField label={"Phone Number"} value={record && record['phoneNumber']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorOrgUnitWorkspacePage;
