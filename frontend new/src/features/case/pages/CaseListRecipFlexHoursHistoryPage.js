import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Recip Flex Hours History Details', route: '/case/view-recip-flex-hours-history-details' },
    { label: 'list Recip Flex Hours', route: '/case/list-recip-flex-hours' }
  ];

export function CaseListRecipFlexHoursHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listRecipFlexHoursHistory"}
      title={"Recipient Flexible Hours History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Month"} value={record && record['month']} />
          <UimField label={"Year"} value={record && record['year']} />
          <UimField label={"Frequency"} value={record && record['frequency']} />
          <UimField label={"Flexible Hours End Date"} value={record && record['flexibleHoursEndDate']} />
          <UimField label={"Program Type"} value={record && record['programType']} />
          <UimField label={"Recipient Requested Hours"} value={record && record['recipientRequestedHours']} />
          <UimField label={"County Approved Hours"} value={record && record['countyApprovedHours']} />
          <UimField label={"Approved"} value={record && record['approved']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Last Updated"} value={record && record['lastUpdated']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-recip-flex-hours-history-details')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListRecipFlexHoursHistoryPage;
