import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'user Home Popup', route: '/organization/user-home-popup' },
    { label: 'list Message Center', route: '/person/list-message-center' }
  ];

export function PersonViewMessageCenterPage() {
  const navigate = useNavigate();
  const { data, loading, error } = useDomainData('person', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Person_viewMessageCenter"}
      title={"View Message:"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Entered By"} value={record && record['enteredBy']} />
          <UimField label={"High Priority"} value={record && record['highPriority']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Creation Date"} value={record && record['creationDate']} />
        </div>
      </UimSection>
      <UimSection title={"Message Details"}>
        <div className="uim-form-grid">
          <UimField label={"Read Date"} value={record && record['readDate']} />
          <UimField label={"Subject"} value={record && record['subject']} />
          <UimField label={"Body"} value={record && record['body']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Close</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonViewMessageCenterPage;
