import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'soc Evidence Home', route: '/evidence/soc-evidence-home' }
  ];

export function EvidenceCreateSOCEvidencePage() {
  const navigate = useNavigate();
  const eligibilityApi = getDomainApi('evidence');
  return (
    <UimPageLayout
      pageId={"Evidence_createSOCEvidence"}
      title={"Create Share of Cost Evidence"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Share of Cost Evidence"}>
        <div className="uim-form-grid">
          <UimField label={"Share of Cost Linkage"} />
          <UimField label={"Dependents"} />
          <UimField label={"Benefit Level Code"} />
        </div>
      </UimSection>
      <UimSection title={"Share of Cost Calculation"}>
        <div className="uim-form-grid">
          <UimField label={"Countable Income"} />
          <UimField label={"IHSS Share of Cost"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { eligibilityApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceCreateSOCEvidencePage;
