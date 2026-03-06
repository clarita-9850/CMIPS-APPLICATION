import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'search S C I', route: '/person/search-sci' },
    { label: 'list Alternate I D', route: '/person/list-alternate-id' }
  ];

export function ParticipantCreateAlternateIDPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  return (
    <UimPageLayout
      pageId={"Participant_createAlternateID"}
      title={"Create Alternative ID:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Alternative ID"} />
          <UimField label={"From"} />
        </div>
      </UimSection>
      <UimSection title={"Comments"}>
        <div className="uim-form-grid">
          <UimField label={"Type"} />
          <UimField label={"Blank SSN Reason"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => alert('Action: CIN Clearance')}>CIN Clearance</button>
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default ParticipantCreateAlternateIDPage;
