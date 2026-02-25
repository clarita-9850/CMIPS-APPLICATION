import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Contractor Service Months', route: '/case/view-contractor-service-months' }
  ];

export function CaseListContractorServiceMonthsPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listContractorServiceMonths"}
      title={"County Contractor Service Month:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Contractor"} value={record && record['contractor']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Amount"} value={record && record['amount']} />
          <UimField label={"Service Month"} value={record && record['serviceMonth']} />
          <UimField label={"Contractor"} value={record && record['contractor']} />
          <UimField label={"Hours"} value={record && record['hours']} />
          <UimField label={"Amount"} value={record && record['amount']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-contractor-service-months')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-contractor-service-months')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListContractorServiceMonthsPage;
