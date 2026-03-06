import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function OrganizationModifyOrganizationPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const organizationApi = getDomainApi('organization');
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_modifyOrganization"}
      title={"Modify Organization:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Address</button>
      </div>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Name"} value={record && record['name']} />
          <UimField label={"Tax Number"} value={record && record['taxNumber']} />
          <UimField label={"Registration Number"} value={record && record['registrationNumber']} />
        </div>
      </UimSection>
      <UimSection title={"Address"}>
        <div className="uim-form-grid">
          <UimField label={"Location Security Level"} value={record && record['locationSecurityLevel']} />
          <UimField label={"Location Data Security"} value={record && record['locationDataSecurity']} />
          <UimField label={"Address"} value={record && record['address']} />
        </div>
      </UimSection>
      <UimSection title={"Contact Details"}>
        <div className="uim-form-grid">
          <UimField label={"Phone Area Code"} value={record && record['phoneAreaCode']} />
          <UimField label={"Phone Number"} value={record && record['phoneNumber']} />
          <UimField label={"Email Address"} value={record && record['emailAddress']} />
        </div>
      </UimSection>
      <UimSection title={"Description"}>
        <div className="uim-form-grid">
          <UimField label={"Fax Area Code"} value={record && record['faxAreaCode']} />
          <UimField label={"Fax Number"} value={record && record['faxNumber']} />
          <UimField label={"Web Address"} value={record && record['webAddress']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { organizationApi.update(id, {}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationModifyOrganizationPage;
