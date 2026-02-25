import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'household Evidence Home', route: '/evidence/household-evidence-home' },
    { label: 'reload Household Member Evidence Home', route: '/evidence/reload-household-member-evidence-home' }
  ];

export function EvidenceCreateHouseholdMemberPage() {
  const navigate = useNavigate();
  const eligibilityApi = getDomainApi('evidence');
  return (
    <UimPageLayout
      pageId={"Evidence_createHouseholdMember"}
      title={"Create Household Member:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Companion Case Details"}>
        <div className="uim-form-grid">
          <UimField label={"Companion Case Number"} />
          <UimField label={"Protective Supervision Status"} />
          <UimField label={"Relationship"} />
          <UimField label={"Last Name"} />
        </div>
      </UimSection>
      <UimSection title={"Household Member Details"}>
        <div className="uim-form-grid">
          <UimField label={"Spouse / Parent"} />
          <UimField label={"Date of Birth"} />
          <UimField label={"First Name"} />
          <UimField label={"Protective Supervision Proration"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; New')}>Save &amp; New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceCreateHouseholdMemberPage;
