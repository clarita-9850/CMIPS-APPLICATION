import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function PACaseSearchPaymentsPage() {
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PACase_searchPayments"}
      title={"Provider Management - Payment Search:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Issued"} value={record && record['issued']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Gross"} value={record && record['gross']} />
          <UimField label={"Net"} value={record && record['net']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"SOC"} value={record && record['sOC']} />
          <UimField label={"Service From"} value={record && record['serviceFrom']} />
          <UimField label={"Issued"} value={record && record['issued']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Warrant Number"} value={record && record['warrantNumber']} />
          <UimField label={"Gross"} value={record && record['gross']} />
          <UimField label={"Net"} value={record && record['net']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Payee Name"} value={record && record['payeeName']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"SOC"} value={record && record['sOC']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
      </div>
    </UimPageLayout>
  );
}

export default PACaseSearchPaymentsPage;
