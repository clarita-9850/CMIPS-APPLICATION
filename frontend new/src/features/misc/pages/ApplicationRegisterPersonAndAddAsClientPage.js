import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'Search For Clients', route: '/misc/application-search-for-clients' },
    { label: 'Add Client Confirmation', route: '/misc/application-add-client-confirmation' }
  ];

export function ApplicationRegisterPersonAndAddAsClientPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Application_registerPersonAndAddAsClient"}
      title={"Un-reserve Task:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>Residence Address</button>
      </div>
      <UimSection title={"Name"}>
        <div className="uim-form-grid">
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Nationality"} value={record && record['nationality']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
        </div>
      </UimSection>
      <UimSection title={"Residence Address"}>
        <div className="uim-form-grid">
          <UimField label={"Reference"} value={record && record['reference']} />
          <UimField label={"Marital Status"} value={record && record['maritalStatus']} />
          <UimField label={"Country Of Birth"} value={record && record['countryOfBirth']} />
          <UimField label={"Primary Address Data"} value={record && record['primaryAddressData']} />
          <UimField label={"Type"} value={record && record['type']} />
        </div>
      </UimSection>
      <UimSection title={"Primary Phone Number"}>
        <div className="uim-form-grid">
          <UimField label={"Area Code"} value={record && record['areaCode']} />
          <UimField label={"Extension"} value={record && record['extension']} />
          <UimField label={"Country Code"} value={record && record['countryCode']} />
          <UimField label={"Phone Number"} value={record && record['phoneNumber']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Back</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Create Case')}>Create Case</button>
      </div>
    </UimPageLayout>
  );
}

export default ApplicationRegisterPersonAndAddAsClientPage;
