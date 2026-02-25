import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'cases Home', route: '/supervisor/cases-home' },
    { label: 'case Workspace', route: '/supervisor/case-workspace' }
  ];

export function SupervisorCaseViewHomePage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_caseViewHome"}
      title={"Cases"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Filter Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Product"} value={record && record['product']} />
          <UimField label={"Case Type"} value={record && record['caseType']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Case Reference"} value={record && record['caseReference']} />
          <UimField label={"Product"} value={record && record['product']} />
          <UimField label={"Primary Client"} value={record && record['primaryClient']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Case Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorCaseViewHomePage;
