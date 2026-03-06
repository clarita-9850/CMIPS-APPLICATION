import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'home', route: '/case/ihss-case-home' }
  ];

export function SupervisorListUserCasesByOrgObjectPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_listUserCasesByOrgObject"}
      title={"Cases"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Authorization Start Date"} value={record && record['authorizationStartDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Reassessment Date"} value={record && record['reassessmentDate']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Case Name"} value={record && record['caseName']} />
          <UimField label={"Authorization Start Date"} value={record && record['authorizationStartDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Reassessment Date"} value={record && record['reassessmentDate']} />
          <UimField label={"Funding Source"} value={record && record['fundingSource']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Previous')}>Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Previous')}>Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorListUserCasesByOrgObjectPage;
