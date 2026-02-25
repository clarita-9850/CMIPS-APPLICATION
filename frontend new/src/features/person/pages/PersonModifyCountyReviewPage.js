import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonModifyCountyReviewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_modifyCountyReview"}
      title={"Modify County Review:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"County Review"}>
        <div className="uim-form-grid">
          <UimField label={"County Review Outcome"} value={record && record['countyReviewOutcome']} />
          <UimField label={"County Review Outcome Date"} value={record && record['countyReviewOutcomeDate']} />
          <UimField label={"Supervisor Review Outcome"} value={record && record['supervisorReviewOutcome']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"County Review Letter Date"} value={record && record['countyReviewLetterDate']} />
          <UimField label={"Date Training Completed"} value={record && record['dateTrainingCompleted']} />
          <UimField label={"County Reviewer Name"} value={record && record['countyReviewerName']} />
          <UimField label={"County Review Outcome Entered Date"} value={record && record['countyReviewOutcomeEnteredDate']} />
          <UimField label={"Supervisor Name"} value={record && record['supervisorName']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
          <UimField label={"Training Letter Date"} value={record && record['trainingLetterDate']} />
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

export default PersonModifyCountyReviewPage;
