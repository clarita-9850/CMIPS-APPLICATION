import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'Supervisor Custom Home', route: '/supervisor/custom-home' },
    { label: 'manage Case Load', route: '/organization/manage-case-load' }
  ];

export function OrganizationListSupervisorPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('organization', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Organization_listSupervisor"}
      title={"Select a Supervisor"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"District Office"} value={record && record['districtOffice']} />
        </div>
      </UimSection>
      <UimSection title={"All Supervisors"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Select and Continue')}>Select and Continue</button>
      </div>
    </UimPageLayout>
  );
}

export default OrganizationListSupervisorPage;
