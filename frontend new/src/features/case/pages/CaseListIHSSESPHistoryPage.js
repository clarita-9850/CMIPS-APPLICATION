import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list I H S S E S P History Popup', route: '/case/list-ihssesp-history-popup' },
    { label: 'timesheet Preferences', route: '/case/timesheet-preferences' }
  ];

export function CaseListIHSSESPHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listIHSSESPHistory"}
      title={"IHSS ESP Registration History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Registration Date"} value={record && record['registrationDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Status From Date"} value={record && record['statusFromDate']} />
          <UimField label={"Status To Date"} value={record && record['statusToDate']} />
          <UimField label={"ESP User Name"} value={record && record['eSPUserName']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Comments')}>View Comments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListIHSSESPHistoryPage;
