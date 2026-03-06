import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Location Structure', route: '/county/modify-location-structure' },
    { label: 'create Root Location', route: '/organization/create-root-location' },
    { label: 'location Home', route: '/organization/location-home' }
  ];

export function OrganizationLocationStructureAdditionalDetailsPage() {
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_locationStructureAdditionalDetails"}
      title={"Additional Details"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Activation Date"} value={record && record['activationDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New Root Location')}>New Root Location</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationLocationStructureAdditionalDetailsPage;
