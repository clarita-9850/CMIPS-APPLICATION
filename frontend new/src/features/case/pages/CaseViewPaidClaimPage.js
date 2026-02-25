import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit Paid Claim', route: '/case/edit-paid-claim' },
    { label: 'list Paid Claims', route: '/case/list-paid-claims' }
  ];

export function CaseViewPaidClaimPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewPaidClaim"}
      title={"View Paid Claim"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Paid Claim"}>
        <div className="uim-form-grid">
          <UimField label={"Service Type"} value={record && record['serviceType']} />
          <UimField label={"Service Start Date"} value={record && record['serviceStartDate']} />
          <UimField label={"Amount Paid"} value={record && record['amountPaid']} />
          <UimField label={"Medi-Cal Provider Name"} value={record && record['mediCalProviderName']} />
        </div>
      </UimSection>
      <UimSection title={"Outcome"}>
        <div className="uim-form-grid">
          <UimField label={"Service End Date"} value={record && record['serviceEndDate']} />
          <UimField label={"Outcome"} value={record && record['outcome']} />
          <UimField label={"Overpayment Amount"} value={record && record['overpaymentAmount']} />
          <UimField label={"Outcome Date"} value={record && record['outcomeDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewPaidClaimPage;
