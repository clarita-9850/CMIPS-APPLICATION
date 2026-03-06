import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'display Travel Time', route: '/person/display-travel-time' },
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function PersonListTravelTimePage() {
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listTravelTime"}
      title={"Travel Time Recipient Case:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Action"} value={record && record['action']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Provider Type"} value={record && record['providerType']} />
          <UimField label={"Provider Status"} value={record && record['providerStatus']} />
          <UimField label={"Weekly Travel Time"} value={record && record['weeklyTravelTime']} />
          <UimField label={"Sun"} value={record && record['sun']} />
          <UimField label={"Mon"} value={record && record['mon']} />
          <UimField label={"Tues"} value={record && record['tues']} />
          <UimField label={"Wed"} value={record && record['wed']} />
          <UimField label={"Thurs"} value={record && record['thurs']} />
          <UimField label={"Fri"} value={record && record['fri']} />
          <UimField label={"Sat"} value={record && record['sat']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default PersonListTravelTimePage;
