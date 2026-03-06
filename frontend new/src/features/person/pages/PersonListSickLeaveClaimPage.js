import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Sick Leave Claim', route: '/person/view-sick-leave-claim' },
    { label: 'edit Sick Leave Claim', route: '/person/edit-sick-leave-claim' },
    { label: 'Cancel Sick Leave Claim', route: '/person/cancel-sick-leave-claim' },
    { label: 'home Page', route: '/person/home-page' }
  ];

export function PersonListSickLeaveClaimPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listSickLeaveClaim"}
      title={"Sick Leave Claim:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient case Number"} value={record && record['recipientCaseNumber']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Mode Of Entry"} value={record && record['modeOfEntry']} />
          <UimField label={"Sick Leave Claim Number"} value={record && record['sickLeaveClaimNumber']} />
          <UimField label={"Service Period Begin Date"} value={record && record['servicePeriodBeginDate']} />
          <UimField label={"Claimed Hours"} value={record && record['claimedHours']} />
          <UimField label={"Claim Entered Date"} value={record && record['claimEnteredDate']} />
          <UimField label={"Issued Date"} value={record && record['issuedDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-sick-leave-claim')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListSickLeaveClaimPage;
