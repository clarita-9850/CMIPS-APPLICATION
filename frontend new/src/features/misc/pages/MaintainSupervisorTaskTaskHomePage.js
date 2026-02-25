import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Forward', route: '/supervisor/task-forward' },
    { label: 'task Reallocate', route: '/supervisor/task-reallocate' },
    { label: 'reserve Task', route: '/supervisor/reserve-task' },
    { label: 'unreserve Task', route: '/supervisor/unreserve-task' },
    { label: 'defer Task', route: '/supervisor/defer-task' },
    { label: 'restart Task', route: '/supervisor/restart-task' },
    { label: 'add Task Comment', route: '/supervisor/add-task-comment' },
    { label: 'close Task', route: '/misc/maintain-supervisor-task-close-task' }
  ];

export function MaintainSupervisorTaskTaskHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const miscApi = getDomainApi('misc');
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"MaintainSupervisorTask_taskHome"}
      title={"Task Home"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Manage"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.update(id, { action: 'forward' }).then(() => { alert('Forwarded successfully'); navigate(-1); }).catch(err => alert('Forward failed: ' + err.message)); }}>Forward</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.update(id, { action: 'reallocate' }).then(() => { alert('Reallocated successfully'); navigate(-1); }).catch(err => alert('Reallocate failed: ' + err.message)); }}>Reallocate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { miscApi.update(id, { action: 'reserve' }).then(() => { alert('Reserved successfully'); navigate(-1); }).catch(err => alert('Reserve failed: ' + err.message)); }}>Reserve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Un-Reserve')}>Un-Reserve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/defer-task')}>Defer Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/restart-task')}>Restart Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Comment')}>Add Comment</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/misc/maintain-supervisor-task-close-task')}>Close Task</button>
      </div>
    </UimPageLayout>
  );
}

export default MaintainSupervisorTaskTaskHomePage;
