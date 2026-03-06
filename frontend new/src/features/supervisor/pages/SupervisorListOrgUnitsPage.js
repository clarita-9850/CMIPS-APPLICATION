import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Org Unit Details From List', route: '/organization/view-org-unit-details-from-list' },
    { label: 'org Unit Workspace', route: '/supervisor/org-unit-workspace' }
  ];

export function SupervisorListOrgUnitsPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_listOrgUnits"}
      title={"List Org Units"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date Created"} value={record && record['dateCreated']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorListOrgUnitsPage;
