import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'reassign Case', route: '/supervisor/reassign-case' },
    { label: 'resolvercase Workspace', route: '/supervisor/resolvercase-workspace' }
  ];

export function SupervisorCaseWorkspacePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_caseWorkspace"}
      title={"Case Workspace"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/reassign-case')}>Reassign Case</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Tasks Due in Next Week')}>Tasks Due in Next Week</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Tasks Due in Next Month')}>Tasks Due in Next Month</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorCaseWorkspacePage;
