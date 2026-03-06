import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Recipient Monthly O T Max', route: '/case/view-recipient-monthly-ot-max' }
  ];

export function CaseViewOTMaxHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewOTMaxHistory"}
      title={"Monthly Overtime Maximum History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Monthly Auth Hours"} value={record && record['monthlyAuthHours']} />
          <UimField label={"Weekly Maximum"} value={record && record['weeklyMaximum']} />
          <UimField label={"Monthly Overtime Maximum"} value={record && record['monthlyOvertimeMaximum']} />
          <UimField label={"Last Updated Date"} value={record && record['lastUpdatedDate']} />
          <UimField label={"Last Updated By"} value={record && record['lastUpdatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewOTMaxHistoryPage;
