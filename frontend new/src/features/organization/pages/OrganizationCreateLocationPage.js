import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'resolve Location Home', route: '/organization/resolve-location-home' }
  ];

export function OrganizationCreateLocationPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  return (
    <UimPageLayout
      pageId={"Organization_createLocation"}
      title={"Create Child Location"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} />
          <UimField label={"Read SID"} />
          <UimField label={"Create Location SID"} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} />
          <UimField label={"Public Office"} />
          <UimField label={"Maintain SID"} />
        </div>
      </UimSection>
      <UimSection title={"Contact Details"}>
        <div className="uim-form-grid">
          <UimField label={"Phone Area Code"} />
          <UimField label={"Phone Number"} />
          <UimField label={"Email"} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Fax Area Code"} />
          <UimField label={"Fax Number"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationCreateLocationPage;
