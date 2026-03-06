import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/person/view-timesheet' }
  ];

export function HelpDeskSearchTimesheetPage() {
  const helpDeskApi = getDomainApi('help-desk');
  const { data, loading, error } = useDomainData('help-desk', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HelpDesk_searchTimesheet"}
      title={"Timesheet Search:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From Date"} value={record && record['fromDate']} />
          <UimField label={"To Date"} value={record && record['toDate']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Hours Claimed"} value={record && record['hoursClaimed']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Hours Claimed"} value={record && record['hoursClaimed']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Service Period"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Hours Claimed"} value={record && record['hoursClaimed']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Hours Claimed"} value={record && record['hoursClaimed']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results - By Recipient"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Search Results - By Provider"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Search Results - By Recipient"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Search Results - By Provider"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { helpDeskApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt; Search Previous 6 Months')}>&lt;&lt; Search Previous 6 Months</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Search Next 6 Months &gt;&gt;')}>Search Next 6 Months &gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
      </div>
    </UimPageLayout>
  );
}

export default HelpDeskSearchTimesheetPage;
