import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'activate Org Structure', route: '/organization/activate-org-structure' },
    { label: 'create Root Org Unit', route: '/organization/create-root-org-unit' },
    { label: 'clone Org Structure', route: '/organization/clone-org-structure' },
    { label: 'modify Org Structure From View', route: '/organization/modify-org-structure-from-view' },
    { label: 'org Unit Home', route: '/organization/org-unit-home' },
    { label: 'resolve Organisation Structure Tree', route: '/organization/resolve-organisation-structure-tree' }
  ];

export function OrganizationOrgStructureHomePage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_orgStructureHome"}
      title={"Organization Structure Home:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Options</button>
      </div>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Activation Date"} value={record && record['activationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Root Unit"} value={record && record['rootUnit']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/activate-org-structure')}>Activate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Root Organization Unit')}>Create Root Organization Unit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/clone-org-structure')}>Clone</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Browse')}>Browse</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationOrgStructureHomePage;
