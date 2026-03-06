import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'remove User From Position', route: '/organization/remove-user-from-position' },
    { label: 'org Unit Home', route: '/organization/org-unit-home' }
  ];

export function OrganizationListUsersForPositionPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_listUsersForPosition"}
      title={"Un-reserve Task:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Organisation Unit"} value={record && record['organisationUnit']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Remove</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationListUsersForPositionPage;
