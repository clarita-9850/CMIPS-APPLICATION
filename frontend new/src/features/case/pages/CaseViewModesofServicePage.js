import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Modesof Service History', route: '/case/list-modesof-service-history' },
    { label: 'edit Modesof Service', route: '/case/edit-modesof-service' },
    { label: 'list Modesof Service', route: '/case/list-modesof-service' }
  ];

export function CaseViewModesofServicePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_viewModesofService"}
      title={"View Modes of Service:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Individual Provider Hours (HH:MM)"} value={record && record['individualProviderHoursHHMM']} />
          <UimField label={"County Contractor Hours (HH:MM)"} value={record && record['countyContractorHoursHHMM']} />
          <UimField label={"Homemaker/PA Contract Hours (HH:MM)"} value={record && record['homemakerPAContractHoursHHMM']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Modes of Service Details')}>View Modes of Service Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/list-modesof-service-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseViewModesofServicePage;
