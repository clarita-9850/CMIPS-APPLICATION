import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overtime Violations For C D S S', route: '/person/view-overtime-violations-for-cdss' }
  ];

export function PersonModifyCDSSSupervisorReviewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_modifyCDSSSupervisorReview"}
      title={"Modify CDSS Supervisor Review:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"CDSS Review"}>
        <div className="uim-form-grid">
          <UimField label={"CDSS Review Filed Date"} value={record && record['cDSSReviewFiledDate']} />
          <UimField label={"Unanticipated Need"} value={record && record['unanticipatedNeed']} />
          <UimField label={"Immediate Need"} value={record && record['immediateNeed']} />
          <UimField label={"CDSS Review Entered Date"} value={record && record['cDSSReviewEnteredDate']} />
          <UimField label={"Health and Safety Issue"} value={record && record['healthAndSafetyIssue']} />
          <UimField label={"Other"} value={record && record['other']} />
          <UimField label={"CDSS Review Outcome"} value={record && record['cDSSReviewOutcome']} />
          <UimField label={"Override Reason"} value={record && record['overrideReason']} />
        </div>
      </UimSection>
      <UimSection title={"CDSS Review Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"Upheld - Need not unanticipated"} value={record && record['upheldNeedNotUnanticipated']} />
          <UimField label={"CDSS Review User Name"} value={record && record['cDSSReviewUserName']} />
          <UimField label={"CDSS Review Outcome Date"} value={record && record['cDSSReviewOutcomeDate']} />
          <UimField label={"Upheld - Need not immediate"} value={record && record['upheldNeedNotImmediate']} />
          <UimField label={"Upheld - No Health or Safety Issue"} value={record && record['upheldNoHealthOrSafetyIssue']} />
          <UimField label={"CDSS Review Outcome Entered Date"} value={record && record['cDSSReviewOutcomeEnteredDate']} />
          <UimField label={"Date Training Completed"} value={record && record['dateTrainingCompleted']} />
          <UimField label={"CDSS Supervisor Review Outcome"} value={record && record['cDSSSupervisorReviewOutcome']} />
        </div>
      </UimSection>
      <UimSection title={"CDSS Supervisor Review"}>
        <div className="uim-form-grid">
          <UimField label={"CDSS Supervisor Review User Name"} value={record && record['cDSSSupervisorReviewUserName']} />
          <UimField label={"CDSS Review Letter Date"} value={record && record['cDSSReviewLetterDate']} />
          <UimField label={"CDSS Supervisor Review Outcome Date"} value={record && record['cDSSSupervisorReviewOutcomeDate']} />
          <UimField label={"CDSS Supervisor Review Outcome Entered Date"} value={record && record['cDSSSupervisorReviewOutcomeEnteredDate']} />
          <UimField label={"CDSS Review Letter Entered Date"} value={record && record['cDSSReviewLetterEnteredDate']} />
          <UimField label={"CDSS Supervisor Review Comments"} value={record && record['cDSSSupervisorReviewComments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonModifyCDSSSupervisorReviewPage;
