import React from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'modify Household Member From View', route: '/evidence/modify-household-member-from-view' },
    { label: 'delete Household Member', route: '/evidence/delete-household-member' }
  ];

export function EvidenceViewHouseholdMemberPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const eligibilityApi = getDomainApi('evidence');
  const { data, loading, error } = useDomainData('evidence', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Evidence_viewHouseholdMember"}
      title={"View Household Member:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Companion Case Details"}>
        <div className="uim-form-grid">
          <UimField label={"Companion Case Number"} value={record && record['companionCaseNumber']} />
          <UimField label={"Protective Supervision Status"} value={record && record['protectiveSupervisionStatus']} />
          <UimField label={"Relationship"} value={record && record['relationship']} />
          <UimField label={"Last Name"} value={record && record['lastName']} />
        </div>
      </UimSection>
      <UimSection title={"Household Member Details"}>
        <div className="uim-form-grid">
          <UimField label={"Spouse / Parent"} value={record && record['spouseParent']} />
          <UimField label={"Date of Birth"} value={record && record['dateOfBirth']} />
          <UimField label={"First Name"} value={record && record['firstName']} />
          <UimField label={"Protective Supervision Proration"} value={record && record['protectiveSupervisionProration']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Opening edit mode')}>Edit</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { if (window.confirm('Are you sure you want to delete?')) { eligibilityApi.update(id, { status: 'deleted' }).then(() => navigate(-1)).catch(err => alert('Delete failed: ' + err.message)); } }}>Delete</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default EvidenceViewHouseholdMemberPage;
