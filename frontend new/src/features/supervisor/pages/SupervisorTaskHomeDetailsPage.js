import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Task History', route: '/supervisor/view-task-history' },
    { label: 'view Assignment List', route: '/supervisor/view-assignment-list' },
    { label: 'add Comment', route: '/task-management/add-comment' },
    { label: 'close Task', route: '/task-management/close-task' },
    { label: 'Reserve Task', route: '/task-management/reserve-task' },
    { label: 'Un Reserve Task', route: '/task-management/un-reserve-task' },
    { label: 'forward', route: '/task-management/forward' },
    { label: 'reallocate', route: '/task-management/reallocate' },
    { label: 'Defer Task', route: '/task-management/defer-task' },
    { label: 'Restart Task', route: '/task-management/restart-task' },
    { label: 'resolve User', route: '/organization/resolve-user' },
    { label: 'view User Details', route: '/organization/view-user-details' },
    { label: 'modify Time Worked', route: '/task-management/modify-time-worked' }
  ];

export function SupervisorTaskHomeDetailsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const supervisorApi = getDomainApi('supervisor');
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_TaskHomeDetails"}
      title={"Task Home:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"Task ID"} value={record && record['taskID']} />
          <UimField label={"Priority"} value={record && record['priority']} />
        </div>
      </UimSection>
      <UimSection title={"Subject"}>
        <div className="uim-form-grid">
          <UimField label={"Reserved By"} value={record && record['reservedBy']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Deadline"} value={record && record['deadline']} />
          <UimField label={"Last Assigned"} value={record && record['lastAssigned']} />
        </div>
      </UimSection>
      <UimSection title={"Primary Action"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Supporting Information"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/view-task-history')}>View Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/view-task-history')}>Task History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/view-assignment-list')}>Assignment List</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/add-comment')}>Add Comment</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'reserve' }).then(() => { alert('Reserved successfully'); navigate(-1); }).catch(err => alert('Reserve failed: ' + err.message)); }}>Reserve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Un-Reserve')}>Un-Reserve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'forward' }).then(() => { alert('Forwarded successfully'); navigate(-1); }).catch(err => alert('Forward failed: ' + err.message)); }}>Forward</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'reallocate' }).then(() => { alert('Reallocated successfully'); navigate(-1); }).catch(err => alert('Reallocate failed: ' + err.message)); }}>Reallocate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'defer' }).then(() => { alert('Deferred successfully'); navigate(-1); }).catch(err => alert('Defer failed: ' + err.message)); }}>Defer</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/restart-task')}>Restart</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: [Change]')}>[Change]</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorTaskHomeDetailsPage;
