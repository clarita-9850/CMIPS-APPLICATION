import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Wage Rate', route: '/county/list-wage-rate' }
  ];

export function CountyCreateWageRatePage() {
  const navigate = useNavigate();
  const countyApi = getDomainApi('county');
  return (
    <UimPageLayout
      pageId={"County_createWageRate"}
      title={"Create Wage Rate:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Effective Date"} />
          <UimField label={"Type"} />
          <UimField label={"Rate"} />
          <UimField label={"Wage"} />
          <UimField label={"End Date"} />
          <UimField label={"Non-State Share Over"} />
        </div>
      </UimSection>
      <UimSection title={"Health Benefits"}>
        <div className="uim-form-grid">
          <UimField label={"Admin Rate"} />
          <UimField label={"Estimated Taxes"} />
          <UimField label={"State Shared"} />
          <UimField label={"Non-State Share Over"} />
          <UimField label={"Non-Health Benefits"} />
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

export default CountyCreateWageRatePage;
