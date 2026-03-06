import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'reassign Cases For User With Search', route: '/supervisor/reassign-cases-for-user-with-search' },
    { label: 'list User Cases By Org Object', route: '/supervisor/list-user-cases-by-org-object' },
    { label: 'resolve Case Home', route: '/case/resolve-case-home' }
  ];

export function SupervisorListUserCasesPage() {
  const navigate = useNavigate();
  const supervisorApi = getDomainApi('supervisor');
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_listUserCases"}
      title={"Cases"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Cases Owned As"} value={record && record['casesOwnedAs']} />
          <UimField label={"Case Reference"} value={record && record['caseReference']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Primary Client"} value={record && record['primaryClient']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/reassign-cases-for-user-with-search')}>Reassign Cases</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorListUserCasesPage;
