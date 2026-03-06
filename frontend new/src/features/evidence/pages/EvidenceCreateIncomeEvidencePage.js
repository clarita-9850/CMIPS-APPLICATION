import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'soc Evidence Home', route: '/evidence/soc-evidence-home' },
    { label: 'reload Income Evidence', route: '/evidence/reload-income-evidence' }
  ];

export function EvidenceCreateIncomeEvidencePage() {
  const navigate = useNavigate();
  const eligibilityApi = getDomainApi('evidence');
  return (
    <UimPageLayout
      pageId={"Evidence_createIncomeEvidence"}
      title={"Create Income Evidence"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Recipient Income"}>
        <div className="uim-form-grid">
          <UimField label={"Source"} />
          <UimField label={"Monthly Income Amount"} />
          <UimField label={"Deduction"} />
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

export default EvidenceCreateIncomeEvidencePage;
