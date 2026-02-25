import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'display Travel Time', route: '/person/display-travel-time' }
  ];

export function PersonCreateTravelTimePage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  return (
    <UimPageLayout
      pageId={"Person_createTravelTime"}
      title={"Create Travel Time:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Begin Date"} />
          <UimField label={"Weekly Travel Time Hours"} />
          <UimField label={"Program Type"} />
          <UimField label={"End Date"} />
          <UimField label={"Traveling From"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonCreateTravelTimePage;
