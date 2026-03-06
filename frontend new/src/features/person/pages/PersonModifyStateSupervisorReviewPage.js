import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function PersonModifyStateSupervisorReviewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_modifyStateSupervisorReview"}
      title={"Modify State Supervisor Review:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"State Review"}>
        <div className="uim-form-grid">
          <UimField label={"State Review Filed Date"} value={record && record['stateReviewFiledDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"State Review Entered Date"} value={record && record['stateReviewEnteredDate']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"State Review Outcome"} value={record && record['stateReviewOutcome']} />
        </div>
      </UimSection>
      <UimSection title={"State Outcome Review"}>
        <div className="uim-form-grid">
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
          <UimField label={"State Review User Name"} value={record && record['stateReviewUserName']} />
          <UimField label={"State Review Outcome Date"} value={record && record['stateReviewOutcomeDate']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"State Review Outcome Entered Date"} value={record && record['stateReviewOutcomeEnteredDate']} />
        </div>
      </UimSection>
      <UimSection title={"State Supervisor Review"}>
        <div className="uim-form-grid">
          <UimField label={"State Supervisor Review Outcome"} value={record && record['stateSupervisorReviewOutcome']} />
          <UimField label={"State Supervisor Review Outcome Date"} value={record && record['stateSupervisorReviewOutcomeDate']} />
          <UimField label={"State Supervisor User Name"} value={record && record['stateSupervisorUserName']} />
          <UimField label={"State Supervisor Review Outcome Entered Date"} value={record && record['stateSupervisorReviewOutcomeEnteredDate']} />
          <UimField label={"State Review Letter Date"} value={record && record['stateReviewLetterDate']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonModifyStateSupervisorReviewPage;
