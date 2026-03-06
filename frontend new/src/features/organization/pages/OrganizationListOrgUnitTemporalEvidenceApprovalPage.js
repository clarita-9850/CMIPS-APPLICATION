import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Temporal Evidence Approval Check For Org Unit', route: '/organization/create-temporal-evidence-approval-check-for-org-unit' },
    { label: 'view Temporal Evidence Approval Check For Org Unit', route: '/organization/view-temporal-evidence-approval-check-for-org-unit' },
    { label: 'modify Temporal Evidence Approval Check For Org Unit From List', route: '/organization/modify-temporal-evidence-approval-check-for-org-unit-from-list' },
    { label: 'cancel Temporal Evidence Approval Check For Org Unit', route: '/organization/cancel-temporal-evidence-approval-check-for-org-unit' },
    { label: 'resolve Organisation Unit Tree', route: '/organization/resolve-organisation-unit-tree' }
  ];

export function OrganizationListOrgUnitTemporalEvidenceApprovalPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_listOrgUnitTemporalEvidenceApproval"}
      title={"Organization Unit Temporal Evidence Approvals:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Evidence Type"} value={record && record['evidenceType']} />
          <UimField label={"Percentage"} value={record && record['percentage']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/view-temporal-evidence-approval-check-for-org-unit')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Browse')}>Browse</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationListOrgUnitTemporalEvidenceApprovalPage;
