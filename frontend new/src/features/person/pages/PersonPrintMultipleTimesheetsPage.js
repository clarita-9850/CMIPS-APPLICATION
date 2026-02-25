import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'resolve Multiple Timesheets', route: '/misc/print-resolve-multiple-timesheets' }
  ];

export function PersonPrintMultipleTimesheetsPage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_printMultipleTimesheets"}
      title={"Print Multiple Timesheets:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
        </div>
      </UimSection>
      <UimSection title={"Search Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => window.print()}>Print</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt; Search Previous 6 Months')}>&lt;&lt; Search Previous 6 Months</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Search Next 6 Months &gt;&gt;')}>Search Next 6 Months &gt;&gt;</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonPrintMultipleTimesheetsPage;
