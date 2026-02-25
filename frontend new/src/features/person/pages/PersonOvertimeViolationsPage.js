import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' },
    { label: 'resolve View Hours', route: '/misc/action-resolve-view-hours' }
  ];

export function PersonOvertimeViolationsPage() {
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_overtimeViolations"}
      title={"Overtime Violations:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Violation Search"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Violation Number"} value={record && record['violationNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Case County"} value={record && record['caseCounty']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Transaction Number"} value={record && record['transactionNumber']} />
          <UimField label={"Transaction Type"} value={record && record['transactionType']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Violation Date"} value={record && record['violationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <div className="uim-form-grid">
          <UimField label={"Violation Status"} value={record && record['violationStatus']} />
          <UimField label={"Violation Number"} value={record && record['violationNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Case County"} value={record && record['caseCounty']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <div className="uim-form-grid">
          <UimField label={"Transaction Number"} value={record && record['transactionNumber']} />
          <UimField label={"Transaction Type"} value={record && record['transactionType']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Violation Date"} value={record && record['violationDate']} />
          <UimField label={"Violation Status"} value={record && record['violationStatus']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonOvertimeViolationsPage;
