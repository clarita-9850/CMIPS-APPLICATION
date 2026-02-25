import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'timesheet History', route: '/case/timesheet-history' },
    { label: 'view Timesheet Hard Copy Tab', route: '/case/view-timesheet-hard-copy-tab' },
    { label: 'view Approve Reject Timesheet Tab', route: '/case/view-approve-reject-timesheet-tab' },
    { label: 'view E V V Timsheet Details', route: '/case/view-evv-timsheet-details' },
    { label: 'search Timesheet', route: '/case/search-timesheet' }
  ];

export function CaseViewTimesheetPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewTimesheet"}
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
          <UimField label={"Timesheet Number"} value={record && record['timesheetNumber']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
        </div>
      </UimSection>
      <UimSection title={"TTS Recipient Timesheet Processing Details"}>
        <div className="uim-form-grid">
          <UimField label={"Large Font Timesheet"} value={record && record['largeFontTimesheet']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Related Special Transactions"} value={record && record['relatedSpecialTransactions']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"Print Date"} value={record && record['printDate']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
          <UimField label={"Legacy Timesheet Number"} value={record && record['legacyTimesheetNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Mode of Entry"} value={record && record['modeOfEntry']} />
          <UimField label={"Late Submission Release Date"} value={record && record['lateSubmissionReleaseDate']} />
          <UimField label={"Processed Through Telephonic System"} value={record && record['processedThroughTelephonicSystem']} />
          <UimField label={"Processed By TTS Assistance Line Agent"} value={record && record['processedByTTSAssistanceLineAgent']} />
          <UimField label={"Hours Claimed (HH:MM)"} value={record && record['hoursClaimedHHMM']} />
          <UimField label={"Case Hours Paid (HH:MM)"} value={record && record['caseHoursPaidHHMM']} />
          <UimField label={"Hours Paid at Overtime Rate(HH:MM)"} value={record && record['hoursPaidAtOvertimeRateHHMM']} />
          <UimField label={"Case Hours Not Paid (HH:MM)"} value={record && record['caseHoursNotPaidHHMM']} />
        </div>
      </UimSection>
      <UimSection title={"Electronic Timesheet Signature"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Signature Date/Time"} value={record && record['providerSignatureDateTime']} />
          <UimField label={"Provider Signature Method"} value={record && record['providerSignatureMethod']} />
          <UimField label={"Recipient Signature Date/Time"} value={record && record['recipientSignatureDateTime']} />
          <UimField label={"Recipient Signature Method"} value={record && record['recipientSignatureMethod']} />
          <UimField label={"Code"} value={record && record['code']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <UimSection title={"Exceptions"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Timesheet Details')}>View Timesheet Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/timesheet-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Hardcopy')}>Hardcopy</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Approve/Reject')}>Approve/Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: EVV Details')}>EVV Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewTimesheetPage;
