import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view W P C S Service Hours', route: '/case/view-wpcs-service-hours' }
  ];

export function CaseSearchPaidHoursPage() {
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_searchPaidHours"}
      title={"Paid Hours:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"IHSS Auth To Purchase"} value={record && record['iHSSAuthToPurchase']} />
          <UimField label={"IHSS Available Hours"} value={record && record['iHSSAvailableHours']} />
          <UimField label={"IHSS Remaining Hours"} value={record && record['iHSSRemainingHours']} />
          <UimField label={"IHSS Paid Hours"} value={record && record['iHSSPaidHours']} />
          <UimField label={"IP Assigned Hours"} value={record && record['iPAssignedHours']} />
        </div>
      </UimSection>
      <UimSection title={"Service Month"}>
        <div className="uim-form-grid">
          <UimField label={"IP Paid Hours"} value={record && record['iPPaidHours']} />
          <UimField label={"CC Assigned Hours"} value={record && record['cCAssignedHours']} />
          <UimField label={"CC Paid Hours"} value={record && record['cCPaidHours']} />
          <UimField label={"HM/PAC Assigned Hours"} value={record && record['hMPACAssignedHours']} />
          <UimField label={"HM/PAC Paid Hours"} value={record && record['hMPACPaidHours']} />
          <UimField label={"WPCS Auth To Purchase"} value={record && record['wPCSAuthToPurchase']} />
          <UimField label={"WPCS Paid Hours"} value={record && record['wPCSPaidHours']} />
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
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Click here to view')}>Click here to view</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseSearchPaidHoursPage;
