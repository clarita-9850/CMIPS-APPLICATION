import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimTable }      from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Decisions Details', route: '/misc/pa-case-authorization-view-decisions-details' }
  ];

export function PACaseHomePage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('misc', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"PACase_home"}
      title={"Provider Management - Case Home:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Case Number"} value={record && record['caseNumber']} />
          <UimField label={"Recipient Name"} value={record && record['recipientName']} />
          <UimField label={"Interpreter Available"} value={record && record['interpreterAvailable']} />
          <UimField label={"Number of Household Members"} value={record && record['numberOfHouseholdMembers']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Status Date"} value={record && record['statusDate']} />
          <UimField label={"County"} value={record && record['county']} />
          <UimField label={"District Office ID"} value={record && record['districtOfficeID']} />
          <UimField label={"Companion Case"} value={record && record['companionCase']} />
          <UimField label={"Mail Designee"} value={record && record['mailDesignee']} />
          <UimField label={"Decision"} value={record && record['decision']} />
          <UimField label={"Auth to Purchase"} value={record && record['authToPurchase']} />
          <UimField label={"Segment Start Date"} value={record && record['segmentStartDate']} />
          <UimField label={"Segment End Date"} value={record && record['segmentEndDate']} />
          <UimField label={"Authorization Start Date"} value={record && record['authorizationStartDate']} />
          <UimField label={"Authorization End Date"} value={record && record['authorizationEndDate']} />
          <UimField label={"Determination Date"} value={record && record['determinationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Authorization Segments"}>
        <UimTable
          columns={['ID', 'Name', 'Status', 'Date']}
          rows={Array.isArray(data) ? data : []}
          onRowClick={() => {}}
        />
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/misc/pa-case-authorization-view-decisions-details')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default PACaseHomePage;
