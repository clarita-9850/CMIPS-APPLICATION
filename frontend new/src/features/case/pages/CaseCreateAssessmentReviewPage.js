import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Assessment Review', route: '/case/list-assessment-review' }
  ];

export function CaseCreateAssessmentReviewPage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  return (
    <UimPageLayout
      pageId={"Case_createAssessmentReview"}
      title={"Create QA Assessment Review"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Create QA Assessment Review"}>
        <div className="uim-form-grid">
          <UimField label={"QA Reviewer Name"} />
          <UimField label={"Review Type"} />
          <UimField label={"Corrective Action Due Date"} />
          <UimField label={"Targeted Review"} />
          <UimField label={"Date Review Completed"} />
          <UimField label={"Case Remediation Date"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseCreateAssessmentReviewPage;
