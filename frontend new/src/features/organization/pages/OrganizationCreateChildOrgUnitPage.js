import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function OrganizationCreateChildOrgUnitPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  const [activeTab, setActiveTab] = React.useState(0);
  return (
    <UimPageLayout
      pageId={"Organization_createChildOrgUnit"}
      title={"Create Organization Unit:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Details</button>
      </div>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} />
          <UimField label={"Type"} />
          <UimField label={"Read SID"} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Maintain SID"} />
          <UimField label={"Create Unit SID"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationCreateChildOrgUnitPage;
