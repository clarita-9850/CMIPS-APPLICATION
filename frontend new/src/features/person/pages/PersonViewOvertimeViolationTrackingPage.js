import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Overtime Violations', route: '/person/view-overtime-violations' }
  ];

export function PersonViewOvertimeViolationTrackingPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewOvertimeViolationTracking"}
      title={"Violation Tracking:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Violation Tracking"}>
        <div className="uim-form-grid">
          <UimField label={"Violation Date"} value={record && record['violationDate']} />
          <UimField label={"Violation Count"} value={record && record['violationCount']} />
          <UimField label={"Violation Status"} value={record && record['violationStatus']} />
          <UimField label={"CO Outcome Due Date"} value={record && record['cOOutcomeDueDate']} />
          <UimField label={"CO Outcome Entered Date"} value={record && record['cOOutcomeEnteredDate']} />
          <UimField label={"CO Outcome Date"} value={record && record['cOOutcomeDate']} />
          <UimField label={"Sup Due Date"} value={record && record['supDueDate']} />
          <UimField label={"Sup Entered Date"} value={record && record['supEnteredDate']} />
        </div>
      </UimSection>
      <UimSection title={"County Review"}>
        <div className="uim-form-grid">
          <UimField label={"Sup Outcome Date"} value={record && record['supOutcomeDate']} />
          <UimField label={"Training Completed Due Date"} value={record && record['trainingCompletedDueDate']} />
          <UimField label={"Training Completed Entered Date"} value={record && record['trainingCompletedEnteredDate']} />
          <UimField label={"Training Completed Date"} value={record && record['trainingCompletedDate']} />
          <UimField label={"Dispute Filing Due Date"} value={record && record['disputeFilingDueDate']} />
          <UimField label={"Dispute Entry Due Date"} value={record && record['disputeEntryDueDate']} />
          <UimField label={"Dispute Outcome Due Date"} value={record && record['disputeOutcomeDueDate']} />
          <UimField label={"Dispute Filed Date"} value={record && record['disputeFiledDate']} />
        </div>
      </UimSection>
      <UimSection title={"County Dispute"}>
        <div className="uim-form-grid">
          <UimField label={"Dispute Entered Date"} value={record && record['disputeEnteredDate']} />
          <UimField label={"Dispute Outcome Date"} value={record && record['disputeOutcomeDate']} />
          <UimField label={"Dispute Outcome Entered Date"} value={record && record['disputeOutcomeEnteredDate']} />
          <UimField label={"Sup Dispute Due Date"} value={record && record['supDisputeDueDate']} />
          <UimField label={"Sup Outcome Entered Date"} value={record && record['supOutcomeEnteredDate']} />
          <UimField label={"Sup Outcome Date"} value={record && record['supOutcomeDate']} />
          <UimField label={"SAR Filing Due Date"} value={record && record['sARFilingDueDate']} />
          <UimField label={"SAR Outcome Due Date"} value={record && record['sAROutcomeDueDate']} />
        </div>
      </UimSection>
      <UimSection title={"State Administrative Review"}>
        <div className="uim-form-grid">
          <UimField label={"SAR Filed Date"} value={record && record['sARFiledDate']} />
          <UimField label={"SAR Outcome Date"} value={record && record['sAROutcomeDate']} />
          <UimField label={"SAR Outcome Entered Date"} value={record && record['sAROutcomeEnteredDate']} />
          <UimField label={"SAR Entered Date"} value={record && record['sAREnteredDate']} />
          <UimField label={"SAR Sup Rvw Due Date"} value={record && record['sARSupRvwDueDate']} />
          <UimField label={"SAR Sup Outcome Date"} value={record && record['sARSupOutcomeDate']} />
          <UimField label={"SAR Sup Outcome Entered Date"} value={record && record['sARSupOutcomeEnteredDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewOvertimeViolationTrackingPage;
