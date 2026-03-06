import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Recip Flex Hours', route: '/case/modify-recip-flex-hours' },
    { label: 'modify Recip Flex Hours After Pending', route: '/case/modify-recip-flex-hours-after-pending' },
    { label: 'list Recip Flex Hours', route: '/case/list-recip-flex-hours' }
  ];

export function CaseViewRecipFlexHoursPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewRecipFlexHours"}
      title={"View Recipient Flexible Hours:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"View Recipient Flexible Hours"}>
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
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewRecipFlexHoursPage;
