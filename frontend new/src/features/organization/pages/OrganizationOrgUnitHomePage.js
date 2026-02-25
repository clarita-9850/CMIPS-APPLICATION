import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'create Child Org Unit', route: '/organization/create-child-org-unit' },
    { label: 'add Position For Org Unit', route: '/organization/add-position-for-org-unit' },
    { label: 'org Unit Evidence Approval', route: '/organization/org-unit-evidence-approval' },
    { label: 'list Org Unit Case Approval', route: '/organization/list-org-unit-case-approval' },
    { label: 'list Organization Unit Resource', route: '/organization/list-organization-unit-resource' },
    { label: 'Assign Default Location', route: '/organization/assign-default-location' },
    { label: 'modify Org Unit From View', route: '/organization/modify-org-unit-from-view' },
    { label: 'cancel Org Unit', route: '/organization/cancel-org-unit' },
    { label: 'user Home Popup', route: '/organization/user-home-popup' }
  ];

export function OrganizationOrgUnitHomePage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_orgUnitHome"}
      title={"Organization Unit Home:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Options</button>
      </div>
      <UimSection title={"Options"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Parent Unit"} value={record && record['parentUnit']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Location"} value={record && record['location']} />
          <UimField label={"Unit Status"} value={record && record['unitStatus']} />
          <UimField label={"Comment"} value={record && record['comment']} />
        </div>
      </UimSection>
      <UimSection title={"Lead Users"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Position"} value={record && record['position']} />
          <UimField label={"Status"} value={record && record['status']} />
        </div>
      </UimSection>
      <UimSection title={"Location Details"}>
        <div className="uim-form-grid">
          <UimField label={"Location Name"} value={record && record['locationName']} />
          <UimField label={"Location Status"} value={record && record['locationStatus']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Organization Unit')}>Create Organization Unit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Position')}>Create Position</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Evidence Approval Checks')}>View Evidence Approval Checks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Case Approval Checks')}>View Case Approval Checks</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: View Resources')}>View Resources</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/organization/assign-default-location')}>Assign Default Location</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure?')) { organizationApi.update(id, { status: 'terminated' }).then(() => { alert('Terminated'); navigate(-1); }).catch(err => alert('Failed: ' + err.message)); } }}>Inactivate</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationOrgUnitHomePage;
