import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view E V V Timsheet Details For History', route: '/case/view-evv-timsheet-details-for-history' },
    { label: 'timesheet History', route: '/case/timesheet-history' }
  ];

export function CaseViewTimesheetSnapshotPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewTimesheetSnapshot"}
      title={"View Timesheet:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Recipient Number"} value={record && record['recipientNumber']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Print Method"} value={record && record['printMethod']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Timesheet#"} value={record && record['timesheet']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Large Font Timesheet"} value={record && record['largeFontTimesheet']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Related Special Transactions"} value={record && record['relatedSpecialTransactions']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Print Date"} value={record && record['printDate']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
          <UimField label={"Legacy Timesheet Number"} value={record && record['legacyTimesheetNumber']} />
          <UimField label={"Mode of Entry"} value={record && record['modeOfEntry']} />
          <UimField label={"Late Submission Release Date"} value={record && record['lateSubmissionReleaseDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Timesheet')}>View Timesheet</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: EVV Details')}>EVV Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewTimesheetSnapshotPage;
