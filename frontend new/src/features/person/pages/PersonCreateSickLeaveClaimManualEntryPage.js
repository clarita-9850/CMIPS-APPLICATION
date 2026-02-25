import React from 'react';
import { useNavigate } from 'react-router-dom';
import { UimPageLayout } from '../../../shared/components';
import { UimSection }    from '../../../shared/components';
import { UimField }      from '../../../shared/components';
import { getDomainApi } from '../../../api/domainApi';

const NAV_LINKS = [
    { label: 'enter Sick Leave Claim Manual Entry', route: '/person/enter-sick-leave-claim-manual-entry' }
  ];

export function PersonCreateSickLeaveClaimManualEntryPage() {
  const navigate = useNavigate();
  const personsApi = getDomainApi('person');
  return (
    <UimPageLayout
      pageId={"Person_createSickLeaveClaimManualEntry"}
      title={"Sick Leave Claim Manual Entry - Time Entries:\\\\"}
      navLinks={NAV_LINKS}
      hidePlaceholderBanner={true}
    >
      <UimSection title={"Details"}>
        <div className="uim-form-grid">
          <UimField label={"Provider Name"} />
          <UimField label={"Provider Type"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Recipient Name"} />
          <UimField label={"Service Period From"} />
        </div>
      </UimSection>
      <UimSection title={"Time Entries"}>
        <div className="uim-form-grid">
          <UimField label={"Total (HH:MM)"} />
          <UimField label={"Total (HH:MM)"} />
        </div>
      </UimSection>
      <div className="uim-action-bar">
        <button className="uim-btn uim-btn-primary" onClick={() => { personsApi.create({}).then(() => { alert('Save successful'); navigate(-1); }).catch(err => alert('Save failed: ' + err.message)); }}>Save</button>
        <button className="uim-btn uim-btn-primary" onClick={() => navigate(-1)}>Cancel</button>
      </div>
    </UimPageLayout>
  );
}

export default PersonCreateSickLeaveClaimManualEntryPage;
