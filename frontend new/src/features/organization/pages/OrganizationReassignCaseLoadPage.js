import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Supervisor', route: '/organization/list-supervisor' }
  ];

export function OrganizationReassignCaseLoadPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_reassignCaseLoad"}
      title={"Reassign Cases To"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"New Owner"} value={record && record['newOwner']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.update(id, { action: 'assign' }).then(() => { alert('Assigned successfully'); navigate(-1); }).catch(err => alert('Assign failed: ' + err.message)); }}>Assign</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationReassignCaseLoadPage;
