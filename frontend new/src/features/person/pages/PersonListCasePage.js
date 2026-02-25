import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'home', route: '/case/ihss-case-home' },
    { label: 'user Home Popup', route: '/organization/user-home-popup' }
  ];

export function PersonListCasePage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listCase"}
      title={"Cases:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"Case Status"} value={record && record['caseStatus']} />
          <UimField label={"Auth Hours"} value={record && record['authHours']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Assigned Hours"} value={record && record['assignedHours']} />
          <UimField label={"PDD Status"} value={record && record['pDDStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Case List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
    </UimPageLayout>
  );
}

export default PersonListCasePage;
