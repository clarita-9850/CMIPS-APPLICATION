import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'Eligibility  Popup  Screen', route: '/misc/meds-eligibility-popup-screen' }
  ];

export function CaseSearchSCIPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Case_searchSCI"}
      title={"Statewide Client Index - CIN Search"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Search Criteria"}>
        <div className="uim-form-grid">
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"CIN"} value={record && record['cIN']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Date Of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
          <UimField label={"CIN"} value={record && record['cIN']} />
          <UimField label={"SSN"} value={record && record['sSN']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"Suffix"} value={record && record['suffix']} />
          <UimField label={"Gender"} value={record && record['gender']} />
          <UimField label={"DOB"} value={record && record['dOB']} />
          <UimField label={"MEDS"} value={record && record['mEDS']} />
        </div>
      </UimSection>
      <UimSection title={"Search Results"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseSearchSCIPage;
