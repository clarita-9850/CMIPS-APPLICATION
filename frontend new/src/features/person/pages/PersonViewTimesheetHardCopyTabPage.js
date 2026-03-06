import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/person/view-timesheet' },
    { label: 'timesheet History', route: '/person/timesheet-history' },
    { label: 'view E V V Timsheet Details', route: '/person/view-evv-timsheet-details' },
    { label: 'confirm E V V Hard Copy Print', route: '/person/confirm-evv-hard-copy-print' },
    { label: 'confirm E V V Hard Copy Print Prov', route: '/case/confirm-evv-hard-copy-print-prov' }
  ];

export function PersonViewTimesheetHardCopyTabPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewTimesheetHardCopyTab"}
      title={"Timesheet Hardcopy:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-timesheet')}>View Timesheet Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/timesheet-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Hardcopy')}>Hardcopy</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: EVV Details')}>EVV Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print E-Timesheet')}>Print E-Timesheet</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send Hardcopy to Provider')}>Send Hardcopy to Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send Hardcopy to Recipient')}>Send Hardcopy to Recipient</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewTimesheetHardCopyTabPage;
