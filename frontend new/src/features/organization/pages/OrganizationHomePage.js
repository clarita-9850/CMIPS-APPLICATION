import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Organization', route: '/organization/modify-organization' }
  ];

export function OrganizationHomePage() {
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_home"}
      title={"Organization Home:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Tax Number"} value={record && record['taxNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"Registration Number"} value={record && record['registrationNumber']} />
          <UimField label={"Location Security Level"} value={record && record['locationSecurityLevel']} />
        </div>
      </UimSection>
      <UimSection title={"Contact Details"}>
        <div className="uim-form-grid">
          <UimField label={"Location Data Security"} value={record && record['locationDataSecurity']} />
          <UimField label={"Address"} value={record && record['address']} />
        </div>
      </UimSection>
      <UimSection title={"Description"}>
        <div className="uim-form-grid">
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
          <UimField label={"Web Address"} value={record && record['webAddress']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationHomePage;
