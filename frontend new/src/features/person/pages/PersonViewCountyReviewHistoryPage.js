import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonViewCountyReviewHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCountyReviewHistory"}
      title={"County Review History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"County Review Outcome"} value={record && record['countyReviewOutcome']} />
          <UimField label={"County Reviewer Name"} value={record && record['countyReviewerName']} />
          <UimField label={"County Review Outcome Date"} value={record && record['countyReviewOutcomeDate']} />
          <UimField label={"County Review Outcome Entered Date"} value={record && record['countyReviewOutcomeEnteredDate']} />
          <UimField label={"Supervisor Review Outcome"} value={record && record['supervisorReviewOutcome']} />
          <UimField label={"Supervisor Name"} value={record && record['supervisorName']} />
          <UimField label={"Supervisor Outcome Date"} value={record && record['supervisorOutcomeDate']} />
          <UimField label={"Supervisor Outcome Entered Date"} value={record && record['supervisorOutcomeEnteredDate']} />
          <UimField label={"County Review Letter Date"} value={record && record['countyReviewLetterDate']} />
          <UimField label={"Date Training Completed"} value={record && record['dateTrainingCompleted']} />
          <UimField label={"Training Letter Date"} value={record && record['trainingLetterDate']} />
          <UimField label={"Last Updated Date"} value={record && record['lastUpdatedDate']} />
          <UimField label={"Last Updated By"} value={record && record['lastUpdatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewCountyReviewHistoryPage;
