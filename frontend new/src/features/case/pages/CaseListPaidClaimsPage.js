import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Paid Claim', route: '/case/view-paid-claim' },
    { label: 'edit Paid Claim', route: '/case/edit-paid-claim' }
  ];

export function CaseListPaidClaimsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listPaidClaims"}
      title={"Paid Claims List:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"Service Start Date"} value={record && record['serviceStartDate']} />
          <UimField label={"Service End Date"} value={record && record['serviceEndDate']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
          <UimField label={"Outcome Date"} value={record && record['outcomeDate']} />
          <UimField label={"QA Reviewer Name"} value={record && record['qAReviewerName']} />
        </div>
      </UimSection>
      <UimSection title={"Medi-Cal Paid Claims"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-paid-claim')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListPaidClaimsPage;
