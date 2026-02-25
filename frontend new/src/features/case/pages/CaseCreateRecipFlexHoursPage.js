import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Recip Flex Hours', route: '/case/list-recip-flex-hours' }
  ];

export function CaseCreateRecipFlexHoursPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createRecipFlexHours"}
      title={"Create Recipient Flexible Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Create Recipient Flexible Hours"}>
        <div className="uim-form-grid">
          <UimField label={"Month"} />
          <UimField label={"Program Type"} />
          <UimField label={"Recipient Requested Hours"} />
          <UimField label={"Recipient Request Date"} />
          <UimField label={"Request Outcome Date"} />
          <UimField label={"Outcome Letter Date"} />
          <UimField label={"Year"} />
          <UimField label={"Frequency"} />
        </div>
      </UimSection>
      <UimSection title={"Request Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"County Approved Hours"} />
          <UimField label={"Flexible Hours End Date"} />
          <UimField label={"Cancellation Letter Date"} />
          <UimField label={"Approved"} />
          <UimField label={"Denied - Need not unanticipated"} />
          <UimField label={"Denied - Need not immediate"} />
          <UimField label={"Denied - No Health or Safety Issue"} />
          <UimField label={"Comments"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateRecipFlexHoursPage;
