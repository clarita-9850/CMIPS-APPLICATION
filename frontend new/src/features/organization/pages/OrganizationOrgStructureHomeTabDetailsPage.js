import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'org Unit Home', route: '/organization/org-unit-home' }
  ];

export function OrganizationOrgStructureHomeTabDetailsPage() {
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_orgStructureHomeTabDetails"}
      title={"Org Structure Home Tab Details"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Activation Date"} value={record && record['activationDate']} />
          <UimField label={"Root Unit"} value={record && record['rootUnit']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default OrganizationOrgStructureHomeTabDetailsPage;
