import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'edit Wage Rate', route: '/county/edit-wage-rate' },
    { label: 'list Wage Rate', route: '/county/list-wage-rate' }
  ];

export function CountyViewWageRatePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('county', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"County_viewWageRate"}
      title={"View Wage Rate:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Rate"} value={record && record['rate']} />
          <UimField label={"Wage"} value={record && record['wage']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Non-State Share Over"} value={record && record['nonStateShareOver']} />
        </div>
      </UimSection>
      <UimSection title={"Health Benefits"}>
        <div className="uim-form-grid">
          <UimField label={"Admin Rate"} value={record && record['adminRate']} />
          <UimField label={"Estimated Taxes"} value={record && record['estimatedTaxes']} />
          <UimField label={"State Shared"} value={record && record['stateShared']} />
          <UimField label={"Non-State Share Over"} value={record && record['nonStateShareOver']} />
          <UimField label={"Non-Health Benefits"} value={record && record['nonHealthBenefits']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CountyViewWageRatePage;
