import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'create County Contractor Rate', route: '/county/create-county-contractor-rate' },
    { label: 'view County Contractor Rate', route: '/county/view-county-contractor-rate' },
    { label: 'edit County Contractor Rate', route: '/county/edit-county-contractor-rate' }
  ];

export function CountyListCountyContractorRatePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('county', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"County_listCountyContractorRate"}
      title={"County Contractor Rate:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Select County"}>
        <div className="uim-form-grid">
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Contractor Name"} value={record && record['contractorName']} />
          <UimField label={"Effective Date"} value={record && record['effectiveDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Rate"} value={record && record['rate']} />
          <UimField label={"Wage"} value={record && record['wage']} />
          <UimField label={"MACR"} value={record && record['mACR']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Get County Contractor Rates')}>Get County Contractor Rates</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New')}>New</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/county/view-county-contractor-rate')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CountyListCountyContractorRatePage;
