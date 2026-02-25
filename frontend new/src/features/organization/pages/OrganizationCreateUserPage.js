import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function OrganizationCreateUserPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  return (
    <UimPageLayout
      pageId={"Organization_createUser"}
      title={"Create User"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Title"} />
          <UimField label={"First Name"} />
          <UimField label={"Location"} />
          <UimField label={"Sensitivity"} />
          <UimField label={"Last Name"} />
          <UimField label={"Worker Number"} />
          <UimField label={"Primary Alternate Language"} />
          <UimField label={"Secondary Alternate Language"} />
        </div>
      </UimSection>
      <UimSection title={"Contact"}>
        <div className="uim-form-grid">
          <UimField label={"Business Email"} />
          <UimField label={"User Name"} />
          <UimField label={"Password"} />
          <UimField label={"Password Expires (Days)"} />
          <UimField label={"Application"} />
          <UimField label={"Call Center User"} />
          <UimField label={"Role Name"} />
          <UimField label={"Confirm Password"} />
        </div>
      </UimSection>
      <UimSection title={"Security"}>
        <div className="uim-form-grid">
          <UimField label={"Password Expires (Logins)"} />
          <UimField label={"Account Expires On"} />
          <UimField label={"Account Enabled"} />
          <UimField label={"Set Access Periods"} />
          <UimField label={"Monday"} />
          <UimField label={"Wednesday"} />
          <UimField label={"Friday"} />
          <UimField label={"From"} />
        </div>
      </UimSection>
      <UimSection title={"Access Periods"}>
        <div className="uim-form-grid">
          <UimField label={"Sunday"} />
          <UimField label={"Tuesday"} />
          <UimField label={"Thursday"} />
          <UimField label={"Saturday"} />
          <UimField label={"To"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationCreateUserPage;
