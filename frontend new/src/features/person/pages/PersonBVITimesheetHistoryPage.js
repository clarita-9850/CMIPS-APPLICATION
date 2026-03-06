import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/person/view-timesheet' },
    { label: 'timesheet History', route: '/person/timesheet-history' },
    { label: 'View Provider E T Reject Release', route: '/person/view-provider-et-reject-release' },
    { label: 'view B V I Timesheet History', route: '/person/view-bvi-timesheet-history' }
  ];

export function PersonBVITimesheetHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_BVITimesheetHistory"}
      title={"BVI Timesheet Release/Reject History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
          <UimField label={"Text"} value={record && record['text']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-timesheet')}>View Timesheet</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/timesheet-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: TTS Timesheet Release/Reject History')}>TTS Timesheet Release/Reject History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: E-Timesheet Release/Reject History')}>E-Timesheet Release/Reject History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-timesheet')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonBVITimesheetHistoryPage;
