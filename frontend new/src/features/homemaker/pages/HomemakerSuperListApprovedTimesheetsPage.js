import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Approved Timesheets', route: '/homemaker/super-view-approved-timesheets' }
  ];

export function HomemakerSuperListApprovedTimesheetsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('homemaker', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"HomemakerSuper_listApprovedTimesheets"}
      title={"Homemaker Supervisor Approved Timesheets"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/homemaker/super-view-approved-timesheets')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default HomemakerSuperListApprovedTimesheetsPage;
