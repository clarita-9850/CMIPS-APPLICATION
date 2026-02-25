import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'task Home', route: '/task-management/task-home' },
    { label: 'view Assignment List', route: '/task-management/view-assignment-list' }
  ];

export function TaskManagementViewHistoryTextPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"TaskManagement_viewHistoryText"}
      title={"Task History:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Comment"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Task')}>View Task</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Task History')}>Task History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/view-assignment-list')}>Assignment List</button>
      </div>
    </UimPageLayout>
  );
}

export default TaskManagementViewHistoryTextPage;
