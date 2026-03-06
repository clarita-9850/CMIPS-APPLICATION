import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/person/view-timesheet' },
    { label: 'view Timesheet Hard Copy Tab', route: '/person/view-timesheet-hard-copy-tab' },
    { label: 'view E V V Timsheet Details', route: '/person/view-evv-timsheet-details' },
    { label: 'view Timesheet Snapshot', route: '/person/view-timesheet-snapshot' }
  ];

export function PersonTimesheetHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_timesheetHistory"}
      title={"Timesheet History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Hours Claimed"} value={record && record['hoursClaimed']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Large Font Timesheet"} value={record && record['largeFontTimesheet']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Print Date"} value={record && record['printDate']} />
          <UimField label={"Print Method"} value={record && record['printMethod']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <UimSection title={"Timesheet History List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-timesheet')}>View Timesheet Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: History')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Hardcopy')}>Hardcopy</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: EVV Details')}>EVV Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-timesheet')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonTimesheetHistoryPage;
