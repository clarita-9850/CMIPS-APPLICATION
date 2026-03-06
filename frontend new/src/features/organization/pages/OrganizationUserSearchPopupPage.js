import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function OrganizationUserSearchPopupPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_userSearchPopup"}
      title={"User Search"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Display Deleted Users"} value={record && record['displayDeletedUsers']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Job"} value={record && record['job']} />
          <UimField label={"Display Closed Users"} value={record && record['displayClosedUsers']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Position"} value={record && record['position']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationUserSearchPopupPage;
