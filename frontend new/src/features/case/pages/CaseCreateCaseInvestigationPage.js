import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Case Investigation', route: '/case/list-case-investigation' }
  ];

export function CaseCreateCaseInvestigationPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createCaseInvestigation"}
      title={"Create Case Investigation Referral:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Create Case Investigation Referral"}>
        <div className="uim-form-grid">
          <UimField label={"Case Referred To"} />
          <UimField label={"Case Investigation Outcome"} />
          <UimField label={"Provider"} />
          <UimField label={"Date Case Referred"} />
          <UimField label={"Case Investigation Outcome Date"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateCaseInvestigationPage;
