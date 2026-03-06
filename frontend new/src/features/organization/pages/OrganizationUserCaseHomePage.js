import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function OrganizationUserCaseHomePage() {
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_userCaseHome"}
      title={"My Cases"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient"} value={record && record['recipient']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"City"} value={record && record['city']} />
          <UimField label={"Zip"} value={record && record['zip']} />
          <UimField label={"Language"} value={record && record['language']} />
          <UimField label={"Date Of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Companion"} value={record && record['companion']} />
          <UimField label={"Assigned"} value={record && record['assigned']} />
          <UimField label={"Reassessment Date"} value={record && record['reassessmentDate']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
        </div>
      </UimSection>
      <UimSection title={"Caseload"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
    </UimPageLayout>
  );
}

export default OrganizationUserCaseHomePage;
