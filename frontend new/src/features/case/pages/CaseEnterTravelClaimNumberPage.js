import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Travel Claim Special Transaction', route: '/case/create-travel-claim-special-transaction' }
  ];

export function CaseEnterTravelClaimNumberPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_enterTravelClaimNumber"}
      title={"Travel Claim Manual Entry - Travel Claim Number:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Enter Travel Claim Number"}>
        <div className="uim-form-grid">
          <UimField label={"Travel Claim Number"} value={record && record['travelClaimNumber']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Navigating to next step')}>Continue</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseEnterTravelClaimNumberPage;
