import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view User Details', route: '/organization/view-user-details' },
    { label: 'task History Inline', route: '/task-management/task-history-inline' }
  ];

export function SupervisorViewTaskHistoryTextPage() {
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_viewTaskHistoryText"}
      title={"History and Comments"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"User"} value={record && record['user']} />
          <UimField label={"Date"} value={record && record['date']} />
          <UimField label={"Change Type"} value={record && record['changeType']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"To"} value={record && record['to']} />
          <UimField label={"Comment"} value={record && record['comment']} />
        </div>
      </UimSection>
    </UimPageLayout>
  );
}

export default SupervisorViewTaskHistoryTextPage;
