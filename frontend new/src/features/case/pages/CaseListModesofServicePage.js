import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'home', route: '/case/ihss-case-home' },
    { label: 'view Modesof Service', route: '/case/view-modesof-service' },
    { label: 'edit Modesof Service', route: '/case/edit-modesof-service' }
  ];

export function CaseListModesofServicePage() {
  const navigate = useNavigate();
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_listModesofService"}
      title={"Modes of Service:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Start Date Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
          <UimField label={"IP Hours"} value={record && record['iPHours']} />
          <UimField label={"CC Hours"} value={record && record['cCHours']} />
          <UimField label={"HM/PAC Hours"} value={record && record['hMPACHours']} />
          <UimField label={"Auth to Purchase"} value={record && record['authToPurchase']} />
          <UimField label={"Case Cost"} value={record && record['caseCost']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"IP Hours"} value={record && record['iPHours']} />
          <UimField label={"CC Hours"} value={record && record['cCHours']} />
          <UimField label={"HM/PAC Hours"} value={record && record['hMPACHours']} />
          <UimField label={"Auth to Purchase"} value={record && record['authToPurchase']} />
          <UimField label={"Case Cost"} value={record && record['caseCost']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-modesof-service')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/case/view-modesof-service')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseListModesofServicePage;
