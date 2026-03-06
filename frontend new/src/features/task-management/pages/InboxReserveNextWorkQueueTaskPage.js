import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'view User Inbox', route: '/task-management/view-user-inbox' }
  ];

export function InboxReserveNextWorkQueueTaskPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const tasksApi = getDomainApi('task-management');
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_reserveNextWorkQueueTask"}
      title={"Reserve Next Task From Work Queue:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Work Queue"} value={record && record['workQueue']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { tasksApi.update(id, { action: 'reserve' }).then(() => { alert('Reserved successfully'); navigate(-1); }).catch(err => alert('Reserve failed: ' + err.message)); }}>Reserve</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reserve Next 5 Tasks')}>Reserve Next 5 Tasks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Reserve Next 20 Tasks')}>Reserve Next 20 Tasks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxReserveNextWorkQueueTaskPage;
