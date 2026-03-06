import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view County Dispute History', route: '/person/view-county-dispute-history' }
  ];

export function PersonViewCountyDisputeHistoryDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCountyDisputeHistoryDetails"}
      title={"View County Dispute History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"County Dispute History Details"}>
        <div className="uim-form-grid">
          <UimField label={"County Dispute Filed Date"} value={record && record['countyDisputeFiledDate']} />
          <UimField label={"Dispute Entered Date"} value={record && record['disputeEnteredDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"County Dispute Outcome"} value={record && record['countyDisputeOutcome']} />
          <UimField label={"County Dispute Outcome Date"} value={record && record['countyDisputeOutcomeDate']} />
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"County Dispute User Name"} value={record && record['countyDisputeUserName']} />
          <UimField label={"County Dispute Outcome Entered Date"} value={record && record['countyDisputeOutcomeEnteredDate']} />
          <UimField label={"Supervisor Dispute Outcome"} value={record && record['supervisorDisputeOutcome']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"Dispute Supervisor Name"} value={record && record['disputeSupervisorName']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
          <UimField label={"County Dispute Letter Date"} value={record && record['countyDisputeLetterDate']} />
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

export default PersonViewCountyDisputeHistoryDetailsPage;
