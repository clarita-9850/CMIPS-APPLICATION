import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' },
    { label: 'modify C D S S Review', route: '/person/modify-cdss-review' },
    { label: 'modify C D S S Review After Pending', route: '/person/modify-cdss-review-after-pending' },
    { label: 'modify C D S S Supervisor Review', route: '/person/modify-cdss-supervisor-review' },
    { label: 'modify C D S S Supervisor Review After Pending', route: '/person/modify-cdss-supervisor-review-after-pending' },
    { label: 'view C D S S Review Comments', route: '/person/view-cdss-review-comments' },
    { label: 'view C D S S Review History', route: '/person/view-cdss-review-history' }
  ];

export function PersonViewOvertimeViolationsForCDSSPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewOvertimeViolationsForCDSS"}
      title={"View Overtime Violation - CDSS Review:"}
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
          <UimField label={"Violation Status"} value={record && record['violationStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Next Possible Violation Date"} value={record && record['nextPossibleViolationDate']} />
          <UimField label={"Violation Reduction Letter Date"} value={record && record['violationReductionLetterDate']} />
          <UimField label={"Exceeds Travel Time"} value={record && record['exceedsTravelTime']} />
          <UimField label={"Exceeds Weekly Maximum"} value={record && record['exceedsWeeklyMaximum']} />
          <UimField label={"Violation Number"} value={record && record['violationNumber']} />
          <UimField label={"Violation Date"} value={record && record['violationDate']} />
        </div>
      </UimSection>
      <UimSection title={"CDSS Review"}>
        <div className="uim-form-grid">
          <UimField label={"Violation Status Date"} value={record && record['violationStatusDate']} />
          <UimField label={"Ineligible Begin Date"} value={record && record['ineligibleBeginDate']} />
          <UimField label={"Ineligible End Date"} value={record && record['ineligibleEndDate']} />
          <UimField label={"CDSS Review Filed Date"} value={record && record['cDSSReviewFiledDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
        </div>
      </UimSection>
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"CDSS Review Entered Date"} value={record && record['cDSSReviewEnteredDate']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"CDSS Review Outcome"} value={record && record['cDSSReviewOutcome']} />
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
        </div>
      </UimSection>
      <UimSection title={"CDSS Review Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"CDSS Review User Name"} value={record && record['cDSSReviewUserName']} />
          <UimField label={"CDSS Review Outcome Date"} value={record && record['cDSSReviewOutcomeDate']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"CDSS Review Outcome Entered Date"} value={record && record['cDSSReviewOutcomeEnteredDate']} />
          <UimField label={"Date Training Completed"} value={record && record['dateTrainingCompleted']} />
        </div>
      </UimSection>
      <UimSection title={"CDSS Supervisor Review"}>
        <div className="uim-form-grid">
          <UimField label={"CDSS Supervisor Outcome"} value={record && record['cDSSSupervisorOutcome']} />
          <UimField label={"CDSS Supervisor User Name"} value={record && record['cDSSSupervisorUserName']} />
          <UimField label={"CDSS Review Letter Date"} value={record && record['cDSSReviewLetterDate']} />
          <UimField label={"CDSS Supervisor Outcome Date"} value={record && record['cDSSSupervisorOutcomeDate']} />
          <UimField label={"CDSS Supervisor Outcome Entered Date"} value={record && record['cDSSSupervisorOutcomeEnteredDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: CDSS Review')}>CDSS Review</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: CDSS Supervisor')}>CDSS Supervisor</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-cdss-review-comments')}>Comments</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-cdss-review-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewOvertimeViolationsForCDSSPage;
