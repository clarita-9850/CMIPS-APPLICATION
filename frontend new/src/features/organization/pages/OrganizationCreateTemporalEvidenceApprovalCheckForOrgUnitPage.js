import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function OrganizationCreateTemporalEvidenceApprovalCheckForOrgUnitPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  return (
    <UimPageLayout
      pageId={"Organization_createTemporalEvidenceApprovalCheckForOrgUnit"}
      title={"Create Organization Unit Temporal Evidence Approval:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Evidence Type Approval Configuration"}>
        <div className="uim-form-grid">
          <UimField label={"Evidence Type"} />
          <UimField label={"Percentage"} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Applies to all Evidence Types"} />
          <UimField label={"Comments"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationCreateTemporalEvidenceApprovalCheckForOrgUnitPage;
