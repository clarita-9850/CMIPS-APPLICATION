import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view C P Training Claim', route: '/person/view-cp-training-claim' },
    { label: 'view C P Claim Snapshot', route: '/person/view-cp-claim-snapshot' }
  ];

export function PersonViewCPClaimHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCPClaimHistory"}
      title={"Training Time Claim History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Number"} value={record && record['recipientNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Hours Claimed"} value={record && record['hoursClaimed']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date Updated"} value={record && record['dateUpdated']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
        </div>
      </UimSection>
      <UimSection title={"Training Time Claim History List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Training Time Claim Details')}>View Training Time Claim Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: History')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-cp-training-claim')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewCPClaimHistoryPage;
