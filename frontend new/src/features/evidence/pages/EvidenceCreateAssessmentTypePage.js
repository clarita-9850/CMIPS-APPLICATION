import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'home', route: '/evidence/home' },
    { label: 'resolve Household Home Page Evidence I D', route: '/evidence/resolve-household-home-page-evidence-id' }
  ];

export function EvidenceCreateAssessmentTypePage() {
  const navigate = useNavigate();
  const eligibilityApi = getDomainApi('evidence');
  return (
    <UimPageLayout
      pageId={"Evidence_createAssessmentType"}
      title={"Select Assessment Type:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Assessment Type"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Save &amp; Next')}>Save &amp; Next</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceCreateAssessmentTypePage;
