import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'maintain Non Standard Working Pattern', route: '/organization/maintain-non-standard-working-pattern' },
    { label: 'modify Working Pattern From View', route: '/organization/modify-working-pattern-from-view' },
    { label: 'cancel Working Pattern', route: '/organization/cancel-working-pattern' },
    { label: 'list Working Pattern', route: '/organization/list-working-pattern' }
  ];

export function OrganizationViewWorkingPatternPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_viewWorkingPattern"}
      title={"Un-reserve Task:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Start"} value={record && record['start']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"End"} value={record && record['end']} />
          <UimField label={"Start Time"} value={record && record['startTime']} />
          <UimField label={"End Time"} value={record && record['endTime']} />
        </div>
      </UimSection>
      <UimSection title={"Default"}>
        <div className="uim-form-grid">
          <UimField label={"Day Number"} value={record && record['dayNumber']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Start Time"} value={record && record['startTime']} />
          <UimField label={"End Time"} value={record && record['endTime']} />
        </div>
      </UimSection>
      <UimSection title={"Days"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Non Standard')}>Edit Non Standard</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Working Pattern')}>Edit Working Pattern</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { organizationApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationViewWorkingPatternPage;
