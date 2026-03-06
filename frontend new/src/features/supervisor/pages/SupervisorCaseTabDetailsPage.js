import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'user Workspace', route: '/supervisor/user-workspace' },
    { label: 'resolve Case Home', route: '/case/resolve-case-home' }
  ];

export function SupervisorCaseTabDetailsPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_caseTabDetails"}
      title={"Workspace - Case"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Primary Client"} value={record && record['primaryClient']} />
          <UimField label={"Owner Type"} value={record && record['ownerType']} />
          <UimField label={"Supervisor"} value={record && record['supervisor']} />
          <UimField label={"Case Reference"} value={record && record['caseReference']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Owner"} value={record && record['owner']} />
          <UimField label={"Supervisor Email"} value={record && record['supervisorEmail']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorCaseTabDetailsPage;
