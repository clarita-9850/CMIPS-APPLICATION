import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view State Admin Review History', route: '/person/view-state-admin-review-history' }
  ];

export function PersonViewStateAdminReviewHistoryDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewStateAdminReviewHistoryDetails"}
      title={"View State Review History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"State Review History Details"}>
        <div className="uim-form-grid">
          <UimField label={"State Review Filed Date"} value={record && record['stateReviewFiledDate']} />
          <UimField label={"State Review Entered Date"} value={record && record['stateReviewEnteredDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"State Review Outcome"} value={record && record['stateReviewOutcome']} />
          <UimField label={"State Review Outcome Date"} value={record && record['stateReviewOutcomeDate']} />
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"State Review User Name"} value={record && record['stateReviewUserName']} />
          <UimField label={"State Review Outcome Entered Date"} value={record && record['stateReviewOutcomeEnteredDate']} />
          <UimField label={"State Supervisor Review Outcome"} value={record && record['stateSupervisorReviewOutcome']} />
          <UimField label={"State Supervisor Review Outcome Date"} value={record && record['stateSupervisorReviewOutcomeDate']} />
          <UimField label={"State Supervisor User Name"} value={record && record['stateSupervisorUserName']} />
          <UimField label={"State Supervisor Review Outcome Entered Date"} value={record && record['stateSupervisorReviewOutcomeEnteredDate']} />
          <UimField label={"State Review Letter Date"} value={record && record['stateReviewLetterDate']} />
          <UimField label={"Last Update Date"} value={record && record['lastUpdateDate']} />
          <UimField label={"Last Update By"} value={record && record['lastUpdateBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewStateAdminReviewHistoryDetailsPage;
