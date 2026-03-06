import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view C P Provider Summary', route: '/person/view-cp-provider-summary' },
    { label: 'view C P Claim', route: '/person/view-cp-claim' }
  ];

export function PersonSearchCPClaimsPage() {
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_searchCPClaims"}
      title={"Career Pathways Claim History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Claim Type"} value={record && record['claimType']} />
          <UimField label={"Claim Status"} value={record && record['claimStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Claim Number"} value={record && record['claimNumber']} />
          <UimField label={"Claim Type"} value={record && record['claimType']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
        </div>
      </UimSection>
      <UimSection title={"Career Pathways Claim History"}>
        <div className="uim-form-grid">
          <UimField label={"Training Hours Claimed"} value={record && record['trainingHoursClaimed']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Claim Number"} value={record && record['claimNumber']} />
          <UimField label={"Claim Type"} value={record && record['claimType']} />
        </div>
      </UimSection>
      <UimSection title={"Career Pathways Claim History"}>
        <div className="uim-form-grid">
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Training Hours Claimed"} value={record && record['trainingHoursClaimed']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Cumulative Training Hours To Date By Pathway')}>Cumulative Training Hours To Date By Pathway</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonSearchCPClaimsPage;
