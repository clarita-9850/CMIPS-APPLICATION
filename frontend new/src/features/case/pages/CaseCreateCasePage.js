import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'register', route: '/person/register' },
    { label: 'resolve Home', route: '/case/resolve-home' }
  ];

export function CaseCreateCasePage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createCase"}
      title={"Create Case"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Create Case"}>
        <div className="uim-form-grid">
          <UimField label={"Applicant Name"} />
          <UimField label={"Spoken Language"} />
          <UimField label={"Zip Code"} />
          <UimField label={"Written Language"} />
        </div>
      </UimSection>
      <UimSection title={"Applicant Information"}>
        <div className="uim-form-grid">
          <UimField label={"IHSS Referral Date"} />
          <UimField label={"Assigned Worker"} />
          <UimField label={"Interpreter Available"} />
          <UimField label={"Client Index Number"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateCasePage;
