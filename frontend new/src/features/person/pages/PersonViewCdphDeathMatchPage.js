import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'list Cdph Death Match', route: '/person/list-cdph-death-match' }
  ];

export function PersonViewCdphDeathMatchPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = React.useState(0);
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewCdphDeathMatch"}
      title={"View CDPH Death Match"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <div className="uim-tab-nav">
        <button className={activeTab===0 ? 'tab-item active' : 'tab-item'} onClick={() => setActiveTab(0)}>CDPH Information</button>
      </div>
      <UimSection title={"CDPH Information"}>
        <div className="uim-form-grid">
          <UimField label={"State File Number"} value={record && record['stateFileNumber']} />
          <UimField label={"Revision Identification"} value={record && record['revisionIdentification']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"Spouse's First Name"} value={record && record['spouseSFirstName']} />
          <UimField label={"Mother's First Name"} value={record && record['motherSFirstName']} />
          <UimField label={"Father's First Name"} value={record && record['fatherSFirstName']} />
          <UimField label={"Residence Street"} value={record && record['residenceStreet']} />
          <UimField label={"Residence County"} value={record && record['residenceCounty']} />
          <UimField label={"Date of Death"} value={record && record['dateOfDeath']} />
        </div>
      </UimSection>
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"County of Death"} value={record && record['countyOfDeath']} />
          <UimField label={"Middle Name"} value={record && record['middleName']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
          <UimField label={"Age in Years"} value={record && record['ageInYears']} />
          <UimField label={"Marital Status"} value={record && record['maritalStatus']} />
          <UimField label={"Spouse's Last Name"} value={record && record['spouseSLastName']} />
          <UimField label={"Mother's Maiden Name"} value={record && record['motherSMaidenName']} />
          <UimField label={"Father's Last Name"} value={record && record['fatherSLastName']} />
          <UimField label={"Residence City"} value={record && record['residenceCity']} />
          <UimField label={"City of Death"} value={record && record['cityOfDeath']} />
          <UimField label={"Notification Date"} value={record && record['notificationDate']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewCdphDeathMatchPage;
