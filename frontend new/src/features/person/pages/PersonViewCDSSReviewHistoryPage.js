import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view C D S S Review History Details', route: '/person/view-cdss-review-history-details' },
    { label: 'view Overtime Violations For C D S S', route: '/person/view-overtime-violations-for-cdss' }
  ];

export function PersonViewCDSSReviewHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCDSSReviewHistory"}
      title={"CDSS Review History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"CDSS Review Filed Date"} value={record && record['cDSSReviewFiledDate']} />
          <UimField label={"CDSS Review Entered Date"} value={record && record['cDSSReviewEnteredDate']} />
          <UimField label={"CDSS Review Outcome"} value={record && record['cDSSReviewOutcome']} />
          <UimField label={"CDSS Review User Name"} value={record && record['cDSSReviewUserName']} />
          <UimField label={"CDSS Supervisor Outcome"} value={record && record['cDSSSupervisorOutcome']} />
          <UimField label={"CDSS Supervisor Outcome Date"} value={record && record['cDSSSupervisorOutcomeDate']} />
          <UimField label={"CDSS Supervisor User Name"} value={record && record['cDSSSupervisorUserName']} />
          <UimField label={"CDSS Supervisor Outcome Entered Date"} value={record && record['cDSSSupervisorOutcomeEnteredDate']} />
          <UimField label={"Last Updated Date"} value={record && record['lastUpdatedDate']} />
          <UimField label={"Last Updated By"} value={record && record['lastUpdatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-cdss-review-history-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewCDSSReviewHistoryPage;
