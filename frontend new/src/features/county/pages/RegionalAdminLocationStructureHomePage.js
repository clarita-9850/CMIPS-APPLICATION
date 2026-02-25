import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'activate Location Structure', route: '/county/activate-location-structure' },
    { label: 'create Root Location', route: '/organization/create-root-location' },
    { label: 'modify Location Structure', route: '/county/modify-location-structure' },
    { label: 'location Home', route: '/organization/location-home' },
    { label: 'resolve Location Structure Tree', route: '/organization/resolve-location-structure-tree' }
  ];

export function RegionalAdminLocationStructureHomePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('county', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"RegionalAdmin_locationStructureHome"}
      title={"Location Structure Home:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Activation Date"} value={record && record['activationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/county/activate-location-structure')}>Activate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/create-root-location')}>Create Root Location</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Browse')}>Browse</button>
      </div>
    </UimPageLayout>
  );
}

export default RegionalAdminLocationStructureHomePage;
