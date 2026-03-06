import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view C P Claim History', route: '/person/view-cp-claim-history' }
  ];

export function PersonViewCPClaimSnapshotPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCPClaimSnapshot"}
      title={"View Training Time Claim History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Claim Number"} value={record && record['claimNumber']} />
          <UimField label={"Provider Name"} value={record && record['providerName']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Program"} value={record && record['program']} />
          <UimField label={"Received Date"} value={record && record['receivedDate']} />
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Recipient Number"} value={record && record['recipientNumber']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Submitted By"} value={record && record['submittedBy']} />
          <UimField label={"Approved By/Rejected By"} value={record && record['approvedByRejectedBy']} />
          <UimField label={"Submitted By"} value={record && record['submittedBy']} />
          <UimField label={"Rejected Comments"} value={record && record['rejectedComments']} />
          <UimField label={"Approved By/Rejected By"} value={record && record['approvedByRejectedBy']} />
          <UimField label={"Comments"} value={record && record['comments']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewCPClaimSnapshotPage;
