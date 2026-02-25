import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'person list Case', route: '/help-desk/person-list-case' }
  ];

export function HelpDeskSearchPersonPage() {
  const helpDeskApi = getDomainApi('help-desk');
  const { data, loading, error } = useDomainData('help-desk', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HelpDesk_searchPerson"}
      title={"HelpDesk Person Search"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Day Of Month"} value={record && record['dayOfMonth']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
        </div>
      </UimSection>
      <UimSection title={"General"}>
        <div className="uim-form-grid">
          <UimField label={"CIN"} value={record && record['cIN']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Street Number"} value={record && record['streetNumber']} />
          <UimField label={"City"} value={record && record['city']} />
          <UimField label={"Street Name"} value={record && record['streetName']} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
          <UimField label={"Full Name"} value={record && record['fullName']} />
          <UimField label={"CIN"} value={record && record['cIN']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
        </div>
      </UimSection>
      <UimSection title={"Other Contact Information"}>
        <div className="uim-form-grid">
          <UimField label={"Residence Address"} value={record && record['residenceAddress']} />
          <UimField label={"City"} value={record && record['city']} />
          <UimField label={"County"} value={record && record['county']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => { helpDeskApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
      </div>
    </UimPageLayout>
  );
}

export default HelpDeskSearchPersonPage;
