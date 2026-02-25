import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Overtime Violations For C D S S', route: '/person/view-overtime-violations-for-cdss' },
    { label: 'view Overtime Violation Tracking', route: '/person/view-overtime-violation-tracking' },
    { label: 'view Violation History', route: '/person/view-violation-history' },
    { label: 'modify County Review', route: '/person/modify-county-review' },
    { label: 'modify County Review After Pending', route: '/person/modify-county-review-after-pending' },
    { label: 'modify County Supervisor Review', route: '/person/modify-county-supervisor-review' },
    { label: 'modify County Supervisor Review After Pending', route: '/person/modify-county-supervisor-review-after-pending' },
    { label: 'view County Review Comments', route: '/person/view-county-review-comments' },
    { label: 'view County Review History', route: '/person/view-county-review-history' },
    { label: 'modify County Dispute', route: '/person/modify-county-dispute' },
    { label: 'modify County Dispute After Pending', route: '/person/modify-county-dispute-after-pending' },
    { label: 'modify Dispute Supervisor', route: '/person/modify-dispute-supervisor' },
    { label: 'modify Dispute Supervisor After Pending', route: '/person/modify-dispute-supervisor-after-pending' },
    { label: 'view County Dispute Comments', route: '/person/view-county-dispute-comments' },
    { label: 'view County Dispute History', route: '/person/view-county-dispute-history' },
    { label: 'modify State Admin Review', route: '/person/modify-state-admin-review' },
    { label: 'modify State Admin Review After Pending', route: '/person/modify-state-admin-review-after-pending' },
    { label: 'modify State Supervisor Review', route: '/person/modify-state-supervisor-review' },
    { label: 'modify State Supervisor Review After Pending', route: '/person/modify-state-supervisor-review-after-pending' },
    { label: 'view State Admin Review Comments', route: '/person/view-state-admin-review-comments' },
    { label: 'view State Admin Review History', route: '/person/view-state-admin-review-history' }
  ];

export function PersonViewOvertimeViolationsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewOvertimeViolations"}
      title={"View Overtime Violations:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Violation Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Case County"} value={record && record['caseCounty']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Violation Count"} value={record && record['violationCount']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Violation Status"} value={record && record['violationStatus']} />
          <UimField label={"Next Possible Violation Date"} value={record && record['nextPossibleViolationDate']} />
          <UimField label={"Violation Reduction Letter Date"} value={record && record['violationReductionLetterDate']} />
          <UimField label={"Exceeds Travel Time"} value={record && record['exceedsTravelTime']} />
          <UimField label={"Exceeds Weekly Maximum"} value={record && record['exceedsWeeklyMaximum']} />
        </div>
      </UimSection>
      <UimSection title={"County Review"}>
        <div className="uim-form-grid">
          <UimField label={"Violation Number"} value={record && record['violationNumber']} />
          <UimField label={"Violation Date"} value={record && record['violationDate']} />
          <UimField label={"Violation Status Date"} value={record && record['violationStatusDate']} />
          <UimField label={"Ineligible Begin Date"} value={record && record['ineligibleBeginDate']} />
          <UimField label={"Ineligible End Date"} value={record && record['ineligibleEndDate']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"County Review Outcome"} value={record && record['countyReviewOutcome']} />
          <UimField label={"County Review Outcome Date"} value={record && record['countyReviewOutcomeDate']} />
          <UimField label={"Supervisor Review Outcome"} value={record && record['supervisorReviewOutcome']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"County Review Letter Date"} value={record && record['countyReviewLetterDate']} />
        </div>
      </UimSection>
      <UimSection title={"County Dispute"}>
        <div className="uim-form-grid">
          <UimField label={"Date Training Completed"} value={record && record['dateTrainingCompleted']} />
          <UimField label={"County Reviewer Name"} value={record && record['countyReviewerName']} />
          <UimField label={"County Review Outcome Entered Date"} value={record && record['countyReviewOutcomeEnteredDate']} />
          <UimField label={"Supervisor Name"} value={record && record['supervisorName']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Training Letter Date"} value={record && record['trainingLetterDate']} />
          <UimField label={"County Dispute Filed Date"} value={record && record['countyDisputeFiledDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"Dispute Entered Date"} value={record && record['disputeEnteredDate']} />
        </div>
      </UimSection>
      <UimSection title={"County Dispute"}>
        <div className="uim-form-grid">
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"County Dispute Outcome"} value={record && record['countyDisputeOutcome']} />
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
        </div>
      </UimSection>
      <UimSection title={"Dispute Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"County Dispute User Name"} value={record && record['countyDisputeUserName']} />
          <UimField label={"County Dispute Outcome Date"} value={record && record['countyDisputeOutcomeDate']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"County Dispute Outcome Entered Date"} value={record && record['countyDisputeOutcomeEnteredDate']} />
        </div>
      </UimSection>
      <UimSection title={"Dispute Supervisor"}>
        <div className="uim-form-grid">
          <UimField label={"Supervisor Dispute Outcome"} value={record && record['supervisorDisputeOutcome']} />
          <UimField label={"Dispute Supervisor Name"} value={record && record['disputeSupervisorName']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
          <UimField label={"County Dispute Letter Date"} value={record && record['countyDisputeLetterDate']} />
        </div>
      </UimSection>
      <UimSection title={"State Review"}>
        <div className="uim-form-grid">
          <UimField label={"State Review Filed Date"} value={record && record['stateReviewFiledDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"State Review Entered Date"} value={record && record['stateReviewEnteredDate']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"State Review Outcome"} value={record && record['stateReviewOutcome']} />
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
          <UimField label={"State Review User Name"} value={record && record['stateReviewUserName']} />
        </div>
      </UimSection>
      <UimSection title={"State Review Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"State Review Outcome Date"} value={record && record['stateReviewOutcomeDate']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"State Review Outcome Entered Date"} value={record && record['stateReviewOutcomeEnteredDate']} />
          <UimField label={"State Supervisor Review Outcome"} value={record && record['stateSupervisorReviewOutcome']} />
        </div>
      </UimSection>
      <UimSection title={"State Supervisor Review"}>
        <div className="uim-form-grid">
          <UimField label={"State Supervisor Review Outcome Date"} value={record && record['stateSupervisorReviewOutcomeDate']} />
          <UimField label={"State Supervisor User Name"} value={record && record['stateSupervisorUserName']} />
          <UimField label={"State Supervisor Review Outcome Entered Date"} value={record && record['stateSupervisorReviewOutcomeEnteredDate']} />
          <UimField label={"State Review Letter Date"} value={record && record['stateReviewLetterDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: CDSS Review')}>CDSS Review</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-overtime-violation-tracking')}>Tracking</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-violation-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/modify-county-review')}>County Review</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/modify-county-supervisor-review')}>Supervisor Review</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-county-review-comments')}>Comments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-violation-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/modify-county-dispute')}>County Dispute</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/modify-dispute-supervisor')}>Dispute Supervisor</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-county-review-comments')}>Comments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-violation-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: State Review')}>State Review</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/modify-state-supervisor-review')}>State Supervisor Review</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-county-review-comments')}>Comments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-violation-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewOvertimeViolationsPage;
