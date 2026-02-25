import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseListSOCHoursDetailsPage() {
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listSOCHoursDetails"}
      title={"Share of Cost Hours Details:"}
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
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"SOC Amount"} value={record && record['sOCAmount']} />
          <UimField label={"IHSS Authorized Hours"} value={record && record['iHSSAuthorizedHours']} />
          <UimField label={"Recipient SOC Hours"} value={record && record['recipientSOCHours']} />
        </div>
      </UimSection>
      <UimSection title={"Share Of Cost Hours Details"}>
        <div className="uim-form-grid">
          <UimField label={"IHSS Available Hours"} value={record && record['iHSSAvailableHours']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"SOC Amount"} value={record && record['sOCAmount']} />
        </div>
      </UimSection>
      <UimSection title={"Share Of Cost Hours Details"}>
        <div className="uim-form-grid">
          <UimField label={"IHSS Authorized Hours"} value={record && record['iHSSAuthorizedHours']} />
          <UimField label={"Recipient SOC Hours"} value={record && record['recipientSOCHours']} />
          <UimField label={"IHSS Available Hours"} value={record && record['iHSSAvailableHours']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListSOCHoursDetailsPage;
