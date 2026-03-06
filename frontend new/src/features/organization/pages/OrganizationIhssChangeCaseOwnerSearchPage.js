import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'resolvemodify Case Owner', route: '/organization/resolvemodify-case-owner' }
  ];

export function OrganizationIhssChangeCaseOwnerSearchPage() {
  const navigate = useNavigate();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_ihssChangeCaseOwnerSearch"}
      title={"User Search"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Worker Number"} value={record && record['workerNumber']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
          <UimField label={"Zip Code"} value={record && record['zipCode']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Username"} value={record && record['username']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Unit"} value={record && record['unit']} />
          <UimField label={"Position Name"} value={record && record['positionName']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Worker Number"} value={record && record['workerNumber']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
          <UimField label={"Language 1"} value={record && record['language1']} />
          <UimField label={"Language 2"} value={record && record['language2']} />
          <UimField label={"Case Count"} value={record && record['caseCount']} />
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

export default OrganizationIhssChangeCaseOwnerSearchPage;
