import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Phone Number', route: '/person/view-phone-number' },
    { label: 'view Phone Number History', route: '/person/view-phone-number-history' }
  ];

export function ParticipantListPhoneNumberHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Participant_listPhoneNumberHistory"}
      title={"Phone Numbers History:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Primary"} value={record && record['primary']} />
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Phone Number"} value={record && record['phoneNumber']} />
          <UimField label={"Extension"} value={record && record['extension']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Updated By"} value={record && record['updatedBy']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-phone-number')}>View Phone Number</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-phone-number-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-phone-number')}>View</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default ParticipantListPhoneNumberHistoryPage;
