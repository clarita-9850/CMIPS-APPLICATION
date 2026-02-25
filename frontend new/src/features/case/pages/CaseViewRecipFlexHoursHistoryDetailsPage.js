import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Recip Flex Hours History', route: '/case/list-recip-flex-hours-history' }
  ];

export function CaseViewRecipFlexHoursHistoryDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewRecipFlexHoursHistoryDetails"}
      title={"View Recipient Flexible Hours History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Recipient Flexible Hours History Details"}>
        <div className="uim-form-grid">
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Year"} value={record && record['year']} />
          <UimField label={"Frequency"} value={record && record['frequency']} />
          <UimField label={"Flexible Hours End Date"} value={record && record['flexibleHoursEndDate']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Recipient Requested Hours"} value={record && record['recipientRequestedHours']} />
          <UimField label={"County Approved Hours"} value={record && record['countyApprovedHours']} />
          <UimField label={"Recipient Request Date"} value={record && record['recipientRequestDate']} />
          <UimField label={"Approved"} value={record && record['approved']} />
          <UimField label={"Need Not Unanticipated"} value={record && record['needNotUnanticipated']} />
          <UimField label={"Need Not Immediate"} value={record && record['needNotImmediate']} />
          <UimField label={"No Health or Safety Issue"} value={record && record['noHealthOrSafetyIssue']} />
          <UimField label={"Request Outcome Date"} value={record && record['requestOutcomeDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Created On"} value={record && record['createdOn']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewRecipFlexHoursHistoryDetailsPage;
