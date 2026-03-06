import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function CaseEditStateHearingPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_editStateHearing"}
      title={"Modify State Hearing:"}
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
          <UimField label={"Compliance Form Sent Date"} value={record && record['complianceFormSentDate']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Rescheduled Reason"} value={record && record['rescheduledReason']} />
          <UimField label={"Outcome Date"} value={record && record['outcomeDate']} />
          <UimField label={"Status"} value={record && record['status']} />
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
          <UimField label={"Scheduled Hearing Date"} value={record && record['scheduledHearingDate']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Outcome"} value={record && record['outcome']} />
          <UimField label={"Compliance Form Sent Date"} value={record && record['complianceFormSentDate']} />
          <UimField label={"Rescheduled Reason"} value={record && record['rescheduledReason']} />
          <UimField label={"Outcome Date"} value={record && record['outcomeDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseEditStateHearingPage;
