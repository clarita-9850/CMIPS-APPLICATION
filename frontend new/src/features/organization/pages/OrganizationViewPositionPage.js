import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'add User For Position', route: '/organization/add-user-for-position' },
    { label: 'create And Assign User For Position', route: '/organization/create-and-assign-user-for-position' },
    { label: 'Assign Location To Position', route: '/organization/assign-location-to-position' },
    { label: 'list Position Temporal Evidence Approval', route: '/organization/list-position-temporal-evidence-approval' },
    { label: 'modify Position From View', route: '/organization/modify-position-from-view' },
    { label: 'cancel Position By Workflow', route: '/organization/cancel-position-by-workflow' },
    { label: 'user Home', route: '/organization/user-home' },
    { label: 'org Unit Home', route: '/organization/org-unit-home' }
  ];

export function OrganizationViewPositionPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_viewPosition"}
      title={"Position Home:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Options</button>
        <button className={activeTab===1 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(1)}>Options</button>
      </div>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Lead Position"} value={record && record['leadPosition']} />
          <UimField label={"Reports To"} value={record && record['reportsTo']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
        </div>
      </UimSection>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Job"} value={record && record['job']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"End Date"} value={record && record['endDate']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Location Name"} value={record && record['locationName']} />
          <UimField label={"Location Status"} value={record && record['locationStatus']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Organization Unit"} value={record && record['organizationUnit']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Location Details"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Supervisor Users"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Subordinate Users"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/create-and-assign-user-for-position')}>Assign User</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New User and Assign')}>New User and Assign</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/assign-location-to-position')}>Assign Location</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Evidence Approvals')}>View Evidence Approvals</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { organizationApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationViewPositionPage;
