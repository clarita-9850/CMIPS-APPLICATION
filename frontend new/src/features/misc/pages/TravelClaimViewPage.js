import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Work Queue List', route: '/task-management/view-work-queue-list' }
  ];

export function TravelClaimViewPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"TravelClaim_view"}
      title={"View Travel Claim"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Number"} value={record && record['providerNumber']} />
          <UimField label={"Recipient Number"} value={record && record['recipientNumber']} />
          <UimField label={"Travel Claim Number"} value={record && record['travelClaimNumber']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
          <UimField label={"Code"} value={record && record['code']} />
          <UimField label={"Description"} value={record && record['description']} />
        </div>
      </UimSection>
      <UimSection title={"Exceptions"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Image')}>View Image</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default TravelClaimViewPage;
