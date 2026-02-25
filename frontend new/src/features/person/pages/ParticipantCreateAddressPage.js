import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [];

export function ParticipantCreateAddressPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  return (
    <UimPageLayout
      pageId={"Participant_createAddress"}
      title={"Create Address"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Create Address"}>
        <div className="uim-form-grid">
          <UimField label={"Address"} />
          <UimField label={"Type"} />
          <UimField label={"Type"} />
          <UimField label={"Use As Mailing Address"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default ParticipantCreateAddressPage;
