import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'view Address', route: '/person/view-address' },
    { label: 'view Address From History', route: '/person/view-address-from-history' }
  ];

export function ParticipantListAddressHistoryPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Participant_listAddressHistory"}
      title={"Address History:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} value={record && record['type']} />
          <UimField label={"Address"} value={record && record['address']} />
          <UimField label={"City"} value={record && record['city']} />
          <UimField label={"From"} value={record && record['from']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"History Created"} value={record && record['historyCreated']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-address')}>View Address</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-address-from-history')}>History</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate('/person/view-address')}>View</button>
      </div>
    </UimPageLayout>
  );
}

export default ParticipantListAddressHistoryPage;
