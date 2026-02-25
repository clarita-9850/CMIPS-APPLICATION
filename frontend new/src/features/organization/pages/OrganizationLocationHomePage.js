import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Daily Schedule', route: '/organization/create-daily-schedule' },
    { label: 'list Location Resource', route: '/organization/list-location-resource' },
    { label: 'cancel Location', route: '/organization/cancel-location' },
    { label: 'create Location', route: '/organization/create-location' },
    { label: 'resolve Location Holiday Calendar', route: '/organization/resolve-location-holiday-calendar' },
    { label: 'assign Location Access', route: '/organization/assign-location-access' },
    { label: 'modify Location From View', route: '/organization/modify-location-from-view' },
    { label: 'remove Location Access', route: '/organization/remove-location-access' },
    { label: 'view Daily Schedule', route: '/organization/view-daily-schedule' },
    { label: 'modify Daily Schedule From List', route: '/organization/modify-daily-schedule-from-list' },
    { label: 'resolve Location Tree', route: '/organization/resolve-location-tree' }
  ];

export function OrganizationLocationHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_locationHome"}
      title={"Location Home:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Address</button>
        <button className={activeTab===1 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(1)}>Contact Details</button>
      </div>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Public Office"} value={record && record['publicOffice']} />
          <UimField label={"Location Status"} value={record && record['locationStatus']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Maintain SID"} value={record && record['maintainSID']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Comments"} value={record && record['comments']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Parent Location"} value={record && record['parentLocation']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
          <UimField label={"Read SID"} value={record && record['readSID']} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"Create Location SID"} value={record && record['createLocationSID']} />
          <UimField label={"Address"} value={record && record['address']} />
          <UimField label={"Email"} value={record && record['email']} />
          <UimField label={"Location Name"} value={record && record['locationName']} />
          <UimField label={"Rights"} value={record && record['rights']} />
        </div>
      </UimSection>
      <UimSection title={"Contact Details"}>
        <div className="uim-form-grid">
          <UimField label={"Location Status"} value={record && record['locationStatus']} />
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Start Date"} value={record && record['startDate']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Additional Location Access"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <UimSection title={"Schedules"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Schedule')}>Create Schedule</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Resources')}>View Resources</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Child Location')}>Create Child Location</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Location Calendar')}>Location Calendar</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/assign-location-access')}>Assign Location Access</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { organizationApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Remove</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/modify-location-from-view')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Browse')}>Browse</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationLocationHomePage;
