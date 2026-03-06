import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'modify Residence Information', route: '/evidence/modify-residence-information' },
    { label: 'create Household Member', route: '/evidence/create-household-member' },
    { label: 'view Household Member', route: '/evidence/view-household-member' },
    { label: 'modify Household Member From List', route: '/evidence/modify-household-member-from-list' },
    { label: 'home', route: '/evidence/home' },
    { label: 'service Evidence Home', route: '/evidence/service-evidence-home' },
    { label: 'view Disaster Preparedness', route: '/case/view-disaster-preparedness' }
  ];

export function EvidenceHouseholdEvidenceHomePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_householdEvidenceHome"}
      title={"Household Evidence:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Residence Information"}>
        <div className="uim-form-grid">
          <UimField label={"Stove"} value={record && record['stove']} />
          <UimField label={"Refrigerator"} value={record && record['refrigerator']} />
          <UimField label={"Washer"} value={record && record['washer']} />
          <UimField label={"Dryer"} value={record && record['dryer']} />
          <UimField label={"Yard"} value={record && record['yard']} />
          <UimField label={"Living Arrangement"} value={record && record['livingArrangement']} />
          <UimField label={"Residence Type"} value={record && record['residenceType']} />
          <UimField label={"Number of Recipient only Rooms"} value={record && record['numberOfRecipientOnlyRooms']} />
          <UimField label={"Number of Shared Rooms"} value={record && record['numberOfSharedRooms']} />
          <UimField label={"Number of Rooms not Used"} value={record && record['numberOfRoomsNotUsed']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"Relationship"} value={record && record['relationship']} />
          <UimField label={"Age"} value={record && record['age']} />
          <UimField label={"Case Owner"} value={record && record['caseOwner']} />
          <UimField label={"Companion Case Number"} value={record && record['companionCaseNumber']} />
          <UimField label={"Protective Supervision Status"} value={record && record['protectiveSupervisionStatus']} />
        </div>
      </UimSection>
      <UimSection title={"Household Members"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Edit Residence Information')}>Edit Residence Information</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Add Household Members')}>Add Household Members</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/view-household-member')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/evidence/home')}>Evidence Home</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Next')}>Next</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceHouseholdEvidenceHomePage;
