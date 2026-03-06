import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit State Hearing', route: '/case/edit-state-hearing' }
  ];

export function CaseViewStateHearingPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewStateHearing"}
      title={"View State Hearing:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"State Hearing Request"}>
        <div className="uim-form-grid">
          <UimField label={"Number"} value={record && record['number']} />
          <UimField label={"Request Date"} value={record && record['requestDate']} />
          <UimField label={"Scheduled Hearing Date"} value={record && record['scheduledHearingDate']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Compliance Form Sent Date"} value={record && record['complianceFormSentDate']} />
          <UimField label={"Rescheduled Reason"} value={record && record['rescheduledReason']} />
          <UimField label={"Outcome Date"} value={record && record['outcomeDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Previous Scheduled Hearings"}>
        <div className="uim-form-grid">
          <UimField label={"Date"} value={record && record['date']} />
          <UimField label={"Rescheduled Reason"} value={record && record['rescheduledReason']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewStateHearingPage;
