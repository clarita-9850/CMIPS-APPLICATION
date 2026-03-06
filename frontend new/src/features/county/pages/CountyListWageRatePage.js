import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create Wage Rate', route: '/county/create-wage-rate' },
    { label: 'view Wage Rate', route: '/county/view-wage-rate' },
    { label: 'edit Wage Rate', route: '/county/edit-wage-rate' }
  ];

export function CountyListWageRatePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('county', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"County_listWageRate"}
      title={"Public Authority Wage Rate:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Select County"}>
        <div className="uim-form-grid">
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Rate"} value={record && record['rate']} />
          <UimField label={"Wage"} value={record && record['wage']} />
          <UimField label={"Health Benefit State Shared"} value={record && record['healthBenefitStateShared']} />
          <UimField label={"Health Benefit Not State Shared"} value={record && record['healthBenefitNotStateShared']} />
          <UimField label={"Non-Health Benefits"} value={record && record['nonHealthBenefits']} />
          <UimField label={"Admin Rate"} value={record && record['adminRate']} />
          <UimField label={"Estimated Taxes"} value={record && record['estimatedTaxes']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Get Public Authority Wage Rates')}>Get Public Authority Wage Rates</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/county/view-wage-rate')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CountyListWageRatePage;
