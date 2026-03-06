import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Timesheet', route: '/case/view-timesheet' },
    { label: 'timesheet History', route: '/case/timesheet-history' },
    { label: 'view Timesheet Hard Copy Tab', route: '/case/view-timesheet-hard-copy-tab' },
    { label: 'view Approve Reject Timesheet Tab', route: '/case/view-approve-reject-timesheet-tab' }
  ];

export function CaseViewEVVTimsheetDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewEVVTimsheetDetails"}
      title={"EVV Details:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-timesheet')}>View Timesheet Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/timesheet-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Hardcopy')}>Hardcopy</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Approve/Reject')}>Approve/Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: EVV Details')}>EVV Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewEVVTimsheetDetailsPage;
