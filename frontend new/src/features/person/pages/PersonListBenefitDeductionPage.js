import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'add Provider Benefits', route: '/person/add-provider-benefits' }
  ];

export function PersonListBenefitDeductionPage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listBenefitDeduction"}
      title={"Benefit Deduction:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Deduction Plan"} value={record && record['deductionPlan']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListBenefitDeductionPage;
