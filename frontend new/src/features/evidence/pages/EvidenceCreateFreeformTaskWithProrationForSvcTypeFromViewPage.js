import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Service Type With Proration From View', route: '/evidence/modify-service-type-with-proration-from-view' },
    { label: 'modify Teach And Demo Service From View', route: '/evidence/modify-teach-and-demo-service-from-view' }
  ];

export function EvidenceCreateFreeformTaskWithProrationForSvcTypeFromViewPage() {
  const navigate = useNavigate();
  const eligibilityApi = getDomainApi('evidence');
  return (
    <UimPageLayout
      pageId={"Evidence_createFreeformTaskWithProrationForSvcTypeFromView"}
      title={"Un-reserve Task:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Task"} />
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

export default EvidenceCreateFreeformTaskWithProrationForSvcTypeFromViewPage;
