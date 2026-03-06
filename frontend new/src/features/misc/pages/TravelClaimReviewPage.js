import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'reject Travel Claim', route: '/case/reject-travel-claim' },
    { label: 'view Work Queue List', route: '/task-management/view-work-queue-list' }
  ];

export function TravelClaimReviewPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"TravelClaim_Review"}
      title={"Review Travel Claim"}
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
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Service Period From"} value={record && record['servicePeriodFrom']} />
          <UimField label={"Service Period To"} value={record && record['servicePeriodTo']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
          <UimField label={"Total (HH:MM)"} value={record && record['totalHHMM']} />
          <UimField label={"Total (HH:MM)"} value={record && record['totalHHMM']} />
          <UimField label={"Code"} value={record && record['code']} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries [HH:MM]"}>
        <div className="uim-form-grid">
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
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.update(id, { status: 'denied' }).then(() => { alert('Denied successfully'); navigate(-1); }).catch(err => alert('Action failed: ' + err.message)); }}>Reject</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default TravelClaimReviewPage;
