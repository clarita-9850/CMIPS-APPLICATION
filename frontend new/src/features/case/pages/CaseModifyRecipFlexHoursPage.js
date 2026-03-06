import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'list Recip Flex Hours', route: '/case/list-recip-flex-hours' }
  ];

export function CaseModifyRecipFlexHoursPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_modifyRecipFlexHours"}
      title={"Modify Recipient Flexible Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Modify Recipient Flexible Hours"}>
        <div className="uim-form-grid">
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Recipient Requested Hours"} value={record && record['recipientRequestedHours']} />
          <UimField label={"Recipient Request Date"} value={record && record['recipientRequestDate']} />
          <UimField label={"Request Outcome Date"} value={record && record['requestOutcomeDate']} />
          <UimField label={"Outcome Letter Date"} value={record && record['outcomeLetterDate']} />
          <UimField label={"Year"} value={record && record['year']} />
          <UimField label={"Frequency"} value={record && record['frequency']} />
        </div>
      </UimSection>
      <UimSection title={"Request Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"County Approved Hours"} value={record && record['countyApprovedHours']} />
          <UimField label={"Flexible Hours End Date"} value={record && record['flexibleHoursEndDate']} />
          <UimField label={"Cancellation Letter Date"} value={record && record['cancellationLetterDate']} />
          <UimField label={"Approved"} value={record && record['approved']} />
          <UimField label={"Denied - Need not unanticipated"} value={record && record['deniedNeedNotUnanticipated']} />
          <UimField label={"Denied - Need not immediate"} value={record && record['deniedNeedNotImmediate']} />
          <UimField label={"Denied - No Health or Safety Issue"} value={record && record['deniedNoHealthOrSafetyIssue']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseModifyRecipFlexHoursPage;
