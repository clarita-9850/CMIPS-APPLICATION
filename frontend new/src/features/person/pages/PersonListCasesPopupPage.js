import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'user Home Popup', route: '/organization/user-home-popup' },
    { label: 'user Home', route: '/organization/user-home' }
  ];

export function PersonListCasesPopupPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listCasesPopup"}
      title={"Cases:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Case Status"} value={record && record['caseStatus']} />
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Case Status"} value={record && record['caseStatus']} />
          <UimField label={"IHSS Authorized Hours"} value={record && record['iHSSAuthorizedHours']} />
          <UimField label={"WPCS Authorized Hours"} value={record && record['wPCSAuthorizedHours']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Provider IHSS Assigned Hours"} value={record && record['providerIHSSAssignedHours']} />
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Case Status"} value={record && record['caseStatus']} />
          <UimField label={"IHSS Authorized Hours"} value={record && record['iHSSAuthorizedHours']} />
          <UimField label={"WPCS Authorized Hours"} value={record && record['wPCSAuthorizedHours']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Provider IHSS Assigned Hours"} value={record && record['providerIHSSAssignedHours']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListCasesPopupPage;
