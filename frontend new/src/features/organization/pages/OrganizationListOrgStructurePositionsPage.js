import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Position From Org Struct Pos List', route: '/organization/modify-position-from-org-struct-pos-list' },
    { label: 'add User For Position', route: '/organization/add-user-for-position' },
    { label: 'create And Assign User For Position', route: '/organization/create-and-assign-user-for-position' },
    { label: 'remove Reports To User From Position', route: '/organization/remove-reports-to-user-from-position' },
    { label: 'Assign Location To Position', route: '/organization/assign-location-to-position' },
    { label: 'cancel Position By Workflow', route: '/organization/cancel-position-by-workflow' },
    { label: 'list Users For Position', route: '/organization/list-users-for-position' },
    { label: 'org Unit Home', route: '/organization/org-unit-home' },
    { label: 'view Job', route: '/organization/view-job' },
    { label: 'resolve Organisation Structure Tree', route: '/organization/resolve-organisation-structure-tree' }
  ];

export function OrganizationListOrgStructurePositionsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_listOrgStructurePositions"}
      title={"All Positions"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Position Name"} value={record && record['positionName']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Lead Position"} value={record && record['leadPosition']} />
          <UimField label={"Job"} value={record && record['job']} />
          <UimField label={"Position"} value={record && record['position']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Job"} value={record && record['job']} />
          <UimField label={"Lead Position"} value={record && record['leadPosition']} />
          <UimField label={"Vacant"} value={record && record['vacant']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/create-and-assign-user-for-position')}>Assign User</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New User and Assign')}>New User and Assign</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/remove-reports-to-user-from-position')}>Remove Reports To</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/assign-location-to-position')}>Assign Location</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Browse')}>Browse</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationListOrgStructurePositionsPage;
