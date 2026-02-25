import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Person History', route: '/person/view-person-history' },
    { label: 'home Page', route: '/person/home-page' }
  ];

export function PersonListPersonHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_listPersonHistory"}
      title={"Person History:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Lastname"} value={record && record['lastname']} />
          <UimField label={"Lastname"} value={record && record['lastname']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Marital Status"} value={record && record['maritalStatus']} />
          <UimField label={"Nationality"} value={record && record['nationality']} />
          <UimField label={"Ethnicity"} value={record && record['ethnicity']} />
          <UimField label={"Last Updated By"} value={record && record['lastUpdatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-person-history')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonListPersonHistoryPage;
