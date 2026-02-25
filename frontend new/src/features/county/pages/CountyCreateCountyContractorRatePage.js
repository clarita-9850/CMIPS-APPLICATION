import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list County Contractor Rate', route: '/county/list-county-contractor-rate' }
  ];

export function CountyCreateCountyContractorRatePage() {
  const navigate = useNavigate();
  const countyApi = getDomainApi('county');
  return (
    <UimPageLayout
      pageId={"County_createCountyContractorRate"}
      title={"Create County Contractor Rate:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Contractor Name"} />
          <UimField label={"Rate"} />
          <UimField label={"MACR"} />
          <UimField label={"Effective Date"} />
          <UimField label={"End Date"} />
          <UimField label={"Wage"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { countyApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CountyCreateCountyContractorRatePage;
