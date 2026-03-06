import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Assessment Review', route: '/case/create-assessment-review' },
    { label: 'modify Assessment Review', route: '/case/modify-assessment-review' }
  ];

export function CaseListAssessmentReviewPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listAssessmentReview"}
      title={"QA Assessment Review List:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Review Type"} value={record && record['reviewType']} />
          <UimField label={"Targeted Review"} value={record && record['targetedReview']} />
          <UimField label={"Date Review Completed"} value={record && record['dateReviewCompleted']} />
          <UimField label={"QA Reviewer Name"} value={record && record['qAReviewerName']} />
          <UimField label={"Corrective Action Due Date"} value={record && record['correctiveActionDueDate']} />
          <UimField label={"Case Remediation Date"} value={record && record['caseRemediationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Assessment Reviews"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListAssessmentReviewPage;
