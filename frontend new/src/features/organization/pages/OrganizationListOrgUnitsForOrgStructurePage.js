import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Org Unit From List', route: '/organization/modify-org-unit-from-list' },
    { label: 'cancel Org Unit', route: '/organization/cancel-org-unit' },
    { label: 'org Unit Home', route: '/organization/org-unit-home' },
    { label: 'resolve Organisation Structure Tree', route: '/organization/resolve-organisation-structure-tree' }
  ];

export function OrganizationListOrgUnitsForOrgStructurePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_listOrgUnitsForOrgStructure"}
      title={"Organization Units:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Location"} value={record && record['location']} />
          <UimField label={"Unit Status"} value={record && record['unitStatus']} />
          <UimField label={"Name"} value={record && record['name']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <div className="uim-form-grid">
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Browse')}>Browse</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationListOrgUnitsForOrgStructurePage;
