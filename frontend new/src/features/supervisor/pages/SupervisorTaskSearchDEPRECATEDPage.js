import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Search', route: '/supervisor/task-search' },
    { label: 'view Task', route: '/supervisor/view-task' },
    { label: 'task Forward', route: '/supervisor/task-forward' },
    { label: 'task Reallocate', route: '/supervisor/task-reallocate' },
    { label: 'reserve Task', route: '/supervisor/reserve-task' },
    { label: 'unreserve Task', route: '/supervisor/unreserve-task' },
    { label: 'defer Task', route: '/supervisor/defer-task' },
    { label: 'restart Task', route: '/supervisor/restart-task' },
    { label: 'add Task Comment', route: '/supervisor/add-task-comment' },
    { label: 'close Task', route: '/misc/maintain-supervisor-task-close-task' }
  ];

export function SupervisorTaskSearchDEPRECATEDPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const supervisorApi = getDomainApi('supervisor');
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_taskSearch_DEPRECATED"}
      title={"Task Search D E P R E C A T E D"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Case Reference"} value={record && record['caseReference']} />
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Assigned"} value={record && record['assigned']} />
          <UimField label={"Deadline"} value={record && record['deadline']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.search({}).then(results => alert('Found ' + (results?.length || 0) + ' results')).catch(err => alert('Search failed: ' + err.message)); }}>Search</button>
        <button className="uim-btn uim-btn-primary" onClick={() => window.location.reload()}>Reset</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'forward' }).then(() => { alert('Forwarded successfully'); navigate(-1); }).catch(err => alert('Forward failed: ' + err.message)); }}>Forward</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'reallocate' }).then(() => { alert('Reallocated successfully'); navigate(-1); }).catch(err => alert('Reallocate failed: ' + err.message)); }}>Reallocate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reserve For')}>Reserve For</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Un-reserve')}>Un-reserve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/defer-task')}>Defer Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/restart-task')}>Restart</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Comment')}>Add Comment</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/misc/maintain-supervisor-task-close-task')}>Close Task</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorTaskSearchDEPRECATEDPage;
