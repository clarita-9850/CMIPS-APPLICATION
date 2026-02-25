import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function RegionalAdminCreateLocationStructurePage() {
  const navigate = useNavigate();
  const countyApi = getDomainApi('county');
  return (
    <UimPageLayout
      pageId={"RegionalAdmin_createLocationStructure"}
      title={"New Location Structure"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} />
          <UimField label={"Comments"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { countyApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default RegionalAdminCreateLocationStructurePage;
