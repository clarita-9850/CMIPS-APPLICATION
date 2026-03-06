import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [];

export function EvidenceListCompanionCasesPage() {
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_listCompanionCases"}
      title={"Companion Case:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Protective Supervision Status"} value={record && record['protectiveSupervisionStatus']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Protective Supervision Status"} value={record && record['protectiveSupervisionStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Companion Case List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Companion Case List"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: &lt;&lt;Previous')}>&lt;&lt;Previous</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next&gt;&gt;')}>Next&gt;&gt;</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select')}>Select</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceListCompanionCasesPage;
