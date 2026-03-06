import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view State Admin Review History Details', route: '/person/view-state-admin-review-history-details' },
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonViewStateAdminReviewHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewStateAdminReviewHistory"}
      title={"State Review History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"State Review Filed Date"} value={record && record['stateReviewFiledDate']} />
          <UimField label={"State Review Entered Date"} value={record && record['stateReviewEnteredDate']} />
          <UimField label={"State Review Outcome"} value={record && record['stateReviewOutcome']} />
          <UimField label={"State Review User Name"} value={record && record['stateReviewUserName']} />
          <UimField label={"State Supervisor Review Outcome"} value={record && record['stateSupervisorReviewOutcome']} />
          <UimField label={"State Supervisor Review Outcome Date"} value={record && record['stateSupervisorReviewOutcomeDate']} />
          <UimField label={"State Supervisor User Name"} value={record && record['stateSupervisorUserName']} />
          <UimField label={"State Supervisor Review Outcome Entered Date"} value={record && record['stateSupervisorReviewOutcomeEnteredDate']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Last Update By"} value={record && record['lastUpdateBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-state-admin-review-history-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewStateAdminReviewHistoryPage;
