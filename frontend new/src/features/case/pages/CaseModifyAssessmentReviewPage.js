import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Assessment Review', route: '/case/list-assessment-review' }
  ];

export function CaseModifyAssessmentReviewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyAssessmentReview"}
      title={"Modify QA Assessment Review"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Modify QA Assessment Review"}>
        <div className="uim-form-grid">
          <UimField label={"QA Reviewer Name"} value={record && record['qAReviewerName']} />
          <UimField label={"Review Type"} value={record && record['reviewType']} />
          <UimField label={"Corrective Action Due Date"} value={record && record['correctiveActionDueDate']} />
          <UimField label={"Targeted Review"} value={record && record['targetedReview']} />
          <UimField label={"Date Review Completed"} value={record && record['dateReviewCompleted']} />
          <UimField label={"Case Remediation Date"} value={record && record['caseRemediationDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyAssessmentReviewPage;
