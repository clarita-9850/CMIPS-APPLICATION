import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Task', route: '/supervisor/view-task' },
    { label: 'task Forward', route: '/supervisor/task-forward' },
    { label: 'task Reallocate', route: '/supervisor/task-reallocate' },
    { label: 'reserve Task', route: '/supervisor/reserve-task' },
    { label: 'unreserve Task', route: '/supervisor/unreserve-task' },
    { label: 'defer Task', route: '/supervisor/defer-task' },
    { label: 'restart Task', route: '/supervisor/restart-task' },
    { label: 'add Task Comment', route: '/supervisor/add-task-comment' },
    { label: 'close Task', route: '/misc/maintain-supervisor-task-close-task' },
    { label: 'user Workspace', route: '/supervisor/user-workspace' }
  ];

export function SupervisorOrgUnitTasksByWeekPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const supervisorApi = getDomainApi('supervisor');
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_orgUnitTasksByWeek"}
      title={"Organization Unit Tasks By Week:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Priority"} value={record && record['priority']} />
          <UimField label={"Reserved By"} value={record && record['reservedBy']} />
          <UimField label={"Assigned/Reserved"} value={record && record['assignedReserved']} />
          <UimField label={"Deadline"} value={record && record['deadline']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
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

export default SupervisorOrgUnitTasksByWeekPage;
