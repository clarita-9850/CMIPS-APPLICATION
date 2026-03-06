import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function PersonSearchRecipientPopupPage() {
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_searchRecipientPopup"}
      title={"Person Search"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Phonetic Search"} value={record && record['phoneticSearch']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"CIN"} value={record && record['cIN']} />
        </div>
      </UimSection>
      <UimSection title={"General"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
          <UimField label={"Street Number"} value={record && record['streetNumber']} />
          <UimField label={"City"} value={record && record['city']} />
          <UimField label={"Street Name"} value={record && record['streetName']} />
          <UimField label={"Full Name"} value={record && record['fullName']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"CIN"} value={record && record['cIN']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Person Type"} value={record && record['personType']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonSearchRecipientPopupPage;
