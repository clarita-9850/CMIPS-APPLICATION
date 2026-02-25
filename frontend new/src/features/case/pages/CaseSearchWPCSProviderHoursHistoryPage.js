import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Provider', route: '/case/view-provider' },
    { label: 'view Provider W P C S Hours', route: '/case/view-provider-wpcs-hours' },
    { label: 'search I H S S Provider Hours History', route: '/case/search-ihss-provider-hours-history' },
    { label: 'view F P O Eligibility', route: '/case/view-fpo-eligibility' },
    { label: 'view W P C S D P R Hours History', route: '/case/view-wpcsdpr-hours-history' },
    { label: 'modify Provider W P C S Hours', route: '/case/modify-provider-wpcs-hours' }
  ];

export function CaseSearchWPCSProviderHoursHistoryPage() {
  const casesApi = getDomainApi('case');
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_searchWPCSProviderHoursHistory"}
      title={"View WPCS Provider Hours History:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Begin Date"}>
        <div className="uim-form-grid">
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
          <UimField label={"Begin Date"} value={record && record['beginDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"WPCS Authorized Hours"} value={record && record['wPCSAuthorizedHours']} />
          <UimField label={"Pay Rate"} value={record && record['payRate']} />
          <UimField label={"Differential Pay Rate"} value={record && record['differentialPayRate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <UimSection title={"Provider Hours"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Case Provider')}>View Case Provider</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Details')}>View WPCS Details</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View IHSS Provider Hours History')}>View IHSS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View WPCS Provider Hours History')}>View WPCS Provider Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: FPO Eligible')}>FPO Eligible</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { casesApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Differential Pay Rate Hours History')}>Differential Pay Rate Hours History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseSearchWPCSProviderHoursHistoryPage;
