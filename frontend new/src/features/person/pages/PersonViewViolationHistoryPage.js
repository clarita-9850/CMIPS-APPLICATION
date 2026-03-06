import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Violation History Details', route: '/person/view-violation-history-details' },
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonViewViolationHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewViolationHistory"}
      title={"Violation Details History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Exceeds Travel Time"} value={record && record['exceedsTravelTime']} />
          <UimField label={"Case County"} value={record && record['caseCounty']} />
          <UimField label={"Exceeds Weekly Maximum"} value={record && record['exceedsWeeklyMaximum']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Violation Count"} value={record && record['violationCount']} />
          <UimField label={"Violation Status Date"} value={record && record['violationStatusDate']} />
          <UimField label={"Violation Status"} value={record && record['violationStatus']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Last Update By"} value={record && record['lastUpdateBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-violation-history-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewViolationHistoryPage;
