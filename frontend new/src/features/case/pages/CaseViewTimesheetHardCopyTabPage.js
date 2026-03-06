import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/case/view-timesheet' },
    { label: 'timesheet History', route: '/case/timesheet-history' },
    { label: 'view Approve Reject Timesheet Tab', route: '/case/view-approve-reject-timesheet-tab' },
    { label: 'view E V V Timsheet Details', route: '/case/view-evv-timsheet-details' },
    { label: 'confirm E V V Hard Copy Print', route: '/case/confirm-evv-hard-copy-print' },
    { label: 'confirm E V V Hard Copy Print Recip', route: '/person/confirm-evv-hard-copy-print-recip' },
    { label: 'confirm E V V Exception T S Print', route: '/case/confirm-evv-exception-ts-print' }
  ];

export function CaseViewTimesheetHardCopyTabPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewTimesheetHardCopyTab"}
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
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-timesheet')}>View Timesheet Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/timesheet-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Hardcopy')}>Hardcopy</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Approve/Reject')}>Approve/Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: EVV Details')}>EVV Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print E-Timesheet')}>Print E-Timesheet</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send Hardcopy to Recipient')}>Send Hardcopy to Recipient</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Send Hardcopy to Provider')}>Send Hardcopy to Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Print Exception Timesheet')}>Print Exception Timesheet</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewTimesheetHardCopyTabPage;
