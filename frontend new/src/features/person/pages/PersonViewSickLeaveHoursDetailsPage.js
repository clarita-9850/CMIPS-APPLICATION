import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Payment Details', route: '/payment/view-payment-details' },
    { label: 'resolve Reissue Sick Leave Claim', route: '/person/resolve-reissue-sick-leave-claim' },
    { label: 'list Sick Leave Hours', route: '/person/list-sick-leave-hours' }
  ];

export function PersonViewSickLeaveHoursDetailsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewSickLeaveHoursDetails"}
      title={"View Sick Leave Hours Details:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient"} value={record && record['recipient']} />
          <UimField label={"Claim Number"} value={record && record['claimNumber']} />
          <UimField label={"Time Entry Type"} value={record && record['timeEntryType']} />
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Service Period Begin Date"} value={record && record['servicePeriodBeginDate']} />
          <UimField label={"Claimed Sick Leave Hours"} value={record && record['claimedSickLeaveHours']} />
          <UimField label={"Paid Sick Leave Hours"} value={record && record['paidSickLeaveHours']} />
          <UimField label={"Sick Leave Claim Status"} value={record && record['sickLeaveClaimStatus']} />
          <UimField label={"Warrant Status"} value={record && record['warrantStatus']} />
          <UimField label={"Void Reason"} value={record && record['voidReason']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/payment/view-payment-details')}>View Payment</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/resolve-reissue-sick-leave-claim')}>Reissue Sick Leave</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewSickLeaveHoursDetailsPage;
