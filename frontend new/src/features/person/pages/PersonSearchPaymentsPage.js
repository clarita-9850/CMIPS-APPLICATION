import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'provider Verifications', route: '/person/provider-verifications' },
    { label: 'view Payment Details', route: '/payment/view-payment-details' }
  ];

export function PersonSearchPaymentsPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_searchPayments"}
      title={"Payment Search:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Issued"} value={record && record['issued']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Gross"} value={record && record['gross']} />
          <UimField label={"Net"} value={record && record['net']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"SOC"} value={record && record['sOC']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Issued"} value={record && record['issued']} />
        </div>
      </UimSection>
      <UimSection title={"Issue Date"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Gross"} value={record && record['gross']} />
          <UimField label={"Net"} value={record && record['net']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"SOC"} value={record && record['sOC']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print Verification Form')}>Print Verification Form</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonSearchPaymentsPage;
