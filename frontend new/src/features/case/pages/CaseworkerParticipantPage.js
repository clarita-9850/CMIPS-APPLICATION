import React from 'react';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { useDomainData } from '../../../shared/hooks/useDomainData';

const NAV_LINKS = [
    { label: 'duplicate Referral Check', route: '/person/duplicate-referral-check' },
    { label: 'duplicate Registration Check', route: '/person/duplicate-registration-check' },
    { label: 'search', route: '/person/search' },
    { label: 'duplicate Provider Check', route: '/person/duplicate-provider-check' }
  ];

export function CaseworkerParticipantPage() {
  const { data, loading, error } = useDomainData('case', 'list');
  const record = Array.isArray(data) ? data[0] : data;
  return (
    <UimPageLayout
      pageId={"Caseworker_participant"}
      title={"Persons"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      {loading && <div className="uim-info-banner">Loading data...</div>}
      {error && <div className="uim-info-banner" style={{background:'#f8d7da',borderColor:'#f5c6cb',color:'#721c24'}}>Unable to load data. The backend may be unavailable.</div>}
      <UimSection title={"Person Shortcuts"}>
        <div className="uim-form-grid">
          <UimField label={"ID"} value={record && record['iD']} />
          <UimField label={"Status"} value={record && record['status']} />
          <UimField label={"Date"} value={record && record['date']} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New Referral')}>New Referral</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: New Application')}>New Application</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Find a Person')}>Find a Person</button>
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: Register a Provider')}>Register a Provider</button>
      </div>
    </UimPageLayout>
  );
}

export default CaseworkerParticipantPage;
