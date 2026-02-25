import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'view Work Queue Details', route: '/task-management/view-work-queue-details' },
    { label: 'create Work Queue Subscription', route: '/supervisor/create-work-queue-subscription' },
    { label: 'work Queue Workspace', route: '/supervisor/work-queue-workspace' }
  ];

export function SupervisorWorkQueuePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const supervisorApi = getDomainApi('supervisor');
  const { data, loading, error } = useDomainData('supervisor', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Supervisor_workQueue"}
      title={"Work Queue"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={false}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Tasks In Work Queue"} value={record && record['tasksInWorkQueue']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Subscriber"} value={record && record['subscriber']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Tasks In Work Queue"} value={record && record['tasksInWorkQueue']} />
        </div>
      </UimSection>
      <UimSection title={"User Subscribed Work Queues"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Organization Object Subscribed Work Queues"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { supervisorApi.update(id, { action: 'subscribe' }).then(() => alert('Subscribed')).catch(err => alert('Subscribe failed: ' + err.message)); }}>Subscribe</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/supervisor/work-queue-workspace')}>View Work Queue Workspace</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Work Queue Summary')}>View Work Queue Summary</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Subscribe User')}>Subscribe User</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Subscribe Organization Object')}>Subscribe Organization Object</button>
      </div>
    </UimPageLayout>
  );
}

export default SupervisorWorkQueuePage;
