import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'delete Multiple Alerts', route: '/task-management/delete-multiple-alerts' },
    { label: 'view Alert', route: '/task-management/view-alert' },
    { label: 'delete Alert', route: '/task-management/delete-alert' },
    { label: 'view Alert  Inline', route: '/task-management/view-alert-inline' }
  ];

export function InboxListAlertsPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const tasksApi = getDomainApi('task-management');
  const { data, loading, error } = useDomainData('task-management', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Inbox_listAlerts"}
      title={"My Notifications:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Category"} value={record && record['category']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Inactivate Selected')}>Inactivate Selected</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/task-management/view-alert')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { tasksApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default InboxListAlertsPage;
