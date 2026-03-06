import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Approved Timesheets', route: '/homemaker/super-list-approved-timesheets' },
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function HomemakerSuperViewApprovedTimesheetsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HomemakerSuper_viewApprovedTimesheets"}
      title={"Homemaker/PA Contract Supervisor Approved Timesheets"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Homemaker/PA Contract"} value={record && record['homemakerPAContract']} />
        </div>
      </UimSection>
      <UimSection title={"Homemaker/PA Contract Timesheet Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default HomemakerSuperViewApprovedTimesheetsPage;
